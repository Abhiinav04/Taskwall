package com.abhinav.taskwall.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhinav.taskwall.data.AppPreferences
import com.abhinav.taskwall.data.Quote
import com.abhinav.taskwall.data.QuoteRepository
import com.abhinav.taskwall.data.Task
import com.abhinav.taskwall.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val quoteRepository: QuoteRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val activeTasks: StateFlow<List<Task>> = taskRepository.getActiveTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<Task>> = taskRepository.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuotes: StateFlow<List<Quote>> = quoteRepository.getAllQuotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            quoteRepository.populateInitialQuotesIfEmpty()
        }
    }

    fun addTask(title: String, notes: String? = null, targetDate: Long? = null) {
        viewModelScope.launch {
            taskRepository.insertTask(title, notes, targetDate)
        }
    }

    fun completeTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.completeTask(taskId)
        }
    }

    fun shiftTaskTargetDate(taskId: Long, targetDate: Long?) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            if (task != null) {
                taskRepository.updateTask(task.copy(targetDate = targetDate))
            }
        }
    }

    val is24Hour: StateFlow<Boolean> = appPreferences.is24Hour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showSeconds: StateFlow<Boolean> = appPreferences.showSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun set24Hour(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.set24Hour(enabled)
        }
    }

    fun setShowSeconds(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setShowSeconds(enabled)
        }
    }

    fun searchTasks(query: String): StateFlow<List<Task>> {
        return taskRepository.searchTasks(query).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getCompletedTasks(dateStart: Long, dateEnd: Long): StateFlow<List<Task>> {
        return taskRepository.getCompletedTasks(dateStart, dateEnd).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}

class TaskViewModelFactory(
    private val taskRepository: TaskRepository,
    private val quoteRepository: QuoteRepository,
    private val appPreferences: AppPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskRepository, quoteRepository, appPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
