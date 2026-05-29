package com.egorroman.hopfield.domain

object NetworkConfig {
    const val DEFAULT_GRID_SIZE = 5
    const val MIN_GRID_SIZE = 3
    const val MAX_GRID_SIZE = 10

    const val MAX_RECOGNITION_ITERATIONS = 100

    // Cell states
    const val STATE_ACTIVE = 1
    const val STATE_INACTIVE = -1
}