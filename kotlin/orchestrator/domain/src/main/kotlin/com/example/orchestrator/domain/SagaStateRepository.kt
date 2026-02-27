package com.example.orchestrator.domain

import com.example.orchestrator.domain.model.SagaState

interface SagaStateRepository {
    fun save(sagaState: SagaState): SagaState
}
