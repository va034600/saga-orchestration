package com.example.common.dto

import com.example.common.enums.PaymentStatus

data class PaymentResponse(
    val paymentId: String,
    val orderId: String,
    val status: PaymentStatus,
    val success: Boolean
)
