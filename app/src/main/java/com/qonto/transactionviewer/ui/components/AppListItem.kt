package com.qonto.transactionviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun AppListItem(
    primaryText: String,
    secondaryText: String,
    trailingPrimaryText: String,
    trailingSecondaryText: String,
    modifier: Modifier = Modifier,
    trailingPrimaryColor: Color = Color.Unspecified,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = trailingPrimaryText,
                style = MaterialTheme.typography.bodyLarge,
                color = trailingPrimaryColor,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = trailingSecondaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Loading skeleton that mirrors [AppListItem]'s layout so that row height stays identical
 * when a paging placeholder is swapped for loaded data, preventing scroll jumps.
 */
@Composable
fun AppListItemPlaceholder(modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.onSurface.copy(alpha = PLACEHOLDER_ALPHA)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PlaceholderBar(style = MaterialTheme.typography.bodyLarge, color = barColor, widthFraction = 0.5f)
            PlaceholderBar(style = MaterialTheme.typography.bodyLarge, color = barColor, widthFraction = 0.2f)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PlaceholderBar(style = MaterialTheme.typography.bodySmall, color = barColor, widthFraction = 0.3f)
            PlaceholderBar(style = MaterialTheme.typography.bodySmall, color = barColor, widthFraction = 0.15f)
        }
    }
}

/**
 * A skeleton bar that reserves the exact line height of [style] via an invisible space, so the
 * placeholder matches a real text row's height without hardcoding it. The colored bar is drawn on
 * top and its height is purely cosmetic.
 */
@Composable
private fun PlaceholderBar(style: TextStyle, color: Color, widthFraction: Float) {
    Box(
        modifier = Modifier.fillMaxWidth(widthFraction),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = " ", style = style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PLACEHOLDER_BAR_HEIGHT)
                .clip(RoundedCornerShape(PLACEHOLDER_BAR_CORNER))
                .background(color),
        )
    }
}

private const val PLACEHOLDER_ALPHA = 0.12f
private val PLACEHOLDER_BAR_HEIGHT = 12.dp
private val PLACEHOLDER_BAR_CORNER = 4.dp
