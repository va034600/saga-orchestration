package com.saga.common.dto

import com.saga.common.enums.PaymentStatus

data class PaymentResponse(
    val paymentId: String,
    val orderId: String,
    val status: PaymentStatus,
    val success: Boolean
)
