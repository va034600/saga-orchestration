package com.example.orchestrator.domain.model

import java.time.Instant

class OutboxEvent private constructor(
    val id: Long?,
    val orderId: String,
    val eventType: String,
    val payload: String,
    published: Boolean,
    val createdAt: Instant,
    publishedAt: Instant?,
) {
    var published: Boolean = published
        private set

    var publishedAt: Instant? = publishedAt
        private set

    fun markPublished() {
        published = true
        publishedAt = Instant.now()
    }

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
