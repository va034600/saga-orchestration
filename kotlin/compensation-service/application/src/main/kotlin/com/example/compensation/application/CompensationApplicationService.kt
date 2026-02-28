package com.example.compensation.application

import com.example.common.dto.CompensationEvent
import com.example.compensation.application.port.CompensationExecutor
import com.example.compensation.domain.CompensationRepository
import com.example.compensation.domain.CompensationType
import com.example.compensation.domain.OutboxTaskRepository
import com.example.compensation.domain.model.Compensation
import com.example.compensation.domain.model.OutboxTask
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Service
class CompensationApplicationService(
    private val compensationRepository: CompensationRepository,
    private val outboxTaskRepository: OutboxTaskRepository,
    private val compensationExecutor: CompensationExecutor,
    private val objectMapper: ObjectMapper,
    private val transactionTemplate: TransactionTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun receiveCompensationEvent(event: CompensationEvent) {
        val compensation = Compensation.create(
            orderId = event.orderId,
            compensationType = CompensationType.valueOf(event.compensationType.name),
        )
        val saved = compensationRepository.save(compensation)

        val savedId = saved.id
            ?: throw IllegalStateException("Compensation ID must not be null after save")
        val task = OutboxTask.create(
            compensationId = savedId,
            orderId = event.orderId,
            taskType = event.compensationType.name,
            payload = objectMapper.writeValueAsString(event),
        )
        outboxTaskRepository.save(task)
    }

    @Scheduled(fixedDelay = 5000)
    fun processPendingTasks() {
        val tasks = outboxTaskRepository.findUnpublishedOrderByCreatedAt()
        for (task in tasks) {
            try {
                val compensation = compensationRepository.findById(task.compensationId) ?: continue

                transactionTemplate.execute {
                    val processing = compensation.startProcessing()
                    compensationRepository.save(processing)
                }

                val event = objectMapper.readValue(task.payload, CompensationEvent::class.java)
                compensationExecutor.execute(event)

                transactionTemplate.execute {
                    outboxTaskRepository.save(task.markPublished())
                    val updated = compensationRepository.findById(task.compensationId)
                    if (updated != null) {
                        compensationRepository.save(updated.complete())
                    }
                }
            } catch (ex: Exception) {
                log.error("Failed to process outbox task id={}: {}", task.id, ex.message, ex)
                transactionTemplate.execute {
                    val comp = compensationRepository.findById(task.compensationId)
                    if (comp != null) {
                        compensationRepository.save(comp.fail(ex.message ?: "Unknown error"))
                    }
                }
            }
        }
    }
}
