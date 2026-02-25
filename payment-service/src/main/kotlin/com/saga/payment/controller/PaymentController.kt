package com.saga.payment.controller

import com.saga.common.dto.PaymentRequest
import com.saga.common.dto.PaymentResponse
import com.saga.common.idempotency.Idempotent
import com.saga.payment.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/authorize")
    @Idempotent
    fun authorize(@RequestBody request: PaymentRequest): ResponseEntity<PaymentResponse> {
        val response = paymentService.authorize(request)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{orderId}/capture")
    @Idempotent
    fun capture(@PathVariable orderId: String): ResponseEntity<PaymentResponse> {
        val response = paymentService.capture(orderId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{orderId}/refund")
    @Idempotent
    fun refund(@PathVariable orderId: String): ResponseEntity<PaymentResponse> {
        val response = paymentService.refund(orderId)
        return ResponseEntity.ok(response)
    }
}
