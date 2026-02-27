plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":compensation-service:domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
