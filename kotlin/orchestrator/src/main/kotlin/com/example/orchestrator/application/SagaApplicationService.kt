package com.example.orchestrator.application

import com.example.common.dto.CompensationEvent
import com.example.common.dto.CompensationType
import com.example.common.dto.OrderRequest
import com.example.common.dto.PaymentRequest
import com.example.common.dto.SagaResult
import com.example.orchestrator.domain.OutboxEventRepository
import com.example.orchestrator.domain.SagaStateRepository
import com.example.orchestrator.domain.model.OutboxEvent
import com.example.orchestrator.domain.model.SagaState
import com.example.orchestrator.infrastructure.http.ServiceClients
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SagaApplicationService(
    private val serviceClients: ServiceClients,
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
    private val sagaStateRepository: SagaStateRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun executeSaga(request: OrderRequest, traceId: String): SagaResult {
        val sagaState = SagaState.create(orderId = request.orderId)
        sagaStateRepository.save(sagaState)

        try {
            // Step 1: Create Order (PENDING)
            val orderStep = sagaState.addStep("CREATE_ORDER")
            sagaStateRepository.save(sagaState)
            serviceClients.createOrder(request, traceId)
            orderStep.complete()

            // Step 2: Execute Payment (authorize + capture)
            val paymentStep = sagaState.addStep("EXECUTE_PAYMENT")
            sagaStateRepository.save(sagaState)
            val paymentRequest = PaymentRequest(orderId = request.orderId, amount = request.amount)
            serviceClients.authorizePayment(paymentRequest, traceId)
            val paymentResponse = serviceClients.capturePayment(request.orderId, traceId)
            paymentStep.complete()

            // Step 3: Complete Order (COMPLETED)
            val completeOrderStep = sagaState.addStep("COMPLETE_ORDER")
            sagaStateRepository.save(sagaState)
            val finalOrder = serviceClients.completeOrder(request.orderId, traceId)
            completeOrderStep.complete()

            sagaState.markCompleted()
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
            sagaState.markFailed()
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
        val completedSteps = sagaState.completedStepNames()

        if ("EXECUTE_PAYMENT" in completedSteps) {
            val event = CompensationEvent(orderId = request.orderId, compensationType = CompensationType.REFUND_PAYMENT)
            outboxEventRepository.save(OutboxEvent.create(
                orderId = request.orderId,
                eventType = "CompensationRequested",
                payload = objectMapper.writeValueAsString(event),
            ))
        }
        if ("CREATE_ORDER" in completedSteps) {
            val event = CompensationEvent(orderId = request.orderId, compensationType = CompensationType.CANCEL_ORDER)
            outboxEventRepository.save(OutboxEvent.create(
                orderId = request.orderId,
                eventType = "CompensationRequested",
                payload = objectMapper.writeValueAsString(event),
            ))
        }
    }
}
