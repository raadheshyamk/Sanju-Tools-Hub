package com.example.sanjutools.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val folder: String = "General",
    val tags: String = "",
    val isPinned: Boolean = false,
    val isChecklist: Boolean = false,
    val checklistData: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val priority: String = "Medium", // High, Medium, Low
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val category: String = "Daily",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String = "Other", // Food, Transport, Utilities, Shopping, Entertainment, Other
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tool_history")
data class ToolHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolId: String,
    val toolName: String,
    val summary: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val toolId: String,
    val isFavorite: Boolean = true
)

@Entity(tableName = "suggestions")
data class SuggestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolName: String,
    val description: String,
    val category: String = "General",
    val status: String = "Under Review",
    val createdAt: Long = System.currentTimeMillis()
)
