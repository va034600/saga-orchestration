package com.example.orchestrator.domain.model

import com.example.orchestrator.domain.SagaStatus
import com.example.orchestrator.domain.StepStatus
import java.time.Instant

class SagaState private constructor(
    val orderId: String,
    val status: SagaStatus,
    val currentStep: String?,
    val steps: List<SagaStep>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun addStep(stepName: String): SagaState {
        val step = SagaStep.create(stepName)
        return SagaState(
            orderId = orderId,
            status = status,
            currentStep = stepName,
            steps = steps + step,
            createdAt = createdAt,
            updatedAt = Instant.now(),
        )
    }

    fun completeCurrentStep(): SagaState {
        val lastStep = steps.last()
        val completedStep = lastStep.complete()
        return SagaState(
            orderId = orderId,
            status = status,
            currentStep = currentStep,
            steps = steps.dropLast(1) + completedStep,
            createdAt = createdAt,
            updatedAt = Instant.now(),
        )
    }

    fun markCompleted(): SagaState = SagaState(
        orderId = orderId,
        status = SagaStatus.COMPLETED,
        currentStep = currentStep,
        steps = steps,
        createdAt = createdAt,
        updatedAt = Instant.now(),
    )

    fun markFailed(): SagaState = SagaState(
        orderId = orderId,
        status = SagaStatus.FAILED,
        currentStep = currentStep,
        steps = steps,
        createdAt = createdAt,
        updatedAt = Instant.now(),
    )

    fun completedStepNames(): List<String> =
        steps.filter { it.status == StepStatus.COMPLETED }
            .map { it.stepName }

    companion object {
        fun create(orderId: String): SagaState {
            val now = Instant.now()
            return SagaState(
                orderId = orderId,
                status = SagaStatus.STARTED,
                currentStep = null,
                steps = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            orderId: String,
            status: SagaStatus,
            currentStep: String?,
            steps: List<SagaStep>,
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
