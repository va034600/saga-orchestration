package com.example.orchestrator.client

import com.example.common.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ServiceClients(
    @Qualifier("orderWebClient") private val orderClient: WebClient,
    @Qualifier("paymentWebClient") private val paymentClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun createOrder(request: OrderRequest, traceId: String): OrderResponse {
        log.info("[{}] Creating order: {}", traceId, request.orderId)
        return orderClient.post()
            .uri("/api/orders")
            .header("X-Trace-Id", traceId)
            .header("Idempotency-Key", "create-order-${request.orderId}")
            .bodyValue(request)
            .retrieve()
            .bodyToMono<OrderResponse>()
            .block()!!
    }

    fun completeOrder(orderId: String, traceId: String): OrderResponse {
        log.info("[{}] Completing order: {}", traceId, orderId)
        return orderClient.put()
            .uri("/api/orders/$orderId/complete")
            .header("X-Trace-Id", traceId)
            .header("Idempotency-Key", "complete-order-$orderId")
            .retrieve()
            .bodyToMono<OrderResponse>()
            .block()!!
    }

    fun authorizePayment(request: PaymentRequest, traceId: String): PaymentResponse {
        log.info("[{}] Authorizing payment for order: {}", traceId, request.orderId)
        return paymentClient.post()
            .uri("/api/payments/authorize")
            .header("X-Trace-Id", traceId)
            .header("Idempotency-Key", "authorize-payment-${request.orderId}")
            .bodyValue(request)
            .retrieve()
            .bodyToMono<PaymentResponse>()
            .block()!!
    }

    fun capturePayment(orderId: String, traceId: String): PaymentResponse {
        log.info("[{}] Capturing payment for order: {}", traceId, orderId)
        return paymentClient.put()
            .uri("/api/payments/$orderId/capture")
            .header("X-Trace-Id", traceId)
            .header("Idempotency-Key", "capture-payment-$orderId")
            .retrieve()
            .bodyToMono<PaymentResponse>()
            .block()!!
    }
}
