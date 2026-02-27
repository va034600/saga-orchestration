plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":compensation-service:compensation-domain"))
    implementation(project(":compensation-service:compensation-application"))
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
}
