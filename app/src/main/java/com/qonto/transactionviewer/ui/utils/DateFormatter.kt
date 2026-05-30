package com.qonto.transactionviewer.ui.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DateFormatter {
    companion object {
        private const val INPUT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        private const val OUTPUT_FORMAT = "dd MMM yyyy"
        private const val UTC = "UTC"

        fun formatSettledAt(isoDate: String): String? =
            SimpleDateFormat(INPUT_FORMAT, Locale.US).apply {
                timeZone = TimeZone.getTimeZone(UTC)
            }.parse(isoDate)?.let { parsed ->
                SimpleDateFormat(OUTPUT_FORMAT, Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone(UTC)
                }.format(parsed)
            }
    }
}
