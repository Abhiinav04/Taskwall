package com.abhinav.taskwall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.abhinav.taskwall.data.AppPreferences
import com.abhinav.taskwall.data.QuoteRepository
import com.abhinav.taskwall.data.TaskRepository
import com.abhinav.taskwall.data.TaskWallDatabase
import com.abhinav.taskwall.ui.TaskViewModel
import com.abhinav.taskwall.ui.TaskViewModelFactory
import com.abhinav.taskwall.ui.TaskWallApp
import com.abhinav.taskwall.ui.theme.TaskWallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TaskWallDatabase.getDatabase(this)
        val taskRepository = TaskRepository(database.taskDao())
        val quoteRepository = QuoteRepository(database.quoteDao())
        val appPreferences = AppPreferences(this)

        val factory = TaskViewModelFactory(taskRepository, quoteRepository, appPreferences)
        val viewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        val openAddTask = intent.getBooleanExtra("OPEN_ADD_TASK", false)

        setContent {
            TaskWallTheme {
                TaskWallApp(viewModel = viewModel, openAddTask = openAddTask)
            }
        }
    }
}
