package com.saga.common.dto

data class SagaResult(
    val orderId: String,
    val success: Boolean,
    val message: String,
    val order: OrderResponse? = null,
    val payment: PaymentResponse? = null
)
