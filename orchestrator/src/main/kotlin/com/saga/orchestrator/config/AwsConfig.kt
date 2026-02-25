package com.saga.orchestrator.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import java.net.URI

@Configuration
class AwsConfig {

    @Bean
    fun eventBridgeClient(
        @Value("\${spring.cloud.aws.endpoint:http://localhost:4566}") endpoint: String,
        @Value("\${spring.cloud.aws.region.static:ap-northeast-1}") region: String
    ): EventBridgeClient = EventBridgeClient.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"))
        )
        .build()
}
