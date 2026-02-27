package com.example.order.application

import com.example.common.dto.OrderRequest
import com.example.common.dto.OrderResponse
import com.example.common.dto.OrderStatus as DtoOrderStatus
import com.example.common.exception.OrderNotFoundException
import com.example.common.exception.SagaException
import com.example.order.domain.OrderRepository
import com.example.order.domain.OrderStatus
import com.example.order.domain.exception.InvalidOrderStateException
import com.example.order.domain.model.Order
import com.example.order.domain.model.OrderId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderApplicationService(
    private val orderRepository: OrderRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createOrder(request: OrderRequest): OrderResponse {
        val orderId = OrderId(request.orderId)
        val existing = orderRepository.findById(orderId)
        if (existing != null) {
            log.info("Order already exists: {}", request.orderId)
            return toResponse(existing)
        }

        val order = Order.create(
            orderId = orderId,
            productId = request.productId,
            quantity = request.quantity,
            amount = request.amount,
        )
        val saved = orderRepository.save(order)
        log.info("Order created: {}", saved.id.value)
        return toResponse(saved)
    }

    @Transactional
    fun completeOrder(orderId: String): OrderResponse {
        val order = findOrThrow(orderId)
        val completed = try {
            order.complete()
        } catch (e: InvalidOrderStateException) {
            throw SagaException(e.message ?: "Invalid state transition", e)
        }
        val saved = orderRepository.save(completed)
        log.info("Order completed: {}", orderId)
        return toResponse(saved)
    }

    @Transactional
    fun cancelOrder(orderId: String): OrderResponse {
        val order = findOrThrow(orderId)
        val cancelled = try {
            order.cancel()
        } catch (e: InvalidOrderStateException) {
            throw SagaException(e.message ?: "Invalid state transition", e)
        }
        val saved = orderRepository.save(cancelled)
        log.info("Order cancelled: {}", orderId)
        return toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getOrder(orderId: String): OrderResponse {
        val order = findOrThrow(orderId)
        return toResponse(order)
    }

    private fun findOrThrow(orderId: String): Order =
        orderRepository.findById(OrderId(orderId))
            ?: throw OrderNotFoundException(orderId)

    private fun toResponse(order: Order): OrderResponse = OrderResponse(
        orderId = order.id.value,
        status = toDto(order.status),
    )

    private fun toDto(status: OrderStatus): DtoOrderStatus = when (status) {
        OrderStatus.PENDING -> DtoOrderStatus.PENDING
        OrderStatus.COMPLETED -> DtoOrderStatus.COMPLETED
        OrderStatus.CANCELLED -> DtoOrderStatus.CANCELLED
    }
}
