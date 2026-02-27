package com.example.compensation.application

import com.example.common.dto.CompensationEvent
import com.example.compensation.domain.CompensationRepository
import com.example.compensation.domain.CompensationType
import com.example.compensation.domain.OutboxTaskRepository
import com.example.compensation.domain.model.Compensation
import com.example.compensation.domain.model.OutboxTask
import com.example.compensation.infrastructure.http.CompensationExecutor
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompensationApplicationService(
    private val compensationRepository: CompensationRepository,
    private val outboxTaskRepository: OutboxTaskRepository,
    private val compensationExecutor: CompensationExecutor,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun receiveCompensationEvent(event: CompensationEvent) {
        val compensation = Compensation.create(
            orderId = event.orderId,
            compensationType = CompensationType.valueOf(event.compensationType.name),
        )
        val saved = compensationRepository.save(compensation)

        val task = OutboxTask.create(
            compensationId = saved.id!!,
            orderId = event.orderId,
            taskType = event.compensationType.name,
            payload = objectMapper.writeValueAsString(event),
        )
        outboxTaskRepository.save(task)
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    fun processPendingTasks() {
        val tasks = outboxTaskRepository.findUnpublishedOrderByCreatedAt()
        for (task in tasks) {
            try {
                val event = objectMapper.readValue(task.payload, CompensationEvent::class.java)
                compensationExecutor.execute(event)

                task.markPublished()
                outboxTaskRepository.save(task)

                val compensation = compensationRepository.findById(task.compensationId)
                if (compensation != null) {
                    compensation.complete()
                    compensationRepository.save(compensation)
                }
            } catch (ex: Exception) {
                log.warn("Failed to process outbox task id={}: {}", task.id, ex.message)

                val compensation = compensationRepository.findById(task.compensationId)
                if (compensation != null) {
                    compensation.fail(ex.message ?: "Unknown error")
                    compensationRepository.save(compensation)
                }
                break
            }
        }
    }
}
