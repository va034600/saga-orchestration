package com.example.payment.domain

import com.example.payment.domain.model.Payment

interface PaymentRepository {
    fun findByOrderId(orderId: String): Payment?
    fun save(payment: Payment): Payment
}
