package com.qonto.transactionviewer.ui.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qonto.transactionviewer.R
import com.qonto.transactionviewer.data.remote.error.ApiError

@Composable
fun Throwable?.toUserMessage(@StringRes fallback: Int = R.string.error_something_went_wrong): String =
    when (this) {
        is ApiError.NetworkError -> stringResource(R.string.error_no_internet)
        is ApiError.HttpError -> stringResource(R.string.error_server)
        else -> stringResource(fallback)
    }
