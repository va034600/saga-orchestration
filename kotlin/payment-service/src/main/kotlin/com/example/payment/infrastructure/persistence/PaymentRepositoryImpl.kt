package com.example.payment.infrastructure.persistence

import com.example.payment.domain.PaymentRepository
import com.example.payment.domain.PaymentStatus
import com.example.payment.domain.model.Money
import com.example.payment.domain.model.Payment
import com.example.payment.domain.model.PaymentId
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryImpl(
    private val jpaRepository: PaymentJpaRepository,
) : PaymentRepository {

    override fun findByOrderId(orderId: String): Payment? =
        jpaRepository.findByOrderId(orderId).orElse(null)?.let(::toDomain)

    override fun save(payment: Payment): Payment {
        val entity = toEntity(payment)
        return toDomain(jpaRepository.save(entity))
    }

    private fun toDomain(entity: PaymentJpaEntity): Payment = Payment.reconstitute(
        paymentId = PaymentId(entity.paymentId),
        orderId = entity.orderId,
        amount = Money(entity.amount),
        status = PaymentStatus.valueOf(entity.status),
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    private fun toEntity(payment: Payment): PaymentJpaEntity = PaymentJpaEntity(
        paymentId = payment.id.value,
        orderId = payment.orderId,
        amount = payment.amount.amount,
        status = payment.status.name,
        createdAt = payment.createdAt,
        updatedAt = payment.updatedAt,
    )
}
