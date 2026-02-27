package com.example.orchestrator.infrastructure.aws

import com.example.orchestrator.application.port.SagaExecutionClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sfn.model.DescribeExecutionRequest
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest

@Component
class StepFunctionsSagaExecutionClient(
    private val sfnClient: SfnClient,
    @Value("\${aws.stepfunctions.state-machine-arn}") private val stateMachineArn: String,
) : SagaExecutionClient {

    override fun startExecution(name: String, input: String): String {
        val result = sfnClient.startExecution(
            StartExecutionRequest.builder()
                .stateMachineArn(stateMachineArn)
                .name(name)
                .input(input)
                .build()
        )
        return result.executionArn()
    }

    override fun describeExecution(executionArn: String): SagaExecutionClient.ExecutionDescription {
        val result = sfnClient.describeExecution(
            DescribeExecutionRequest.builder()
                .executionArn(executionArn)
                .build()
        )
        return SagaExecutionClient.ExecutionDescription(
            executionArn = result.executionArn(),
            status = result.statusAsString(),
            input = result.input(),
            output = result.output(),
            startDate = result.startDate()?.toString(),
            stopDate = result.stopDate()?.toString(),
        )
    }
}
