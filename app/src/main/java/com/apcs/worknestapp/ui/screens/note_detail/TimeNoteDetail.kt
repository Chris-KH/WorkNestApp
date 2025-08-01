package com.apcs.worknestapp.ui.screens.note_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NoteScheduleCard(
    startDate: Long?,
    startHour: Int?,
    startMinute: Int?,
    onSelectStartDateClick: () -> Unit,
    onSelectStartTimeClick: () -> Unit,
    endDate: Long?,
    endHour: Int?,
    endMinute: Int?,
    onSelectEndDateClick: () -> Unit,
    onSelectEndTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Schedule", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            // --- Start Date & Time ---
            Text("Start", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onSelectStartDateClick) {
                    Text(
                        startDate?.let {
                            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(it)
                        } ?: "Select Start Date"
                    )
                }
                Button(
                    onClick = onSelectStartTimeClick,
                    enabled = startDate != null // Enable only if a start date is selected
                ) {
                    Text(
                        if (startHour != null && startMinute != null) {
                            String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute)
                        } else {
                            "Select Time"
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- End Date & Time ---
            Text("End", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onSelectEndDateClick) {
                    Text(
                        endDate?.let {
                            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(it)
                        } ?: "Select End Date"
                    )
                }
                Button(
                    onClick = onSelectEndTimeClick,
                    enabled = endDate != null // Enable only if an end date is selected
                ) {
                    Text(
                        if (endHour != null && endMinute != null) {
                            String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)
                        } else {
                            "Select Time"
                        }
                    )
                }
            }
        }
    }
}