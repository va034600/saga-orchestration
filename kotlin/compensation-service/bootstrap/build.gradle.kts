plugins {
    kotlin("plugin.jpa")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":compensation-service:domain"))
    implementation(project(":compensation-service:application"))
    implementation(project(":compensation-service:infrastructure:persistence"))
    implementation(project(":compensation-service:infrastructure:messaging"))
    implementation(project(":compensation-service:infrastructure:http"))
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springDocVersion")}")
    implementation("org.flywaydb:flyway-core:${property("flywayVersion")}")
    implementation("org.flywaydb:flyway-database-postgresql:${property("flywayVersion")}")
    runtimeOnly("org.postgresql:postgresql:${property("postgresDriverVersion")}")
}
