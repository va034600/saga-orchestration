package com.saga.orchestrator.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "saga_steps")
class SagaStep(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val sagaState: SagaState,

    @Column(name = "step_name", nullable = false)
    val stepName: String,

    @Column(nullable = false)
    var status: String = "PENDING",

    @Column(name = "error_message")
    var errorMessage: String? = null,

    @Column(name = "executed_at")
    var executedAt: Instant? = null
)
