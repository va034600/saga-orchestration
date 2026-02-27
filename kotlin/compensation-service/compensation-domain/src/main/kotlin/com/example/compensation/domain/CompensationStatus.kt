package com.example.compensation.domain

enum class CompensationStatus {
    PENDING, PROCESSING, COMPLETED, FAILED;

    fun canTransitionTo(target: CompensationStatus): Boolean = when (this) {
        PENDING -> target == PROCESSING
        PROCESSING -> target in setOf(COMPLETED, FAILED)
        COMPLETED, FAILED -> false
    }
}
