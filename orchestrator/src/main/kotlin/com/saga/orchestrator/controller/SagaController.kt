package com.saga.orchestrator.controller

import com.saga.common.dto.OrderRequest
import com.saga.common.dto.SagaResult
import com.saga.common.idempotency.Idempotent
import com.saga.orchestrator.saga.SagaOrchestrator
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/saga")
class SagaController(
    private val sagaOrchestrator: SagaOrchestrator
) {

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
}
