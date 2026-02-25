package com.saga.payment.repository

import com.saga.payment.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentRepository : JpaRepository<Payment, String> {
    fun findByOrderId(orderId: String): Optional<Payment>
}
