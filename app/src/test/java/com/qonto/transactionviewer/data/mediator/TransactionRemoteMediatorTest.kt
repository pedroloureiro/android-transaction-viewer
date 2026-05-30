package com.qonto.transactionviewer.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import com.qonto.transactionviewer.data.local.TransactionLocalDataSource
import com.qonto.transactionviewer.data.local.entity.TransactionEntity
import com.qonto.transactionviewer.data.local.model.PaginationState
import com.qonto.transactionviewer.data.remote.dto.PaginationInfoDto
import com.qonto.transactionviewer.data.remote.dto.TransactionResponseDto
import com.qonto.transactionviewer.data.remote.service.TransactionService
import com.qonto.transactionviewer.data.mediator.TransactionRemoteMediator.Companion.MAX_PAGE
import com.qonto.transactionviewer.data.mediator.TransactionRemoteMediator.Companion.PAGE_SIZE
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class TransactionRemoteMediatorTest {

    private val service = mockk<TransactionService>()
    private val localDataSource = mockk<TransactionLocalDataSource>(relaxed = true)
    private val mediator = TransactionRemoteMediator(service, localDataSource)

    private fun pagingState() = PagingState<Int, TransactionEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = PAGE_SIZE),
        leadingPlaceholderCount = 0,
    )

    private fun mockResponse(seed: String, page: Int = 1) = TransactionResponseDto(
        results = emptyList(),
        info = PaginationInfoDto(seed = seed, results = 0, page = page, version = "1.0"),
    )

    @Test
    fun `REFRESH with no existing seed fetches page 1 without seed and persists response seed`() = runTest {
        coEvery { localDataSource.getPaginationState() } returns null
        coEvery { service.getTransactions(results = PAGE_SIZE, page = 1, seed = null) } returns mockResponse("new-seed")

        val result = mediator.load(LoadType.REFRESH, pagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertFalse((result as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify { localDataSource.refresh(emptyList(), "new-seed") }
    }

    @Test
    fun `REFRESH with existing seed reuses seed and resets to page 1`() = runTest {
        coEvery { localDataSource.getPaginationState() } returns PaginationState("existing-seed", 5)
        coEvery { service.getTransactions(results = PAGE_SIZE, page = 1, seed = "existing-seed") } returns mockResponse("existing-seed")

        mediator.load(LoadType.REFRESH, pagingState())

        coVerify { service.getTransactions(results = PAGE_SIZE, page = 1, seed = "existing-seed") }
        coVerify { localDataSource.refresh(emptyList(), "existing-seed") }
    }

    @Test
    fun `APPEND uses persisted seed and fetches next page`() = runTest {
        coEvery { localDataSource.getPaginationState() } returns PaginationState("seed", 3)
        coEvery { service.getTransactions(results = PAGE_SIZE, page = 3, seed = "seed") } returns mockResponse("seed", 3)

        val result = mediator.load(LoadType.APPEND, pagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertFalse((result as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify { localDataSource.append(emptyList(), "seed", 4) }
    }

    @Test
    fun `APPEND returns endOfPaginationReached when nextPage exceeds MAX_PAGE`() = runTest {
        coEvery { localDataSource.getPaginationState() } returns PaginationState("seed", MAX_PAGE + 1)

        val result = mediator.load(LoadType.APPEND, pagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertTrue((result as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { service.getTransactions(any(), any(), any()) }
    }

    @Test
    fun `APPEND with no pagination state returns endOfPaginationReached`() = runTest {
        coEvery { localDataSource.getPaginationState() } returns null

        val result = mediator.load(LoadType.APPEND, pagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertTrue((result as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `PREPEND always returns endOfPaginationReached`() = runTest {
        val result = mediator.load(LoadType.PREPEND, pagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertTrue((result as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `REFRESH on network failure returns MediatorResult Error`() = runTest {
        coEvery { localDataSource.getPaginationState() } returns null
        coEvery { service.getTransactions(any(), any(), anyNullable()) } throws IOException("Network error")

        val result = mediator.load(LoadType.REFRESH, pagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun `APPEND on network failure returns MediatorResult Error`() = runTest {
        coEvery { localDataSource.getPaginationState() } returns PaginationState("seed", 2)
        coEvery { service.getTransactions(any(), any(), anyNullable()) } throws IOException("Network error")

        val result = mediator.load(LoadType.APPEND, pagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Error)
    }
}
