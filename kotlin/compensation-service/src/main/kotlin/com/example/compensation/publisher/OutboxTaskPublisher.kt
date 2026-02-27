package com.example.compensation.publisher

import com.example.common.dto.CompensationEvent
import com.example.common.dto.CompensationStatus
import com.example.compensation.executor.CompensationExecutor
import com.example.compensation.repository.CompensationRepository
import com.example.compensation.repository.OutboxTaskRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class OutboxTaskPublisher(
    private val outboxTaskRepository: OutboxTaskRepository,
    private val compensationRepository: CompensationRepository,
    private val compensationExecutor: CompensationExecutor,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 5000)
    @Transactional
    fun processPendingTasks() {
        val tasks = outboxTaskRepository.findByPublishedFalseOrderByCreatedAtAsc()
        for (task in tasks) {
            try {
                val event = objectMapper.readValue(task.payload, CompensationEvent::class.java)
                compensationExecutor.execute(event)

                task.published = true
                task.publishedAt = Instant.now()
                outboxTaskRepository.save(task)

                val compensation = compensationRepository.findById(task.compensationId).orElse(null)
                if (compensation != null) {
                    compensation.status = CompensationStatus.COMPLETED
                    compensation.completedAt = Instant.now()
                    compensationRepository.save(compensation)
                }
            } catch (ex: Exception) {
                log.warn("Failed to process outbox task id={}: {}", task.id, ex.message)

                val compensation = compensationRepository.findById(task.compensationId).orElse(null)
                if (compensation != null) {
                    compensation.status = CompensationStatus.FAILED
                    compensation.errorMessage = ex.message
                    compensationRepository.save(compensation)
                }
                break
            }
        }
    }
}
