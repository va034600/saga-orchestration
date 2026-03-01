plugins {
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("org.openapi.generator")
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/../services/payment-service/openapi.yml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
    apiPackage.set("com.example.payment.api")
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
            "PaymentRequest" to "com.example.common.dto.PaymentRequest",
            "PaymentResponse" to "com.example.common.dto.PaymentResponse",
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

tasks.processResources {
    from("$rootDir/../services/payment-service") {
        include("openapi.yml")
        into("static")
    }
    from("$rootDir/../services/payment-service/db/migration") {
        into("db/migration")
    }
}

dependencies {
    implementation(project(":payment-service:domain"))
    implementation(project(":payment-service:application"))
    implementation(project(":payment-service:infrastructure:persistence"))
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springDocVersion")}")
    runtimeOnly("org.postgresql:postgresql:${property("postgresDriverVersion")}")
}
