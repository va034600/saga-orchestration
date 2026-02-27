package com.example.orchestrator.domain.model

import java.time.Instant

class OutboxEvent private constructor(
    val id: Long?,
    val orderId: String,
    val eventType: String,
    val payload: String,
    val published: Boolean,
    val createdAt: Instant,
    val publishedAt: Instant?,
) {
    fun markPublished(): OutboxEvent = OutboxEvent(
        id = id,
        orderId = orderId,
        eventType = eventType,
        payload = payload,
        published = true,
        createdAt = createdAt,
        publishedAt = Instant.now(),
    )

    companion object {
        fun create(
            orderId: String,
            eventType: String,
            payload: String,
        ): OutboxEvent = OutboxEvent(
            id = null,
            orderId = orderId,
            eventType = eventType,
            payload = payload,
            published = false,
            createdAt = Instant.now(),
            publishedAt = null,
        )

        fun reconstitute(
            id: Long,
            orderId: String,
            eventType: String,
            payload: String,
            published: Boolean,
            createdAt: Instant,
            publishedAt: Instant?,
        ): OutboxEvent = OutboxEvent(
            id = id,
            orderId = orderId,
            eventType = eventType,
            payload = payload,
            published = published,
            createdAt = createdAt,
            publishedAt = publishedAt,
        )
    }
}
