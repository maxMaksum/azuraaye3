package com.azura.azuratime.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azura.azuratime.viewmodel.FaceViewModel
import com.azura.azuratime.db.FaceEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceManagementScreen(
    faceViewModel: FaceViewModel = viewModel(),
    onBack: () -> Unit
) {
    val faces by faceViewModel.faceList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Face Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name or ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            faces.filter {
                searchQuery.isBlank() || it.name.contains(searchQuery, true) || it.studentId.contains(searchQuery, true)
            }.forEach { face ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("ID: ${face.studentId}", style = MaterialTheme.typography.bodyLarge)
                        Text("Name: ${face.name}")
                        Text("Role: ${face.role}")
                        Text("Class: ${face.className}")
                        Text("Registered: ${face.timestamp}")
                        // Add edit/delete actions as needed
                    }
                }
            }
        }
    }
}
