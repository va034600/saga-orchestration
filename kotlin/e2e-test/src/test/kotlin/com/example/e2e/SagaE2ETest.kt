package com.example.e2e

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class SagaE2ETest {

    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val orchestratorUrl = System.getenv("ORCHESTRATOR_URL") ?: "http://localhost:8080"

    private val restClient = RestClient.builder()
        .baseUrl(orchestratorUrl)
        .messageConverters { converters ->
            converters.removeIf { it is MappingJackson2HttpMessageConverter }
            converters.add(MappingJackson2HttpMessageConverter(objectMapper))
        }
        .build()

    @Test
    fun `同期Sagaで注文が正常に完了する`() {
        val orderId = UUID.randomUUID().toString()

        val response = try {
            restClient.post()
                .uri("/api/saga/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .body(OrderRequest(orderId = orderId, productId = "PROD-001", quantity = 1, amount = 1000))
                .retrieve()
                .body(SagaResult::class.java)!!
        } catch (e: RestClientResponseException) {
            System.err.println("=== E2E Sync Saga Error: ${e.statusCode} ===")
            System.err.println(e.responseBodyAsString)
            fail("${e.statusCode} ${e.responseBodyAsString}")
        }

        assertTrue(response.success)
        assertEquals("COMPLETED", response.order?.status)
        assertEquals("CAPTURED", response.payment?.status)
    }

    @Test
    fun `非同期Sagaで注文が正常に完了する`() {
        val orderId = UUID.randomUUID().toString()

        val startResult = try {
            restClient.post()
                .uri("/api/saga/orders/async")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .body(OrderRequest(orderId = orderId, productId = "PROD-001", quantity = 1, amount = 1000))
                .retrieve()
                .body(StartResult::class.java)!!
        } catch (e: RestClientResponseException) {
            System.err.println("=== E2E Async Saga Start Error: ${e.statusCode} ===")
            System.err.println(e.responseBodyAsString)
            fail("${e.statusCode} ${e.responseBodyAsString}")
        }

        assertTrue(startResult.executionArn.isNotBlank())

        await atMost Duration.ofSeconds(120) untilAsserted {
            val status = try {
                restClient.get()
                    .uri("/api/saga/executions?executionArn={arn}", startResult.executionArn)
                    .retrieve()
                    .body(ExecutionStatus::class.java)!!
            } catch (e: RestClientResponseException) {
                System.err.println("=== E2E Async Saga Poll Error: ${e.statusCode} ===")
                System.err.println(e.responseBodyAsString)
                fail("${e.statusCode} ${e.responseBodyAsString}")
            }

            if (status.status != "RUNNING") {
                System.err.println("=== E2E Async Saga Execution Result ===")
                System.err.println("status=${status.status}")
                System.err.println("output=${status.output}")
            }
            assertEquals("SUCCEEDED", status.status)
        }
    }

    data class OrderRequest(
        val orderId: String,
        val productId: String,
        val quantity: Int,
        val amount: Int,
    )

    data class SagaResult(
        val orderId: String? = null,
        val success: Boolean = false,
        val message: String? = null,
        val order: OrderState? = null,
        val payment: PaymentState? = null,
    )

    data class OrderState(
        val orderId: String? = null,
        val status: String? = null,
    )

    data class PaymentState(
        val paymentId: String? = null,
        val orderId: String? = null,
        val status: String? = null,
        val success: Boolean? = null,
    )

    data class StartResult(
        val executionArn: String = "",
        val orderId: String? = null,
    )

    data class ExecutionStatus(
        val executionArn: String? = null,
        val status: String? = null,
        val input: String? = null,
        val output: String? = null,
        val startDate: String? = null,
        val stopDate: String? = null,
    )
}
