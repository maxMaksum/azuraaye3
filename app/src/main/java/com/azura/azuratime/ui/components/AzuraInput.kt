package com.azura.azuratime.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun AzuraInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    borderColor: Color = Color(0xFF008080),
    labelColor: Color = Color(0xFF008080)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
    )
}

@Composable
fun AzuraFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    helperText: String? = null,
    modifier: Modifier = Modifier
) {
    AzuraInput(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier
    )
    // Optionally show error/helper text below
    if (isError && helperText != null) {
        Text(helperText, color = Color.Red)
    }
}
