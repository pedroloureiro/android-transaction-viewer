package com.qonto.transactionviewer.di

import com.qonto.transactionviewer.data.local.database.AppDatabase
import com.qonto.transactionviewer.data.remote.service.TransactionService
import com.qonto.transactionviewer.domain.repository.TransactionRepository
import com.qonto.transactionviewer.domain.repository.TransactionRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<TransactionRepository> {
        TransactionRepositoryImpl(
            service = get<TransactionService>(),
            database = get<AppDatabase>(),
        )
    }
}
