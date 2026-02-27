package com.example.compensation.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.example.common.dto.CompensationEvent
import com.example.common.dto.CompensationStatus
import com.example.compensation.entity.Compensation
import com.example.compensation.entity.OutboxTask
import com.example.compensation.repository.CompensationRepository
import com.example.compensation.repository.OutboxTaskRepository
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CompensationEventListener(
    private val compensationRepository: CompensationRepository,
    private val outboxTaskRepository: OutboxTaskRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener("compensation-queue")
    @Transactional
    fun onCompensationEvent(message: String) {
        log.info("Received compensation event: {}", message)

        val event = objectMapper.readValue(message, CompensationEvent::class.java)
        val compensation = Compensation(
            orderId = event.orderId,
            compensationType = event.compensationType,
            status = CompensationStatus.PROCESSING
        )
        compensationRepository.save(compensation)

        outboxTaskRepository.save(OutboxTask(
            compensationId = compensation.compId!!,
            orderId = event.orderId,
            taskType = event.compensationType.name,
            payload = objectMapper.writeValueAsString(event)
        ))
    }
}
