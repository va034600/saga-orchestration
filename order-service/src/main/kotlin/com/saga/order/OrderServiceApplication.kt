package com.saga.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.saga.order", "com.saga.common"])
@EntityScan(basePackages = ["com.saga.order.entity", "com.saga.common.idempotency"])
@EnableJpaRepositories(basePackages = ["com.saga.order.repository", "com.saga.common.idempotency"])
class OrderServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
