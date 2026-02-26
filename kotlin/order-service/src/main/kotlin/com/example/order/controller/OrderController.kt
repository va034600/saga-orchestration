package com.example.order.controller

import com.example.common.dto.OrderRequest
import com.example.common.dto.OrderResponse
import com.example.common.idempotency.Idempotent
import com.example.order.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    @Idempotent
    fun createOrder(@RequestBody request: OrderRequest): ResponseEntity<OrderResponse> {
        val response = orderService.createOrder(request)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{orderId}/complete")
    @Idempotent
    fun completeOrder(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val response = orderService.completeOrder(orderId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{orderId}/cancel")
    @Idempotent
    fun cancelOrder(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val response = orderService.cancelOrder(orderId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val response = orderService.getOrder(orderId)
        return ResponseEntity.ok(response)
    }
}
