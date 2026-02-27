plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":orchestrator:domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
