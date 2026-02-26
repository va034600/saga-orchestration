package com.example.payment.controller

import com.example.common.dto.PaymentRequest
import com.example.common.dto.PaymentResponse
import com.example.common.idempotency.Idempotent
import com.example.payment.service.PaymentService
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
