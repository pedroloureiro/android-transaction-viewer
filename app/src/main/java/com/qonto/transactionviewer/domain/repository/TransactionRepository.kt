package com.qonto.transactionviewer.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.qonto.transactionviewer.data.local.datasource.TransactionLocalDataSource
import com.qonto.transactionviewer.data.mediator.TransactionRemoteMediator
import com.qonto.transactionviewer.data.remote.mapper.toDomain
import com.qonto.transactionviewer.data.remote.service.TransactionService
import com.qonto.transactionviewer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TransactionRepository {
    fun getTransactions(): Flow<PagingData<Transaction>>
}

@OptIn(ExperimentalPagingApi::class)
class TransactionRepositoryImpl(
    private val service: TransactionService,
    private val localDataSource: TransactionLocalDataSource,
) : TransactionRepository {

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 5
    }
    override fun getTransactions(): Flow<PagingData<Transaction>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = true,
            ),
            remoteMediator = TransactionRemoteMediator(service, localDataSource),
            pagingSourceFactory = { localDataSource.pagingSource() },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
}
