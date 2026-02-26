package com.example.orchestrator.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "saga_states")
class SagaState(
    @Id
    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Column(nullable = false)
    var status: String = "STARTED",

    @Column(name = "current_step")
    var currentStep: String? = null,

    @OneToMany(mappedBy = "sagaState", cascade = [CascadeType.ALL], orphanRemoval = true)
    val steps: MutableList<SagaStep> = mutableListOf(),

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
