package com.example.compensation.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class CompensationWebClientConfig {

    @Bean
    fun orderRestClient(
        @Value("\${services.order.url:http://localhost:8081}") baseUrl: String
    ): RestClient = RestClient.builder().baseUrl(baseUrl).build()

    @Bean
    fun paymentRestClient(
        @Value("\${services.payment.url:http://localhost:8083}") baseUrl: String
    ): RestClient = RestClient.builder().baseUrl(baseUrl).build()
}
