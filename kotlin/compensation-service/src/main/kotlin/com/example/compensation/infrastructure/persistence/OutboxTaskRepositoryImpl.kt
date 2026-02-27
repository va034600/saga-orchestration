package com.example.compensation.infrastructure.persistence

import com.example.compensation.domain.OutboxTaskRepository
import com.example.compensation.domain.model.OutboxTask
import org.springframework.stereotype.Repository

@Repository
class OutboxTaskRepositoryImpl(
    private val jpaRepository: OutboxTaskJpaRepository,
) : OutboxTaskRepository {

    override fun save(task: OutboxTask): OutboxTask {
        val entity = toEntity(task)
        return toDomain(jpaRepository.save(entity))
    }

    override fun findUnpublishedOrderByCreatedAt(): List<OutboxTask> =
        jpaRepository.findByPublishedFalseOrderByCreatedAtAsc().map(::toDomain)

    private fun toDomain(entity: OutboxTaskJpaEntity): OutboxTask = OutboxTask.reconstitute(
        id = entity.id!!,
        compensationId = entity.compensationId,
        orderId = entity.orderId,
        taskType = entity.taskType,
        payload = entity.payload,
        published = entity.published,
        createdAt = entity.createdAt,
        publishedAt = entity.publishedAt,
    )

    private fun toEntity(task: OutboxTask): OutboxTaskJpaEntity = OutboxTaskJpaEntity(
        id = task.id,
        compensationId = task.compensationId,
        orderId = task.orderId,
        taskType = task.taskType,
        payload = task.payload,
        published = task.published,
        createdAt = task.createdAt,
        publishedAt = task.publishedAt,
    )
}
