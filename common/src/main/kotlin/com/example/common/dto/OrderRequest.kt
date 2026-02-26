package com.example.common.dto

import java.math.BigDecimal

data class OrderRequest(
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val amount: BigDecimal
)
