package com.egorroman.hopfield.presentation

import androidx.compose.runtime.Immutable
import com.egorroman.hopfield.domain.NetworkConfig

@Immutable
data class MainState(
    val gridSize: Int = NetworkConfig.DEFAULT_GRID_SIZE,
    val gridState: List<Int> = List(NetworkConfig.DEFAULT_GRID_SIZE * NetworkConfig.DEFAULT_GRID_SIZE) { NetworkConfig.STATE_INACTIVE },
    val learnedCount: Int = 0,
    val isRecognized: Boolean = false,
    val learnedPatterns: List<List<Int>> = emptyList(),
    val showPatternsSheet: Boolean = false
)

sealed class MainIntent {
    data class ToggleCell(val index: Int) : MainIntent()
    data class ToggleCells(val indices: Set<Int>, val activate: Boolean) : MainIntent()
    object LearnPattern : MainIntent()
    object RecognizePattern : MainIntent()
    object ClearGrid : MainIntent()
    object ResetMemory : MainIntent()
    data class ChangeGridSize(val newSize: Int) : MainIntent()
    data class ShowPatternsSheet(val show: Boolean) : MainIntent()
}
