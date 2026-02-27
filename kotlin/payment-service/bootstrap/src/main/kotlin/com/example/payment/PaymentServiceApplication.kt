package com.example.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.example.payment", "com.example.common"])
@EntityScan(basePackages = ["com.example.payment.infrastructure.persistence", "com.example.common.idempotency"])
@EnableJpaRepositories(basePackages = ["com.example.payment.infrastructure.persistence", "com.example.common.idempotency"])
class PaymentServiceApplication

fun main(args: Array<String>) {
    runApplication<PaymentServiceApplication>(*args)
}
