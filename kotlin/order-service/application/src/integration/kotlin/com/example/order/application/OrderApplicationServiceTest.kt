package com.example.order.application

import com.example.common.dto.OrderRequest
import com.example.common.dto.OrderStatus
import com.example.common.exception.OrderNotFoundException
import com.example.common.exception.SagaException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("integration")
@Transactional
class OrderApplicationServiceTest {

    @Autowired
    private lateinit var orderApplicationService: OrderApplicationService

    private fun createRequest(
        orderId: String = "order-001",
        productId: String = "product-001",
        quantity: Int = 2,
        amount: BigDecimal = BigDecimal("1000"),
    ) = OrderRequest(orderId, productId, quantity, amount)

    @Nested
    inner class CreateOrder {

        @Test
        fun `新規注文を作成しPENDINGステータスで返す`() {
            val response = orderApplicationService.createOrder(createRequest())

            assertEquals("order-001", response.orderId)
            assertEquals(OrderStatus.PENDING, response.status)
        }

        @Test
        fun `同一orderIdで既存注文を返す`() {
            val first = orderApplicationService.createOrder(createRequest())
            val second = orderApplicationService.createOrder(createRequest())

            assertEquals(first.orderId, second.orderId)
            assertEquals(first.status, second.status)
        }
    }

    @Nested
    inner class CompleteOrder {

        @Test
        fun `PENDING注文をCOMPLETEDに遷移する`() {
            orderApplicationService.createOrder(createRequest())

            val response = orderApplicationService.completeOrder("order-001")

            assertEquals("order-001", response.orderId)
            assertEquals(OrderStatus.COMPLETED, response.status)
        }

        @Test
        fun `注文が存在しない場合はOrderNotFoundExceptionを投げる`() {
            assertThrows<OrderNotFoundException> {
                orderApplicationService.completeOrder("non-existent")
            }
        }

        @Test
        fun `不正な状態遷移の場合はSagaExceptionを投げる`() {
            orderApplicationService.createOrder(createRequest())
            orderApplicationService.completeOrder("order-001")

            assertThrows<SagaException> {
                orderApplicationService.completeOrder("order-001")
            }
        }
    }

    @Nested
    inner class CancelOrder {

        @Test
        fun `PENDING注文をCANCELLEDに遷移する`() {
            orderApplicationService.createOrder(createRequest())

            val response = orderApplicationService.cancelOrder("order-001")

            assertEquals("order-001", response.orderId)
            assertEquals(OrderStatus.CANCELLED, response.status)
        }

        @Test
        fun `注文が存在しない場合はOrderNotFoundExceptionを投げる`() {
            assertThrows<OrderNotFoundException> {
                orderApplicationService.cancelOrder("non-existent")
            }
        }

        @Test
        fun `不正な状態遷移の場合はSagaExceptionを投げる`() {
            orderApplicationService.createOrder(createRequest())
            orderApplicationService.completeOrder("order-001")

            assertThrows<SagaException> {
                orderApplicationService.cancelOrder("order-001")
            }
        }
    }

    @Nested
    inner class GetOrder {

        @Test
        fun `注文情報を返す`() {
            orderApplicationService.createOrder(createRequest())

            val response = orderApplicationService.getOrder("order-001")

            assertEquals("order-001", response.orderId)
            assertEquals(OrderStatus.PENDING, response.status)
        }

        @Test
        fun `注文が存在しない場合はOrderNotFoundExceptionを投げる`() {
            assertThrows<OrderNotFoundException> {
                orderApplicationService.getOrder("non-existent")
            }
        }
    }
}
