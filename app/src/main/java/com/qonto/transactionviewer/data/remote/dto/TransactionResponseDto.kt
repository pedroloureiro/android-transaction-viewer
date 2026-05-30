package com.qonto.transactionviewer.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponseDto(
    val results: List<TransactionDto>,
    val info: PaginationInfoDto,
)

@Serializable
data class PaginationInfoDto(
    val seed: String,
    val results: Int,
    val page: Int,
    val version: String,
)

@Serializable
data class TransactionDto(
    val id: String,
    val amount: AmountDto,
    val counterpartyName: String,
    val emittedAt: String,
    val settledAt: String? = null,
    val side: String,
    val status: String,
    val operationMethod: String,
    val operationType: String,
    val description: String,
    val activityTag: String,
    val note: String? = null,
    val initiator: InitiatorDto? = null,
    val bankAccount: BankAccountDto,
)

@Serializable
data class AmountDto(
    val value: String,
    val currency: String,
)

@Serializable
data class InitiatorDto(
    val id: String,
    val fullName: String,
)

@Serializable
data class BankAccountDto(
    val id: String,
    val name: String,
)
