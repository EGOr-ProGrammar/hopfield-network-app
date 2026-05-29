package com.egorroman.hopfield.domain

class HopfieldNetwork(private val size: Int = NetworkConfig.DEFAULT_ROWS * NetworkConfig.DEFAULT_COLS) {
    private var weightMatrix = Array(size) { IntArray(size) }

    fun learn(pattern: IntArray) {
        if (pattern.size != size) return

        for (i in 0 until size) {
            for (j in 0 until size) {
                if (i == j) {
                    weightMatrix[i][j] = 0
                } else {
                    weightMatrix[i][j] += pattern[i] * pattern[j]
                }
            }
        }
    }

    fun recognize(
        pattern: IntArray,
        maxIterations: Int = NetworkConfig.MAX_RECOGNITION_ITERATIONS
    ): IntArray {
        if (pattern.size != size) return pattern

        val currentPattern = pattern.copyOf()
        val indices = (0 until size).toList()

        for (iter in 0 until maxIterations) {
            val lastPattern = currentPattern.copyOf()
            val shuffledIndices = indices.shuffled()

            for (i in shuffledIndices) {
                var sum = 0
                for (j in 0 until size) {
                    sum += weightMatrix[i][j] * currentPattern[j]
                }
                currentPattern[i] =
                    if (sum >= 0) NetworkConfig.STATE_ACTIVE else NetworkConfig.STATE_INACTIVE
            }

            if (lastPattern.contentEquals(currentPattern)) {
                break
            }
        }

        return currentPattern
    }

    fun clearMemory() {
        for (i in 0 until size) {
            for (j in 0 until size) {
                weightMatrix[i][j] = 0
            }
        }
    }
}

