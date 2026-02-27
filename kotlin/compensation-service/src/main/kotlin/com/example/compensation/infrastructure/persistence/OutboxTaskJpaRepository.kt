package com.example.compensation.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface OutboxTaskJpaRepository : JpaRepository<OutboxTaskJpaEntity, Long> {
    fun findByPublishedFalseOrderByCreatedAtAsc(): List<OutboxTaskJpaEntity>
}
