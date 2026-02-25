package com.saga.compensation.executor

import com.saga.common.dto.CompensationEvent
import com.saga.common.dto.OrderResponse
import com.saga.common.dto.PaymentResponse
import com.saga.common.enums.CompensationType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class CompensationExecutor(
    @Qualifier("orderRestClient") private val orderClient: RestClient,
    @Qualifier("paymentRestClient") private val paymentClient: RestClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun execute(event: CompensationEvent) {
        when (event.compensationType) {
            CompensationType.REFUND_PAYMENT -> refundPayment(event)
            CompensationType.CANCEL_ORDER -> cancelOrder(event)
        }
    }

    private fun refundPayment(event: CompensationEvent) {
        log.info("Refunding payment for order: {}", event.orderId)
        paymentClient.put()
            .uri("/api/payments/${event.orderId}/refund")
            .header("Idempotency-Key", "comp-refund-${event.orderId}")
            .retrieve()
            .body(PaymentResponse::class.java)
        log.info("Payment refunded for order: {}", event.orderId)
    }

    private fun cancelOrder(event: CompensationEvent) {
        log.info("Cancelling order: {}", event.orderId)
        orderClient.put()
            .uri("/api/orders/${event.orderId}/cancel")
            .header("Idempotency-Key", "comp-cancel-${event.orderId}")
            .retrieve()
            .body(OrderResponse::class.java)
        log.info("Order cancelled: {}", event.orderId)
    }
}
