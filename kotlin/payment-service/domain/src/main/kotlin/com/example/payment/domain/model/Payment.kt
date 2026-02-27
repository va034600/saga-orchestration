package com.example.payment.domain.model

import com.example.payment.domain.PaymentStatus
import com.example.payment.domain.exception.InvalidPaymentStateException
import java.math.BigDecimal
import java.time.Instant

class Payment private constructor(
    val id: PaymentId,
    val orderId: String,
    val amount: Money,
    status: PaymentStatus,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    var status: PaymentStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun capture() {
        transitTo(PaymentStatus.CAPTURED)
    }

    fun refund() {
        transitTo(PaymentStatus.REFUNDED)
    }

    private fun transitTo(target: PaymentStatus) {
        if (!status.canTransitionTo(target)) {
            throw InvalidPaymentStateException(status, target)
        }
        status = target
        updatedAt = Instant.now()
    }

    companion object {
        fun authorize(
            paymentId: PaymentId,
            orderId: String,
            amount: BigDecimal,
        ): Payment {
            val now = Instant.now()
            return Payment(
                id = paymentId,
                orderId = orderId,
                amount = Money(amount),
                status = PaymentStatus.AUTHORIZED,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            paymentId: PaymentId,
            orderId: String,
            amount: Money,
            status: PaymentStatus,
            createdAt: Instant,
            updatedAt: Instant,
        ): Payment = Payment(
            id = paymentId,
            orderId = orderId,
            amount = amount,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
