package com.egorroman.hopfield.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.patternDataStore: DataStore<Preferences> by preferencesDataStore(name = "hopfield_settings")

class PatternRepository(private val context: Context) {

    val patternsFlow: Flow<Triple<Int, Int, List<List<Int>>>> =
        context.patternDataStore.data.map { preferences ->
            val rows = preferences[GRID_ROWS] ?: 5
            val cols = preferences[GRID_COLS] ?: 5
            val patternsString = preferences[LEARNED_PATTERNS] ?: ""
            val patterns = if (patternsString.isEmpty()) {
                emptyList()
            } else {
                patternsString.split("|").map { patternStr ->
                    patternStr.split(",").mapNotNull { it.toIntOrNull() }
                }
            }
            Triple(rows, cols, patterns)
        }

    suspend fun saveState(gridRows: Int, gridCols: Int, patterns: List<List<Int>>) {
        val patternsString = patterns.joinToString(separator = "|") { pattern ->
            pattern.joinToString(separator = ",")
        }
        context.patternDataStore.edit { preferences ->
            preferences[GRID_ROWS] = gridRows
            preferences[GRID_COLS] = gridCols
            preferences[LEARNED_PATTERNS] = patternsString
        }
    }

    suspend fun clear() {
        context.patternDataStore.edit { it.clear() }
    }

    companion object {
        private val GRID_ROWS = intPreferencesKey("grid_rows")
        private val GRID_COLS = intPreferencesKey("grid_cols")

        private val LEARNED_PATTERNS = stringPreferencesKey("learned_patterns")
    }

}
