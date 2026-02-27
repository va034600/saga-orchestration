package com.example.orchestrator.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEventJpaRepository : JpaRepository<OutboxEventJpaEntity, Long> {
    fun findByPublishedFalseOrderByCreatedAtAsc(): List<OutboxEventJpaEntity>
}
