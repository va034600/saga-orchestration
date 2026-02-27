package com.example.payment.domain.model

import com.example.payment.domain.PaymentStatus
import com.example.payment.domain.exception.InvalidPaymentStateException
import java.math.BigDecimal
import java.time.Instant

class Payment private constructor(
    val id: PaymentId,
    val orderId: String,
    val amount: Money,
    val status: PaymentStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun capture(): Payment {
        if (!status.canTransitionTo(PaymentStatus.CAPTURED)) {
            throw InvalidPaymentStateException(status, PaymentStatus.CAPTURED)
        }
        return Payment(
            id = id,
            orderId = orderId,
            amount = amount,
            status = PaymentStatus.CAPTURED,
            createdAt = createdAt,
            updatedAt = Instant.now(),
        )
    }

    fun refund(): Payment {
        if (!status.canTransitionTo(PaymentStatus.REFUNDED)) {
            throw InvalidPaymentStateException(status, PaymentStatus.REFUNDED)
        }
        return Payment(
            id = id,
            orderId = orderId,
            amount = amount,
            status = PaymentStatus.REFUNDED,
            createdAt = createdAt,
            updatedAt = Instant.now(),
        )
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
