package com.example.compensation.domain.model

import java.time.Instant

class OutboxTask private constructor(
    val id: Long?,
    val compensationId: Long,
    val orderId: String,
    val taskType: String,
    val payload: String,
    val published: Boolean,
    val createdAt: Instant,
    val publishedAt: Instant?,
) {
    fun markPublished(): OutboxTask = OutboxTask(
        id = id,
        compensationId = compensationId,
        orderId = orderId,
        taskType = taskType,
        payload = payload,
        published = true,
        createdAt = createdAt,
        publishedAt = Instant.now(),
    )

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
