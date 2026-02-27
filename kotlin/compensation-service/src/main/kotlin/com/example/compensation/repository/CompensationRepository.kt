package com.example.compensation.repository

import com.example.compensation.entity.Compensation
import org.springframework.data.jpa.repository.JpaRepository

interface CompensationRepository : JpaRepository<Compensation, Long>
