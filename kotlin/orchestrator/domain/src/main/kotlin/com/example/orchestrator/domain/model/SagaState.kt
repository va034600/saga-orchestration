package com.example.orchestrator.domain.model

import com.example.orchestrator.domain.SagaStatus
import java.time.Instant

class SagaState private constructor(
    val orderId: String,
    status: SagaStatus,
    currentStep: String?,
    val steps: MutableList<SagaStep>,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    var status: SagaStatus = status
        private set

    var currentStep: String? = currentStep
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun addStep(stepName: String): SagaStep {
        val step = SagaStep.create(stepName)
        steps.add(step)
        currentStep = stepName
        updatedAt = Instant.now()
        return step
    }

    fun markCompleted() {
        status = SagaStatus.COMPLETED
        updatedAt = Instant.now()
    }

    fun markFailed() {
        status = SagaStatus.FAILED
        updatedAt = Instant.now()
    }

    fun completedStepNames(): List<String> =
        steps.filter { it.status == com.example.orchestrator.domain.StepStatus.COMPLETED }
            .map { it.stepName }

    companion object {
        fun create(orderId: String): SagaState {
            val now = Instant.now()
            return SagaState(
                orderId = orderId,
                status = SagaStatus.STARTED,
                currentStep = null,
                steps = mutableListOf(),
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            orderId: String,
            status: SagaStatus,
            currentStep: String?,
            steps: MutableList<SagaStep>,
            createdAt: Instant,
            updatedAt: Instant,
        ): SagaState = SagaState(
            orderId = orderId,
            status = status,
            currentStep = currentStep,
            steps = steps,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
