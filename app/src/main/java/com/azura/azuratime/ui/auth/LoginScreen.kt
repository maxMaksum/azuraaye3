package com.azura.azuratime.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azura.azuratime.ui.components.AzuraFormField
import com.azura.azuratime.utils.sha256
import com.azura.azuratime.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    userViewModel: UserViewModel = viewModel(),
    onLoginSuccess: (role: String) -> Unit,
    onBackToSignup: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = onBackToSignup) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Sign Up")
            }
        }
        Text("Login", style = MaterialTheme.typography.headlineMedium)
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
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = {
                loading = true
                val hash = password.sha256()
                userViewModel.login(username, hash,
                    onSuccess = {
                        loading = false
                        onLoginSuccess(userViewModel.currentUser.value?.role ?: "")
                    },
                    onError = {
                        loading = false
                        error = it
                    }
                )
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}
