package com.example.payment.domain

enum class PaymentStatus {
    AUTHORIZED, CAPTURED, REFUNDED, FAILED;

    fun canTransitionTo(target: PaymentStatus): Boolean = when (this) {
        AUTHORIZED -> target in setOf(CAPTURED, REFUNDED)
        CAPTURED -> target == REFUNDED
        REFUNDED, FAILED -> false
    }
}
