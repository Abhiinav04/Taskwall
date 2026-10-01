package com.abhinav.taskwall.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.SurfaceHolder
import com.abhinav.taskwall.data.AppPreferences
import com.abhinav.taskwall.data.Quote
import com.abhinav.taskwall.data.QuoteRepository
import com.abhinav.taskwall.data.Task
import com.abhinav.taskwall.data.TaskRepository
import com.abhinav.taskwall.data.TaskWallDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskWallService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return TaskWallEngine()
    }

    inner class TaskWallEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val drawRunnable = Runnable { draw() }
        private var visible = false

        private val database = TaskWallDatabase.getDatabase(this@TaskWallService)
        private val taskRepository = TaskRepository(database.taskDao())
        private val quoteRepository = QuoteRepository(database.quoteDao())
        private val appPreferences = AppPreferences(this@TaskWallService)

        private val engineScope = CoroutineScope(Dispatchers.Main + Job())
        
        private var currentTasks: List<Task> = emptyList()
        private var currentQuote: Quote? = null

        private val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 50f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        
        private val quoteTextPaint = TextPaint().apply {
            color = Color.LTGRAY
            isAntiAlias = true
            textSize = 45f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        
        private val authorPaint = Paint().apply {
            color = Color.LTGRAY
            isAntiAlias = true
            textSize = 35f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        
        private val clockPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 140f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        
        private val datePaint = Paint().apply {
            color = Color.LTGRAY
            isAntiAlias = true
            textSize = 55f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        
        private val sectionPaint = Paint().apply {
            color = Color.parseColor("#888888")
            isAntiAlias = true
            textSize = 45f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        
        private val gradientPaint = Paint().apply {
            isAntiAlias = true
            isDither = true
        }

        private var is24Hour: Boolean = false
        private var showSeconds: Boolean = false
        private var timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        private val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

        private fun updateTimeFormat() {
            val pattern = buildString {
                if (is24Hour) append("HH:mm") else append("h:mm")
                if (showSeconds) append(":ss")
                if (!is24Hour) append(" a")
            }
            timeFormat = SimpleDateFormat(pattern, Locale.getDefault())
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            surfaceHolder?.setFormat(android.graphics.PixelFormat.RGBA_8888)
            
            engineScope.launch {
                quoteRepository.populateInitialQuotesIfEmpty()
                currentQuote = quoteRepository.getNextEligibleQuote()

                launch {
                    appPreferences.is24Hour.collect { is24 ->
                        is24Hour = is24
                        updateTimeFormat()
                        if (visible) requestDraw()
                    }
                }

                launch {
                    appPreferences.showSeconds.collect { seconds ->
                        showSeconds = seconds
                        updateTimeFormat()
                        if (visible) requestDraw()
                    }
                }

                taskRepository.getActiveTasks().collect { tasks ->
                    currentTasks = tasks
                    if (visible) {
                        requestDraw()
                    }
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawRunnable)
            engineScope.cancel()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                requestDraw()
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            requestDraw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        private fun requestDraw() {
            handler.removeCallbacks(drawRunnable)
            handler.post(drawRunnable)
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    drawWallpaper(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
            
            if (visible) {
                handler.removeCallbacks(drawRunnable)
                val delayMs = if (showSeconds) {
                    1000L - (System.currentTimeMillis() % 1000L)
                } else {
                    60000L - (System.currentTimeMillis() % 60000L)
                }
                handler.postDelayed(drawRunnable, delayMs)
            }
        }

        private fun drawWallpaper(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val centerX = width / 2f
            val centerY = height / 2f
            
            // 1. Dynamic AMOLED Background
            val timeOffset = (System.currentTimeMillis() % 100000L) / 100000f // 100s per orbit
            val dx = Math.sin(timeOffset * Math.PI * 2) * (width * 0.2f)
            val dy = Math.cos(timeOffset * Math.PI * 2) * (height * 0.1f)

            val shader = android.graphics.RadialGradient(
                centerX + dx.toFloat(), 
                centerY + dy.toFloat(), 
                width * 1.2f, 
                intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#000000")), // Slate blue to Pitch Black
                null, 
                android.graphics.Shader.TileMode.CLAMP
            )
            gradientPaint.shader = shader
            canvas.drawRect(0f, 0f, width, height, gradientPaint)
            var startY = height * 0.15f
            
            val dateText = dateFormat.format(Date()).uppercase()
            val dateWidth = datePaint.measureText(dateText)
            canvas.drawText(dateText, centerX - (dateWidth / 2f), startY, datePaint)
            
            startY += clockPaint.textSize * 1.05f
            val timeText = timeFormat.format(Date())
            val timeWidth = clockPaint.measureText(timeText)
            canvas.drawText(timeText, centerX - (timeWidth / 2f), startY, clockPaint)
            
            // 3. Quote
            startY += height * 0.08f
            currentQuote?.let { quote ->
                val staticLayout = StaticLayout.Builder.obtain(
                    quote.text, 0, quote.text.length, quoteTextPaint, (width * 0.8f).toInt()
                )
                .setAlignment(Layout.Alignment.ALIGN_NORMAL) // TextPaint is CENTER, so ALIGN_NORMAL centers it relative to X=0
                .setLineSpacing(0f, 1.2f)
                .build()

                canvas.save()
                canvas.translate(centerX, startY) // translate to center X
                staticLayout.draw(canvas)
                canvas.restore()
                
                startY += staticLayout.height + authorPaint.textSize * 1.5f
                
                val authorText = "— ${quote.author ?: "Unknown"}"
                canvas.drawText(authorText, centerX, startY, authorPaint)
            }
            
            // Calculate Tomorrow Range
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
            
            for (task in currentTasks) {
                if (task.targetDate != null && task.targetDate >= tomorrowStart && task.targetDate < tomorrowEnd) {
                    tomorrowTasks.add(task)
                } else {
                    todayTasks.add(task)
                }
            }

            // 4. Tasks (Left Aligned)
            startY = height * 0.55f // Fixed starting point for tasks
            val leftMargin = 100f
            
            canvas.drawText("TODAY", leftMargin, startY, sectionPaint)
            startY += sectionPaint.textSize * 1.5f
            
            if (todayTasks.isEmpty()) {
                val emptyPaint = Paint(textPaint).apply { color = Color.DKGRAY }
                canvas.drawText("All clear.", leftMargin, startY, emptyPaint)
                startY += emptyPaint.textSize * 1.8f
            } else {
                for (task in todayTasks.take(6)) {
                    canvas.drawText("• ${task.title}", leftMargin, startY, textPaint)
                    startY += textPaint.textSize * 1.8f
                }
                if (todayTasks.size > 6) {
                    val morePaint = Paint(textPaint).apply { color = Color.GRAY }
                    canvas.drawText("+ ${todayTasks.size - 6} more", leftMargin, startY, morePaint)
                    startY += morePaint.textSize * 1.8f
                }
            }
            
            if (tomorrowTasks.isNotEmpty()) {
                startY += sectionPaint.textSize * 1.0f
                canvas.drawText("TOMORROW", leftMargin, startY, sectionPaint)
                startY += sectionPaint.textSize * 1.5f
                
                for (task in tomorrowTasks.take(4)) {
                    canvas.drawText("• ${task.title}", leftMargin, startY, textPaint)
                    startY += textPaint.textSize * 1.8f
                }
                if (tomorrowTasks.size > 4) {
                    val morePaint = Paint(textPaint).apply { color = Color.GRAY }
                    canvas.drawText("+ ${tomorrowTasks.size - 4} more", leftMargin, startY, morePaint)
                }
            }
        }
    }
}
