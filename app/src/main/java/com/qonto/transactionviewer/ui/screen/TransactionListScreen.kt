package com.qonto.transactionviewer.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.qonto.transactionviewer.ui.theme.CreditGreen
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.qonto.transactionviewer.R
import com.qonto.transactionviewer.domain.model.Transaction
import com.qonto.transactionviewer.domain.model.TransactionSide
import com.qonto.transactionviewer.ui.components.AppListItem
import com.qonto.transactionviewer.ui.components.AppListItemPlaceholder
import com.qonto.transactionviewer.ui.utils.AmountFormatter
import com.qonto.transactionviewer.ui.utils.DateFormatter
import com.qonto.transactionviewer.ui.viewmodel.TransactionListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: TransactionListViewModel = koinViewModel(),
) {
    val pagingItems = viewModel.transactions.collectAsLazyPagingItems()

    Box(modifier = modifier.fillMaxSize()) {
        when (val refresh = pagingItems.loadState.refresh) {
            is LoadState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is LoadState.Error -> ErrorView(
                message = refresh.error.localizedMessage ?: stringResource(R.string.error_something_went_wrong),
                onRetry = pagingItems::retry,
                modifier = Modifier.align(Alignment.Center),
            )
            is LoadState.NotLoading -> TransactionList(
                pagingItems = pagingItems,
                contentPadding = contentPadding,
            )
        }
    }
}

@Composable
private fun TransactionList(
    pagingItems: LazyPagingItems<Transaction>,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id },
        ) { index ->
            val transaction = pagingItems[index]
            if (transaction != null) {
                TransactionItem(transaction = transaction)
            } else {
                AppListItemPlaceholder()
            }
            HorizontalDivider()
        }
        when (val append = pagingItems.loadState.append) {
            is LoadState.Loading -> item { AppendLoadingItem() }
            is LoadState.Error -> item {
                AppendErrorItem(
                    message = append.error.localizedMessage ?: stringResource(R.string.error_failed_to_load_more),
                    onRetry = pagingItems::retry,
                )
            }
            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    AppListItem(
        primaryText = transaction.counterpartyName,
        trailingPrimaryText = AmountFormatter.formatAmount(transaction.amount),
        secondaryText = transaction.settledAt?.let { DateFormatter.formatSettledAt(it) }
            ?: stringResource(R.string.label_settled_at_empty),
        trailingSecondaryText = transaction.status.name,
        trailingPrimaryColor = if (transaction.side == TransactionSide.CREDIT) CreditGreen else Color.Unspecified,
    )
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.label_retry)) }
    }
}

@Composable
private fun AppendLoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AppendErrorItem(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.label_retry)) }
    }
}
