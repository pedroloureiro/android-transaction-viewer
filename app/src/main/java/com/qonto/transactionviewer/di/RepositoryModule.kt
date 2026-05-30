package com.qonto.transactionviewer.di

import com.qonto.transactionviewer.data.local.datasource.TransactionLocalDataSource
import com.qonto.transactionviewer.data.remote.service.TransactionService
import com.qonto.transactionviewer.domain.repository.TransactionRepository
import com.qonto.transactionviewer.domain.repository.TransactionRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<TransactionRepository> {
        TransactionRepositoryImpl(get<TransactionService>(), get<TransactionLocalDataSource>())
    }
}
