package com.example.orchestrator.infrastructure.persistence

import com.example.orchestrator.domain.SagaStateRepository
import com.example.orchestrator.domain.SagaStatus
import com.example.orchestrator.domain.StepStatus
import com.example.orchestrator.domain.model.SagaState
import com.example.orchestrator.domain.model.SagaStep
import org.springframework.stereotype.Repository

@Repository
class SagaStateRepositoryImpl(
    private val jpaRepository: SagaStateJpaRepository,
) : SagaStateRepository {

    override fun save(sagaState: SagaState): SagaState {
        val entity = toEntity(sagaState)
        return toDomain(jpaRepository.save(entity))
    }

    private fun toDomain(entity: SagaStateJpaEntity): SagaState = SagaState.reconstitute(
        orderId = entity.orderId,
        status = SagaStatus.valueOf(entity.status),
        currentStep = entity.currentStep,
        steps = entity.steps.map { step ->
            SagaStep.reconstitute(
                id = step.id!!,
                stepName = step.stepName,
                status = StepStatus.valueOf(step.status),
                errorMessage = step.errorMessage,
                executedAt = step.executedAt,
            )
        }.toMutableList(),
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    private fun toEntity(sagaState: SagaState): SagaStateJpaEntity {
        val entity = SagaStateJpaEntity(
            orderId = sagaState.orderId,
            status = sagaState.status.name,
            currentStep = sagaState.currentStep,
            createdAt = sagaState.createdAt,
            updatedAt = sagaState.updatedAt,
        )
        entity.steps.clear()
        entity.steps.addAll(sagaState.steps.map { step ->
            SagaStepJpaEntity(
                id = step.id,
                sagaState = entity,
                stepName = step.stepName,
                status = step.status.name,
                errorMessage = step.errorMessage,
                executedAt = step.executedAt,
            )
        })
        return entity
    }
}
