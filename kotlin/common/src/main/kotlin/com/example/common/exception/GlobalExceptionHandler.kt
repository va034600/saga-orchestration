package com.example.common.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    data class ErrorResponse(
        val timestamp: Instant = Instant.now(),
        val status: Int,
        val error: String,
        val message: String?
    )

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleNotFound(ex: OrderNotFoundException): ResponseEntity<ErrorResponse> {
        log.warn(ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(status = 404, error = "Not Found", message = ex.message))
    }

    @ExceptionHandler(PaymentFailedException::class)
    fun handlePaymentFailed(ex: PaymentFailedException): ResponseEntity<ErrorResponse> {
        log.warn(ex.message)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(status = 422, error = "Unprocessable Entity", message = ex.message))
    }

    @ExceptionHandler(DuplicateRequestException::class)
    fun handleDuplicate(ex: DuplicateRequestException): ResponseEntity<ErrorResponse> {
        log.info(ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(status = 409, error = "Conflict", message = ex.message))
    }

    @ExceptionHandler(SagaException::class)
    fun handleSaga(ex: SagaException): ResponseEntity<ErrorResponse> {
        log.error("Saga error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(status = 500, error = "Internal Server Error", message = ex.message))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(status = 500, error = "Internal Server Error", message = "An unexpected error occurred"))
    }
}
