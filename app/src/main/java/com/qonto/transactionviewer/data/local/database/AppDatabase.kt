package com.qonto.transactionviewer.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    companion object {
        private const val DATABASE_NAME = "transaction_viewer.db"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
    }
}
