plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":orchestrator:orchestrator-domain"))
    implementation(project(":orchestrator:orchestrator-application"))
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.awspring.cloud:spring-cloud-aws-starter")
    implementation("software.amazon.awssdk:eventbridge")
    implementation("software.amazon.awssdk:sfn")
}
