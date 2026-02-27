package com.example.payment.domain.model

import java.math.BigDecimal

data class Money(val amount: BigDecimal) {
    init {
        require(amount >= BigDecimal.ZERO)
    }
}
