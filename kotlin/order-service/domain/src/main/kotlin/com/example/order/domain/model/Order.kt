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
    val status: OrderStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun complete(): Order {
        if (!status.canTransitionTo(OrderStatus.COMPLETED)) {
            throw InvalidOrderStateException(status, OrderStatus.COMPLETED)
        }
        return Order(
            id = id,
            productId = productId,
            quantity = quantity,
            amount = amount,
            status = OrderStatus.COMPLETED,
            createdAt = createdAt,
            updatedAt = Instant.now(),
        )
    }

    fun cancel(): Order {
        if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
            throw InvalidOrderStateException(status, OrderStatus.CANCELLED)
        }
        return Order(
            id = id,
            productId = productId,
            quantity = quantity,
            amount = amount,
            status = OrderStatus.CANCELLED,
            createdAt = createdAt,
            updatedAt = Instant.now(),
        )
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
