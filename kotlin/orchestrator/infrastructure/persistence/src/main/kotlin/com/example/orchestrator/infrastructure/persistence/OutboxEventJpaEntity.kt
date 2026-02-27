package com.example.orchestrator.infrastructure.persistence

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "outbox_events")
class OutboxEventJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val orderId: String,
    val eventType: String,
    val payload: String,
    var published: Boolean = false,
    val createdAt: Instant = Instant.now(),
    var publishedAt: Instant? = null,
)
