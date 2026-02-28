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
    // order-service
    "order-service:domain",
    "order-service:application",
    "order-service:infrastructure:persistence",
    "order-service:bootstrap",
    // payment-service
    "payment-service:domain",
    "payment-service:application",
    "payment-service:infrastructure:persistence",
    "payment-service:bootstrap",
    // compensation-service
    "compensation-service:domain",
    "compensation-service:application",
    "compensation-service:infrastructure:persistence",
    "compensation-service:infrastructure:messaging",
    "compensation-service:infrastructure:http",
    "compensation-service:bootstrap",
    // orchestrator
    "orchestrator:domain",
    "orchestrator:application",
    "orchestrator:infrastructure:persistence",
    "orchestrator:infrastructure:http",
    "orchestrator:infrastructure:messaging",
    "orchestrator:infrastructure:aws",
    "orchestrator:bootstrap",
    // e2e test
    "e2e-test",
)
