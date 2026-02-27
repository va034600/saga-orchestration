package com.example.orchestrator.application.port

interface SagaExecutionClient {
    data class ExecutionDescription(
        val executionArn: String,
        val status: String,
        val input: String?,
        val output: String?,
        val startDate: String?,
        val stopDate: String?,
    )

    fun startExecution(name: String, input: String): String
    fun describeExecution(executionArn: String): ExecutionDescription
}
