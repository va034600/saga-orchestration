package com.example.order.domain.exception

import com.example.order.domain.OrderStatus

class InvalidOrderStateException(current: OrderStatus, target: OrderStatus) :
    IllegalStateException("Cannot transition from $current to $target")
