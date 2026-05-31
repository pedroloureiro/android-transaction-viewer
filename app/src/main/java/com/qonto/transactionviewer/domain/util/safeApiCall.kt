package com.qonto.transactionviewer.domain.util

import com.qonto.transactionviewer.data.remote.error.ApiError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
    return try {
        Result.success(call())
    } catch (e: HttpException) {
        Result.failure(ApiError.HttpError(e.code()))
    } catch (e: ApiError) {
        Result.failure(e)
    } catch (e: IOException) {
        Result.failure(ApiError.NetworkError)
    } catch (e: SerializationException) {
        Result.failure(ApiError.UnknownError)
    } catch (e: Exception) {
        Result.failure(ApiError.UnknownError)
    }
}
