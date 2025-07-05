package com.azura.azuratime.ui

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.InsertPhoto
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azura.azuratime.ui.components.FaceAvatar
import com.azura.azuratime.db.FaceEntity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Log
import kotlinx.coroutines.launch
import com.azura.azuratime.viewmodel.FaceViewModel
import androidx.compose.ui.window.Dialog
import com.azura.azuratime.ui.add.CaptureMode
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.azura.azuratime.ui.components.AzuraButton
import com.azura.azuratime.ui.components.AzuraInput
import com.azura.azuratime.ui.components.AzuraCard
import com.azura.azuratime.ui.components.AzuraLazyList
import com.azura.azuratime.ui.components.AzuraFormField
import com.azura.azuratime.ui.components.AzuraAvatar




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceListScreen(
    viewModel: FaceViewModel = viewModel(),
    onEditUser: (FaceEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val allFaces by viewModel.faceList.collectAsStateWithLifecycle(emptyList())
    val filteredFaces = allFaces.filter { face ->
        face.name.contains(searchQuery, ignoreCase = true)
    }

    // Enhanced debug logging
    LaunchedEffect(allFaces.size) {
        Log.d("FaceListScreen", "=== FACELISTSCREEN DATA UPDATE ===")
        Log.d("FaceListScreen", "Total faces loaded: ${allFaces.size}")
        Log.d("FaceListScreen", "ViewModel instance: ${viewModel.hashCode()}")

        if (allFaces.isEmpty()) {
            Log.d("FaceListScreen", "❌ NO FACES FOUND IN DATABASE")

            // Check database directly
            try {
                val directFaces = com.azura.azuratime.db.AppDatabase
                    .getInstance(context)
                    .faceDao()
                    .getAllFaces()
                Log.d("FaceListScreen", "Direct database query result: ${directFaces.size} faces")
                directFaces.forEachIndexed { index, face ->
                    Log.d("FaceListScreen", "  Direct Face $index: ${face.name} (${face.studentId})")
                }
            } catch (e: Exception) {
                Log.e("FaceListScreen", "❌ Error querying database directly", e)
            }
        } else {
            Log.d("FaceListScreen", "✅ FACES FOUND:")
            allFaces.forEachIndexed { index, face ->
                Log.d("FaceListScreen", "Face $index: ${face.name} (${face.studentId})")
                Log.d("FaceListScreen", "  Photo URL: ${face.photoUrl}")
                Log.d("FaceListScreen", "  Timestamp: ${face.timestamp}")
                if (!face.photoUrl.isNullOrEmpty()) {
                    val isUrl = face.photoUrl.startsWith("http://") || face.photoUrl.startsWith("https://")
                    if (isUrl) {
                        Log.d("FaceListScreen", "  Photo type: Internet URL")
                    } else {
                        val file = java.io.File(face.photoUrl)
                        Log.d("FaceListScreen", "  Photo type: Local file")
                        Log.d("FaceListScreen", "  Photo exists: ${file.exists()}")
                        if (file.exists()) {
                            Log.d("FaceListScreen", "  Photo size: ${file.length()} bytes")
                        }
                    }
                }
            }
        }
        Log.d("FaceListScreen", "=== END DATA UPDATE ===")
    }

    var editingFace by remember { mutableStateOf<FaceEntity?>(null) }

    // Editable fields
    var editName by remember { mutableStateOf("") }
    var editClass by remember { mutableStateOf("") }
    var editSubClass by remember { mutableStateOf("") }
    var editGrade by remember { mutableStateOf("") }
    var editSubGrade by remember { mutableStateOf("") }
    var editProgram by remember { mutableStateOf("") }
    var editRole by remember { mutableStateOf("") }

    // Show edit dialog when needed
    editingFace?.let { face ->
        AlertDialog(
            onDismissRequest = { editingFace = null },
            title = { Text("Edit Face Data") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AzuraFormField(value = editName, onValueChange = { editName = it }, label = "Name", isError = editName.isBlank(), helperText = if (editName.isBlank()) "Name is required" else null)
                    AzuraFormField(value = editClass, onValueChange = { editClass = it }, label = "Class")
                    AzuraFormField(value = editSubClass, onValueChange = { editSubClass = it }, label = "SubClass")
                    AzuraFormField(value = editGrade, onValueChange = { editGrade = it }, label = "Grade")
                    AzuraFormField(value = editSubGrade, onValueChange = { editSubGrade = it }, label = "SubGrade")
                    AzuraFormField(value = editProgram, onValueChange = { editProgram = it }, label = "Program")
                    AzuraFormField(value = editRole, onValueChange = { editRole = it }, label = "Role")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateFace(
                        face.copy(
                            name = editName,
                            className = editClass,
                            subClass = editSubClass,
                            grade = editGrade,
                            subGrade = editSubGrade,
                            program = editProgram,
                            role = editRole
                        )
                    ) { editingFace = null }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFace = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    var showEditPhotoCapture by remember { mutableStateOf<FaceEntity?>(null) }
    var editPhotoError by remember { mutableStateOf("") }
    var photoEditMode by remember { mutableStateOf<String?>(null) } // "capture" or "upload"

    // Show EditUserPhotoCaptureScreen when requested
    showEditPhotoCapture?.let { face ->
        var isProcessing by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        // Gallery launcher for photo upload
        val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val bmp = com.azura.azuratime.utils.PhotoStorageUtils.loadBitmapFromUri(context, it)
                if (bmp != null) {
                    scope.launch {
                        val result = com.azura.azuratime.utils.PhotoProcessingUtils.processBitmapForFaceEmbedding(context, bmp)
                        if (result != null) {
                            val (faceBitmap, embedding) = result
                            isProcessing = true
                            viewModel.updateFaceWithPhoto(
                                face = face,
                                photoBitmap = faceBitmap,
                                embedding = embedding,
                                onComplete = {
                                    isProcessing = false
                                    showEditPhotoCapture = null
                                    errorMessage = ""
                                    photoEditMode = null
                                },
                                onError = { error ->
                                    isProcessing = false
                                    errorMessage = error
                                }
                            )
                        } else {
                            errorMessage = "No face detected. Please try another photo."
                        }
                    }
                } else {
                    errorMessage = "Failed to load selected image."
                }
            }
        }
        Dialog(
            onDismissRequest = {
                showEditPhotoCapture = null
                photoEditMode = null
            }
        ) {
            // Use a transparent background for the dialog
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // semi-transparent overlay
            ) {
                Column(Modifier.fillMaxSize()) {
                    if (photoEditMode == null) {
                        // Choice UI with card for better contrast
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AzuraCard(
                                modifier = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(0.9f),
                            ) {
                                Column(
                                    Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Choose Photo Source", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(24.dp))
                                    AzuraButton(
                                        onClick = { photoEditMode = "capture" },
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "Take Photo"
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    AzuraButton(
                                        onClick = { photoEditMode = "upload" },
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "Upload Photo"
                                    )
                                }
                            }
                        }
                    } else if (photoEditMode == "capture") {
                        // Camera capture (FaceCaptureScreen)
                        com.azura.azuratime.ui.add.FaceCaptureScreen(
                            mode = CaptureMode.PHOTO,
                            onClose = {
                                showEditPhotoCapture = null
                                photoEditMode = null
                            },
                            editFace = face,
                            onPhotoUpdate = { bitmap, embedding ->
                                isProcessing = true
                                viewModel.updateFaceWithPhoto(
                                    face = face,
                                    photoBitmap = bitmap,
                                    embedding = embedding,
                                    onComplete = {
                                        isProcessing = false
                                        showEditPhotoCapture = null
                                        errorMessage = ""
                                        photoEditMode = null
                                    },
                                    onError = { error ->
                                        isProcessing = false
                                        errorMessage = error
                                    }
                                )
                            }
                        )
                    } else if (photoEditMode == "upload") {
                        // Launch gallery picker immediately
                        LaunchedEffect(Unit) {
                            galleryLauncher.launch("image/*")
                        }
                        // Show a loading or info UI
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a photo from your library...", color = Color.White)
                        }
                    }
                }
                if (isProcessing) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                if (errorMessage.isNotEmpty()) {
                    Snackbar(
                        action = {
                            TextButton(onClick = { errorMessage = "" }) { Text("Dismiss") }
                        },
                        modifier = Modifier.padding(16.dp)
                    ) { Text(errorMessage) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Face Management (${allFaces.size})") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                Log.d("FaceListScreen", "Refresh button clicked")
                                // Force refresh by clearing cache and reloading
                                com.azura.azuratime.db.FaceCache.refresh(context)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }

                    // Debug button - check existing face photo paths
                    IconButton(
                        onClick = {
                            try {
                                Log.d("FaceListScreen", "=== CHECKING EXISTING FACE PHOTOS ===")

                                allFaces.forEachIndexed { index, face ->
                                    Log.d("FaceListScreen", "--- Face $index ---")
                                    Log.d("FaceListScreen", "Name: ${face.name}")
                                    Log.d("FaceListScreen", "Student ID: ${face.studentId}")
                                    Log.d("FaceListScreen", "Photo URL: '${face.photoUrl}'")
                                    Log.d("FaceListScreen", "Photo URL is null: ${face.photoUrl == null}")
                                    Log.d("FaceListScreen", "Photo URL is empty: ${face.photoUrl?.isEmpty()}")

                                    if (!face.photoUrl.isNullOrEmpty()) {
                                        val file = java.io.File(face.photoUrl)
                                        Log.d("FaceListScreen", "File path: ${file.absolutePath}")
                                        Log.d("FaceListScreen", "File exists: ${file.exists()}")
                                        if (file.exists()) {
                                            Log.d("FaceListScreen", "File size: ${file.length()} bytes")
                                            Log.d("FaceListScreen", "File readable: ${file.canRead()}")
                                        } else {
                                            Log.d("FaceListScreen", "Parent dir exists: ${file.parentFile?.exists()}")
                                            Log.d("FaceListScreen", "Parent dir: ${file.parentFile?.absolutePath}")
                                        }
                                    } else {
                                        Log.d("FaceListScreen", "❌ NO PHOTO URL STORED")
                                    }
                                    Log.d("FaceListScreen", "")
                                }

                                Log.d("FaceListScreen", "=== PHOTO CHECK COMPLETED ===")

                            } catch (e: Exception) {
                                Log.e("FaceListScreen", "❌ Error checking photos", e)
                                e.printStackTrace()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Debug Photos"
                        )
                    }

                    // Test button - create a face with photo to test the flow
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    Log.d("FaceListScreen", "=== CREATING TEST FACE WITH PHOTO ===")

                                    // Create a test bitmap
                                    val testBitmap = createTestBitmap("Test User")

                                    // Save it using PhotoStorageUtils
                                    val testStudentId = "TEST_${System.currentTimeMillis()}"
                                    val photoUrl = com.azura.azuratime.utils.PhotoStorageUtils.saveFacePhoto(
                                        context, testBitmap, testStudentId
                                    )

                                    Log.d("FaceListScreen", "Photo saved to: $photoUrl")

                                    // Create embedding
                                    val embedding = FloatArray(128) { kotlin.random.Random.nextFloat() }

                                    // Register face
                                    viewModel.registerFace(
                                        studentId = testStudentId,
                                        name = "Test User ${System.currentTimeMillis() % 1000}",
                                        embedding = embedding,
                                        photoUrl = photoUrl,
                                        className = "Test Class",
                                        onSuccess = {
                                            Log.d("FaceListScreen", "✅ Test face created successfully!")
                                            Log.d("FaceListScreen", "Now checking if it appears in the list...")
                                        },
                                        onDuplicate = { existing ->
                                            Log.d("FaceListScreen", "⚠️ Duplicate: $existing")
                                        }
                                    )

                                    Log.d("FaceListScreen", "=== TEST FACE CREATION COMPLETED ===")
                                } catch (e: Exception) {
                                    Log.e("FaceListScreen", "❌ Error creating test face", e)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Test Face"
                        )
                    }

                    // Database verification button
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    Log.d("FaceListScreen", "=== DATABASE VERIFICATION ===")

                                    // Check database directly
                                    val database = com.azura.azuratime.db.AppDatabase.getInstance(context)
                                    val directFaces = database.faceDao().getAllFaces()

                                    Log.d("FaceListScreen", "Direct database query: ${directFaces.size} faces")
                                    directFaces.forEach { face ->
                                        Log.d("FaceListScreen", "  DB Face: ${face.name} (${face.studentId})")
                                        Log.d("FaceListScreen", "    Photo: ${face.photoUrl}")
                                        Log.d("FaceListScreen", "    Timestamp: ${face.timestamp}")
                                    }

                                    // Check ViewModel state
                                    Log.d("FaceListScreen", "ViewModel faceList size: ${allFaces.size}")
                                    Log.d("FaceListScreen", "ViewModel instance: ${viewModel.hashCode()}")

                                    // Force refresh
                                    Log.d("FaceListScreen", "Forcing cache refresh...")
                                    com.azura.azuratime.db.FaceCache.refresh(context)

                                    Log.d("FaceListScreen", "=== VERIFICATION COMPLETED ===")
                                } catch (e: Exception) {
                                    Log.e("FaceListScreen", "❌ Error in database verification", e)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Verify Database"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Test AzuraAvatar component (was FaceAvatar)
            Card(
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Internet test
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Internet: ")
                        AzuraAvatar(
                            photoPath = "https://picsum.photos/200/200?random=123",
                            size = 48
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("✅ Working", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(8.dp))

                    // Local file test
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Local File: ")
                        val testPath = "${context.filesDir}/faces/face_DETAILED_TEST.jpg"
                        AzuraAvatar(
                            photoPath = testPath,
                            size = 48
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (java.io.File(testPath).exists()) "✅ Found" else "❌ Missing",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (java.io.File(testPath).exists())
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    // Show file path for debugging
                    Text(
                        "Path: ${context.filesDir}/faces/face_DETAILED_TEST.jpg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        "Click + button to create test file",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (filteredFaces.isEmpty()) {
                Column {
                    Text("No records found.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Debug Info:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "• Total faces: ${allFaces.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "• Filtered faces: ${filteredFaces.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "• Search query: '${searchQuery}'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Try: 1) Tap ➕ to create test face, 2) Tap 🔄 to verify database, 3) Use Manual Registration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                AzuraLazyList(
                    items = filteredFaces,
                    modifier = Modifier,
                ) { face ->
                    AzuraCard(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar - Use actual photo path
                            Log.d("FaceListScreen", "Rendering avatar for ${face.name} with path: ${face.photoUrl}")

                            AzuraAvatar(
                                photoPath = face.photoUrl,
                                size = 64
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Face info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = face.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ID: ${face.studentId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (face.className.isNotEmpty()) {
                                    Text(
                                        text = "Class: ${face.className}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // Debug: Show photo status
                                Text(
                                    text = if (!face.photoUrl.isNullOrEmpty()) {
                                        val isUrl = face.photoUrl.startsWith("http://") || face.photoUrl.startsWith("https://")
                                        val isValid = if (isUrl) {
                                            true // Assume URLs are valid for now
                                        } else {
                                            java.io.File(face.photoUrl).exists()
                                        }
                                        val type = if (isUrl) "URL" else "File"
                                        "📷 Photo ($type): ${if (isValid) "✅" else "❌"}"
                                    } else {
                                        "📷 No photo"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (!face.photoUrl.isNullOrEmpty()) {
                                        val isUrl = face.photoUrl.startsWith("http://") || face.photoUrl.startsWith("https://")
                                        val isValid = if (isUrl) true else java.io.File(face.photoUrl).exists()
                                        if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                TextButton(
                                    onClick = {
                                        editingFace = face
                                        editName = face.name
                                        editClass = face.className
                                        editSubClass = face.subClass
                                        editGrade = face.grade
                                        editSubGrade = face.subGrade
                                        editProgram = face.program
                                        editRole = face.role
                                    }
                                ) {
                                    Text("Quick Edit")
                                }
                                TextButton(
                                    onClick = {
                                        showEditPhotoCapture = face
                                    }
                                ) {
                                    Text("Edit + Photo")
                                }
                            TextButton(
                                onClick = { viewModel.deleteFace(face) }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Create a single test face safely
 */
private suspend fun createSingleTestFace(context: android.content.Context, viewModel: FaceViewModel) {
    try {
        Log.d("FaceListScreen", "=== CREATING SINGLE TEST FACE ===")

        // Create a simple internet URL face first (safest option)
        Log.d("FaceListScreen", "Step 1: Creating embedding...")
        val embedding = FloatArray(128) { kotlin.random.Random.nextFloat() }
        Log.d("FaceListScreen", "Step 1: Embedding created with ${embedding.size} values")

        val studentId = "TEST_${System.currentTimeMillis()}"
        val name = "Test User"
        val photoUrl = "https://picsum.photos/200/200?random=${System.currentTimeMillis()}"

        Log.d("FaceListScreen", "Step 2: Preparing registration...")
        Log.d("FaceListScreen", "  Student ID: $studentId")
        Log.d("FaceListScreen", "  Name: $name")
        Log.d("FaceListScreen", "  Photo URL: $photoUrl")

        Log.d("FaceListScreen", "Step 3: Switching to Main thread...")
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            Log.d("FaceListScreen", "Step 4: Calling viewModel.registerFace...")

            viewModel.registerFace(
                studentId = studentId,
                name = name,
                embedding = embedding,
                photoUrl = photoUrl,
                className = "Test Class",
                subClass = "",
                grade = "",
                subGrade = "",
                program = "",
                role = "",
                onSuccess = {
                    Log.d("FaceListScreen", "✅ SUCCESS: Test face created successfully!")
                },
                onDuplicate = { existingName ->
                    Log.d("FaceListScreen", "⚠️ DUPLICATE: Face already exists as $existingName")
                }
            )

            Log.d("FaceListScreen", "Step 5: registerFace call completed")
        }

        Log.d("FaceListScreen", "=== SINGLE TEST FACE CREATION COMPLETED ===")

    } catch (e: Exception) {
        Log.e("FaceListScreen", "❌ CRASH in createSingleTestFace", e)
        e.printStackTrace()

        // Log the exact location of the crash
        Log.e("FaceListScreen", "Stack trace:")
        e.stackTrace.forEach { element ->
            Log.e("FaceListScreen", "  at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
        }
    }
}

/**
 * Create a test bitmap with text
 */
private fun createTestBitmap(name: String): android.graphics.Bitmap {
    // Removed usage of createBitmap as per cleanup request
    // Placeholder for future implementation
    throw UnsupportedOperationException("Bitmap creation has been removed as part of cleanup")
}
