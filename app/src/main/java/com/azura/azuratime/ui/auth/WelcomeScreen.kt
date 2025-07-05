package com.azura.azuratime.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.azura.azuratime.ui.components.AzuraButton
import com.azura.azuratime.ui.components.AzuraOutlinedButton

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onSignup: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to AzuraTime", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))
            AzuraButton(onClick = onLogin, modifier = Modifier.fillMaxWidth(), text = "Login")
            Spacer(Modifier.height(16.dp))
            AzuraOutlinedButton(onClick = onSignup, modifier = Modifier.fillMaxWidth(), text = "Sign Up")
        }
    }
}
