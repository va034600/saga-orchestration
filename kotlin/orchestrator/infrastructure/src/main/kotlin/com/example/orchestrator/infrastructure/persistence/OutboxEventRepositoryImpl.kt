package com.example.orchestrator.infrastructure.persistence

import com.example.orchestrator.domain.OutboxEventRepository
import com.example.orchestrator.domain.model.OutboxEvent
import org.springframework.stereotype.Repository

@Repository
class OutboxEventRepositoryImpl(
    private val jpaRepository: OutboxEventJpaRepository,
) : OutboxEventRepository {

    override fun save(event: OutboxEvent): OutboxEvent {
        val entity = toEntity(event)
        return toDomain(jpaRepository.save(entity))
    }

    override fun findUnpublishedOrderByCreatedAt(): List<OutboxEvent> =
        jpaRepository.findByPublishedFalseOrderByCreatedAtAsc().map(::toDomain)

    private fun toDomain(entity: OutboxEventJpaEntity): OutboxEvent = OutboxEvent.reconstitute(
        id = entity.id!!,
        orderId = entity.orderId,
        eventType = entity.eventType,
        payload = entity.payload,
        published = entity.published,
        createdAt = entity.createdAt,
        publishedAt = entity.publishedAt,
    )

    private fun toEntity(event: OutboxEvent): OutboxEventJpaEntity = OutboxEventJpaEntity(
        id = event.id,
        orderId = event.orderId,
        eventType = event.eventType,
        payload = event.payload,
        published = event.published,
        createdAt = event.createdAt,
        publishedAt = event.publishedAt,
    )
}
