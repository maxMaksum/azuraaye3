package com.azura.azuratime

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

import com.azura.azuratime.ui.auth.AuthNavHost
import com.azura.azuratime.ml.FaceRecognizer
import com.azura.azuratime.util.InsertTestCheckInRecord
import com.azura.protect.NativeIntegrity

class MainActivity : ComponentActivity() {

    private var isRecognizerReady by mutableStateOf(false)
    private var recognizerError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("INTEGRITY", "MainActivity onCreate started")

        Log.d("INTEGRITY", "Before calling checkAppIntegrity")
        val isValid = NativeIntegrity.checkAppIntegrity(this)
        Log.d("INTEGRITY", "After calling checkAppIntegrity: $isValid")

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        InsertTestCheckInRecord.insert(this)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                FaceRecognizer.initialize(applicationContext) // ✅ Tidak return apa-apa
                Log.d("Startup", "✅ FaceRecognizer initialized")

                withContext(Dispatchers.Main) {
                    isRecognizerReady = true
                    recognizerError = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isRecognizerReady = false
                    recognizerError = e.message ?: "Unknown error during FaceRecognizer initialization"
                }
            }
        }

        setContent {
            // CrashcourseTheme {
            if (!isRecognizerReady) {
                SplashScreen(error = recognizerError)
            } else {
                AuthNavHost() // Use AuthNavHost as the entry point for authentication and welcome flow
            }
            // }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FaceRecognizer.close()
    }
}

@Composable
fun SplashScreen(error: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Initializing AzuraTime...", style = MaterialTheme.typography.bodyLarge)
            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
