package com.example.order.presentation

import com.example.common.dto.OrderResponse
import com.example.common.dto.OrderStatus
import com.example.common.exception.GlobalExceptionHandler
import com.example.common.exception.OrderNotFoundException
import com.example.common.exception.SagaException
import com.example.order.application.OrderApplicationService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class OrderControllerTest {

    private val orderApplicationService = mockk<OrderApplicationService>()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(OrderController(orderApplicationService))
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    private val orderResponse = OrderResponse("order-001", OrderStatus.PENDING)

    @Nested
    inner class CreateOrder {

        @Test
        fun `注文を作成し201を返す`() {
            every { orderApplicationService.createOrder(any()) } returns orderResponse

            mockMvc.perform(
                post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"orderId":"order-001","productId":"product-001","quantity":2,"amount":1000}""")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.orderId").value("order-001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
        }
    }

    @Nested
    inner class CompleteOrder {

        @Test
        fun `注文を完了し200を返す`() {
            val completed = OrderResponse("order-001", OrderStatus.COMPLETED)
            every { orderApplicationService.completeOrder("order-001") } returns completed

            mockMvc.perform(put("/api/orders/order-001/complete"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("COMPLETED"))
        }

        @Test
        fun `存在しない注文で404を返す`() {
            every { orderApplicationService.completeOrder("non-existent") } throws OrderNotFoundException("non-existent")

            mockMvc.perform(put("/api/orders/non-existent/complete"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `不正な状態遷移で500を返す`() {
            every { orderApplicationService.completeOrder("order-001") } throws SagaException("Cannot transition from COMPLETED to COMPLETED")

            mockMvc.perform(put("/api/orders/order-001/complete"))
                .andExpect(status().isInternalServerError)
        }
    }

    @Nested
    inner class CancelOrder {

        @Test
        fun `注文をキャンセルし200を返す`() {
            val cancelled = OrderResponse("order-001", OrderStatus.CANCELLED)
            every { orderApplicationService.cancelOrder("order-001") } returns cancelled

            mockMvc.perform(put("/api/orders/order-001/cancel"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("CANCELLED"))
        }

        @Test
        fun `存在しない注文で404を返す`() {
            every { orderApplicationService.cancelOrder("non-existent") } throws OrderNotFoundException("non-existent")

            mockMvc.perform(put("/api/orders/non-existent/cancel"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class GetOrder {

        @Test
        fun `注文情報を取得し200を返す`() {
            every { orderApplicationService.getOrder("order-001") } returns orderResponse

            mockMvc.perform(get("/api/orders/order-001"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.orderId").value("order-001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
        }

        @Test
        fun `存在しない注文で404を返す`() {
            every { orderApplicationService.getOrder("non-existent") } throws OrderNotFoundException("non-existent")

            mockMvc.perform(get("/api/orders/non-existent"))
                .andExpect(status().isNotFound)
        }
    }
}
