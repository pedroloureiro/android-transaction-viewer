package com.qonto.transactionviewer.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qonto.transactionviewer.data.local.dao.RemoteKeyDao
import com.qonto.transactionviewer.data.local.dao.TransactionDao
import com.qonto.transactionviewer.data.local.entity.RemoteKeyEntity
import com.qonto.transactionviewer.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
