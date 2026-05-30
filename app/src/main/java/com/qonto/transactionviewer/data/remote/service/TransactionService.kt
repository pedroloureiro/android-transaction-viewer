package com.qonto.transactionviewer.data.remote.service

import com.qonto.transactionviewer.data.remote.dto.TransactionResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionService {
    @GET("transactions")
    suspend fun getTransactions(
        @Query("results") results: Int,
        @Query("page") page: Int,
        @Query("seed") seed: String? = null,
    ): TransactionResponseDto
}
