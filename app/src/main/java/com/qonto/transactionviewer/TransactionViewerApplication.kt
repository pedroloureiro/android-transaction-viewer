package com.qonto.transactionviewer

import android.app.Application
import com.qonto.transactionviewer.di.databaseModule
import com.qonto.transactionviewer.di.networkModule
import com.qonto.transactionviewer.di.repositoryModule
import com.qonto.transactionviewer.di.useCaseModule
import com.qonto.transactionviewer.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TransactionViewerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TransactionViewerApplication)
            modules(networkModule, databaseModule, repositoryModule, useCaseModule, viewModelModule)
        }
    }
}
