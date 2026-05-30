package com.qonto.transactionviewer.ui.utils

import com.qonto.transactionviewer.domain.model.Amount
import java.util.Currency

class AmountFormatter {
    companion object {
        fun formatAmount(amount: Amount): String = try {
            val symbol = Currency.getInstance(amount.currency).symbol
            "$symbol${amount.value}"
        } catch (_: IllegalArgumentException) {
            "${amount.value} ${amount.currency}"
        }
    }
}
