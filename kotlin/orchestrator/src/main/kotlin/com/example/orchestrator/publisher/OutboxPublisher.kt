package com.example.orchestrator.publisher

import com.example.common.dto.CompensationEvent
import com.example.orchestrator.repository.OutboxEventRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class OutboxPublisher(
    private val outboxEventRepository: OutboxEventRepository,
    private val eventBridgePublisher: EventBridgePublisher,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 5000)
    @Transactional
    fun publishPendingEvents() {
        val events = outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc()
        for (event in events) {
            try {
                val compensationEvent = objectMapper.readValue(event.payload, CompensationEvent::class.java)
                eventBridgePublisher.publishCompensation(compensationEvent)
                event.published = true
                event.publishedAt = Instant.now()
                outboxEventRepository.save(event)
            } catch (ex: Exception) {
                log.warn("Failed to publish outbox event id={}: {}", event.id, ex.message)
                break
            }
        }
    }
}
