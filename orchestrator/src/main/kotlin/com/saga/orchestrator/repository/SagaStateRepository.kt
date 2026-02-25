package com.saga.orchestrator.repository

import com.saga.orchestrator.entity.SagaState
import org.springframework.data.jpa.repository.JpaRepository

interface SagaStateRepository : JpaRepository<SagaState, String>
