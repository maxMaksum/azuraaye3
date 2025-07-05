package com.azura.azuratime.ui

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azura.azuratime.scanner.FaceScanner
import com.azura.azuratime.ui.components.AzuraCard
import com.azura.azuratime.utils.PhotoStorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

import com.azura.azuratime.db.ClassOption
import com.azura.azuratime.db.SubClassOption
import com.azura.azuratime.db.GradeOption
import com.azura.azuratime.db.SubGradeOption
import com.azura.azuratime.db.ProgramOption
import com.azura.azuratime.db.RoleOption
import kotlinx.coroutines.launch
import java.util.*
import com.azura.azuratime.viewmodel.FaceViewModel

@Composable
fun RegisterFaceScreen(
    useBackCamera: Boolean,
    viewModel: FaceViewModel = viewModel(),
    onNavigateToBulkRegister: () -> Unit = {}
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var subClass by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var subGrade by remember { mutableStateOf("") }
    var program by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var embedding by remember { mutableStateOf<FloatArray?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    var faceDetected by remember { mutableStateOf(false) }
    var selectedCameraIsBack by remember { mutableStateOf(useBackCamera) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    // --- Side Effects ---
    LaunchedEffect(Unit) {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.US
                tts.value?.setSpeechRate(1.0f)
            }
        }.also { tts.value = it }
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
    }
    DisposableEffect(Unit) { onDispose { tts.value?.shutdown() } }
    fun speak(message: String) { tts.value?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null) }
    LaunchedEffect(isSaved) {
        if (isSaved) {
            speak("Welcome $name")
            snackbarHostState.showSnackbar(
                message = "Registered \"$name\" successfully!",
                duration = SnackbarDuration.Short
            )
        }
    }
    LaunchedEffect(faceDetected) {
        if (faceDetected) {
            kotlinx.coroutines.delay(3000)
            faceDetected = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.fillMaxSize()) {
                FaceScanner(useBackCamera = selectedCameraIsBack) { _, newEmbedding ->
                    embedding = newEmbedding
                    faceDetected = true
                    isSaved = false
                    capturedBitmap = null
                }
                IconButton(
                    onClick = { selectedCameraIsBack = !selectedCameraIsBack },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                if (faceDetected && embedding != null) {
                    AzuraCard(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Face detected",
                                tint = Color(0xFF008080),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Face Detected!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF008080)
                            )
                        }
                    }
                }
                RegisterFaceForm(
                    name = name,
                    onNameChange = { name = it; isSaved = false },
                    studentId = studentId,
                    onStudentIdChange = { studentId = it; isSaved = false },
                    isSubmitting = isSubmitting,
                    isSaved = isSaved,
                    embedding = embedding,
                    onSubmit = { emb ->
                        isSubmitting = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val finalStudentId = studentId.ifEmpty { "STU" + System.currentTimeMillis().toString() }
                                val photoUrl = try {
                                    capturedBitmap?.let { bitmap ->
                                        Log.d("RegisterFaceScreen", "Saving captured bitmap for student: $finalStudentId")
                                        PhotoStorageUtils.saveFacePhoto(context, bitmap, finalStudentId)
                                    } ?: run {
                                        Log.d("RegisterFaceScreen", "Creating and saving placeholder bitmap for student: $finalStudentId")
                                        val placeholderBitmap = createPlaceholderBitmap(name.trim())
                                        PhotoStorageUtils.saveFacePhoto(context, placeholderBitmap, finalStudentId)
                                    }
                                } catch (saveException: Exception) {
                                    Log.e("RegisterFaceScreen", "Failed to save photo: ${saveException.message}", saveException)
                                    "https://picsum.photos/seed/consistent/200/200"
                                }
                                withContext(Dispatchers.Main) {
                                    Log.d("RegisterFaceScreen", "Registering face with studentId: $finalStudentId, name: ${name.trim()}, photoUrl: $photoUrl")
                                    viewModel.registerFace(
                                        studentId = finalStudentId,
                                        name = name.trim(),
                                        embedding = emb,
                                        photoUrl = photoUrl,
                                        className = className,
                                        subClass = subClass,
                                        grade = grade,
                                        subGrade = subGrade,
                                        program = program,
                                        role = role,
                                        onSuccess = {
                                            Log.d("RegisterFaceScreen", "Face registered successfully for $name")
                                            isSaved = true
                                            isSubmitting = false
                                            embedding = null
                                            capturedBitmap = null
                                        },
                                        onDuplicate = { existingName ->
                                            Log.w("RegisterFaceScreen", "Duplicate face registration detected as $existingName")
                                            isSubmitting = false
                                            speak("This face is already registered as $existingName")
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Already registered as \"$existingName\"",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("RegisterFaceScreen", "Registration error: ${e.message}", e)
                                withContext(Dispatchers.Main) {
                                    isSubmitting = false
                                    speak("Registration failed")
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Registration failed: ${e.message}",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
                if (isSubmitting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp,
                                color = Color(0xFF008080)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Saving Face Data...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterFaceForm(
    name: String,
    onNameChange: (String) -> Unit,
    studentId: String,
    onStudentIdChange: (String) -> Unit,
    isSubmitting: Boolean,
    isSaved: Boolean,
    embedding: FloatArray?,
    onSubmit: (FloatArray) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = studentId,
                onValueChange = onStudentIdChange,
                label = { Text("Student ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { if (embedding != null) onSubmit(embedding) },
                enabled = name.isNotBlank() && embedding != null && !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Register Face", color = Color.White)
                }
            }
            if (isSaved) {
                Text(
                    text = "Registered \"$name\"!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF008080),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

private fun createPlaceholderBitmap(name: String): android.graphics.Bitmap {
    val bitmap = createBitmap(200, 200)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLUE
        textSize = 20f
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawText(name, 100f, 80f, paint)
    paint.color = android.graphics.Color.BLACK
    canvas.drawCircle(100f, 130f, 40f, paint)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(90f, 120f, 6f, paint)
    canvas.drawCircle(110f, 120f, 6f, paint)
    canvas.drawCircle(100f, 135f, 3f, paint)
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 2f
    canvas.drawArc(85f, 140f, 115f, 155f, 0f, 180f, false, paint)
    return bitmap
}
