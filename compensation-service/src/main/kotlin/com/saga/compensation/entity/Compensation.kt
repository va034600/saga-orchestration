package com.saga.compensation.entity

import com.saga.common.enums.CompensationStatus
import com.saga.common.enums.CompensationType
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "compensations")
class Compensation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comp_id")
    val compId: Long? = null,

    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "compensation_type", nullable = false)
    val compensationType: CompensationType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CompensationStatus = CompensationStatus.PENDING,

    @Column(name = "error_message")
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "completed_at")
    var completedAt: Instant? = null
)
