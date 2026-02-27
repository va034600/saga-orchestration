package com.example.compensation.repository

import com.example.compensation.entity.OutboxTask
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxTaskRepository : JpaRepository<OutboxTask, Long> {
    fun findByPublishedFalseOrderByCreatedAtAsc(): List<OutboxTask>
}
