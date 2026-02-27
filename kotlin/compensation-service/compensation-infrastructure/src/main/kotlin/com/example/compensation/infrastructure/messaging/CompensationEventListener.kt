package com.example.compensation.infrastructure.messaging

import com.example.compensation.application.CompensationApplicationService
import com.fasterxml.jackson.databind.ObjectMapper
import com.example.common.dto.CompensationEvent
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CompensationEventListener(
    private val compensationApplicationService: CompensationApplicationService,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener("compensation-queue")
    fun onCompensationEvent(message: String) {
        log.info("Received compensation event: {}", message)
        val event = objectMapper.readValue(message, CompensationEvent::class.java)
        compensationApplicationService.receiveCompensationEvent(event)
    }
}
