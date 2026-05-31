package com.qonto.transactionviewer.data.remote.error

sealed class ApiError : Exception() {
    data object NetworkError : ApiError()
    data class HttpError(val code: Int) : ApiError()
    data object UnknownError : ApiError()
}
