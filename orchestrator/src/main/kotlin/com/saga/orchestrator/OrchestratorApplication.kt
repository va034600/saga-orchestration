package com.saga.orchestrator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.saga.orchestrator", "com.saga.common"])
@EntityScan(basePackages = ["com.saga.orchestrator.entity", "com.saga.common.idempotency"])
@EnableJpaRepositories(basePackages = ["com.saga.orchestrator.repository", "com.saga.common.idempotency"])
class OrchestratorApplication

fun main(args: Array<String>) {
    runApplication<OrchestratorApplication>(*args)
}
