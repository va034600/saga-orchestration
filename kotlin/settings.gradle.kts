pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val openApiGeneratorVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version "1.1.7"
        id("org.openapi.generator") version openApiGeneratorVersion
    }
}

rootProject.name = "saga-orchestration"

include(
    "common",
    "order-service:domain",
    "order-service:application",
    "order-service:infrastructure",
    "order-service:bootstrap",
    "payment-service:domain",
    "payment-service:application",
    "payment-service:infrastructure",
    "payment-service:bootstrap",
    "compensation-service:domain",
    "compensation-service:application",
    "compensation-service:infrastructure",
    "compensation-service:bootstrap",
    "orchestrator:domain",
    "orchestrator:application",
    "orchestrator:infrastructure",
    "orchestrator:bootstrap",
)
