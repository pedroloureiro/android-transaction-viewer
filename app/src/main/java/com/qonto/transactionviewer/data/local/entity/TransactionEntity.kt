package com.qonto.transactionviewer.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

data class AmountEmbedded(
    val value: String,
    val currency: String,
)

data class InitiatorEmbedded(
    val id: String,
    val fullName: String,
)

data class BankAccountEmbedded(
    val id: String,
    val name: String,
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    @Embedded(prefix = "amount_") val amount: AmountEmbedded,
    val counterpartyName: String,
    val emittedAt: String,
    val settledAt: String?,
    val side: String,
    val status: String,
    val operationMethod: String,
    val operationType: String,
    val description: String,
    val activityTag: String,
    val note: String?,
    @Embedded(prefix = "initiator_") val initiator: InitiatorEmbedded?,
    @Embedded(prefix = "bank_account_") val bankAccount: BankAccountEmbedded,
)
