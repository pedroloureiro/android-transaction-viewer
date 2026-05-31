package com.qonto.transactionviewer.di

import com.qonto.transactionviewer.ui.viewmodel.TransactionListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { TransactionListViewModel(get()) }
}
