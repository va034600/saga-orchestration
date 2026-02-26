package com.saga.orchestrator.controller

import com.saga.common.dto.OrderRequest
import com.saga.common.dto.SagaResult
import com.saga.common.idempotency.Idempotent
import com.saga.orchestrator.saga.AsyncSagaOrchestrator
import com.saga.orchestrator.saga.SagaOrchestrator
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/saga")
class SagaController(
    private val sagaOrchestrator: SagaOrchestrator,
    private val asyncSagaOrchestrator: AsyncSagaOrchestrator
) {

    /** 同期Saga: 結果を待って返す */
    @PostMapping("/orders")
    @Idempotent
    fun executeOrderSaga(@RequestBody request: OrderRequest): ResponseEntity<SagaResult> {
        val traceId = MDC.get("traceId") ?: "unknown"
        val result = sagaOrchestrator.executeSaga(request, traceId)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.unprocessableEntity().body(result)
        }
    }

    /** 非同期Saga: Step Functions実行を開始し、executionArnを即返却 */
    @PostMapping("/orders/async")
    @Idempotent
    fun executeOrderSagaAsync(@RequestBody request: OrderRequest): ResponseEntity<AsyncSagaOrchestrator.StartResult> {
        val traceId = MDC.get("traceId") ?: "unknown"
        val result = asyncSagaOrchestrator.startSaga(request, traceId)
        return ResponseEntity.accepted().body(result)
    }

    /** 非同期Sagaの実行状態を取得 */
    @GetMapping("/executions")
    fun getExecutionStatus(@RequestParam executionArn: String): ResponseEntity<AsyncSagaOrchestrator.ExecutionStatus> {
        val status = asyncSagaOrchestrator.getExecutionStatus(executionArn)
        return ResponseEntity.ok(status)
    }
}
