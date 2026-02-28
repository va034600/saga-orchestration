package com.example.orchestrator.infrastructure.messaging

import com.example.common.dto.CompensationEvent
import com.example.orchestrator.application.port.CompensationEventPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry

@Component
class EventBridgePublisher(
    private val eventBridgeClient: EventBridgeClient,
    private val objectMapper: ObjectMapper,
    @Value("\${aws.eventbridge.bus-name:saga-events}") private val busName: String
) : CompensationEventPublisher {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun publishCompensation(event: CompensationEvent) {
        val entry = PutEventsRequestEntry.builder()
            .source("saga.orchestrator")
            .detailType("CompensationRequested")
            .eventBusName(busName)
            .detail(objectMapper.writeValueAsString(event))
            .build()

        val request = PutEventsRequest.builder()
            .entries(entry)
            .build()

        val result = eventBridgeClient.putEvents(request)
        if (result.failedEntryCount() > 0) {
            throw RuntimeException(
                "Failed to publish compensation event for order ${event.orderId}: " +
                    "failedCount=${result.failedEntryCount()}"
            )
        }
        log.info(
            "Published compensation event for order {}: type={}",
            event.orderId, event.compensationType
        )
    }
}
