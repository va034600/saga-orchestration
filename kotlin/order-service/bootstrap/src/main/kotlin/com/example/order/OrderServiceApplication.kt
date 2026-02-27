package com.example.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.example.order", "com.example.common"])
@EntityScan(basePackages = ["com.example.order.infrastructure.persistence", "com.example.common.idempotency"])
@EnableJpaRepositories(basePackages = ["com.example.order.infrastructure.persistence", "com.example.common.idempotency"])
class OrderServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
