package com.example.order.domain.model

import java.math.BigDecimal

data class Money(val amount: BigDecimal) {
    init {
        require(amount >= BigDecimal.ZERO)
    }
}
