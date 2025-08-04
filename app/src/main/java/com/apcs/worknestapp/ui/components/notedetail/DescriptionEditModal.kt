package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionEditModal(
    currentDescription: String?,
    onSave: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var description by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentDescription ?: "",
                selection = TextRange((currentDescription ?: "").length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        dragHandle = null,
        shape = RoundedCornerShape(12.dp),
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
    ) {
        NoteModalBottomTopBar(
            title = "Description",
            onClose = onDismissRequest,
            onSave = { onSave(description.text) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )
        CustomTextField(
            value = description,
            onValueChange = { description = it },
            textStyle = TextStyle(
                fontSize = 15.sp,
                lineHeight = 16.sp,
                letterSpacing = (0.25).sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            ),
            containerColor = Color.Transparent,
            modifier = Modifier
                .focusRequester(focusRequester)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}
