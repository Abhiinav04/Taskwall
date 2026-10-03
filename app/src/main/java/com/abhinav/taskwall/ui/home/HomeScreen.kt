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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.abhinav.taskwall.data.Task
import com.abhinav.taskwall.data.SubTask
import com.abhinav.taskwall.ui.TaskViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel, openAddTask: Boolean = false) {
    val allTasks by viewModel.activeTasks.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(openAddTask) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
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
    val upcomingTasks = mutableListOf<Task>()
    
    for (task in allTasks) {
        if (task.targetDate != null && task.targetDate!! >= tomorrowStart && task.targetDate!! < tomorrowEnd) {
            tomorrowTasks.add(task)
        } else if (task.targetDate != null && task.targetDate!! >= tomorrowEnd) {
            upcomingTasks.add(task)
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
                                onMoveDown = { viewModel.moveTask(task, false, todayTasks) },
                                onEditTask = { taskToEdit = task },
                                viewModel = viewModel
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
                                onMoveDown = { viewModel.moveTask(task, false, tomorrowTasks) },
                                onEditTask = { taskToEdit = task },
                                viewModel = viewModel
                            )
                        }
                    }
                    
                    if (upcomingTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Upcoming",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        items(upcomingTasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task, 
                                onChecked = { viewModel.completeTask(task.id) },
                                onShiftTask = { targetDate -> viewModel.shiftTaskTargetDate(task.id, targetDate) },
                                onMoveUp = { viewModel.moveTask(task, true, upcomingTasks) },
                                onMoveDown = { viewModel.moveTask(task, false, upcomingTasks) },
                                onEditTask = { taskToEdit = task },
                                viewModel = viewModel
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
        var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
        var showDatePicker by remember { mutableStateOf(false) }
        var selectedColor by remember { mutableStateOf<Long?>(null) }
        var recurrence by remember { mutableStateOf<String?>(null) }
        var showOnWallpaper by remember { mutableStateOf(true) }

        val colors = listOf(null, 0xFFE53935, 0xFF43A047, 0xFF1E88E5, 0xFFFDD835, 0xFF8E24AA)

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
                        Checkbox(checked = showOnWallpaper, onCheckedChange = { showOnWallpaper = it })
                        Text("Show on Live Wallpaper")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(if (selectedDateMillis != null) {
                                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                "Date: " + sdf.format(java.util.Date(selectedDateMillis!!))
                            } else "Set Date (Optional)")
                        }
                        if (selectedDateMillis != null) {
                            IconButton(onClick = { selectedDateMillis = null }) {
                                Icon(Icons.Filled.Add, modifier = Modifier.rotate(45f), contentDescription = "Clear Date")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Category Color:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        colors.forEach { colorVal ->
                            val isSelected = selectedColor == colorVal
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = if (colorVal != null) Color(colorVal) else Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorVal }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Repeat:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(null to "None", "DAILY" to "Daily", "WEEKLY" to "Weekly").forEach { (value, label) ->
                            FilterChip(
                                selected = recurrence == value,
                                onClick = { recurrence = value },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addTask(title, notes.takeIf { it.isNotBlank() }, selectedDateMillis, selectedColor, recurrence, showOnWallpaper)
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
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
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

    if (taskToEdit != null) {
        val task = taskToEdit!!
        var title by remember { mutableStateOf(task.title) }
        var notes by remember { mutableStateOf(task.notes ?: "") }
        var selectedDateMillis by remember { mutableStateOf(task.targetDate) }
        var showDatePicker by remember { mutableStateOf(false) }
        var selectedColor by remember { mutableStateOf(task.color) }
        var recurrence by remember { mutableStateOf(task.recurrence) }
        var showOnWallpaper by remember { mutableStateOf(task.showOnWallpaper) }

        val colors = listOf(null, 0xFFE53935, 0xFF43A047, 0xFF1E88E5, 0xFFFDD835, 0xFF8E24AA)

        AlertDialog(
            onDismissRequest = { taskToEdit = null },
            title = { Text("Edit Task") },
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
                        Checkbox(checked = showOnWallpaper, onCheckedChange = { showOnWallpaper = it })
                        Text("Show on Live Wallpaper")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(if (selectedDateMillis != null) {
                                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                "Date: " + sdf.format(java.util.Date(selectedDateMillis!!))
                            } else "Set Date (Optional)")
                        }
                        if (selectedDateMillis != null) {
                            IconButton(onClick = { selectedDateMillis = null }) {
                                Icon(Icons.Filled.Add, modifier = Modifier.rotate(45f), contentDescription = "Clear Date")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Category Color:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        colors.forEach { colorVal ->
                            val isSelected = selectedColor == colorVal
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = if (colorVal != null) Color(colorVal) else Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorVal }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Repeat:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(null to "None", "DAILY" to "Daily", "WEEKLY" to "Weekly").forEach { (value, label) ->
                            FilterChip(
                                selected = recurrence == value,
                                onClick = { recurrence = value },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.updateTask(task.copy(
                            title = title,
                            notes = notes.takeIf { it.isNotBlank() },
                            targetDate = selectedDateMillis,
                            color = selectedColor,
                            recurrence = recurrence,
                            showOnWallpaper = showOnWallpaper
                        ))
                        taskToEdit = null
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDateMillis
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun TaskRow(
    task: Task, 
    onChecked: () -> Unit, 
    onShiftTask: (Long?) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEditTask: () -> Unit,
    viewModel: TaskViewModel
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
        
        if (task.color != null) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        Color(task.color),
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            )
            if (task.recurrence != null) {
                Text(
                    text = "↻ ${task.recurrence.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
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
        // Edit Button
        IconButton(onClick = onEditTask, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Task")
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
                    text = { Text("Edit Task") },
                    onClick = {
                        onEditTask()
                        showMenu = false
                    }
                )
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
    
    // Sub-tasks section
    val subtasks by viewModel.getSubTasksForTask(task.id).collectAsState(initial = emptyList<SubTask>())
    var isExpanded by remember { mutableStateOf(false) }
    var newSubtaskTitle by remember { mutableStateOf("") }
    
    if (subtasks.isNotEmpty() || isExpanded) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, bottom = 4.dp)
            .clickable { isExpanded = !isExpanded }
        ) {
            Text(
                if (isExpanded) "▼ Hide Sub-tasks (${subtasks.count { it.isCompleted }}/${subtasks.size})" 
                else "▶ Show Sub-tasks (${subtasks.count { it.isCompleted }}/${subtasks.size})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, bottom = 4.dp)
            .clickable { isExpanded = true }
        ) {
            Text(
                "+ Add Sub-tasks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
    
    if (isExpanded) {
        Column(modifier = Modifier.padding(start = 48.dp, bottom = 8.dp, end = 16.dp)) {
            subtasks.forEach { subtask ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = subtask.isCompleted,
                        onCheckedChange = { viewModel.toggleSubTaskCompletion(subtask) },
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = subtask.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.deleteSubTask(subtask.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Add, modifier = Modifier.rotate(45f), contentDescription = "Delete Subtask")
                    }
                }
            }
            
            OutlinedTextField(
                value = newSubtaskTitle,
                onValueChange = { newSubtaskTitle = it },
                placeholder = { Text("Add sub-task...", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = {
                    IconButton(onClick = { 
                        if (newSubtaskTitle.isNotBlank()) {
                            viewModel.addSubTask(task.id, newSubtaskTitle)
                            newSubtaskTitle = ""
                        }
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                }
            )
        }
    }
}
