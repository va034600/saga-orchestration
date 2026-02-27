package com.example.orchestrator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.example.orchestrator", "com.example.common"])
@EntityScan(basePackages = ["com.example.orchestrator.entity", "com.example.common.idempotency"])
@EnableJpaRepositories(basePackages = ["com.example.orchestrator.repository", "com.example.common.idempotency"])
class OrchestratorApplication

fun main(args: Array<String>) {
    runApplication<OrchestratorApplication>(*args)
}
