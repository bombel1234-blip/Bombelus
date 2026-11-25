package com.mojdzien.app.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mojdzien.app.MainViewModel
import com.mojdzien.app.data.Habit
import com.mojdzien.app.data.HabitLog
import java.time.LocalDate

private val DAY_LABELS = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(vm: MainViewModel) {
    val habits by vm.habits.collectAsState()
    val logs by vm.logs.collectAsState()

    var showNew by remember { mutableStateOf(false) }
    var editHabit by remember { mutableStateOf<Habit?>(null) }
    var expandedId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nawyki") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNew = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj nawyk")
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
            val active = habits.filter { !it.archived }
            val archived = habits.filter { it.archived }

            if (active.isEmpty()) {
                Text(
                    "Brak nawyków. Dodaj pierwszy przyciskiem „+”.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            active.forEach { habit ->
                HabitCard(
                    habit = habit,
                    logs = logs.filter { it.habitId == habit.id },
                    expanded = expandedId == habit.id,
                    onToggleExpand = { expandedId = if (expandedId == habit.id) null else habit.id },
                    onEdit = { editHabit = habit },
                    onArchive = { vm.setHabitArchived(habit, true) },
                    onDelete = { vm.deleteHabit(habit) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            if (archived.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                SectionHeader("Zarchiwizowane")
                archived.forEach { habit ->
                    HabitCard(
                        habit = habit,
                        logs = logs.filter { it.habitId == habit.id },
                        expanded = expandedId == habit.id,
                        onToggleExpand = { expandedId = if (expandedId == habit.id) null else habit.id },
                        onEdit = { editHabit = habit },
                        onArchive = { vm.setHabitArchived(habit, false) },
                        onDelete = { vm.deleteHabit(habit) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
            Spacer(Modifier.height(88.dp))
        }
    }

    if (showNew) {
        HabitDialog(
            initial = null,
            onSave = { name, emoji, color, days, note ->
                vm.addHabit(name, emoji, color, days, note)
                showNew = false
            },
            onDismiss = { showNew = false }
        )
    }

    editHabit?.let { habit ->
        HabitDialog(
            initial = habit,
            onSave = { name, emoji, color, days, note ->
                vm.updateHabit(habit, name, emoji, color, days, note)
                editHabit = null
            },
            onDismiss = { editHabit = null }
        )
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    logs: List<HabitLog>,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val doneCount = logs.count { it.done }
    val realizations = logs.filter { it.note.isNotBlank() }

    Column(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
        ) {
            Text(habit.emoji, style = MaterialTheme.typography.headlineSmall)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(habit.name, style = MaterialTheme.typography.titleMedium)
                if (habit.note.isNotBlank()) {
                    Text(
                        habit.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    DAY_LABELS.forEachIndexed { index, label ->
                        val active = habit.daysOfWeek.getOrNull(index) == '1'
                        Text(
                            text = if (active) label else label.lowercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
                Text(
                    "🔥 ${streakOf(logs)} dni z rzędu • ✅ $doneCount×",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edytuj")
            }
            TextButton(onClick = onArchive) {
                Text(if (habit.archived) "Przywróć" else "Archiwizuj")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Usuń")
            }
        }
        if (expanded) {
            if (realizations.isEmpty()) {
                Text(
                    "Brak zapisanych realizacji. Dotknij „Realizacja” na ekranie Dziś.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                )
            } else {
                realizations.take(30).forEach { log ->
                    Text(
                        text = buildString {
                            append(log.date.formatPlDate())
                            append(" — ")
                            append(log.note)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}

private fun streakOf(logs: List<HabitLog>): Int {
    val done = logs.filter { it.done }.map { it.date }.toSet()
    var day = LocalDate.now()
    if (day.toString() !in done) day = day.minusDays(1)
    var streak = 0
    while (day.toString() in done) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}
