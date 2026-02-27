dependencies {
    implementation(project(":orchestrator:application"))
    implementation(project(":common"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter")
    implementation("software.amazon.awssdk:eventbridge")
}
