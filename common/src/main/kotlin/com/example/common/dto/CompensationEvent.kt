package com.example.common.dto

import com.example.common.enums.CompensationType
import java.math.BigDecimal

data class CompensationEvent(
    val orderId: String,
    val compensationType: CompensationType,
    val paymentId: String? = null,
    val amount: BigDecimal? = null
)
