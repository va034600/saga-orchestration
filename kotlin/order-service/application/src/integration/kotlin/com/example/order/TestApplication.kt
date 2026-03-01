package com.example.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.example.order"])
@EntityScan(basePackages = ["com.example.order.infrastructure.persistence"])
@EnableJpaRepositories(basePackages = ["com.example.order.infrastructure.persistence"])
class TestApplication
