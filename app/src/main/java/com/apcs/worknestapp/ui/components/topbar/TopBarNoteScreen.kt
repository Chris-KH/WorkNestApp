package com.apcs.worknestapp.ui.components.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R

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
                        Icon(
                            painter = painterResource(R.drawable.symbol_three_dot),
                            contentDescription = "More options",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(-90f)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = onDismissMenu,
                        containerColor = MaterialTheme.colorScheme.surface,
                        shadowElevation = 32.dp,
                        shape = RoundedCornerShape(25f),
                        modifier = Modifier.widthIn(min = 160.dp),
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Edit",
                                    fontSize = 14.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            },
                            onClick = {}, //  onEditClick() },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete all",
                                    fontSize = 14.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            },
                            onClick = {},// onDeleteAllClick,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete All",
                                    modifier = Modifier.size(24.dp),
                                )
                            }
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
