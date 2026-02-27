package com.example.orchestrator.application

import com.example.common.dto.CompensationEvent
import com.example.orchestrator.domain.OutboxEventRepository
import com.example.orchestrator.infrastructure.messaging.EventBridgePublisher
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxPublisher(
    private val outboxEventRepository: OutboxEventRepository,
    private val eventBridgePublisher: EventBridgePublisher,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 5000)
    @Transactional
    fun publishPendingEvents() {
        val events = outboxEventRepository.findUnpublishedOrderByCreatedAt()
        for (event in events) {
            try {
                val compensationEvent = objectMapper.readValue(event.payload, CompensationEvent::class.java)
                eventBridgePublisher.publishCompensation(compensationEvent)
                event.markPublished()
                outboxEventRepository.save(event)
            } catch (ex: Exception) {
                log.warn("Failed to publish outbox event id={}: {}", event.id, ex.message)
                break
            }
        }
    }
}
