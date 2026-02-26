package com.example.orchestrator.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun orderWebClient(
        @Value("\${services.order.url:http://localhost:8081}") baseUrl: String
    ): WebClient = WebClient.builder().baseUrl(baseUrl).build()

    @Bean
    fun paymentWebClient(
        @Value("\${services.payment.url:http://localhost:8083}") baseUrl: String
    ): WebClient = WebClient.builder().baseUrl(baseUrl).build()
}
