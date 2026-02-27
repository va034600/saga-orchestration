package com.example.compensation.domain.model

import java.time.Instant

class OutboxTask private constructor(
    val id: Long?,
    val compensationId: Long,
    val orderId: String,
    val taskType: String,
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
            compensationId: Long,
            orderId: String,
            taskType: String,
            payload: String,
        ): OutboxTask = OutboxTask(
            id = null,
            compensationId = compensationId,
            orderId = orderId,
            taskType = taskType,
            payload = payload,
            published = false,
            createdAt = Instant.now(),
            publishedAt = null,
        )

        fun reconstitute(
            id: Long,
            compensationId: Long,
            orderId: String,
            taskType: String,
            payload: String,
            published: Boolean,
            createdAt: Instant,
            publishedAt: Instant?,
        ): OutboxTask = OutboxTask(
            id = id,
            compensationId = compensationId,
            orderId = orderId,
            taskType = taskType,
            payload = payload,
            published = published,
            createdAt = createdAt,
            publishedAt = publishedAt,
        )
    }
}
