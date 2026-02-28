package com.example.orchestrator.application

import com.example.common.dto.CompensationEvent
import com.example.common.dto.CompensationType
import com.example.common.dto.OrderRequest
import com.example.common.dto.PaymentRequest
import com.example.common.dto.SagaResult
import com.example.orchestrator.application.port.ServiceClients
import com.example.orchestrator.domain.OutboxEventRepository
import com.example.orchestrator.domain.SagaStateRepository
import com.example.orchestrator.domain.model.OutboxEvent
import com.example.orchestrator.domain.model.SagaState
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class SagaApplicationService(
    private val serviceClients: ServiceClients,
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
    private val sagaStateRepository: SagaStateRepository,
    private val transactionTemplate: TransactionTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun executeSaga(request: OrderRequest, traceId: String): SagaResult {
        var sagaState = transactionTemplate.execute {
            sagaStateRepository.save(SagaState.create(orderId = request.orderId))
        }!!

        try {
            // Step 1: Create Order (PENDING)
            sagaState = transactionTemplate.execute {
                sagaStateRepository.save(sagaState.addStep("CREATE_ORDER"))
            }!!
            serviceClients.createOrder(request, traceId)
            sagaState = sagaState.completeCurrentStep()

            // Step 2: Execute Payment (authorize + capture)
            sagaState = transactionTemplate.execute {
                sagaStateRepository.save(sagaState.addStep("EXECUTE_PAYMENT"))
            }!!
            val paymentRequest = PaymentRequest(orderId = request.orderId, amount = request.amount)
            serviceClients.authorizePayment(paymentRequest, traceId)
            val paymentResponse = serviceClients.capturePayment(request.orderId, traceId)
            sagaState = sagaState.completeCurrentStep()

            // Step 3: Complete Order (COMPLETED)
            sagaState = transactionTemplate.execute {
                sagaStateRepository.save(sagaState.addStep("COMPLETE_ORDER"))
            }!!
            val finalOrder = serviceClients.completeOrder(request.orderId, traceId)
            sagaState = sagaState.completeCurrentStep()

            transactionTemplate.execute {
                sagaStateRepository.save(sagaState.markCompleted())
            }

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
            transactionTemplate.execute {
                sagaState = sagaStateRepository.save(sagaState.markFailed())
                publishCompensations(request, sagaState)
            }

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
