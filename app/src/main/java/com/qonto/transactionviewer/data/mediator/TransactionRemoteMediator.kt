package com.qonto.transactionviewer.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.qonto.transactionviewer.data.local.datasource.TransactionLocalDataSource
import com.qonto.transactionviewer.data.local.entity.TransactionEntity
import com.qonto.transactionviewer.data.remote.mapper.toEntity
import com.qonto.transactionviewer.data.remote.service.TransactionService

@OptIn(ExperimentalPagingApi::class)
class TransactionRemoteMediator(
    private val service: TransactionService,
    private val localDataSource: TransactionLocalDataSource,
) : RemoteMediator<Int, TransactionEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TransactionEntity>,
    ): MediatorResult {
        return try {
            when (loadType) {
                LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true)
                LoadType.REFRESH -> refresh(state)
                LoadType.APPEND -> append(state)
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun refresh(state: PagingState<Int, TransactionEntity>): MediatorResult {
        val existingSeed = localDataSource.getPaginationState()?.seed
        val response = service.getTransactions(
            results = state.config.pageSize,
            page = 1,
            seed = existingSeed,
        )
        localDataSource.refresh(
            transactions = response.results.map { it.toEntity() },
            seed = existingSeed ?: response.info.seed,
        )
        return MediatorResult.Success(endOfPaginationReached = false)
    }

    private suspend fun append(state: PagingState<Int, TransactionEntity>): MediatorResult {
        val paginationState = localDataSource.getPaginationState()
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        if (paginationState.nextPage > MAX_PAGE) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        val response = service.getTransactions(
            results = state.config.pageSize,
            page = paginationState.nextPage,
            seed = paginationState.seed,
        )
        localDataSource.append(
            transactions = response.results.map { it.toEntity() },
            seed = paginationState.seed,
            nextPage = paginationState.nextPage + 1,
        )
        return MediatorResult.Success(endOfPaginationReached = false)
    }

    companion object {
        const val MAX_PAGE = 10_000
        const val PAGE_SIZE = 20
    }
}
