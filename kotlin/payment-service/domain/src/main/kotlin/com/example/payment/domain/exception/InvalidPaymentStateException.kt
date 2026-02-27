package com.example.payment.domain.exception

import com.example.payment.domain.PaymentStatus

class InvalidPaymentStateException(current: PaymentStatus, target: PaymentStatus) :
    IllegalStateException("Cannot transition from $current to $target")
