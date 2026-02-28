dependencies {
    testImplementation("org.springframework:spring-web")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.2")
}

tasks.test {
    onlyIf {
        gradle.startParameter.taskNames.any { it.contains("e2e-test") }
    }
}
