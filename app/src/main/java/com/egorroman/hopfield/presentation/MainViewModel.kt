package com.egorroman.hopfield.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.egorroman.hopfield.data.PatternRepository
import com.egorroman.hopfield.domain.HopfieldNetwork
import com.egorroman.hopfield.domain.NetworkConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PatternRepository(application)

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private var hopfieldNetwork: HopfieldNetwork =
        HopfieldNetwork(NetworkConfig.DEFAULT_GRID_SIZE * NetworkConfig.DEFAULT_GRID_SIZE)

    init {
        viewModelScope.launch {
            val (savedSize, savedPatterns) = repository.patternsFlow.first()

            hopfieldNetwork = HopfieldNetwork(savedSize * savedSize)
            savedPatterns.forEach { hopfieldNetwork.learn(it.toIntArray()) }

            _state.update {
                it.copy(
                    gridSize = savedSize,
                    gridState = List(savedSize * savedSize) { NetworkConfig.STATE_INACTIVE },
                    learnedCount = savedPatterns.size,
                    learnedPatterns = savedPatterns
                )
            }
        }
    }

    fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.ToggleCell -> toggleCell(intent.index)
            is MainIntent.ToggleCells -> toggleCells(intent.indices, intent.activate)
            MainIntent.LearnPattern -> learnPattern()
            MainIntent.RecognizePattern -> recognizePattern()
            MainIntent.ClearGrid -> clearGrid()
            MainIntent.ResetMemory -> resetMemory()
            is MainIntent.ChangeGridSize -> changeGridSize(intent.newSize)
            is MainIntent.ShowPatternsSheet -> showPatternsSheet(intent.show)
        }
    }

    private fun toggleCell(index: Int) {
        _state.update { currentState ->
            val newList = currentState.gridState.toMutableList()
            if (index in newList.indices) {
                newList[index] =
                    if (newList[index] == NetworkConfig.STATE_ACTIVE) NetworkConfig.STATE_INACTIVE else NetworkConfig.STATE_ACTIVE
            }
            currentState.copy(gridState = newList, isRecognized = false)
        }
    }

    private fun toggleCells(indices: Set<Int>, activate: Boolean) {
        _state.update { currentState ->
            val newList = currentState.gridState.toMutableList()
            val targetState =
                if (activate) NetworkConfig.STATE_ACTIVE else NetworkConfig.STATE_INACTIVE
            indices.forEach { index ->
                if (index in newList.indices) {
                    newList[index] = targetState
                }
            }
            currentState.copy(gridState = newList, isRecognized = false)
        }
    }

    private fun learnPattern() {
        val pattern = _state.value.gridState
        hopfieldNetwork.learn(pattern.toIntArray())

        val updatedPatterns = _state.value.learnedPatterns + listOf(pattern)

        viewModelScope.launch {
            repository.saveState(_state.value.gridSize, updatedPatterns)
        }

        _state.update {
            it.copy(
                learnedCount = updatedPatterns.size,
                learnedPatterns = updatedPatterns
            )
        }
    }

    private fun recognizePattern() {
        viewModelScope.launch {
            val pattern = _state.value.gridState.toIntArray()
            val recognized = hopfieldNetwork.recognize(pattern)
            _state.update {
                it.copy(
                    gridState = recognized.toList(),
                    isRecognized = true
                )
            }
        }
    }

    private fun clearGrid() {
        _state.update { currentState ->
            currentState.copy(
                gridState = List(currentState.gridSize * currentState.gridSize) { NetworkConfig.STATE_INACTIVE },
                isRecognized = false
            )
        }
    }

    private fun resetMemory() {
        hopfieldNetwork.clearMemory()
        viewModelScope.launch {
            repository.clear()
        }
        _state.update {
            it.copy(
                learnedCount = 0,
                learnedPatterns = emptyList(),
                showPatternsSheet = false
            )
        }
    }

    private fun changeGridSize(newSize: Int) {
        if (newSize < NetworkConfig.MIN_GRID_SIZE || newSize > NetworkConfig.MAX_GRID_SIZE) return
        val totalSize = newSize * newSize
        hopfieldNetwork = HopfieldNetwork(totalSize)
        viewModelScope.launch {
            repository.saveState(newSize, emptyList())
        }
        _state.update {
            it.copy(
                gridSize = newSize,
                gridState = List(totalSize) { NetworkConfig.STATE_INACTIVE },
                learnedCount = 0,
                learnedPatterns = emptyList(),
                isRecognized = false
            )
        }
    }

    private fun showPatternsSheet(show: Boolean) {
        _state.update { it.copy(showPatternsSheet = show) }
    }
}
