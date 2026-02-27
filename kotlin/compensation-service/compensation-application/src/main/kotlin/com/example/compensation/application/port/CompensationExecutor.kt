package com.example.compensation.application.port

import com.example.common.dto.CompensationEvent

interface CompensationExecutor {
    fun execute(event: CompensationEvent)
}
