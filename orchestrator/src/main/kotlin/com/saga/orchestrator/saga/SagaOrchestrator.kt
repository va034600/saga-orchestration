package com.saga.orchestrator.saga

import com.saga.common.dto.CompensationEvent
import com.saga.common.dto.OrderRequest
import com.saga.common.dto.PaymentRequest
import com.saga.common.dto.SagaResult
import com.saga.common.enums.CompensationType
import com.saga.orchestrator.client.ServiceClients
import com.saga.orchestrator.entity.SagaState
import com.saga.orchestrator.entity.SagaStep
import com.saga.orchestrator.publisher.EventBridgePublisher
import com.saga.orchestrator.repository.SagaStateRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SagaOrchestrator(
    private val serviceClients: ServiceClients,
    private val eventBridgePublisher: EventBridgePublisher,
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
            eventBridgePublisher.publishCompensation(
                CompensationEvent(orderId = request.orderId, compensationType = CompensationType.REFUND_PAYMENT)
            )
        }
        if ("CREATE_ORDER" in completedSteps) {
            eventBridgePublisher.publishCompensation(
                CompensationEvent(orderId = request.orderId, compensationType = CompensationType.CANCEL_ORDER)
            )
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
