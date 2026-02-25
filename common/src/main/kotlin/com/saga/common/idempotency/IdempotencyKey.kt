package com.saga.common.idempotency

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "idempotency_keys")
class IdempotencyKey(
    @Id
    @Column(name = "idempotency_key", nullable = false)
    val idempotencyKey: String,

    @Column(name = "response_body", columnDefinition = "TEXT")
    var responseBody: String? = null,

    @Column(name = "status_code")
    var statusCode: Int? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
