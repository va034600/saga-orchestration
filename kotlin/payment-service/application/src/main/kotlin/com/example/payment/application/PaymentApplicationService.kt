package com.example.payment.application

import com.example.common.dto.PaymentRequest
import com.example.common.dto.PaymentResponse
import com.example.common.dto.PaymentStatus as DtoPaymentStatus
import com.example.common.exception.PaymentFailedException
import com.example.common.exception.SagaException
import com.example.payment.domain.PaymentRepository
import com.example.payment.domain.PaymentStatus
import com.example.payment.domain.exception.InvalidPaymentStateException
import com.example.payment.domain.model.Payment
import com.example.payment.domain.model.PaymentId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PaymentApplicationService(
    private val paymentRepository: PaymentRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun authorize(request: PaymentRequest): PaymentResponse {
        val existing = paymentRepository.findByOrderId(request.orderId)
        if (existing != null) {
            log.info("Payment already exists for order: {}", request.orderId)
            return toResponse(existing)
        }

        val payment = Payment.authorize(
            paymentId = PaymentId(UUID.randomUUID().toString()),
            orderId = request.orderId,
            amount = request.amount,
        )
        val saved = paymentRepository.save(payment)
        log.info("Payment authorized: {} for order: {}", saved.id.value, request.orderId)
        return toResponse(saved)
    }

    @Transactional
    fun capture(orderId: String): PaymentResponse {
        val payment = paymentRepository.findByOrderId(orderId)
            ?: throw PaymentFailedException(orderId, "No authorized payment found")
        val captured = try {
            payment.capture()
        } catch (e: InvalidPaymentStateException) {
            throw PaymentFailedException(orderId, "Payment is not in AUTHORIZED status: ${payment.status}")
        }
        val saved = paymentRepository.save(captured)
        log.info("Payment captured: {} for order: {}", saved.id.value, orderId)
        return toResponse(saved)
    }

    @Transactional
    fun refund(orderId: String): PaymentResponse {
        val payment = paymentRepository.findByOrderId(orderId)
            ?: throw PaymentFailedException(orderId, "No payment found to refund")
        val refunded = try {
            payment.refund()
        } catch (e: InvalidPaymentStateException) {
            throw SagaException(e.message ?: "Invalid state transition", e)
        }
        val saved = paymentRepository.save(refunded)
        log.info("Payment refunded: {} for order: {}", saved.id.value, orderId)
        return toResponse(saved)
    }

    private fun toResponse(payment: Payment): PaymentResponse = PaymentResponse(
        paymentId = payment.id.value,
        orderId = payment.orderId,
        status = toDto(payment.status),
        success = payment.status != PaymentStatus.FAILED,
    )

    private fun toDto(status: PaymentStatus): DtoPaymentStatus = when (status) {
        PaymentStatus.AUTHORIZED -> DtoPaymentStatus.AUTHORIZED
        PaymentStatus.CAPTURED -> DtoPaymentStatus.CAPTURED
        PaymentStatus.REFUNDED -> DtoPaymentStatus.REFUNDED
        PaymentStatus.FAILED -> DtoPaymentStatus.FAILED
    }
}
