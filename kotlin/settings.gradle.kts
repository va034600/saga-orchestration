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
    "order-service:order-domain",
    "order-service:order-application",
    "order-service:order-infrastructure",
    "order-service:order-bootstrap",
    "payment-service:payment-domain",
    "payment-service:payment-application",
    "payment-service:payment-infrastructure",
    "payment-service:payment-bootstrap",
    "compensation-service:compensation-domain",
    "compensation-service:compensation-application",
    "compensation-service:compensation-infrastructure",
    "compensation-service:compensation-bootstrap",
    "orchestrator:orchestrator-domain",
    "orchestrator:orchestrator-application",
    "orchestrator:orchestrator-infrastructure",
    "orchestrator:orchestrator-bootstrap",
)
