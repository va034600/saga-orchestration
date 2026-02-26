package com.example.common.dto

import com.example.common.enums.OrderStatus

data class OrderResponse(
    val orderId: String,
    val status: OrderStatus
)
