package com.example.order.infrastructure.persistence

import com.example.order.domain.OrderRepository
import com.example.order.domain.OrderStatus
import com.example.order.domain.model.Money
import com.example.order.domain.model.Order
import com.example.order.domain.model.OrderId
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryImpl(
    private val jpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun findById(id: OrderId): Order? =
        jpaRepository.findById(id.value).orElse(null)?.let(::toDomain)

    override fun save(order: Order): Order {
        val entity = toEntity(order)
        return toDomain(jpaRepository.save(entity))
    }

    private fun toDomain(entity: OrderJpaEntity): Order = Order.reconstitute(
        orderId = OrderId(entity.orderId),
        productId = entity.productId,
        quantity = entity.quantity,
        amount = Money(entity.amount),
        status = OrderStatus.valueOf(entity.status),
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    private fun toEntity(order: Order): OrderJpaEntity = OrderJpaEntity(
        orderId = order.id.value,
        productId = order.productId,
        quantity = order.quantity,
        amount = order.amount.amount,
        status = order.status.name,
        createdAt = order.createdAt,
        updatedAt = order.updatedAt,
    )
}
