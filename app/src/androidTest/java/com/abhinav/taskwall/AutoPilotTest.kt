package com.abhinav.taskwall

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoPilotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun showcaseAppFeatures() {
        // Wait a bit to let the user see it launch
        Thread.sleep(3000)
        
        // Add a new task
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        Thread.sleep(1500)
        
        composeTestRule.onNodeWithText("Task").performTextInput("AutoPilot Task")
        Thread.sleep(1000)
        
        composeTestRule.onNodeWithText("Add").performClick()
        Thread.sleep(2000)
        
        // Add a subtask
        composeTestRule.onNodeWithText("+ Add Sub-tasks").performClick()
        Thread.sleep(1500)
        
        composeTestRule.onNodeWithText("Add sub-task...").performTextInput("Testing subtask")
        Thread.sleep(1000)
        composeTestRule.onNodeWithContentDescription("Add").performClick()
        Thread.sleep(2000)
        
        // Navigate to History
        composeTestRule.onNodeWithText("History").performClick()
        Thread.sleep(2500)
        
        // Navigate to Settings
        composeTestRule.onNodeWithText("Settings").performClick()
        Thread.sleep(2500)
        
        // Navigate back to Home
        composeTestRule.onNodeWithText("Today").performClick()
        Thread.sleep(2000)
    }
}
