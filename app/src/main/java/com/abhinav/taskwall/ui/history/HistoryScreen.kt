package com.abhinav.taskwall.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.abhinav.taskwall.ui.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: TaskViewModel) {
    val history by viewModel.recentHistory.collectAsState()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = "Recent History",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )

            // Heatmap Section
            if (history.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Activity (Last 14 Days)", style = MaterialTheme.typography.titleMedium)
                            
                            // Calculate current streak
                            var currentStreak = 0
                            val today = Calendar.getInstance()
                            today.set(Calendar.HOUR_OF_DAY, 0)
                            today.set(Calendar.MINUTE, 0)
                            today.set(Calendar.SECOND, 0)
                            today.set(Calendar.MILLISECOND, 0)
                            
                            for (i in 0..30) {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = today.timeInMillis
                                cal.add(Calendar.DAY_OF_YEAR, -i)
                                val dayStart = cal.timeInMillis
                                val dayEnd = dayStart + 86400000L
                                
                                val completedThatDay = history.count { 
                                    it.completedAt != null && it.completedAt!! >= dayStart && it.completedAt!! < dayEnd 
                                }
                                if (completedThatDay > 0) {
                                    currentStreak++
                                } else if (i > 0) {
                                    break // Broken streak (ignoring if today is 0 so far)
                                }
                            }
                            
                            if (currentStreak > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        "🔥 $currentStreak Day Streak",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val today = Calendar.getInstance()
                            today.set(Calendar.HOUR_OF_DAY, 0)
                            today.set(Calendar.MINUTE, 0)
                            today.set(Calendar.SECOND, 0)
                            today.set(Calendar.MILLISECOND, 0)
                            
                            // 14 days ago to today
                            for (i in 13 downTo 0) {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = today.timeInMillis
                                cal.add(Calendar.DAY_OF_YEAR, -i)
                                val dayStart = cal.timeInMillis
                                val dayEnd = dayStart + 86400000L
                                
                                val completedThatDay = history.count { 
                                    it.completedAt != null && it.completedAt!! >= dayStart && it.completedAt!! < dayEnd 
                                }
                                
                                val boxColor = when {
                                    completedThatDay == 0 -> Color.DarkGray
                                    completedThatDay in 1..2 -> Color(0xFF388E3C) // Light Green
                                    completedThatDay in 3..5 -> Color(0xFF4CAF50) // Medium Green
                                    else -> Color(0xFF81C784) // Bright Green
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(boxColor)
                                )
                            }
                        }
                    }
                }
            }

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No completed tasks yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(history, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                )
                                if (!task.notes.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = task.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Completed: ${dateFormat.format(Date(task.completedAt ?: 0))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
