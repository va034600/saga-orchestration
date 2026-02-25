package com.saga.common.dto

import com.saga.common.enums.OrderStatus

data class OrderResponse(
    val orderId: String,
    val status: OrderStatus
)
