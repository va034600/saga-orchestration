package com.example.payment.domain.model

data class PaymentId(val value: String) {
    init {
        require(value.isNotBlank())
    }
}
