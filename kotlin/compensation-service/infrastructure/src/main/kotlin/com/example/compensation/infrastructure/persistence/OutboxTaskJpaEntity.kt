package com.example.compensation.infrastructure.persistence

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "outbox_tasks")
class OutboxTaskJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val compensationId: Long,
    val orderId: String,
    val taskType: String,
    val payload: String,
    var published: Boolean = false,
    val createdAt: Instant = Instant.now(),
    var publishedAt: Instant? = null,
)
