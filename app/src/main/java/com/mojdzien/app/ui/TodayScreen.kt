package com.mojdzien.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mojdzien.app.MainViewModel
import com.mojdzien.app.data.Goal
import com.mojdzien.app.data.Habit
import com.mojdzien.app.data.Note
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TODAY_HEADER_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(vm: MainViewModel) {
    var selectedStr by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val selected = remember(selectedStr) { LocalDate.parse(selectedStr) }

    val goals by vm.allGoals.collectAsState()
    val habits by vm.habits.collectAsState()
    val logs by vm.logs.collectAsState()
    val notes by vm.notes.collectAsState()

    var showNewGoal by remember { mutableStateOf(false) }
    var editGoal by remember { mutableStateOf<Goal?>(null) }
    var subgoalFor by remember { mutableStateOf<Goal?>(null) }
    var realizacjaFor by remember { mutableStateOf<Habit?>(null) }
    var editNote by remember { mutableStateOf<Note?>(null) }
    var quickNote by remember { mutableStateOf("") }

    val dayGoals = goals.filter { it.date == selectedStr && it.parentId == null }
    val subgoalsByParent = goals.filter { it.date == selectedStr && it.parentId != null }
        .groupBy { it.parentId }
    val weekdayIdx = selected.dayOfWeek.value - 1
    val dayHabits = habits.filter { !it.archived && it.daysOfWeek.getOrNull(weekdayIdx) == '1' }
    val logForDay = logs.filter { it.date == selectedStr }.associateBy { it.habitId }
    val dayNotes = notes.filter { it.date == selectedStr }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selected == LocalDate.now()) "Dziś" else "Dzień") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            DateNavigator(
                selected = selected,
                label = selected.format(TODAY_HEADER_FORMAT)
                    .replaceFirstChar { it.uppercase() },
                onChange = { selectedStr = it.toString() },
                onToday = { selectedStr = LocalDate.now().toString() }
            )

            Spacer(Modifier.height(8.dp))
            SectionHeader("Cele dnia") {
                TextButton(onClick = { showNewGoal = true }) { Text("+ Dodaj") }
            }
            if (dayGoals.isEmpty()) EmptyHint("Brak celów na ten dzień.")
            dayGoals.forEach { goal ->
                GoalRow(
                    goal = goal,
                    subgoals = subgoalsByParent[goal.id] ?: emptyList(),
                    showDate = false,
                    onToggle = { vm.setGoalDone(goal, it) },
                    onEdit = { editGoal = goal },
                    onDelete = { vm.deleteGoal(goal) },
                    onToggleSub = { sub -> vm.setGoalDone(sub, !sub.done) },
                    onAddSub = { subgoalFor = goal }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Nawyki")
            if (dayHabits.isEmpty()) EmptyHint("Brak nawyków zaplanowanych na ten dzień.")
            dayHabits.forEach { habit ->
                val log = logForDay[habit.id]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = log?.done == true,
                        onCheckedChange = { vm.toggleHabitForDate(habit.id, selectedStr) }
                    )
                    Text(habit.emoji)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            habit.name,
                            textDecoration = if (log?.done == true) TextDecoration.LineThrough else null
                        )
                        if (!log?.note.isNullOrBlank()) {
                            Text(
                                "📝 " + (log?.note ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(onClick = { realizacjaFor = habit }) { Text("Realizacja") }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Uwagi")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = quickNote,
                    onValueChange = { quickNote = it },
                    label = { Text("Nowa uwaga na ten dzień") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (quickNote.isNotBlank()) {
                            vm.addNote(selectedStr, quickNote.trim())
                            quickNote = ""
                        }
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Dodaj uwagę")
                }
            }
            if (dayNotes.isEmpty()) EmptyHint("Brak uwag na ten dzień.")
            dayNotes.forEach { note ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(note.content, modifier = Modifier.weight(1f))
                    IconButton(onClick = { editNote = note }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edytuj")
                    }
                    IconButton(onClick = { vm.deleteNote(note) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Usuń")
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showNewGoal) {
        GoalDialog(
            initial = null,
            defaultDate = selected,
            isSubgoal = false,
            onSave = { title, noteText, date, done ->
                vm.addGoal(date, title, noteText, done = done)
                showNewGoal = false
            },
            onDismiss = { showNewGoal = false }
        )
    }

    editGoal?.let { goal ->
        GoalDialog(
            initial = goal,
            defaultDate = selected,
            isSubgoal = false,
            onSave = { title, noteText, date, done ->
                vm.updateGoal(goal, title, noteText, date, done)
                editGoal = null
            },
            onDismiss = { editGoal = null }
        )
    }

    subgoalFor?.let { parent ->
        GoalDialog(
            initial = null,
            defaultDate = selected,
            isSubgoal = true,
            onSave = { title, noteText, date, _ ->
                vm.addGoal(date, title, noteText, parentId = parent.id)
                subgoalFor = null
            },
            onDismiss = { subgoalFor = null }
        )
    }

    realizacjaFor?.let { habit ->
        val existing = logForDay[habit.id]?.note ?: ""
        RealizacjaDialog(
            habit = habit,
            initialNote = existing,
            initialDate = selected,
            onSave = { noteText, dateStr ->
                vm.setHabitLogNote(habit.id, dateStr, noteText)
                realizacjaFor = null
            },
            onDismiss = { realizacjaFor = null }
        )
    }

    editNote?.let { note ->
        NoteDialog(
            initial = note,
            defaultDate = selected,
            onSave = { content, dateStr ->
                vm.updateNote(note, content, dateStr)
                editNote = null
            },
            onDismiss = { editNote = null }
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
internal fun GoalRow(
    goal: Goal,
    subgoals: List<Goal>,
    showDate: Boolean,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSub: (Goal) -> Unit,
    onAddSub: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = goal.done, onCheckedChange = onToggle)
        Column(Modifier.weight(1f)) {
            Text(
                goal.title,
                textDecoration = if (goal.done) TextDecoration.LineThrough else null
            )
            if (goal.note.isNotBlank()) {
                Text(
                    goal.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showDate) {
                Text(
                    goal.date.formatPlDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Edytuj")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Usuń")
        }
    }
    subgoals.forEach { sub ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 36.dp)
        ) {
            Checkbox(
                checked = sub.done,
                onCheckedChange = { onToggleSub(sub) }
            )
            Text(
                sub.title,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (sub.done) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onToggleSub(sub) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edytuj podcel")
            }
        }
    }
    TextButton(onClick = onAddSub, modifier = Modifier.padding(start = 24.dp)) {
        Text("+ Podcel")
    }
}

@Composable
private fun RealizacjaDialog(
    habit: Habit,
    initialNote: String,
    initialDate: LocalDate,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf(initialNote) }
    var date by remember { mutableStateOf(initialDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Realizacja: ${habit.emoji} ${habit.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Jak poszło? Notatka z realizacji") },
                    minLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Data: ", style = MaterialTheme.typography.bodyMedium)
                    DatePickerButton(date = date, onPicked = { date = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(noteText.trim(), date.toString()) }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

fun String.formatPlDate(): String = runCatching {
    LocalDate.parse(this).format(PL_DATE)
}.getOrDefault(this)
