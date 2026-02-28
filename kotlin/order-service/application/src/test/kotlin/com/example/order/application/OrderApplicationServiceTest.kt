package com.example.order.application

import com.example.common.dto.OrderRequest
import com.example.common.dto.OrderStatus as DtoOrderStatus
import com.example.common.exception.OrderNotFoundException
import com.example.common.exception.SagaException
import com.example.order.domain.OrderRepository
import com.example.order.domain.OrderStatus
import com.example.order.domain.model.Money
import com.example.order.domain.model.Order
import com.example.order.domain.model.OrderId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals

class OrderApplicationServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val sut = OrderApplicationService(orderRepository)

    private val orderId = "order-001"
    private val productId = "product-001"
    private val quantity = 2
    private val amount = BigDecimal("1000")

    private fun pendingOrder(): Order = Order.reconstitute(
        orderId = OrderId(orderId),
        productId = productId,
        quantity = quantity,
        amount = Money(amount),
        status = OrderStatus.PENDING,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    private fun completedOrder(): Order = Order.reconstitute(
        orderId = OrderId(orderId),
        productId = productId,
        quantity = quantity,
        amount = Money(amount),
        status = OrderStatus.COMPLETED,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:01Z"),
    )

    @Nested
    inner class CreateOrder {

        @Test
        fun `creates a new order with PENDING status`() {
            val request = OrderRequest(orderId, productId, quantity, amount)
            every { orderRepository.findById(OrderId(orderId)) } returns null
            every { orderRepository.save(any()) } answers { firstArg() }

            val response = sut.createOrder(request)

            assertEquals(orderId, response.orderId)
            assertEquals(DtoOrderStatus.PENDING, response.status)
            verify { orderRepository.save(match { it.status == OrderStatus.PENDING }) }
        }

        @Test
        fun `returns existing order when already exists`() {
            val request = OrderRequest(orderId, productId, quantity, amount)
            every { orderRepository.findById(OrderId(orderId)) } returns pendingOrder()

            val response = sut.createOrder(request)

            assertEquals(orderId, response.orderId)
            assertEquals(DtoOrderStatus.PENDING, response.status)
            verify(exactly = 0) { orderRepository.save(any()) }
        }
    }

    @Nested
    inner class CompleteOrder {

        @Test
        fun `completes a PENDING order`() {
            every { orderRepository.findById(OrderId(orderId)) } returns pendingOrder()
            every { orderRepository.save(any()) } answers { firstArg() }

            val response = sut.completeOrder(orderId)

            assertEquals(orderId, response.orderId)
            assertEquals(DtoOrderStatus.COMPLETED, response.status)
        }

        @Test
        fun `throws OrderNotFoundException when order does not exist`() {
            every { orderRepository.findById(OrderId(orderId)) } returns null

            assertThrows<OrderNotFoundException> {
                sut.completeOrder(orderId)
            }
        }

        @Test
        fun `throws SagaException on invalid state transition`() {
            every { orderRepository.findById(OrderId(orderId)) } returns completedOrder()

            assertThrows<SagaException> {
                sut.completeOrder(orderId)
            }
        }
    }

    @Nested
    inner class CancelOrder {

        @Test
        fun `cancels a PENDING order`() {
            every { orderRepository.findById(OrderId(orderId)) } returns pendingOrder()
            every { orderRepository.save(any()) } answers { firstArg() }

            val response = sut.cancelOrder(orderId)

            assertEquals(orderId, response.orderId)
            assertEquals(DtoOrderStatus.CANCELLED, response.status)
        }

        @Test
        fun `throws OrderNotFoundException when order does not exist`() {
            every { orderRepository.findById(OrderId(orderId)) } returns null

            assertThrows<OrderNotFoundException> {
                sut.cancelOrder(orderId)
            }
        }

        @Test
        fun `throws SagaException on invalid state transition`() {
            every { orderRepository.findById(OrderId(orderId)) } returns completedOrder()

            assertThrows<SagaException> {
                sut.cancelOrder(orderId)
            }
        }
    }

    @Nested
    inner class GetOrder {

        @Test
        fun `returns existing order`() {
            every { orderRepository.findById(OrderId(orderId)) } returns pendingOrder()

            val response = sut.getOrder(orderId)

            assertEquals(orderId, response.orderId)
            assertEquals(DtoOrderStatus.PENDING, response.status)
        }

        @Test
        fun `throws OrderNotFoundException when order does not exist`() {
            every { orderRepository.findById(OrderId(orderId)) } returns null

            assertThrows<OrderNotFoundException> {
                sut.getOrder(orderId)
            }
        }
    }
}
