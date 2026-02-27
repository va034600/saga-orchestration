package com.example.order.presentation

import com.example.common.dto.OrderRequest
import com.example.common.dto.OrderResponse
import com.example.common.idempotency.Idempotent
import com.example.order.application.OrderApplicationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderApplicationService: OrderApplicationService,
) {

    @PostMapping
    @Idempotent
    fun createOrder(@RequestBody request: OrderRequest): ResponseEntity<OrderResponse> {
        val response = orderApplicationService.createOrder(request)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{orderId}/complete")
    @Idempotent
    fun completeOrder(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val response = orderApplicationService.completeOrder(orderId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{orderId}/cancel")
    @Idempotent
    fun cancelOrder(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val response = orderApplicationService.cancelOrder(orderId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val response = orderApplicationService.getOrder(orderId)
        return ResponseEntity.ok(response)
    }
}
