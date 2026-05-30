package com.qonto.transactionviewer.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qonto.transactionviewer.data.local.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions")
    fun pagingSource(): PagingSource<Int, TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
