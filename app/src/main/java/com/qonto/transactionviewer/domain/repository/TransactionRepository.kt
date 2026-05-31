package com.qonto.transactionviewer.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.MediatorResult
import androidx.paging.map
import androidx.room.withTransaction
import com.qonto.transactionviewer.data.local.database.AppDatabase
import com.qonto.transactionviewer.data.local.entity.RemoteKeyEntity
import com.qonto.transactionviewer.data.local.entity.TransactionEntity
import com.qonto.transactionviewer.data.mediator.TransactionRemoteMediator
import com.qonto.transactionviewer.data.remote.mapper.toDomain
import com.qonto.transactionviewer.data.remote.mapper.toEntity
import com.qonto.transactionviewer.domain.util.safeApiCall
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
    private val database: AppDatabase,
) : TransactionRepository {

    override fun getTransactions(): Flow<PagingData<Transaction>> {
        val mediator = TransactionRemoteMediator(
            onRefresh = { state -> refresh(state) },
            onAppend = { state -> append(state) },
        )
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = true,
            ),
            remoteMediator = mediator,
            pagingSourceFactory = { database.transactionDao().pagingSource() },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    private suspend fun refresh(state: PagingState<Int, TransactionEntity>): MediatorResult {
        val existingSeed = database.remoteKeyDao().get()?.seed
        return safeApiCall { service.getTransactions(results = state.config.pageSize, page = 1, seed = existingSeed) }
            .fold(
                onSuccess = { response ->
                    database.withTransaction {
                        database.transactionDao().clearAll()
                        database.transactionDao().insertAll(response.results.map { it.toEntity() })
                        database.remoteKeyDao().clearAll()
                        database.remoteKeyDao().insert(RemoteKeyEntity(seed = existingSeed ?: response.info.seed, nextPage = 2))
                    }
                    MediatorResult.Success(endOfPaginationReached = false)
                },
                onFailure = { MediatorResult.Error(it) },
            )
    }

    private suspend fun append(state: PagingState<Int, TransactionEntity>): MediatorResult {
        val remoteKey = database.remoteKeyDao().get()
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        if (remoteKey.nextPage > MAX_PAGE) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        return safeApiCall { service.getTransactions(results = state.config.pageSize, page = remoteKey.nextPage, seed = remoteKey.seed) }
            .fold(
                onSuccess = { response ->
                    database.withTransaction {
                        database.transactionDao().insertAll(response.results.map { it.toEntity() })
                        database.remoteKeyDao().insert(RemoteKeyEntity(seed = remoteKey.seed, nextPage = remoteKey.nextPage + 1))
                    }
                    MediatorResult.Success(endOfPaginationReached = false)
                },
                onFailure = { MediatorResult.Error(it) },
            )
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 5
        private const val MAX_PAGE = 10_000
    }
}
