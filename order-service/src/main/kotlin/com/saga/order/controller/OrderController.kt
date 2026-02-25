package com.saga.order.controller

import com.saga.common.dto.OrderRequest
import com.saga.common.dto.OrderResponse
import com.saga.common.idempotency.Idempotent
import com.saga.order.service.OrderService
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
