package com.qonto.transactionviewer.domain.repository

import androidx.paging.PagingData
import com.qonto.transactionviewer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(): Flow<PagingData<Transaction>>
}
