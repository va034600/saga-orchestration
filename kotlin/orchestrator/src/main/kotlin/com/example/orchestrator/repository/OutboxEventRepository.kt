package com.example.orchestrator.repository

import com.example.orchestrator.entity.OutboxEvent
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEventRepository : JpaRepository<OutboxEvent, Long> {
    fun findByPublishedFalseOrderByCreatedAtAsc(): List<OutboxEvent>
}
