package com.example.orchestrator.application

import com.example.common.dto.OrderRequest
import com.example.orchestrator.application.port.SagaExecutionClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AsyncSagaApplicationService(
    private val sagaExecutionClient: SagaExecutionClient,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    data class StartResult(
        val executionArn: String,
        val orderId: String
    )

    data class ExecutionStatus(
        val executionArn: String,
        val status: String,
        val input: String?,
        val output: String?,
        val startDate: String?,
        val stopDate: String?
    )

    fun startSaga(request: OrderRequest, traceId: String): StartResult {
        val input = objectMapper.writeValueAsString(
            mapOf(
                "orderId" to request.orderId,
                "productId" to request.productId,
                "quantity" to request.quantity,
                "amount" to request.amount,
                "traceId" to traceId
            )
        )

        val executionArn = sagaExecutionClient.startExecution("saga-${request.orderId}", input)

        log.info("[{}] Started async saga for order: {}, executionArn: {}", traceId, request.orderId, executionArn)
        return StartResult(executionArn = executionArn, orderId = request.orderId)
    }

    fun getExecutionStatus(executionArn: String): ExecutionStatus {
        val desc = sagaExecutionClient.describeExecution(executionArn)

        return ExecutionStatus(
            executionArn = desc.executionArn,
            status = desc.status,
            input = desc.input,
            output = desc.output,
            startDate = desc.startDate,
            stopDate = desc.stopDate
        )
    }
}
