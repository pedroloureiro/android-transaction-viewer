package com.qonto.transactionviewer.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.qonto.transactionviewer.data.local.entity.TransactionEntity

@OptIn(ExperimentalPagingApi::class)
class TransactionRemoteMediator(
    private val onRefresh: suspend (PagingState<Int, TransactionEntity>) -> MediatorResult,
    private val onAppend: suspend (PagingState<Int, TransactionEntity>) -> MediatorResult,
) : RemoteMediator<Int, TransactionEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TransactionEntity>,
    ): MediatorResult = when (loadType) {
        LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true)
        LoadType.REFRESH -> onRefresh(state)
        LoadType.APPEND -> onAppend(state)
    }
}
