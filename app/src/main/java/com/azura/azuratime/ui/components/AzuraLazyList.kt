package com.azura.azuratime.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> AzuraLazyList(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(items) { item ->
            itemContent(item)
        }
    }
}
