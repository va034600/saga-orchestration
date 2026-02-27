package com.example.compensation.domain

import com.example.compensation.domain.model.OutboxTask

interface OutboxTaskRepository {
    fun save(task: OutboxTask): OutboxTask
    fun findUnpublishedOrderByCreatedAt(): List<OutboxTask>
}
