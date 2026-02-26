package com.example.order.service

import com.example.common.dto.OrderRequest
import com.example.common.dto.OrderResponse
import com.example.common.dto.OrderStatus
import com.example.common.exception.OrderNotFoundException
import com.example.order.entity.Order
import com.example.order.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createOrder(request: OrderRequest): OrderResponse {
        val existing = orderRepository.findById(request.orderId)
        if (existing.isPresent) {
            log.info("Order already exists: {}", request.orderId)
            val order = existing.get()
            return OrderResponse(orderId = order.orderId, status = order.status)
        }

        val order = Order(
            orderId = request.orderId,
            productId = request.productId,
            quantity = request.quantity,
            amount = request.amount
        )
        orderRepository.save(order)
        log.info("Order created: {}", order.orderId)
        return OrderResponse(orderId = order.orderId, status = order.status)
    }

    @Transactional
    fun completeOrder(orderId: String): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }
        order.status = OrderStatus.COMPLETED
        order.updatedAt = Instant.now()
        orderRepository.save(order)
        log.info("Order completed: {}", orderId)
        return OrderResponse(orderId = order.orderId, status = order.status)
    }

    @Transactional
    fun cancelOrder(orderId: String): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }
        order.status = OrderStatus.CANCELLED
        order.updatedAt = Instant.now()
        orderRepository.save(order)
        log.info("Order cancelled: {}", orderId)
        return OrderResponse(orderId = order.orderId, status = order.status)
    }

    @Transactional(readOnly = true)
    fun getOrder(orderId: String): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }
        return OrderResponse(orderId = order.orderId, status = order.status)
    }
}
