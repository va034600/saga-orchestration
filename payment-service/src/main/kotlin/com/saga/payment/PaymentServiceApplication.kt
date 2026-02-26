package com.saga.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.saga.payment", "com.saga.common"])
@EntityScan(basePackages = ["com.saga.payment.entity", "com.saga.common.idempotency"])
@EnableJpaRepositories(basePackages = ["com.saga.payment.repository", "com.saga.common.idempotency"])
class PaymentServiceApplication

fun main(args: Array<String>) {
    runApplication<PaymentServiceApplication>(*args)
}
