package com.saga.compensation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.saga.compensation", "com.saga.common"])
@EntityScan(basePackages = ["com.saga.compensation.entity", "com.saga.common.idempotency"])
@EnableJpaRepositories(basePackages = ["com.saga.compensation.repository", "com.saga.common.idempotency"])
class CompensationServiceApplication

fun main(args: Array<String>) {
    runApplication<CompensationServiceApplication>(*args)
}
