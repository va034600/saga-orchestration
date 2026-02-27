package com.example.order.domain

enum class OrderStatus {
    PENDING, COMPLETED, CANCELLED;

    fun canTransitionTo(target: OrderStatus): Boolean = when (this) {
        PENDING -> target in setOf(COMPLETED, CANCELLED)
        COMPLETED, CANCELLED -> false
    }
}
