package com.abhinav.taskwall.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeTaskDao : TaskDao {
    val tasks = mutableListOf<Task>()

    override fun getActiveTasks(): Flow<List<Task>> {
        return MutableStateFlow(tasks.filter { !it.isCompleted })
    }

    override fun getCompletedTasksByDate(dateStart: Long, dateEnd: Long): Flow<List<Task>> {
        return MutableStateFlow(tasks.filter { it.isCompleted })
    }

    override fun getRecentHistory(): Flow<List<Task>> {
        return MutableStateFlow(tasks.filter { it.isCompleted })
    }

    override fun searchTasks(query: String): Flow<List<Task>> {
        return MutableStateFlow(tasks)
    }

    override suspend fun getTaskById(id: Long): Task? {
        return tasks.find { it.id == id }
    }

    override suspend fun insertTask(task: Task): Long {
        tasks.add(task)
        return task.id
    }

    override suspend fun updateTask(task: Task): Int {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
            return 1
        }
        return 0
    }

    override suspend fun completeTask(taskId: Long, completedAt: Long): Int {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(isCompleted = true, completedAt = completedAt)
            return 1
        }
        return 0
    }
}

class TaskRepositoryTest {

    private lateinit var fakeDao: FakeTaskDao
    private lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        fakeDao = FakeTaskDao()
        taskRepository = TaskRepository(fakeDao)
    }

    @Test
    fun `getActiveTasks returns active tasks from dao`() = runTest {
        fakeDao.tasks.add(Task(id = 1, title = "Active Task", isCompleted = false, createdAt = 123L))
        fakeDao.tasks.add(Task(id = 2, title = "Completed Task", isCompleted = true, createdAt = 124L))

        val result = taskRepository.getActiveTasks().first()

        assertEquals(1, result.size)
        assertEquals("Active Task", result[0].title)
    }

    @Test
    fun `insertTask adds new task to dao`() = runTest {
        taskRepository.insertTask(title = "New Task", notes = "Notes")
        
        assertEquals(1, fakeDao.tasks.size)
        val addedTask = fakeDao.tasks[0]
        assertEquals("New Task", addedTask.title)
        assertEquals("Notes", addedTask.notes)
        assertEquals(false, addedTask.isCompleted)
    }
}
