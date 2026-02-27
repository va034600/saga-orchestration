package com.example.orchestrator.domain

import com.example.orchestrator.domain.model.OutboxEvent

interface OutboxEventRepository {
    fun save(event: OutboxEvent): OutboxEvent
    fun findUnpublishedOrderByCreatedAt(): List<OutboxEvent>
}
