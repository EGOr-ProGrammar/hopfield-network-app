package com.egorroman.hopfield.domain

object NetworkConfig {
    const val DEFAULT_ROWS = 5
    const val DEFAULT_COLS = 5
    const val MIN_ROWS = 3
    const val MAX_ROWS = 10
    const val MIN_COLS = 3
    const val MAX_COLS = 10

    const val MAX_RECOGNITION_ITERATIONS = 100

    // Cell states
    const val STATE_ACTIVE = 1
    const val STATE_INACTIVE = -1
}