package com.example.payment.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentJpaRepository : JpaRepository<PaymentJpaEntity, String> {
    fun findByOrderId(orderId: String): Optional<PaymentJpaEntity>
}
