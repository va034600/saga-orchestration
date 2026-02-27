plugins {
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("org.openapi.generator")
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/openapi.yml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
    apiPackage.set("com.example.order.api")
    modelPackage.set("com.example.common.dto")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useBeanValidation" to "false",
            "documentationProvider" to "none",
            "enumPropertyNaming" to "UPPERCASE",
            "useTags" to "true",
            "sourceFolder" to "src/main/kotlin",
        )
    )
    globalProperties.set(
        mapOf(
            "apis" to "",
            "apiDocs" to "false",
            "apiTests" to "false",
        )
    )
    importMappings.set(
        mapOf(
            "OrderRequest" to "com.example.common.dto.OrderRequest",
            "OrderResponse" to "com.example.common.dto.OrderResponse",
            "ErrorResponse" to "com.example.common.dto.ErrorResponse",
        )
    )
}

sourceSets {
    main {
        kotlin {
            srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

dependencies {
    implementation(project(":order-service:order-domain"))
    implementation(project(":order-service:order-application"))
    implementation(project(":order-service:order-infrastructure"))
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springDocVersion")}")
    implementation("org.flywaydb:flyway-core:${property("flywayVersion")}")
    implementation("org.flywaydb:flyway-database-postgresql:${property("flywayVersion")}")
    runtimeOnly("org.postgresql:postgresql:${property("postgresDriverVersion")}")
}
