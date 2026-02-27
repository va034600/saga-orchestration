package com.example.orchestrator.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SagaStateJpaRepository : JpaRepository<SagaStateJpaEntity, String>
