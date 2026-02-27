dependencies {
    implementation(project(":orchestrator:application"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter")
    implementation("software.amazon.awssdk:sfn")
}
