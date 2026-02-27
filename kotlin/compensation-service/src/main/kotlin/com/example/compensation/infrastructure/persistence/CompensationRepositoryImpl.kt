package com.example.compensation.infrastructure.persistence

import com.example.compensation.domain.CompensationRepository
import com.example.compensation.domain.CompensationStatus
import com.example.compensation.domain.CompensationType
import com.example.compensation.domain.model.Compensation
import org.springframework.stereotype.Repository

@Repository
class CompensationRepositoryImpl(
    private val jpaRepository: CompensationJpaRepository,
) : CompensationRepository {

    override fun findById(id: Long): Compensation? =
        jpaRepository.findById(id).orElse(null)?.let(::toDomain)

    override fun save(compensation: Compensation): Compensation {
        val entity = toEntity(compensation)
        return toDomain(jpaRepository.save(entity))
    }

    private fun toDomain(entity: CompensationJpaEntity): Compensation = Compensation.reconstitute(
        id = entity.compId!!,
        orderId = entity.orderId,
        compensationType = CompensationType.valueOf(entity.compensationType),
        status = CompensationStatus.valueOf(entity.status),
        errorMessage = entity.errorMessage,
        createdAt = entity.createdAt,
        completedAt = entity.completedAt,
    )

    private fun toEntity(compensation: Compensation): CompensationJpaEntity = CompensationJpaEntity(
        compId = compensation.id,
        orderId = compensation.orderId,
        compensationType = compensation.compensationType.name,
        status = compensation.status.name,
        errorMessage = compensation.errorMessage,
        createdAt = compensation.createdAt,
        completedAt = compensation.completedAt,
    )
}
