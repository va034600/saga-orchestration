package com.example.orchestrator.saga

import com.example.common.dto.CompensationEvent
import com.example.common.dto.OrderRequest
import com.example.common.dto.PaymentRequest
import com.example.common.dto.SagaResult
import com.example.common.dto.CompensationType
import com.example.orchestrator.client.ServiceClients
import com.example.orchestrator.entity.SagaState
import com.example.orchestrator.entity.SagaStep
import com.example.orchestrator.entity.OutboxEvent
import com.example.orchestrator.repository.OutboxEventRepository
import com.example.orchestrator.repository.SagaStateRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SagaOrchestrator(
    private val serviceClients: ServiceClients,
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
    private val sagaStateRepository: SagaStateRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun executeSaga(request: OrderRequest, traceId: String): SagaResult {
        val sagaState = SagaState(orderId = request.orderId)
        sagaStateRepository.save(sagaState)

        try {
            // Step 1: Create Order (PENDING)
            val orderStep = addStep(sagaState, "CREATE_ORDER")
            serviceClients.createOrder(request, traceId)
            completeStep(orderStep)

            // Step 2: Execute Payment (authorize + capture)
            val paymentStep = addStep(sagaState, "EXECUTE_PAYMENT")
            val paymentRequest = PaymentRequest(orderId = request.orderId, amount = request.amount)
            serviceClients.authorizePayment(paymentRequest, traceId)
            val paymentResponse = serviceClients.capturePayment(request.orderId, traceId)
            completeStep(paymentStep)

            // Step 3: Complete Order (COMPLETED)
            val completeOrderStep = addStep(sagaState, "COMPLETE_ORDER")
            val finalOrder = serviceClients.completeOrder(request.orderId, traceId)
            completeStep(completeOrderStep)

            sagaState.status = "COMPLETED"
            sagaState.updatedAt = Instant.now()
            sagaStateRepository.save(sagaState)

            log.info("[{}] Saga completed for order: {}", traceId, request.orderId)
            return SagaResult(
                orderId = request.orderId,
                success = true,
                message = "Order processed successfully",
                order = finalOrder,
                payment = paymentResponse
            )
        } catch (ex: Exception) {
            log.error("[{}] Saga failed for order {}: {}", traceId, request.orderId, ex.message)
            sagaState.status = "FAILED"
            sagaState.updatedAt = Instant.now()
            sagaStateRepository.save(sagaState)

            publishCompensations(request, sagaState)

            return SagaResult(
                orderId = request.orderId,
                success = false,
                message = "Order processing failed: ${ex.message}"
            )
        }
    }

    private fun publishCompensations(request: OrderRequest, sagaState: SagaState) {
        val completedSteps = sagaState.steps
            .filter { it.status == "COMPLETED" }
            .map { it.stepName }

        if ("EXECUTE_PAYMENT" in completedSteps) {
            val event = CompensationEvent(orderId = request.orderId, compensationType = CompensationType.REFUND_PAYMENT)
            outboxEventRepository.save(OutboxEvent(
                orderId = request.orderId,
                eventType = "CompensationRequested",
                payload = objectMapper.writeValueAsString(event)
            ))
        }
        if ("CREATE_ORDER" in completedSteps) {
            val event = CompensationEvent(orderId = request.orderId, compensationType = CompensationType.CANCEL_ORDER)
            outboxEventRepository.save(OutboxEvent(
                orderId = request.orderId,
                eventType = "CompensationRequested",
                payload = objectMapper.writeValueAsString(event)
            ))
        }
    }

    private fun addStep(sagaState: SagaState, stepName: String): SagaStep {
        val step = SagaStep(sagaState = sagaState, stepName = stepName, status = "IN_PROGRESS")
        sagaState.steps.add(step)
        sagaState.currentStep = stepName
        sagaState.updatedAt = Instant.now()
        sagaStateRepository.save(sagaState)
        return step
    }

    private fun completeStep(step: SagaStep) {
        step.status = "COMPLETED"
        step.executedAt = Instant.now()
    }
}
