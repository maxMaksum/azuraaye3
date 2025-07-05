package com.azura.azuratime.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azura.azuratime.db.UserEntity
import com.azura.azuratime.ui.components.AzuraButton
import com.azura.azuratime.ui.components.AzuraFormField
import com.azura.azuratime.ui.components.AzuraOutlinedButton
import com.azura.azuratime.utils.sha256
import com.azura.azuratime.viewmodel.UserViewModel

@Composable
fun RegisterUserScreen(
    userViewModel: UserViewModel = viewModel(),
    onUserRegistered: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("admin") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = onBackToLogin) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Login")
            }
        }
        Text("Register User", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        AzuraFormField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            isError = username.isBlank(),
            helperText = if (username.isBlank()) "Username is required" else null,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        AzuraFormField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isError = password.isBlank(),
            helperText = if (password.isBlank()) "Password is required" else null,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        AzuraFormField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            isError = name.isBlank(),
            helperText = if (name.isBlank()) "Name is required" else null,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        DropdownMenuBox(role, onRoleChange = { role = it })
        Spacer(Modifier.height(16.dp))
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        AzuraButton(
            onClick = {
                loading = true
                val hash = password.sha256()
                val user = UserEntity(
                    username = username,
                    passwordHash = hash,
                    name = name,
                    role = role,
                    createdAt = System.currentTimeMillis()
                )
                userViewModel.registerUser(user,
                    onSuccess = {
                        loading = false
                        onUserRegistered()
                    },
                    onError = {
                        loading = false
                        error = it
                    }
                )
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            text = "Register"
        )
    }
}

@Composable
fun DropdownMenuBox(selectedRole: String, onRoleChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("admin", "guru", "siswa")
    Box {
        AzuraOutlinedButton(onClick = { expanded = true }, text = selectedRole)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role) },
                    onClick = {
                        onRoleChange(role)
                        expanded = false
                    }
                )
            }
        }
    }
}
