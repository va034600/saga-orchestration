package com.example.compensation.domain

import com.example.compensation.domain.model.Compensation

interface CompensationRepository {
    fun findById(id: Long): Compensation?
    fun save(compensation: Compensation): Compensation
}
