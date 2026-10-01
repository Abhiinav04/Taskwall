package com.abhinav.taskwall.ui.home

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.abhinav.taskwall.data.Task
import com.abhinav.taskwall.ui.TaskViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel) {
    val allTasks by viewModel.activeTasks.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var voiceInputText by remember { mutableStateOf("") }
    var showVoiceConfirmation by remember { mutableStateOf(false) }
    
    // Calculate tomorrow ranges
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrowStart = cal.timeInMillis
    cal.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrowEnd = cal.timeInMillis
    
    val todayTasks = mutableListOf<Task>()
    val tomorrowTasks = mutableListOf<Task>()
    
    for (task in allTasks) {
        if (task.targetDate != null && task.targetDate!! >= tomorrowStart && task.targetDate!! < tomorrowEnd) {
            tomorrowTasks.add(task)
        } else {
            todayTasks.add(task)
        }
    }
    
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                voiceInputText = matches[0]
                showVoiceConfirmation = true
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(onClick = { 
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    }
                    voiceLauncher.launch(intent)
                }, modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("🎤")
                }
                FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (allTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("All clear. No tasks for today.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    if (todayTasks.isEmpty()) {
                        item {
                            Text(
                                "No tasks for today.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        items(todayTasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task, 
                                onChecked = { viewModel.completeTask(task.id) },
                                onShiftTask = { targetDate -> viewModel.shiftTaskTargetDate(task.id, targetDate) },
                                onMoveUp = { viewModel.moveTask(task, true, todayTasks) },
                                onMoveDown = { viewModel.moveTask(task, false, todayTasks) }
                            )
                        }
                    }
                    
                    if (tomorrowTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tomorrow",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        items(tomorrowTasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task, 
                                onChecked = { viewModel.completeTask(task.id) },
                                onShiftTask = { targetDate -> viewModel.shiftTaskTargetDate(task.id, targetDate) },
                                onMoveUp = { viewModel.moveTask(task, true, tomorrowTasks) },
                                onMoveDown = { viewModel.moveTask(task, false, tomorrowTasks) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var isTomorrow by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Task") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isTomorrow, onCheckedChange = { isTomorrow = it })
                        Text("For Tomorrow")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        val targetDate = if (isTomorrow) tomorrowStart else null
                        viewModel.addTask(title, notes.takeIf { it.isNotBlank() }, targetDate)
                        showAddTaskDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showVoiceConfirmation) {
        AlertDialog(
            onDismissRequest = { showVoiceConfirmation = false },
            title = { Text("Confirm Task") },
            text = { Text("Did you mean: \"$voiceInputText\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addTask(voiceInputText)
                    showVoiceConfirmation = false
                }) {
                    Text("Add for Today")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.addTask(voiceInputText, targetDate = tomorrowStart)
                    showVoiceConfirmation = false
                }) {
                    Text("Add for Tomorrow")
                }
            }
        )
    }
}

@Composable
fun TaskRow(
    task: Task, 
    onChecked: () -> Unit, 
    onShiftTask: (Long?) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onChecked() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            modifier = Modifier.weight(1f)
        )
        
        // Move Up Button
        IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Filled.ArrowDropDown, 
                contentDescription = "Move Up",
                modifier = Modifier.rotate(180f)
            )
        }
        
        // Move Down Button
        IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Move Down")
        }
        
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Shift Task")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Move to Today") },
                    onClick = {
                        onShiftTask(null)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Move to Tomorrow") },
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        onShiftTask(cal.timeInMillis)
                        showMenu = false
                    }
                )
            }
        }
    }
}
