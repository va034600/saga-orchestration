package com.saga.common.dto

import java.math.BigDecimal

data class PaymentRequest(
    val orderId: String,
    val amount: BigDecimal
)
