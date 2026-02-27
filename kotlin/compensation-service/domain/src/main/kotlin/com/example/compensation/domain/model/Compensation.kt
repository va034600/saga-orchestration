package com.example.compensation.domain.model

import com.example.compensation.domain.CompensationStatus
import com.example.compensation.domain.CompensationType
import com.example.compensation.domain.exception.InvalidCompensationStateException
import java.time.Instant

class Compensation private constructor(
    val id: Long?,
    val orderId: String,
    val compensationType: CompensationType,
    val status: CompensationStatus,
    val errorMessage: String?,
    val createdAt: Instant,
    val completedAt: Instant?,
) {
    fun startProcessing(): Compensation {
        if (!status.canTransitionTo(CompensationStatus.PROCESSING)) {
            throw InvalidCompensationStateException(status, CompensationStatus.PROCESSING)
        }
        return Compensation(
            id = id,
            orderId = orderId,
            compensationType = compensationType,
            status = CompensationStatus.PROCESSING,
            errorMessage = errorMessage,
            createdAt = createdAt,
            completedAt = completedAt,
        )
    }

    fun complete(): Compensation {
        if (!status.canTransitionTo(CompensationStatus.COMPLETED)) {
            throw InvalidCompensationStateException(status, CompensationStatus.COMPLETED)
        }
        return Compensation(
            id = id,
            orderId = orderId,
            compensationType = compensationType,
            status = CompensationStatus.COMPLETED,
            errorMessage = errorMessage,
            createdAt = createdAt,
            completedAt = Instant.now(),
        )
    }

    fun fail(message: String): Compensation {
        if (!status.canTransitionTo(CompensationStatus.FAILED)) {
            throw InvalidCompensationStateException(status, CompensationStatus.FAILED)
        }
        return Compensation(
            id = id,
            orderId = orderId,
            compensationType = compensationType,
            status = CompensationStatus.FAILED,
            errorMessage = message,
            createdAt = createdAt,
            completedAt = completedAt,
        )
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
