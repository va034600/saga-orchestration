package com.example.compensation.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class CompensationWebClientConfig {

    @Bean
    fun orderRestClient(
        @Value("\${services.order.url:http://localhost:8081}") baseUrl: String
    ): RestClient {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(5))
            setReadTimeout(Duration.ofSeconds(10))
        }
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build()
    }

    @Bean
    fun paymentRestClient(
        @Value("\${services.payment.url:http://localhost:8083}") baseUrl: String
    ): RestClient {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(5))
            setReadTimeout(Duration.ofSeconds(10))
        }
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build()
    }
}
