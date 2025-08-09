package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.components.TimePickerDialog
import com.apcs.worknestapp.ui.theme.Roboto
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerModal(
    title: String,
    currentDate: Timestamp?,
    onSave: (Timestamp?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val initialDate = currentDate?.toDate() ?: Date()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time,
        initialDisplayMode = DisplayMode.Picker
    )

    var showTimePickerDialog by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance().apply { time = initialDate }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            it != SheetValue.Hidden
        }
    )


    ModalBottomSheet(
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(12.dp),
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                onDismissRequest()
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            NoteModalBottomTopBar(
                title = title,
                onClose = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                },
                onSave = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis == null) onSave(null)
                    else {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val selectedTimestamp = Timestamp(calendar.time)
                        onSave(selectedTimestamp)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            )
            HorizontalDivider(
                thickness = (0.75).dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.primary,
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            ) {
                Text(
                    text = "Time",
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(10f)
                        )
                        .clip(RoundedCornerShape(10f))
                        .clickable(onClick = { showTimePickerDialog = true })
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${
                            timePickerState.hour.toString().padStart(2, '0')
                        }:${timePickerState.minute.toString().padStart(2, '0')}",
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (showTimePickerDialog) {
                TimePickerDialog(
                    state = timePickerState,
                    onDismissRequest = { showTimePickerDialog = false },
                    onConfirm = {
                        timePickerState.hour = it.hour
                        timePickerState.minute = it.minute
                        showTimePickerDialog = false
                    },
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    disabledContentColor = Color.Unspecified,
                ),
                onClick = {
                    onSave(null)
                    onDismissRequest()
                }
            ) {
                Text(
                    text = "Clear date",
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
