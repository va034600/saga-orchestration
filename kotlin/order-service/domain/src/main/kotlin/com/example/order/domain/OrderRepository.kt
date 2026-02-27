package com.example.order.domain

import com.example.order.domain.model.Order
import com.example.order.domain.model.OrderId

interface OrderRepository {
    fun findById(id: OrderId): Order?
    fun save(order: Order): Order
}
