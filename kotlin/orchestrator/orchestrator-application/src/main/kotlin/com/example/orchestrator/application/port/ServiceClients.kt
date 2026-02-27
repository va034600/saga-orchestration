package com.example.orchestrator.application.port

import com.example.common.dto.OrderRequest
import com.example.common.dto.OrderResponse
import com.example.common.dto.PaymentRequest
import com.example.common.dto.PaymentResponse

interface ServiceClients {
    fun createOrder(request: OrderRequest, traceId: String): OrderResponse
    fun completeOrder(orderId: String, traceId: String): OrderResponse
    fun authorizePayment(request: PaymentRequest, traceId: String): PaymentResponse
    fun capturePayment(orderId: String, traceId: String): PaymentResponse
}
