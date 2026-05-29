package com.egorroman.hopfield.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.patternDataStore: DataStore<Preferences> by preferencesDataStore(name = "hopfield_settings")

class PatternRepository(private val context: Context) {

    val patternsFlow: Flow<Pair<Int, List<List<Int>>>> = context.patternDataStore.data.map { preferences ->
        val size = preferences[GRID_SIZE] ?: 5
        val patternsString = preferences[LEARNED_PATTERNS] ?: ""
        val patterns = if (patternsString.isEmpty()) {
            emptyList()
        } else {
            patternsString.split("|").map { patternStr ->
                patternStr.split(",").mapNotNull { it.toIntOrNull() }
            }
        }
        Pair(size, patterns)
    }

    suspend fun saveState(gridSize: Int, patterns: List<List<Int>>) {
        val patternsString = patterns.joinToString(separator = "|") { pattern ->
            pattern.joinToString(separator = ",")
        }
        context.patternDataStore.edit { preferences ->
            preferences[GRID_SIZE] = gridSize
            preferences[LEARNED_PATTERNS] = patternsString
        }
    }

    suspend fun clear() {
        context.patternDataStore.edit { it.clear() }
    }

    companion object {
        private val GRID_SIZE = intPreferencesKey("grid_size")

        private val LEARNED_PATTERNS = stringPreferencesKey("learned_patterns")
    }

}
