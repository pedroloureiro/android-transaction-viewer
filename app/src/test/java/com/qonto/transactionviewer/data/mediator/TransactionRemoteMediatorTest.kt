package com.qonto.transactionviewer.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.MediatorResult
import com.qonto.transactionviewer.data.local.entity.TransactionEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class TransactionRemoteMediatorTest {

    private fun pagingState() = PagingState<Int, TransactionEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0,
    )

    private fun mediator(
        onInitialize: suspend () -> Unit = {},
        onRefresh: suspend (PagingState<Int, TransactionEntity>) -> MediatorResult = { MediatorResult.Success(endOfPaginationReached = false) },
        onAppend: suspend (PagingState<Int, TransactionEntity>) -> MediatorResult = { MediatorResult.Success(endOfPaginationReached = false) },
    ) = TransactionRemoteMediator(onInitialize = onInitialize, onRefresh = onRefresh, onAppend = onAppend)

    @Test
    fun `PREPEND always returns endOfPaginationReached`() = runTest {
        val result = mediator().load(LoadType.PREPEND, pagingState())
        assertTrue(result is MediatorResult.Success)
        assertTrue((result as MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `REFRESH delegates to onRefresh callback`() = runTest {
        val expected = MediatorResult.Success(endOfPaginationReached = false)
        val result = mediator(onRefresh = { expected }).load(LoadType.REFRESH, pagingState())
        assertEquals(expected, result)
    }

    @Test
    fun `APPEND delegates to onAppend callback`() = runTest {
        val expected = MediatorResult.Success(endOfPaginationReached = true)
        val result = mediator(onAppend = { expected }).load(LoadType.APPEND, pagingState())
        assertEquals(expected, result)
    }

    @Test
    fun `REFRESH propagates error result from callback`() = runTest {
        val error = RuntimeException("refresh failed")
        val result = mediator(onRefresh = { MediatorResult.Error(error) }).load(LoadType.REFRESH, pagingState())
        assertTrue(result is MediatorResult.Error)
        assertEquals(error, (result as MediatorResult.Error).throwable)
    }
}
