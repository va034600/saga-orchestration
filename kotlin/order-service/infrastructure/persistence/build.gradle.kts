plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":order-service:domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
