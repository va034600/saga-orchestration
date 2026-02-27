package com.example.common.idempotency

import com.fasterxml.jackson.databind.ObjectMapper
import com.example.common.exception.DuplicateRequestException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class IdempotencyAspect(
    private val repository: IdempotencyKeyRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Around("@annotation(Idempotent)")
    fun checkIdempotency(joinPoint: ProceedingJoinPoint): Any? {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
            ?.request ?: return joinPoint.proceed()

        val key = request.getHeader("Idempotency-Key")
            ?: return joinPoint.proceed()

        val existing = repository.findById(key)
        if (existing.isPresent) {
            val cached = existing.get()
            if (cached.responseBody != null) {
                log.info("Returning cached response for idempotency key: {}", key)
                val body = objectMapper.readValue(cached.responseBody, Any::class.java)
                return ResponseEntity.status(cached.statusCode ?: 200).body(body)
            }
            throw DuplicateRequestException(key)
        }

        val idempotencyKey = IdempotencyKey(idempotencyKey = key)
        repository.save(idempotencyKey)

        val result = joinPoint.proceed()

        if (result is ResponseEntity<*>) {
            idempotencyKey.statusCode = result.statusCode.value()
            idempotencyKey.responseBody = result.body?.let { objectMapper.writeValueAsString(it) }
            repository.save(idempotencyKey)
        }

        return result
    }
}
