package com.example.orchestrator.domain.model

import com.example.orchestrator.domain.StepStatus
import java.time.Instant

class SagaStep(
    val id: Long?,
    val stepName: String,
    status: StepStatus,
    var errorMessage: String?,
    var executedAt: Instant?,
) {
    var status: StepStatus = status
        private set

    fun complete() {
        status = StepStatus.COMPLETED
        executedAt = Instant.now()
    }

    companion object {
        fun create(stepName: String): SagaStep = SagaStep(
            id = null,
            stepName = stepName,
            status = StepStatus.IN_PROGRESS,
            errorMessage = null,
            executedAt = null,
        )

        fun reconstitute(
            id: Long,
            stepName: String,
            status: StepStatus,
            errorMessage: String?,
            executedAt: Instant?,
        ): SagaStep = SagaStep(
            id = id,
            stepName = stepName,
            status = status,
            errorMessage = errorMessage,
            executedAt = executedAt,
        )
    }
}
