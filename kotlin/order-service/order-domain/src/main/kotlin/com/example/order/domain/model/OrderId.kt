package com.example.order.domain.model

data class OrderId(val value: String) {
    init {
        require(value.isNotBlank())
    }
}
