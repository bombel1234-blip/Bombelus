package com.mojdzien.app.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mojdzien.app.MainViewModel
import com.mojdzien.app.data.Goal
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(vm: MainViewModel) {
    val goals by vm.allGoals.collectAsState()
    var filter by rememberSaveable { mutableStateOf(0) }
    var showNew by remember { mutableStateOf(false) }
    var editGoal by remember { mutableStateOf<Goal?>(null) }
    var subgoalFor by remember { mutableStateOf<Goal?>(null) }

    val roots = goals
        .filter { it.parentId == null }
        .filter {
            when (filter) {
                1 -> !it.done
                2 -> it.done
                else -> true
            }
        }
        .sortedByDescending { it.date }
    val subgoalsByParent = goals.filter { it.parentId != null }.groupBy { it.parentId }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cele") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNew = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj cel")
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filter == 0,
                    onClick = { filter = 0 },
                    label = { Text("Wszystkie") }
                )
                FilterChip(
                    selected = filter == 1,
                    onClick = { filter = 1 },
                    label = { Text("Aktywne") }
                )
                FilterChip(
                    selected = filter == 2,
                    onClick = { filter = 2 },
                    label = { Text("Zrobione") }
                )
            }
            Spacer(Modifier.height(8.dp))

            if (roots.isEmpty()) {
                Text(
                    "Brak celów. Dodaj pierwszy przyciskiem „+”.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            roots.forEach { goal ->
                GoalRow(
                    goal = goal,
                    subgoals = subgoalsByParent[goal.id] ?: emptyList(),
                    showDate = true,
                    onToggle = { vm.setGoalDone(goal, it) },
                    onEdit = { editGoal = goal },
                    onDelete = { vm.deleteGoal(goal) },
                    onToggleSub = { sub -> vm.setGoalDone(sub, !sub.done) },
                    onAddSub = { subgoalFor = goal }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
            Spacer(Modifier.height(88.dp))
        }
    }

    if (showNew) {
        GoalDialog(
            initial = null,
            defaultDate = LocalDate.now(),
            isSubgoal = false,
            onSave = { title, noteText, date, done ->
                vm.addGoal(date, title, noteText, done = done)
                showNew = false
            },
            onDismiss = { showNew = false }
        )
    }

    subgoalFor?.let { parent ->
        GoalDialog(
            initial = null,
            defaultDate = LocalDate.parse(parent.date),
            isSubgoal = true,
            onSave = { title, noteText, date, _ ->
                vm.addGoal(date, title, noteText, parentId = parent.id)
                subgoalFor = null
            },
            onDismiss = { subgoalFor = null }
        )
    }

    editGoal?.let { goal ->
        GoalDialog(
            initial = goal,
            defaultDate = LocalDate.now(),
            isSubgoal = false,
            onSave = { title, noteText, date, done ->
                vm.updateGoal(goal, title, noteText, date, done)
                editGoal = null
            },
            onDismiss = { editGoal = null }
        )
    }
}
