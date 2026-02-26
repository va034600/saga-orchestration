package com.example.compensation.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.example.common.dto.CompensationEvent
import com.example.common.enums.CompensationStatus
import com.example.compensation.entity.Compensation
import com.example.compensation.executor.CompensationExecutor
import com.example.compensation.repository.CompensationRepository
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CompensationEventListener(
    private val compensationExecutor: CompensationExecutor,
    private val compensationRepository: CompensationRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener("compensation-queue")
    fun onCompensationEvent(message: String) {
        log.info("Received compensation event: {}", message)

        val event = objectMapper.readValue(message, CompensationEvent::class.java)
        val compensation = Compensation(
            orderId = event.orderId,
            compensationType = event.compensationType,
            status = CompensationStatus.PROCESSING
        )
        compensationRepository.save(compensation)

        try {
            compensationExecutor.execute(event)
            compensation.status = CompensationStatus.COMPLETED
            compensation.completedAt = Instant.now()
        } catch (ex: Exception) {
            log.error("Compensation failed for order {}: {}", event.orderId, ex.message)
            compensation.status = CompensationStatus.FAILED
            compensation.errorMessage = ex.message
        }

        compensationRepository.save(compensation)
    }
}
