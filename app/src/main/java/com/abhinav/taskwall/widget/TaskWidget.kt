package com.abhinav.taskwall.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import com.abhinav.taskwall.data.TaskWallDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TaskWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = TaskWallDatabase.getDatabase(context)
        val tasks = database.taskDao().getActiveTasks().first()

        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(Color(0xFF121212)))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Today's Tasks (${tasks.size})",
                    style = TextStyle(color = ColorProvider(Color(0xFFFFFFFF)))
                )
                
                Spacer(modifier = GlanceModifier.padding(8.dp))
                
                if (tasks.isEmpty()) {
                    Text(
                        text = "All clear.",
                        style = TextStyle(color = ColorProvider(Color(0xFFAAAAAA)))
                    )
                } else {
                    tasks.take(4).forEach { task ->
                        Row(modifier = GlanceModifier.padding(vertical = 4.dp).fillMaxWidth()) {
                            Text(text = "☐", style = TextStyle(color = ColorProvider(Color(0xFFFFFFFF))))
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(text = task.title, style = TextStyle(color = ColorProvider(Color(0xFFFFFFFF))))
                        }
                    }
                    if (tasks.size > 4) {
                        Text(
                            text = "+ ${tasks.size - 4} more",
                            style = TextStyle(color = ColorProvider(Color(0xFFAAAAAA)))
                        )
                    }
                }
            }
        }
    }
}

class TaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TaskWidget()
}
