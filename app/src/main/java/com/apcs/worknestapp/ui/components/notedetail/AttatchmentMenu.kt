package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

data class Attachment(val id: String, val name: String, val type: String)
enum class AttachmentOption {
    UPLOAD_FILE,
    ADD_LINK,
    ADD_IMAGE_FROM_GALLERY,
    TAKE_PHOTO
    //TODO: Mock Class
}

@Composable
fun AttachmentOptionsDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onOptionSelected: (AttachmentOption) -> Unit
) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text("Upload File") },
            onClick = { //onOptionSelected(AttachmentOption.UPLOAD_FILE)
            },
            //leadingIcon = { Icon(Icons.Filled.AttachFile, contentDescription = "Upload File") }
        )
        DropdownMenuItem(
            text = { Text("Add Link") },
            onClick = { },
            //onOptionSelected(AttachmentOption.ADD_LINK)
            //leadingIcon = { Icon(Icons.Filled.Link, contentDescription = "Add Link") }
        )
        HorizontalDivider() // Optional: to group image options
        DropdownMenuItem(
            text = { Text("Image from Gallery") },
            onClick = { //onOptionSelected(AttachmentOption.ADD_IMAGE_FROM_GALLERY)
            },
            //leadingIcon = { Icon(Icons.Filled.Image, contentDescription = "Image from Gallery") }
        )
        DropdownMenuItem(
            text = { Text("Take Photo") },
            onClick = { //onOptionSelected(AttachmentOption.TAKE_PHOTO)
            },
            ///leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = "Take Photo") }
        )

    }
}