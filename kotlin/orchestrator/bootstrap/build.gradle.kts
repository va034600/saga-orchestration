plugins {
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("org.openapi.generator")
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/../services/orchestrator/openapi.yml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
    apiPackage.set("com.example.orchestrator.api")
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
            "PaymentResponse" to "com.example.common.dto.PaymentResponse",
            "SagaResult" to "com.example.common.dto.SagaResult",
            "ErrorResponse" to "com.example.common.dto.ErrorResponse",
            "StartResult" to "com.example.orchestrator.application.AsyncSagaApplicationService.StartResult",
            "ExecutionStatus" to "com.example.orchestrator.application.AsyncSagaApplicationService.ExecutionStatus",
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

tasks.processResources {
    from("$rootDir/../services/orchestrator") {
        include("openapi.yml")
        into("static")
    }
}

dependencies {
    implementation(project(":orchestrator:domain"))
    implementation(project(":orchestrator:application"))
    implementation(project(":orchestrator:infrastructure:persistence"))
    implementation(project(":orchestrator:infrastructure:http"))
    implementation(project(":orchestrator:infrastructure:messaging"))
    implementation(project(":orchestrator:infrastructure:aws"))
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.awspring.cloud:spring-cloud-aws-starter")
    implementation("software.amazon.awssdk:eventbridge")
    implementation("software.amazon.awssdk:sfn")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springDocVersion")}")
    runtimeOnly("org.postgresql:postgresql:${property("postgresDriverVersion")}")
}
