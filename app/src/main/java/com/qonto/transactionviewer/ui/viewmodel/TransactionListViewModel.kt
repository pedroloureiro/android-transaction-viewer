package com.qonto.transactionviewer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.qonto.transactionviewer.domain.model.Transaction
import com.qonto.transactionviewer.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.Flow

class TransactionListViewModel(
    getTransactionsUseCase: GetTransactionsUseCase,
) : ViewModel() {
    val transactions: Flow<PagingData<Transaction>> = getTransactionsUseCase().cachedIn(viewModelScope)
}
