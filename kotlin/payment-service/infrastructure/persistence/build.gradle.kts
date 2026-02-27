plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":payment-service:domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
