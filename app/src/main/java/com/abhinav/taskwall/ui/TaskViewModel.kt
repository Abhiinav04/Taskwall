package com.abhinav.taskwall.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhinav.taskwall.data.AppPreferences
import com.abhinav.taskwall.data.Quote
import com.abhinav.taskwall.data.QuoteRepository
import com.abhinav.taskwall.data.Task
import com.abhinav.taskwall.data.SubTask
import com.abhinav.taskwall.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
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
            
            // Removed dummy task population for production use
        }
    }

    fun addTask(title: String, notes: String? = null, targetDate: Long? = null, color: Long? = null, recurrence: String? = null, showOnWallpaper: Boolean = true) {
        viewModelScope.launch {
            taskRepository.insertTask(title, notes, targetDate, color, recurrence, showOnWallpaper)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }
    }

    fun completeTask(taskId: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            taskRepository.completeTask(taskId)
            
            // Recurrence logic
            if (task != null && task.recurrence != null) {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                
                when (task.recurrence) {
                    "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    "WEEKLY" -> cal.add(Calendar.DAY_OF_YEAR, 7)
                }
                
                taskRepository.insertTask(
                    title = task.title,
                    notes = task.notes,
                    targetDate = cal.timeInMillis,
                    color = task.color,
                    recurrence = task.recurrence,
                    showOnWallpaper = task.showOnWallpaper
                )
            }
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

    fun moveTask(task: Task, moveUp: Boolean, currentList: List<Task>) {
        viewModelScope.launch {
            val index = currentList.indexOfFirst { it.id == task.id }
            if (index == -1) return@launch
            
            val targetIndex = if (moveUp) index - 1 else index + 1
            if (targetIndex in currentList.indices) {
                val mutableList = currentList.mapIndexed { i, t -> t.copy(displayOrder = i) }.toMutableList()
                val temp = mutableList[index]
                mutableList[index] = mutableList[targetIndex].copy(displayOrder = index)
                mutableList[targetIndex] = temp.copy(displayOrder = targetIndex)
                
                // Update the whole list to ensure sequence is maintained
                taskRepository.updateTasks(mutableList)
            }
        }
    }

    // Preferences & Settings
    val is24Hour: StateFlow<Boolean> = appPreferences.is24Hour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showSeconds: StateFlow<Boolean> = appPreferences.showSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val fontFamily: StateFlow<String?> = appPreferences.fontFamily
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeColor: StateFlow<Long?> = appPreferences.themeColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val particleEffectsEnabled: StateFlow<Boolean> = appPreferences.particleEffectsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val activePomodoroTaskId: StateFlow<Long?> = appPreferences.activePomodoroTaskId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pomodoroEndTime: StateFlow<Long?> = appPreferences.pomodoroEndTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun set24Hour(enabled: Boolean) {
        viewModelScope.launch { appPreferences.set24Hour(enabled) }
    }

    fun setShowSeconds(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setShowSeconds(enabled) }
    }

    fun setFontFamily(font: String?) {
        viewModelScope.launch { appPreferences.setFontFamily(font) }
    }

    fun setThemeColor(color: Long?) {
        viewModelScope.launch { appPreferences.setThemeColor(color) }
    }

    fun setParticleEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setParticleEffectsEnabled(enabled) }
    }

    fun startPomodoro(taskId: Long, durationMinutes: Int) {
        viewModelScope.launch { 
            val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            appPreferences.setPomodoroState(taskId, endTime)
        }
    }

    fun stopPomodoro() {
        viewModelScope.launch { appPreferences.setPomodoroState(null, null) }
    }

    fun searchTasks(query: String): StateFlow<List<Task>> {
        return taskRepository.searchTasks(query).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getCompletedTasks(dateStart: Long, dateEnd: Long): StateFlow<List<Task>> {
        return taskRepository.getCompletedTasks(dateStart, dateEnd).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // SubTask Operations
    fun getSubTasksForTask(taskId: Long): StateFlow<List<SubTask>> {
        return taskRepository.getSubTasksForTask(taskId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addSubTask(taskId: Long, title: String) {
        viewModelScope.launch { taskRepository.addSubTask(taskId, title) }
    }

    fun toggleSubTaskCompletion(subTask: SubTask) {
        viewModelScope.launch { taskRepository.updateSubTask(subTask.copy(isCompleted = !subTask.isCompleted)) }
    }

    fun deleteSubTask(subTaskId: Long) {
        viewModelScope.launch { taskRepository.deleteSubTask(subTaskId) }
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
