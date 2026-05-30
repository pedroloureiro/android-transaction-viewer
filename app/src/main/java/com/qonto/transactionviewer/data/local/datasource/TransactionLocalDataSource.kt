package com.qonto.transactionviewer.data.local.datasource

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.qonto.transactionviewer.data.local.database.AppDatabase
import com.qonto.transactionviewer.data.local.entity.RemoteKeyEntity
import com.qonto.transactionviewer.data.local.entity.TransactionEntity
import com.qonto.transactionviewer.data.local.model.PaginationState

interface TransactionLocalDataSource {
    suspend fun getPaginationState(): PaginationState?
    suspend fun refresh(transactions: List<TransactionEntity>, seed: String)
    suspend fun append(transactions: List<TransactionEntity>, seed: String, nextPage: Int)
    fun pagingSource(): PagingSource<Int, TransactionEntity>
}

class TransactionLocalDataSourceImpl(
    private val database: AppDatabase,
) : TransactionLocalDataSource {

    override suspend fun getPaginationState(): PaginationState? {
        return database.remoteKeyDao().get()?.let {
            PaginationState(seed = it.seed, nextPage = it.nextPage)
        }
    }

    override suspend fun refresh(transactions: List<TransactionEntity>, seed: String) {
        database.withTransaction {
            database.transactionDao().clearAll()
            database.transactionDao().insertAll(transactions)
            database.remoteKeyDao().insert(RemoteKeyEntity(seed = seed, nextPage = 2))
        }
    }

    override suspend fun append(transactions: List<TransactionEntity>, seed: String, nextPage: Int) {
        database.withTransaction {
            database.transactionDao().insertAll(transactions)
            database.remoteKeyDao().insert(RemoteKeyEntity(seed = seed, nextPage = nextPage))
        }
    }

    override fun pagingSource(): PagingSource<Int, TransactionEntity> =
        database.transactionDao().pagingSource()
}
