package com.example.orchestrator.presentation

import com.example.common.dto.OrderRequest
import com.example.common.dto.SagaResult
import com.example.common.idempotency.Idempotent
import com.example.orchestrator.application.AsyncSagaApplicationService
import com.example.orchestrator.application.SagaApplicationService
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/saga")
class SagaController(
    private val sagaApplicationService: SagaApplicationService,
    private val asyncSagaApplicationService: AsyncSagaApplicationService,
) {

    @PostMapping("/orders")
    @Idempotent
    fun executeOrderSaga(@RequestBody request: OrderRequest): ResponseEntity<SagaResult> {
        val traceId = MDC.get("traceId") ?: "unknown"
        val result = sagaApplicationService.executeSaga(request, traceId)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.unprocessableEntity().body(result)
        }
    }

    @PostMapping("/orders/async")
    @Idempotent
    fun executeOrderSagaAsync(@RequestBody request: OrderRequest): ResponseEntity<AsyncSagaApplicationService.StartResult> {
        val traceId = MDC.get("traceId") ?: "unknown"
        val result = asyncSagaApplicationService.startSaga(request, traceId)
        return ResponseEntity.accepted().body(result)
    }

    @GetMapping("/executions")
    fun getExecutionStatus(@RequestParam executionArn: String): ResponseEntity<AsyncSagaApplicationService.ExecutionStatus> {
        val status = asyncSagaApplicationService.getExecutionStatus(executionArn)
        return ResponseEntity.ok(status)
    }
}
