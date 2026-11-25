package com.mojdzien.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mojdzien.app.data.Goal
import com.mojdzien.app.data.Habit
import com.mojdzien.app.data.Note
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val PL_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))
val PL_DATE_SHORT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("pl"))

fun LocalDate.toMillisUtc(): Long =
    this.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()

@Composable
fun SectionHeader(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

@Composable
fun DateNavigator(selected: LocalDate, label: String, onChange: (LocalDate) -> Unit, onToday: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(selected.minusDays(1)) }) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Poprzedni dzień")
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = { onChange(selected.plusDays(1)) }) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Następny dzień")
        }
        TextButton(onClick = onToday) { Text("Dziś") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(date: LocalDate, onPicked: (LocalDate) -> Unit) {
    var open by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { open = true }) {
        Text(date.format(PL_DATE))
    }

    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date.toMillisUtc())
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onPicked(it.toLocalDateUtc()) }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDialog(
    initial: Goal?,
    defaultDate: LocalDate,
    isSubgoal: Boolean,
    onSave: (title: String, note: String, date: String, done: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var date by remember { mutableStateOf(initial?.date?.let(LocalDate::parse) ?: defaultDate) }
    var done by remember { mutableStateOf(initial?.done ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) if (isSubgoal) "Nowy podcel" else "Nowy cel" else "Edytuj cel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nazwa") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Uwagi / opis") }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Data: ", style = MaterialTheme.typography.bodyMedium)
                    DatePickerButton(date = date, onPicked = { date = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = done, onCheckedChange = { done = it })
                    Text("Zrealizowany")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onSave(title.trim(), note.trim(), date.toString(), done) }
            ) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

private val HABIT_COLORS = listOf(
    0xFF4CAF50, 0xFF2196F3, 0xFF9C27B0, 0xFFFF9800,
    0xFFF44336, 0xFF00BCD4, 0xFF795548, 0xFF607D8B
)

private val DAY_NAMES = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDialog(
    initial: Habit?,
    onSave: (name: String, emoji: String, colorArgb: Long, daysOfWeek: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var emoji by remember { mutableStateOf(initial?.emoji ?: "✅") }
    var color by remember { mutableStateOf(initial?.colorArgb ?: 0xFF4CAF50) }
    var days by remember { mutableStateOf(initial?.daysOfWeek ?: "1111111") }
    var note by remember { mutableStateOf(initial?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nowy nawyk" else "Edytuj nawyk") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa nawyku") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Ikona (emoji)") },
                    singleLine = true
                )
                Text("Kolor:", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HABIT_COLORS.forEach { c ->
                        val col = Color(c.toInt())
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (color == c) 3.dp else 1.dp,
                                    color = if (color == c) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable { color = c }
                        )
                    }
                }
                Text("Dni:", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DAY_NAMES.forEachIndexed { index, dayName ->
                        val active = days.getOrNull(index) == '1'
                        FilterChip(
                            selected = active,
                            onClick = {
                                val chars = days.padEnd(7, '0').toCharArray()
                                chars[index] = if (active) '0' else '1'
                                days = String(chars)
                            },
                            label = { Text(dayName) }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Opis / zasada nawyku") }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), emoji.trim(), color, days.padEnd(7, '0'), note.trim()) }
            ) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    initial: Note?,
    defaultDate: LocalDate,
    onSave: (content: String, date: String) -> Unit,
    onDismiss: () -> Unit
) {
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var date by remember { mutableStateOf(initial?.date?.let(LocalDate::parse) ?: defaultDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nowa uwaga" else "Edytuj uwagę") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Treść") },
                    minLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Data: ", style = MaterialTheme.typography.bodyMedium)
                    DatePickerButton(date = date, onPicked = { date = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = content.isNotBlank(),
                onClick = { onSave(content.trim(), date.toString()) }
            ) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}
