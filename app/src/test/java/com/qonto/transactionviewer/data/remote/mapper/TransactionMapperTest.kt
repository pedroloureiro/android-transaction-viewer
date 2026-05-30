package com.qonto.transactionviewer.data.remote.mapper

import com.qonto.transactionviewer.data.local.entity.AmountEmbedded
import com.qonto.transactionviewer.data.local.entity.BankAccountEmbedded
import com.qonto.transactionviewer.data.local.entity.InitiatorEmbedded
import com.qonto.transactionviewer.data.local.entity.TransactionEntity
import com.qonto.transactionviewer.data.remote.dto.AmountDto
import com.qonto.transactionviewer.data.remote.dto.BankAccountDto
import com.qonto.transactionviewer.data.remote.dto.TransactionDto
import com.qonto.transactionviewer.domain.model.TransactionSide
import com.qonto.transactionviewer.domain.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TransactionMapperTest {

    private val baseEntity = TransactionEntity(
        id = "id-1",
        amount = AmountEmbedded("100.00", "EUR"),
        counterpartyName = "Test Company",
        emittedAt = "2025-01-01T10:00:00.000Z",
        settledAt = "2025-01-01T10:00:30.000Z",
        side = "CREDIT",
        status = "COMPLETED",
        operationMethod = "CARD",
        operationType = "INCOME",
        description = "Test transaction",
        activityTag = "OTHER_INCOME",
        note = null,
        initiator = null,
        bankAccount = BankAccountEmbedded("bank-id", "Main Account"),
    )

    private val baseDto = TransactionDto(
        id = "dto-id",
        amount = AmountDto("200.00", "EUR"),
        counterpartyName = "DTO Company",
        emittedAt = "2025-01-01T10:00:00.000Z",
        settledAt = null,
        side = "DEBIT",
        status = "PENDING",
        operationMethod = "TRANSFER",
        operationType = "TRANSFER",
        description = "DTO transaction",
        activityTag = "FEES",
        note = "a note",
        initiator = null,
        bankAccount = BankAccountDto("bank-id", "Business Account"),
    )

    // toDomain

    @Test
    fun `toDomain maps all fields correctly`() {
        val domain = baseEntity.toDomain()

        assertEquals("id-1", domain.id)
        assertEquals("100.00", domain.amount.value)
        assertEquals("EUR", domain.amount.currency)
        assertEquals("Test Company", domain.counterpartyName)
        assertEquals(TransactionSide.CREDIT, domain.side)
        assertEquals(TransactionStatus.COMPLETED, domain.status)
        assertEquals("bank-id", domain.bankAccount.id)
    }

    @Test
    fun `toDomain maps nullable initiator when present`() {
        val entity = baseEntity.copy(initiator = InitiatorEmbedded("init-id", "John Doe"))

        val domain = entity.toDomain()

        assertNotNull(domain.initiator)
        assertEquals("init-id", domain.initiator?.id)
        assertEquals("John Doe", domain.initiator?.fullName)
    }

    @Test
    fun `toDomain maps null initiator correctly`() {
        val domain = baseEntity.toDomain()

        assertNull(domain.initiator)
    }

    @Test
    fun `toDomain throws IllegalArgumentException for unrecognised side`() {
        val entity = baseEntity.copy(side = "INVALID_SIDE")

        assertThrows(IllegalArgumentException::class.java) { entity.toDomain() }
    }

    @Test
    fun `toDomain throws IllegalArgumentException for unrecognised status`() {
        val entity = baseEntity.copy(status = "INVALID_STATUS")

        assertThrows(IllegalArgumentException::class.java) { entity.toDomain() }
    }

    // toEntity

    @Test
    fun `toEntity maps all fields correctly`() {
        val entity = baseDto.toEntity()

        assertEquals("dto-id", entity.id)
        assertEquals("200.00", entity.amount.value)
        assertEquals("EUR", entity.amount.currency)
        assertEquals("a note", entity.note)
        assertNull(entity.settledAt)
        assertNull(entity.initiator)
        assertEquals("bank-id", entity.bankAccount.id)
    }

    @Test
    fun `toEntity preserves raw side and status strings`() {
        val entity = baseDto.toEntity()

        assertEquals("DEBIT", entity.side)
        assertEquals("PENDING", entity.status)
    }
}
