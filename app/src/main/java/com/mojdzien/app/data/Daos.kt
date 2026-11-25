package com.mojdzien.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY date DESC, id DESC")
    fun all(): Flow<List<Goal>>

    @Insert
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("DELETE FROM goals WHERE parentId = :parentId")
    suspend fun deleteSubgoals(parentId: Long)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY archived ASC, name COLLATE NOCASE ASC")
    fun all(): Flow<List<Habit>>

    @Insert
    suspend fun insert(habit: Habit)

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs ORDER BY date DESC")
    fun all(): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun find(habitId: Long, date: String): HabitLog?

    @Insert
    suspend fun insert(log: HabitLog)

    @Update
    suspend fun update(log: HabitLog)

    @Delete
    suspend fun delete(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteForHabit(habitId: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY date DESC, createdAt DESC")
    fun all(): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}
