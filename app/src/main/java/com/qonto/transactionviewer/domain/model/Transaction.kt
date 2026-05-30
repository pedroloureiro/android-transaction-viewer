package com.qonto.transactionviewer.domain.model

data class Transaction(
    val id: String,
    val amount: Amount,
    val counterpartyName: String,
    val emittedAt: String,
    val settledAt: String?,
    val side: TransactionSide,
    val status: TransactionStatus,
    val operationMethod: String,
    val operationType: String,
    val description: String,
    val activityTag: String,
    val note: String?,
    val initiator: Initiator?,
    val bankAccount: BankAccount,
)
