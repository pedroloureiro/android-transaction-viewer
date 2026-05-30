package com.qonto.transactionviewer.di

import com.qonto.transactionviewer.data.local.datasource.TransactionLocalDataSource
import com.qonto.transactionviewer.data.local.datasource.TransactionLocalDataSourceImpl
import com.qonto.transactionviewer.data.local.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> { AppDatabase.create(androidContext()) }
    single<TransactionLocalDataSource> { TransactionLocalDataSourceImpl(get()) }
}
