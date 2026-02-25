package com.saga.compensation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.saga.compensation", "com.saga.common"])
class CompensationServiceApplication

fun main(args: Array<String>) {
    runApplication<CompensationServiceApplication>(*args)
}
