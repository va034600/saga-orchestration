package com.example.order.domain.model

import com.example.order.domain.OrderStatus
import com.example.order.domain.exception.InvalidOrderStateException
import java.math.BigDecimal
import java.time.Instant

class Order private constructor(
    val id: OrderId,
    val productId: String,
    val quantity: Int,
    val amount: Money,
    status: OrderStatus,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    var status: OrderStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun complete() {
        transitTo(OrderStatus.COMPLETED)
    }

    fun cancel() {
        transitTo(OrderStatus.CANCELLED)
    }

    private fun transitTo(target: OrderStatus) {
        if (!status.canTransitionTo(target)) {
            throw InvalidOrderStateException(status, target)
        }
        status = target
        updatedAt = Instant.now()
    }

    companion object {
        fun create(
            orderId: OrderId,
            productId: String,
            quantity: Int,
            amount: BigDecimal,
        ): Order {
            val now = Instant.now()
            return Order(
                id = orderId,
                productId = productId,
                quantity = quantity,
                amount = Money(amount),
                status = OrderStatus.PENDING,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            orderId: OrderId,
            productId: String,
            quantity: Int,
            amount: Money,
            status: OrderStatus,
            createdAt: Instant,
            updatedAt: Instant,
        ): Order = Order(
            id = orderId,
            productId = productId,
            quantity = quantity,
            amount = amount,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
