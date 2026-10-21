package com.example.sanjutools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanjutools.data.*
import com.example.sanjutools.model.ToolCategory
import com.example.sanjutools.model.ToolItem
import com.example.sanjutools.model.ToolRegistry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val noteDao = db.noteDao()
    private val taskDao = db.taskDao()
    private val expenseDao = db.expenseDao()
    private val toolHistoryDao = db.toolHistoryDao()
    private val favoriteDao = db.favoriteDao()
    private val suggestionDao = db.suggestionDao()

    // UI Navigation & Filters
    private val _selectedCategory = MutableStateFlow(ToolCategory.ALL)
    val selectedCategory: StateFlow<ToolCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeToolId = MutableStateFlow<String?>(null)
    val activeToolId: StateFlow<String?> = _activeToolId.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Room Database Flows
    val notes: StateFlow<List<NoteEntity>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<ToolHistoryEntity>> = toolHistoryDao.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteIds: StateFlow<List<String>> = favoriteDao.getFavoriteToolIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestions: StateFlow<List<SuggestionEntity>> = suggestionDao.getAllSuggestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Tools
    val filteredTools: StateFlow<List<ToolItem>> = combine(
        _selectedCategory,
        _searchQuery,
        favoriteIds
    ) { category, query, _ ->
        val baseList = if (category == ToolCategory.ALL) {
            ToolRegistry.tools
        } else {
            ToolRegistry.getToolsByCategory(category)
        }

        if (query.isBlank()) {
            baseList
        } else {
            val q = query.trim().lowercase()
            baseList.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.title.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ToolRegistry.tools)

    fun selectCategory(category: ToolCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openTool(toolId: String) {
        _activeToolId.value = toolId
        val tool = ToolRegistry.getToolById(toolId)
        if (tool != null) {
            viewModelScope.launch {
                toolHistoryDao.insertHistory(
                    ToolHistoryEntity(
                        toolId = tool.id,
                        toolName = tool.title,
                        summary = tool.category.title
                    )
                )
            }
        }
    }

    fun closeTool() {
        _activeToolId.value = null
    }

    fun toggleFavorite(toolId: String) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(toolId)
            if (isFav) {
                favoriteDao.removeFavorite(toolId)
            } else {
                favoriteDao.setFavorite(FavoriteEntity(toolId = toolId, isFavorite = true))
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    // Notes Actions
    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.insertNote(note)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteDao.deleteNoteById(noteId)
        }
    }

    fun togglePinNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    // Tasks Actions
    fun addTask(title: String, priority: String = "Medium", category: String = "Daily") {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insertTask(
                TaskEntity(
                    title = title.trim(),
                    priority = priority,
                    category = category
                )
            )
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskDao.deleteTaskById(taskId)
        }
    }

    // Expense Actions
    fun addExpense(title: String, amount: Double, category: String, date: String) {
        if (title.isBlank() || amount <= 0) return
        viewModelScope.launch {
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = title.trim(),
                    amount = amount,
                    category = category,
                    date = date
                )
            )
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            expenseDao.deleteExpenseById(expenseId)
        }
    }

    // Tool Suggestion Actions
    fun submitToolSuggestion(name: String, description: String, category: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            suggestionDao.insertSuggestion(
                SuggestionEntity(
                    toolName = name.trim(),
                    description = description.trim(),
                    category = category
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            toolHistoryDao.clearHistory()
        }
    }
}
