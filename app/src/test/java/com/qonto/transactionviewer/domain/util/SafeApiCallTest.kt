package com.qonto.transactionviewer.domain.util

import com.qonto.transactionviewer.data.remote.error.ApiError
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

class SafeApiCallTest {

    @Test
    fun `returns success when block succeeds`() = runTest {
        val result = safeApiCall { 42 }
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `maps HttpException to ApiError HttpError with correct code`() = runTest {
        val exception = mockk<HttpException> { every { code() } returns 404 }
        val result = safeApiCall<Unit> { throw exception }
        val error = result.exceptionOrNull()
        assertTrue(error is ApiError.HttpError)
        assertEquals(404, (error as ApiError.HttpError).code)
    }

    @Test
    fun `maps IOException to ApiError NetworkError`() = runTest {
        val result = safeApiCall<Unit> { throw IOException() }
        assertTrue(result.exceptionOrNull() is ApiError.NetworkError)
    }

    @Test
    fun `maps SerializationException to ApiError UnknownError`() = runTest {
        val result = safeApiCall<Unit> { throw SerializationException("bad json") }
        assertTrue(result.exceptionOrNull() is ApiError.UnknownError)
    }

    @Test
    fun `maps unknown Exception to ApiError UnknownError`() = runTest {
        val result = safeApiCall<Unit> { throw RuntimeException("unexpected") }
        assertTrue(result.exceptionOrNull() is ApiError.UnknownError)
    }

    @Test
    fun `passes through ApiError directly`() = runTest {
        val result = safeApiCall<Unit> { throw ApiError.NetworkError }
        assertTrue(result.exceptionOrNull() is ApiError.NetworkError)
    }
}
