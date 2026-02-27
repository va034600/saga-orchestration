package com.example.orchestrator.repository

import com.example.orchestrator.entity.SagaState
import org.springframework.data.jpa.repository.JpaRepository

interface SagaStateRepository : JpaRepository<SagaState, String>
