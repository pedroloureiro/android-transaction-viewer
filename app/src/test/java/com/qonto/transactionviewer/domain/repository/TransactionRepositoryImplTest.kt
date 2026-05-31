package com.qonto.transactionviewer.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.MediatorResult
import com.qonto.transactionviewer.data.local.dao.RemoteKeyDao
import com.qonto.transactionviewer.data.local.dao.TransactionDao
import com.qonto.transactionviewer.data.local.database.AppDatabase
import com.qonto.transactionviewer.data.local.entity.RemoteKeyEntity
import com.qonto.transactionviewer.data.local.entity.TransactionEntity
import com.qonto.transactionviewer.data.remote.dto.AmountDto
import com.qonto.transactionviewer.data.remote.dto.BankAccountDto
import com.qonto.transactionviewer.data.remote.dto.PaginationInfoDto
import com.qonto.transactionviewer.data.remote.dto.TransactionDto
import com.qonto.transactionviewer.data.remote.dto.TransactionResponseDto
import com.qonto.transactionviewer.data.remote.error.ApiError
import com.qonto.transactionviewer.data.remote.service.TransactionService
import androidx.room.withTransaction
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class TransactionRepositoryImplTest {

    private val transactionDao: TransactionDao = mockk(relaxed = true)
    private val remoteKeyDao: RemoteKeyDao = mockk(relaxed = true)
    private val database: AppDatabase = mockk()
    private val service: TransactionService = mockk()
    private val repository = TransactionRepositoryImpl(service, database)

    @Before
    fun setUp() {
        every { database.transactionDao() } returns transactionDao
        every { database.remoteKeyDao() } returns remoteKeyDao
        mockkStatic("androidx.room.RoomDatabaseKt")
        //coEvery { database.withTransaction<Unit>(any()) } just Runs
        coEvery {
            database.withTransaction(any<suspend () -> Unit>())
        } coAnswers {
            secondArg<suspend () -> Unit>().invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // region refresh

    @Test
    fun `refresh calls service with page 1 and null seed and returns Success`() = runTest {
        coEvery { service.getTransactions(any(), any(), any()) } returns makeResponse()
        val result = repository.refresh(pagingState())

        assertTrue(result is MediatorResult.Success)
        assertFalse((result as MediatorResult.Success).endOfPaginationReached)
        coVerify { transactionDao.clearAll() }
        coVerify { transactionDao.insertAll(any()) }
        coVerify { remoteKeyDao.clearAll() }
        coVerify { remoteKeyDao.insert(RemoteKeyEntity(seed = "seed-1", nextPage = 2)) }
    }

    @Test
    fun `refresh always calls service with seed null`() = runTest {
        coEvery { service.getTransactions(any(), any(), any()) } returns makeResponse()

        repository.refresh(pagingState())
        coVerify { service.getTransactions(any(), page = 1, seed = null) }
    }

    @Test
    fun `refresh maps IOException to MediatorResult Error with NetworkError`() = runTest {
        coEvery { service.getTransactions(any(), any(), any()) } throws IOException()

        val result = repository.refresh(pagingState())

        assertTrue(result is MediatorResult.Error)
        assertTrue((result as MediatorResult.Error).throwable is ApiError.NetworkError)
    }

    @Test
    fun `refresh maps HttpException to MediatorResult Error with HttpError`() = runTest {
        val exception = mockk<HttpException> { every { code() } returns 500 }
        coEvery { service.getTransactions(any(), any(), any()) } throws exception

        val result = repository.refresh(pagingState())

        assertTrue(result is MediatorResult.Error)
        val error = (result as MediatorResult.Error).throwable
        assertTrue(error is ApiError.HttpError)
        assertEquals(500, (error as ApiError.HttpError).code)
    }

    // endregion

    // region append

    @Test
    fun `append returns endOfPaginationReached when no remote key exists`() = runTest {
        coEvery { remoteKeyDao.get() } returns null

        val result = repository.append(pagingState(anchorPosition = 0))

        assertTrue(result is MediatorResult.Success)
        assertTrue((result as MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { service.getTransactions(any(), any(), any()) }
    }

    @Test
    fun `append returns endOfPaginationReached when nextPage exceeds MAX_PAGE`() = runTest {
        coEvery { remoteKeyDao.get() } returns RemoteKeyEntity(seed = "abc", nextPage = 10_001)

        val result = repository.append(pagingState(anchorPosition = 0))

        assertTrue(result is MediatorResult.Success)
        assertTrue((result as MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { service.getTransactions(any(), any(), any()) }
    }

    @Test
    fun `append does not terminate at exactly MAX_PAGE`() = runTest {
        coEvery { remoteKeyDao.get() } returns RemoteKeyEntity(seed = "abc", nextPage = 10_000)
        coEvery { service.getTransactions(any(), any(), any()) } returns makeResponse(seed = "abc")

        val result = repository.append(pagingState(anchorPosition = 0))

        assertTrue(result is MediatorResult.Success)
        assertFalse((result as MediatorResult.Success).endOfPaginationReached)
        coVerify { service.getTransactions(any(), page = 10_000, seed = "abc") }
    }

    @Test
    fun `append inserts transactions, increments nextPage, returns success`() = runTest {
        val remoteKey = RemoteKeyEntity(seed = "abc", nextPage = 3)
        coEvery { remoteKeyDao.get() } returns remoteKey
        coEvery { service.getTransactions(any(), any(), any()) } returns makeResponse(seed = "abc")

        val result = repository.append(pagingState(anchorPosition = 0))

        assertTrue(result is MediatorResult.Success)
        assertFalse((result as MediatorResult.Success).endOfPaginationReached)
        coVerify { service.getTransactions(any(), page = 3, seed = "abc") }
        coVerify { transactionDao.insertAll(any()) }
        coVerify { remoteKeyDao.insert(RemoteKeyEntity(seed = "abc", nextPage = 4)) }
    }

    @Test
    fun `append maps IOException to MediatorResult Error with NetworkError`() = runTest {
        coEvery { remoteKeyDao.get() } returns RemoteKeyEntity(seed = "abc", nextPage = 2)
        coEvery { service.getTransactions(any(), any(), any()) } throws IOException()

        val result = repository.append(pagingState(anchorPosition = 0))

        assertTrue(result is MediatorResult.Error)
        assertTrue((result as MediatorResult.Error).throwable is ApiError.NetworkError)
    }

    @Test
    fun `append maps HttpException to MediatorResult Error with HttpError`() = runTest {
        val exception = mockk<HttpException> { every { code() } returns 503 }
        coEvery { remoteKeyDao.get() } returns RemoteKeyEntity(seed = "abc", nextPage = 2)
        coEvery { service.getTransactions(any(), any(), any()) } throws exception

        val result = repository.append(pagingState(anchorPosition = 0))

        assertTrue(result is MediatorResult.Error)
        val error = (result as MediatorResult.Error).throwable
        assertTrue(error is ApiError.HttpError)
        assertEquals(503, (error as ApiError.HttpError).code)
    }

    // endregion

    // region helpers

    private fun pagingState(anchorPosition: Int? = null) = PagingState<Int, TransactionEntity>(
        pages = emptyList(),
        anchorPosition = anchorPosition,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0,
    )

    private fun makeResponse(seed: String = "seed-1"): TransactionResponseDto {
        val dto = TransactionDto(
            id = "id-1",
            amount = AmountDto("100.00", "EUR"),
            counterpartyName = "Test",
            emittedAt = "2025-01-01T10:00:00.000Z",
            settledAt = null,
            side = "DEBIT",
            status = "PENDING",
            operationMethod = "TRANSFER",
            operationType = "TRANSFER",
            description = "Desc",
            activityTag = "FEES",
            note = null,
            initiator = null,
            bankAccount = BankAccountDto("bank-id", "Account"),
        )
        return TransactionResponseDto(
            results = listOf(dto),
            info = PaginationInfoDto(seed = seed, results = 1, page = 1, version = "1.0"),
        )
    }

    // endregion
}
