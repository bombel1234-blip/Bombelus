package com.mojdzien.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mojdzien.app.data.Goal
import com.mojdzien.app.data.Habit
import com.mojdzien.app.data.HabitLog
import com.mojdzien.app.data.Note
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db = (app as MyDayApp).database
    private val goalDao = db.goalDao()
    private val habitDao = db.habitDao()
    private val logDao = db.logDao()
    private val noteDao = db.noteDao()

    val allGoals: StateFlow<List<Goal>> = goalDao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val habits: StateFlow<List<Habit>> = habitDao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val logs: StateFlow<List<HabitLog>> = logDao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notes: StateFlow<List<Note>> = noteDao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addGoal(date: String, title: String, note: String, parentId: Long? = null) {
        viewModelScope.launch {
            goalDao.insert(Goal(title = title, note = note, date = date, parentId = parentId))
        }
    }

    fun updateGoal(goal: Goal, title: String, note: String, date: String, done: Boolean) {
        viewModelScope.launch {
            val result = goal.copy(title = title, note = note, date = date, done = done)
            goalDao.update(result)
        }
    }

    fun setGoalDone(goal: Goal, done: Boolean) {
        viewModelScope.launch { goalDao.update(goal.copy(done = done)) }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            if (goal.parentId == null) goalDao.deleteSubgoals(goal.id)
            goalDao.delete(goal)
        }
    }

    fun addHabit(name: String, emoji: String, colorArgb: Long, daysOfWeek: String, note: String) {
        viewModelScope.launch {
            habitDao.insert(
                Habit(
                    name = name,
                    emoji = emoji.ifBlank { "✅" },
                    colorArgb = colorArgb,
                    daysOfWeek = daysOfWeek,
                    note = note
                )
            )
        }
    }

    fun updateHabit(habit: Habit, name: String, emoji: String, colorArgb: Long, daysOfWeek: String, note: String) {
        viewModelScope.launch {
            habitDao.update(
                habit.copy(
                    name = name,
                    emoji = emoji.ifBlank { "✅" },
                    colorArgb = colorArgb,
                    daysOfWeek = daysOfWeek,
                    note = note
                )
            )
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            logDao.deleteForHabit(habit.id)
            habitDao.delete(habit)
        }
    }

    fun setHabitArchived(habit: Habit, archived: Boolean) {
        viewModelScope.launch { habitDao.update(habit.copy(archived = archived)) }
    }

    fun toggleHabitForDate(habitId: Long, date: String) {
        viewModelScope.launch {
            val log = logDao.find(habitId, date)
            if (log == null) {
                logDao.insert(HabitLog(habitId = habitId, date = date, done = true))
            } else {
                logDao.update(log.copy(done = !log.done))
            }
        }
    }

    fun setHabitLogNote(habitId: Long, date: String, logNote: String) {
        viewModelScope.launch {
            val log = logDao.find(habitId, date)
            if (log == null) {
                logDao.insert(HabitLog(habitId = habitId, date = date, done = false, note = logNote))
            } else {
                logDao.update(log.copy(note = logNote))
            }
        }
    }

    fun addNote(date: String, content: String) {
        viewModelScope.launch { noteDao.insert(Note(date = date, content = content)) }
    }

    fun updateNote(note: Note, content: String, date: String) {
        viewModelScope.launch {
            noteDao.update(
                note.copy(
                    content = content,
                    date = date,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { noteDao.delete(note) }
    }
}
