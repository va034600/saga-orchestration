package com.example.common.exception

open class SagaException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class OrderNotFoundException(orderId: String) :
    SagaException("Order not found: $orderId")

class PaymentFailedException(orderId: String, reason: String) :
    SagaException("Payment failed for order $orderId: $reason")

class CompensationFailedException(orderId: String, reason: String) :
    SagaException("Compensation failed for order $orderId: $reason")

class DuplicateRequestException(idempotencyKey: String) :
    SagaException("Duplicate request: $idempotencyKey")
