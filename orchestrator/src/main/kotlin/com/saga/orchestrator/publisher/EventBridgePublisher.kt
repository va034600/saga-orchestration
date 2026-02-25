package com.saga.orchestrator.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import com.saga.common.dto.CompensationEvent
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
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publishCompensation(event: CompensationEvent) {
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
        log.info(
            "Published compensation event for order {}: type={}, failedCount={}",
            event.orderId, event.compensationType, result.failedEntryCount()
        )
    }
}
