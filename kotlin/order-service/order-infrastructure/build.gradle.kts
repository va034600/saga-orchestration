plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":order-service:order-domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
