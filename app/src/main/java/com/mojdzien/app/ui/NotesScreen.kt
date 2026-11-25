package com.mojdzien.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mojdzien.app.MainViewModel
import com.mojdzien.app.data.Note
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(vm: MainViewModel) {
    val notes by vm.notes.collectAsState()
    var showNew by remember { mutableStateOf(false) }
    var editNote by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Uwagi") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNew = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj uwagę")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (notes.isEmpty()) {
                Text(
                    "Brak uwag. Dodaj pierwszą przyciskiem „+”.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            notes.forEach { note ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            note.date.formatPlDate(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(note.content)
                    }
                    IconButton(onClick = { editNote = note }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edytuj")
                    }
                    IconButton(onClick = { vm.deleteNote(note) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Usuń")
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
            Spacer(Modifier.height(88.dp))
        }
    }

    if (showNew) {
        NoteDialog(
            initial = null,
            defaultDate = LocalDate.now(),
            onSave = { content, date ->
                vm.addNote(date, content)
                showNew = false
            },
            onDismiss = { showNew = false }
        )
    }

    editNote?.let { note ->
        NoteDialog(
            initial = note,
            defaultDate = LocalDate.now(),
            onSave = { content, date ->
                vm.updateNote(note, content, date)
                editNote = null
            },
            onDismiss = { editNote = null }
        )
    }
}
