package com.example.compensation.infrastructure.persistence

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "compensations")
class CompensationJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comp_id")
    val compId: Long? = null,

    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Column(name = "compensation_type", nullable = false)
    val compensationType: String,

    @Column(nullable = false)
    var status: String,

    @Column(name = "error_message")
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "completed_at")
    var completedAt: Instant? = null,
)
