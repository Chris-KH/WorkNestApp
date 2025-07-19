package com.apcs.worknestapp.ui.screens.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.components.topbar.TopBarNoteScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var notes by remember { mutableStateOf(emptyList<String>()) }
    var noteText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBarNoteScreen(
                navController = navController,
            )
        },
        bottomBar = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("New") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) ,
                maxLines = 2
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notes yet. Add one below!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // Allow LazyColumn to take available space
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(notes, key = { note -> note }) { note ->
                        NoteItem(
                            note = note,
                            onClick = {} //onNoteClick(note) } // implement later
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun NoteItem(
    //note: Note,
    note: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = note,
            fontSize = 18.sp, // Slightly larger font for titles
            modifier = Modifier.weight(1f)
        )
    }
}