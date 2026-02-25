package com.saga.compensation.repository

import com.saga.compensation.entity.Compensation
import org.springframework.data.jpa.repository.JpaRepository

interface CompensationRepository : JpaRepository<Compensation, Long>
