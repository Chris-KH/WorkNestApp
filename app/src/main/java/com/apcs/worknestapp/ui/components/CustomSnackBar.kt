package com.apcs.worknestapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.runtime.Composable
import kotlin.text.contains

enum class SnackbarType {
    SUCCESS,
    ERROR,
    FAIL,
    WARNING,
    INFO
}

@Composable
fun CustomSnackBar(data: SnackbarData) {
    val type = when {
        data.visuals.message.contains("Success:", ignoreCase = true) -> SnackbarType.SUCCESS
        data.visuals.message.contains("Error:", ignoreCase = true) -> SnackbarType.ERROR
        data.visuals.message.contains("Fail:", ignoreCase = true) -> SnackbarType.FAIL
        data.visuals.message.contains("Warning:", ignoreCase = true) -> SnackbarType.WARNING
        else -> SnackbarType.INFO
    }

    val background = when (type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.secondary
        SnackbarType.ERROR -> MaterialTheme.colorScheme.error
        SnackbarType.FAIL -> MaterialTheme.colorScheme.error
        SnackbarType.WARNING -> MaterialTheme.colorScheme.tertiary
        SnackbarType.INFO -> SnackbarDefaults.color
    }

    val contentColor = when (type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.onSecondary
        SnackbarType.ERROR -> MaterialTheme.colorScheme.onError
        SnackbarType.FAIL -> MaterialTheme.colorScheme.onError
        SnackbarType.WARNING -> MaterialTheme.colorScheme.onTertiary
        SnackbarType.INFO -> SnackbarDefaults.contentColor
    }

    val actionColor = when (type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.onSecondary
        SnackbarType.ERROR -> MaterialTheme.colorScheme.onError
        SnackbarType.FAIL -> MaterialTheme.colorScheme.onError
        SnackbarType.WARNING -> MaterialTheme.colorScheme.onTertiary
        SnackbarType.INFO -> SnackbarDefaults.actionColor
    }

    Snackbar(
        snackbarData = data,
        containerColor = background,
        contentColor = contentColor,
        actionColor = actionColor
    )
}
