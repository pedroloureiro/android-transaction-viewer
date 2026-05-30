package com.qonto.transactionviewer.data.remote.mapper

import com.qonto.transactionviewer.data.local.entity.AmountEmbedded
import com.qonto.transactionviewer.data.local.entity.BankAccountEmbedded
import com.qonto.transactionviewer.data.local.entity.InitiatorEmbedded
import com.qonto.transactionviewer.data.local.entity.TransactionEntity
import com.qonto.transactionviewer.data.remote.dto.TransactionDto
import com.qonto.transactionviewer.domain.model.Amount
import com.qonto.transactionviewer.domain.model.BankAccount
import com.qonto.transactionviewer.domain.model.Initiator
import com.qonto.transactionviewer.domain.model.Transaction
import com.qonto.transactionviewer.domain.model.TransactionSide
import com.qonto.transactionviewer.domain.model.TransactionStatus

fun TransactionDto.toEntity() = TransactionEntity(
    id = id,
    amount = AmountEmbedded(amount.value, amount.currency),
    counterpartyName = counterpartyName,
    emittedAt = emittedAt,
    settledAt = settledAt,
    side = side,
    status = status,
    operationMethod = operationMethod,
    operationType = operationType,
    description = description,
    activityTag = activityTag,
    note = note,
    initiator = initiator?.let { InitiatorEmbedded(it.id, it.fullName) },
    bankAccount = BankAccountEmbedded(bankAccount.id, bankAccount.name),
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = Amount(amount.value, amount.currency),
    counterpartyName = counterpartyName,
    emittedAt = emittedAt,
    settledAt = settledAt,
    side = TransactionSide.valueOf(side),
    status = TransactionStatus.valueOf(status),
    operationMethod = operationMethod,
    operationType = operationType,
    description = description,
    activityTag = activityTag,
    note = note,
    initiator = initiator?.let { Initiator(it.id, it.fullName) },
    bankAccount = BankAccount(bankAccount.id, bankAccount.name),
)
