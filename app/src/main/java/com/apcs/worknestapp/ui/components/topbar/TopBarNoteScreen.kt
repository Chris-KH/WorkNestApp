package com.apcs.worknestapp.ui.components.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarNoteScreen(
    navController: NavHostController,
    menuExpanded: Boolean,
    onMenuClick: () -> Unit, // To toggle the menu
    onDismissMenu: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Text(
                    text = "Notes",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
            },
            actions = {
                Box {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = onDismissMenu
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {}, //  onEditClick() },
                            leadingIcon = { Icon(Icons.Default.Edit, "Edit") }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete All") },
                            onClick ={},// onDeleteAllClick,
                            leadingIcon = { Icon(Icons.Default.Delete, "Delete All") }
                        )
                        // ... more items ...
                    }
                }
            },
            expandedHeight = 52.dp,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = (0.5).dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
