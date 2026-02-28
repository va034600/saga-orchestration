sourceSets {
    create("integrationTest") {
        kotlin.srcDir("src/integration/kotlin")
        resources.srcDir("src/integration/resources")
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    implementation(project(":order-service:domain"))
    implementation(project(":common"))
    implementation("org.springframework:spring-tx")
    implementation("org.springframework:spring-context")
    implementation("org.slf4j:slf4j-api")

    "integrationTestImplementation"(project(":order-service:infrastructure:persistence"))
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-data-jpa")
    "integrationTestImplementation"("org.flywaydb:flyway-core")
    "integrationTestRuntimeOnly"("com.h2database:h2")
    "integrationTestRuntimeOnly"("org.postgresql:postgresql")
    "integrationTestRuntimeOnly"("org.flywaydb:flyway-database-postgresql")
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

tasks.named<ProcessResources>("processIntegrationTestResources") {
    from("$rootDir/../services/order-service/db/migration") {
        into("db/migration")
    }
}
