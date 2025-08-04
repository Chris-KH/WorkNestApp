package com.apcs.worknestapp.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.apcs.worknestapp.ui.theme.Roboto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    state: TimePickerState,
    onDismissRequest: () -> Unit,
    onConfirm: (TimePickerState) -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = state.hour,
        initialMinute = state.minute,
        is24Hour = state.is24hour,
    )

    val buttonTextStyle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = Color.Unspecified,
                ),
                onClick = { onConfirm(timePickerState) },
            ) {
                Text(
                    text = "OK",
                    style = buttonTextStyle,
                )
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    disabledContentColor = Color.Unspecified,
                ),
                onClick = onDismissRequest
            ) {
                Text(
                    text = "Cancel",
                    style = buttonTextStyle,
                )
            }
        },
        title = { Text("Select Time") },
        text = {
            TimeInput(state = timePickerState)
        },
        properties = DialogProperties(dismissOnClickOutside = true)
    )
}
