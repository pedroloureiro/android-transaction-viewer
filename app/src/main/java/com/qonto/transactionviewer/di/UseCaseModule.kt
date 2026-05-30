package com.qonto.transactionviewer.di

import com.qonto.transactionviewer.domain.usecase.GetTransactionsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { GetTransactionsUseCase(get()) }
}
