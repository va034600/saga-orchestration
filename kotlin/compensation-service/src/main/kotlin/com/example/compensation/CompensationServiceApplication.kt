package com.example.compensation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.example.compensation", "com.example.common"])
@EntityScan(basePackages = ["com.example.compensation.entity", "com.example.common.idempotency"])
@EnableJpaRepositories(basePackages = ["com.example.compensation.repository", "com.example.common.idempotency"])
class CompensationServiceApplication

fun main(args: Array<String>) {
    runApplication<CompensationServiceApplication>(*args)
}
