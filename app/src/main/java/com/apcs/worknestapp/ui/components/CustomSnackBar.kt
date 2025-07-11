package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.apcs.worknestapp.ui.theme.onSuccess
import com.apcs.worknestapp.ui.theme.onWarning
import com.apcs.worknestapp.ui.theme.success
import com.apcs.worknestapp.ui.theme.warning
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
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.success
        SnackbarType.ERROR -> MaterialTheme.colorScheme.error
        SnackbarType.FAIL -> MaterialTheme.colorScheme.error
        SnackbarType.WARNING -> MaterialTheme.colorScheme.warning
        SnackbarType.INFO -> SnackbarDefaults.color
    }

    val contentColor = when (type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.onSuccess
        SnackbarType.ERROR -> MaterialTheme.colorScheme.onError
        SnackbarType.FAIL -> MaterialTheme.colorScheme.onError
        SnackbarType.WARNING -> MaterialTheme.colorScheme.onWarning
        SnackbarType.INFO -> SnackbarDefaults.contentColor
    }

    val actionColor = when (type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.onSuccess
        SnackbarType.ERROR -> MaterialTheme.colorScheme.onError
        SnackbarType.FAIL -> MaterialTheme.colorScheme.onError
        SnackbarType.WARNING -> MaterialTheme.colorScheme.onWarning
        SnackbarType.INFO -> SnackbarDefaults.actionColor
    }

    Snackbar(
        snackbarData = data,
        containerColor = background,
        contentColor = contentColor,
        actionColor = actionColor,
        dismissActionContentColor = contentColor,
        shape = RoundedCornerShape(25)
    )
}
