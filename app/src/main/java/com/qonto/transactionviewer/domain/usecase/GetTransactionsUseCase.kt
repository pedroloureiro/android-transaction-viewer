package com.qonto.transactionviewer.domain.usecase

import androidx.paging.PagingData
import com.qonto.transactionviewer.domain.model.Transaction
import com.qonto.transactionviewer.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<PagingData<Transaction>> = repository.getTransactions()
}
