package com.example.orchestrator.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.example.common.dto.OrderRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sfn.model.DescribeExecutionRequest
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest

@Service
class AsyncSagaApplicationService(
    private val sfnClient: SfnClient,
    private val objectMapper: ObjectMapper,
    @Value("\${aws.stepfunctions.state-machine-arn}") private val stateMachineArn: String
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

        val result = sfnClient.startExecution(
            StartExecutionRequest.builder()
                .stateMachineArn(stateMachineArn)
                .name("saga-${request.orderId}")
                .input(input)
                .build()
        )

        log.info("[{}] Started async saga for order: {}, executionArn: {}", traceId, request.orderId, result.executionArn())
        return StartResult(executionArn = result.executionArn(), orderId = request.orderId)
    }

    fun getExecutionStatus(executionArn: String): ExecutionStatus {
        val result = sfnClient.describeExecution(
            DescribeExecutionRequest.builder()
                .executionArn(executionArn)
                .build()
        )

        return ExecutionStatus(
            executionArn = result.executionArn(),
            status = result.statusAsString(),
            input = result.input(),
            output = result.output(),
            startDate = result.startDate()?.toString(),
            stopDate = result.stopDate()?.toString()
        )
    }
}
