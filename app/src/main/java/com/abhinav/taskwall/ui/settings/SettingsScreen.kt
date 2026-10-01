package com.abhinav.taskwall.ui.settings

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.abhinav.taskwall.ui.TaskViewModel
import com.abhinav.taskwall.wallpaper.TaskWallService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val is24HourFormat by viewModel.is24Hour.collectAsState()
    val showSeconds by viewModel.showSeconds.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )

            // Wallpaper Setting
            ListItem(
                headlineContent = { Text("Set Live Wallpaper") },
                supportingContent = { Text("Apply the TaskWall engine to your home and lock screen") },
                leadingContent = {
                    Icon(Icons.Filled.Wallpaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(context, TaskWallService::class.java)
                        )
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(50.dp)
            ) {
                Text("Launch Wallpaper Picker")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Time Format Setting
            ListItem(
                headlineContent = { Text("24-Hour Time Format") },
                supportingContent = { Text("Display time as 14:30 instead of 2:30 PM") },
                leadingContent = {
                    Icon(Icons.Filled.Notifications, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = is24HourFormat,
                        onCheckedChange = { viewModel.set24Hour(it) }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            ListItem(
                headlineContent = { Text("Show Seconds") },
                supportingContent = { Text("Display seconds on the clock") },
                leadingContent = {
                    Icon(Icons.Filled.Notifications, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = showSeconds,
                        onCheckedChange = { viewModel.setShowSeconds(it) }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Appearance
            ListItem(
                headlineContent = { Text("Theme") },
                supportingContent = { Text("TaskWall defaults to Dark Mode for AMOLED efficiency") },
                leadingContent = {
                    Icon(Icons.Filled.ColorLens, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // About
            ListItem(
                headlineContent = { Text("About TaskWall") },
                supportingContent = { Text("Version 1.0.0 (Production)") },
                leadingContent = {
                    Icon(Icons.Filled.Info, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
