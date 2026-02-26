package com.example.payment.service

import com.example.common.dto.PaymentRequest
import com.example.common.dto.PaymentResponse
import com.example.common.dto.PaymentStatus
import com.example.common.exception.PaymentFailedException
import com.example.payment.entity.Payment
import com.example.payment.repository.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun authorize(request: PaymentRequest): PaymentResponse {
        val existing = paymentRepository.findByOrderId(request.orderId)
        if (existing.isPresent) {
            log.info("Payment already exists for order: {}", request.orderId)
            val payment = existing.get()
            return toResponse(payment)
        }

        val payment = Payment(
            paymentId = UUID.randomUUID().toString(),
            orderId = request.orderId,
            amount = request.amount,
            status = PaymentStatus.AUTHORIZED
        )
        paymentRepository.save(payment)
        log.info("Payment authorized: {} for order: {}", payment.paymentId, request.orderId)
        return toResponse(payment)
    }

    @Transactional
    fun capture(orderId: String): PaymentResponse {
        val payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow { PaymentFailedException(orderId, "No authorized payment found") }

        if (payment.status != PaymentStatus.AUTHORIZED) {
            throw PaymentFailedException(orderId, "Payment is not in AUTHORIZED status: ${payment.status}")
        }

        payment.status = PaymentStatus.CAPTURED
        payment.updatedAt = Instant.now()
        paymentRepository.save(payment)
        log.info("Payment captured: {} for order: {}", payment.paymentId, orderId)
        return toResponse(payment)
    }

    @Transactional
    fun refund(orderId: String): PaymentResponse {
        val payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow { PaymentFailedException(orderId, "No payment found to refund") }

        payment.status = PaymentStatus.REFUNDED
        payment.updatedAt = Instant.now()
        paymentRepository.save(payment)
        log.info("Payment refunded: {} for order: {}", payment.paymentId, orderId)
        return toResponse(payment)
    }

    private fun toResponse(payment: Payment) = PaymentResponse(
        paymentId = payment.paymentId,
        orderId = payment.orderId,
        status = payment.status,
        success = payment.status != PaymentStatus.FAILED
    )
}
