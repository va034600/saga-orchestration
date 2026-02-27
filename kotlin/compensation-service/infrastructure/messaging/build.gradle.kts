dependencies {
    implementation(project(":compensation-service:application"))
    implementation(project(":common"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
}
