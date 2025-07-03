package com.example.crashcourse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.crashcourse.ui.theme.CrashcourseTheme
import com.example.crashcourse.ui.MainScreen
import com.example.crashcourse.ml.FaceRecognizer
import com.example.crashcourse.util.InsertTestCheckInRecord
import com.azura.protect.NativeIntegrity



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("INTEGRITY", "MainActivity onCreate started")

        // TEMP: Bypass integrity check for debugging
        android.util.Log.d("INTEGRITY", "Before calling checkAppIntegrity")
        val isValid = com.azura.protect.NativeIntegrity.checkAppIntegrity(this)
        android.util.Log.d("INTEGRITY", "After calling checkAppIntegrity: $isValid")
        // For debugging, do not exit on fail:
        // if (!isValid) {
        //     finish()
        //     return
        // }

        // Enable edge-to-edge display and hide system bars for immersive experience
        enableEdgeToEdge()

        // Hide system UI (status bar and navigation bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Insert a test check-in record for testing
        InsertTestCheckInRecord.insert(this)

        // Initialize the TFLite interpreter once, with error handling
        try {
            FaceRecognizer.initialize(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: show fallback UI or error message
        }

        setContent {
            CrashcourseTheme {
                MainScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release interpreter resources
        FaceRecognizer.close()
    }
}