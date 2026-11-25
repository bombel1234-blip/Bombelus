package com.mojdzien.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.mojdzien.app.MainViewModel

enum class Screen(val label: String, val icon: ImageVector) {
    TODAY("Dziś", Icons.Filled.Home),
    GOALS("Cele", Icons.Filled.Done),
    HABITS("Nawyki", Icons.Filled.Star),
    NOTES("Uwagi", Icons.Filled.Create)
}

@Composable
fun MainRoot(vm: MainViewModel) {
    var screen by rememberSaveable { mutableStateOf(Screen.TODAY.name) }
    val current = Screen.valueOf(screen)

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { s ->
                    NavigationBarItem(
                        selected = s == current,
                        onClick = { screen = s.name },
                        icon = { Icon(s.icon, contentDescription = s.label) },
                        label = { Text(s.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (current) {
                Screen.TODAY -> TodayScreen(vm)
                Screen.GOALS -> GoalsScreen(vm)
                Screen.HABITS -> HabitsScreen(vm)
                Screen.NOTES -> NotesScreen(vm)
            }
        }
    }
}
