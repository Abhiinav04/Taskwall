package com.abhinav.taskwall.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskRepository(private val taskDao: TaskDao) {

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

    suspend fun insertTask(title: String, notes: String?, targetDate: Long? = null) {
        val task = Task(
            title = title,
            notes = notes,
            createdAt = System.currentTimeMillis(),
            targetDate = targetDate
        )
        taskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun completeTask(taskId: Long) {
        taskDao.completeTask(taskId, System.currentTimeMillis())
    }
}
