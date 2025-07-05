@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.azura.azuratime.ui.auth

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import com.azura.azuratime.ui.components.AzuraButton

@Composable
fun DashboardScreen(
    role: String,
    name: String,
    onLogout: () -> Unit,
    onManageUsers: (() -> Unit)? = null,
    onManageFaces: (() -> Unit)? = null,
    onGoToMain: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard - $role") }, actions = {
                AzuraButton(onClick = onLogout, text = "Logout")
            })
        }
    ) { padding ->
        when (role) {
            "admin" -> AdminDashboard(
                name = name,
                onManageUsers = { onManageUsers?.invoke() },
                onManageFaces = { onManageFaces?.invoke() },
                onGoToMain = onGoToMain
            )
            "guru" -> TeacherDashboard(name, onGoToMain)
            "siswa" -> UserDashboard(name, onGoToMain)
            else -> Text("Unknown role")
        }
    }
}

@Composable
fun AdminDashboard(name: String, onManageUsers: () -> Unit, onManageFaces: () -> Unit, onGoToMain: (() -> Unit)? = null) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome Admin $name!", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        AzuraButton(onClick = onManageUsers, text = "Manage Users", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        AzuraButton(onClick = onManageFaces, text = "Manage Face Data", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        AzuraButton(onClick = { onGoToMain?.invoke() }, text = "Go to Main Menu", modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun TeacherDashboard(name: String, onGoToMain: (() -> Unit)? = null) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome Teacher $name!", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        AzuraButton(onClick = { /* TODO: Teacher specific action */ }, text = "Teacher Action", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        AzuraButton(onClick = { onGoToMain?.invoke() }, text = "Go to Main Menu", modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun UserDashboard(name: String, onGoToMain: (() -> Unit)? = null) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome $name!", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        AzuraButton(onClick = { /* TODO: Navigate to CheckInScreen */ }, text = "Check In", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        AzuraButton(onClick = { /* TODO: Navigate to RegistrationMenu */ }, text = "Register Face", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        AzuraButton(onClick = { /* TODO: Navigate to Manage */ }, text = "Manage Faces", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        AzuraButton(onClick = { /* TODO: Navigate to Options */ }, text = "Options", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        AzuraButton(onClick = { onGoToMain?.invoke() }, text = "Go to Main Menu", modifier = Modifier.fillMaxWidth())
    }
}
