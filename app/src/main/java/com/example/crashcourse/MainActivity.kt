package com.example.crashcourse

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

import com.example.crashcourse.ui.theme.CrashcourseTheme
import com.example.crashcourse.ui.MainScreen
import com.example.crashcourse.ml.FaceRecognizer
import com.example.crashcourse.util.InsertTestCheckInRecord
import com.azura.protect.NativeIntegrity

class MainActivity : ComponentActivity() {

    private var isRecognizerReady by mutableStateOf(false)

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
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isRecognizerReady = false
                }
            }
        }

        setContent {
            CrashcourseTheme {
                if (!isRecognizerReady) {
                    SplashScreen()
                } else {
                    MainScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FaceRecognizer.close()
    }
}

@Composable
fun SplashScreen() {
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
        }
    }
}
