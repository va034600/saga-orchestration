package com.example.compensation.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CompensationJpaRepository : JpaRepository<CompensationJpaEntity, Long>
