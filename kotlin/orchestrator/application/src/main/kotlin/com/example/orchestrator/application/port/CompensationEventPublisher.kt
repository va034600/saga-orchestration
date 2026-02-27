package com.example.orchestrator.application.port

import com.example.common.dto.CompensationEvent

interface CompensationEventPublisher {
    fun publishCompensation(event: CompensationEvent)
}
