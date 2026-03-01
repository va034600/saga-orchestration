plugins {
    kotlin("plugin.jpa")
    id("org.springframework.boot")
}

tasks.processResources {
    from("$projectDir") {
        include("openapi.yml")
        into("static")
    }
    from("$rootDir/../services/compensation-service/db/migration") {
        into("db/migration")
    }
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
    runtimeOnly("org.postgresql:postgresql:${property("postgresDriverVersion")}")
}
