package com.abhinav.taskwall.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskRepository(private val taskDao: TaskDao) {

    fun getActiveTasksWithSubtasks(): Flow<List<TaskWithSubtasks>> {
        return taskDao.getActiveTasksWithSubtasks()
    }

    fun getActiveTasks(): Flow<List<Task>> {
        return taskDao.getActiveTasks()
    }

    fun getCompletedTasks(dateStart: Long, dateEnd: Long): Flow<List<Task>> {
        return taskDao.getCompletedTasksByDate(dateStart, dateEnd)
    }

    fun getRecentHistory(): Flow<List<Task>> {
        return taskDao.getRecentHistory()
    }

    fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(query)
    }

    suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(title: String, notes: String?, targetDate: Long? = null, color: Long? = null, recurrence: String? = null) {
        val task = Task(
            title = title,
            notes = notes,
            createdAt = System.currentTimeMillis(),
            targetDate = targetDate,
            color = color,
            recurrence = recurrence
        )
        taskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun updateTasks(tasks: List<Task>) {
        for (task in tasks) {
            taskDao.updateTask(task)
        }
    }

    suspend fun completeTask(taskId: Long) {
        taskDao.completeTask(taskId, System.currentTimeMillis())
    }

    // SubTask Operations
    fun getSubTasksForTask(taskId: Long): Flow<List<SubTask>> {
        return taskDao.getSubTasksForTask(taskId)
    }

    suspend fun addSubTask(taskId: Long, title: String) {
        taskDao.insertSubTask(SubTask(taskId = taskId, title = title))
    }

    suspend fun updateSubTask(subTask: SubTask) {
        taskDao.updateSubTask(subTask)
    }

    suspend fun deleteSubTask(subTaskId: Long) {
        taskDao.deleteSubTask(subTaskId)
    }
}
