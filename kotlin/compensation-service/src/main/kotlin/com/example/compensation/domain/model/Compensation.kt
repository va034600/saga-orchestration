package com.example.compensation.domain.model

import com.example.compensation.domain.CompensationStatus
import com.example.compensation.domain.CompensationType
import com.example.compensation.domain.exception.InvalidCompensationStateException
import java.time.Instant

class Compensation private constructor(
    val id: Long?,
    val orderId: String,
    val compensationType: CompensationType,
    status: CompensationStatus,
    errorMessage: String?,
    val createdAt: Instant,
    completedAt: Instant?,
) {
    var status: CompensationStatus = status
        private set

    var errorMessage: String? = errorMessage
        private set

    var completedAt: Instant? = completedAt
        private set

    fun startProcessing() {
        transitTo(CompensationStatus.PROCESSING)
    }

    fun complete() {
        transitTo(CompensationStatus.COMPLETED)
        completedAt = Instant.now()
    }

    fun fail(message: String) {
        transitTo(CompensationStatus.FAILED)
        errorMessage = message
    }

    private fun transitTo(target: CompensationStatus) {
        if (!status.canTransitionTo(target)) {
            throw InvalidCompensationStateException(status, target)
        }
        status = target
    }

    companion object {
        fun create(
            orderId: String,
            compensationType: CompensationType,
        ): Compensation {
            val now = Instant.now()
            return Compensation(
                id = null,
                orderId = orderId,
                compensationType = compensationType,
                status = CompensationStatus.PROCESSING,
                errorMessage = null,
                createdAt = now,
                completedAt = null,
            )
        }

        fun reconstitute(
            id: Long,
            orderId: String,
            compensationType: CompensationType,
            status: CompensationStatus,
            errorMessage: String?,
            createdAt: Instant,
            completedAt: Instant?,
        ): Compensation = Compensation(
            id = id,
            orderId = orderId,
            compensationType = compensationType,
            status = status,
            errorMessage = errorMessage,
            createdAt = createdAt,
            completedAt = completedAt,
        )
    }
}
