package com.azura.azuratime.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azura.azuratime.session.SessionManager
import com.azura.azuratime.viewmodel.UserViewModel

@Composable
fun AuthNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userViewModel: UserViewModel = viewModel()

    LaunchedEffect(Unit) {
        if (sessionManager.isLoggedIn()) {
            navController.navigate("dashboard") { popUpTo(0) }
        }
    }

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onLogin = { navController.navigate("login") },
                onSignup = { navController.navigate("register") }
            )
        }
        composable("login") {
            LoginScreen(userViewModel,
                onLoginSuccess = { role ->
                    sessionManager.saveUserSession(userViewModel.currentUser.value!!)
                    navController.navigate("dashboard") { popUpTo(0) }
                },
                onBackToSignup = { navController.navigate("register") }
            )
        }
        composable("dashboard") {
            val role = sessionManager.getRole() ?: ""
            val name = sessionManager.getName() ?: ""
            DashboardScreen(
                role = role,
                name = name,
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate("welcome") { popUpTo(0) }
                },
                onManageUsers = { navController.navigate("user_management") },
                onManageFaces = { navController.navigate("face_management") },
                onGoToMain = { navController.navigate("main_screen") { popUpTo(0) } }
            )
        }
        composable("main_screen") {
            com.azura.azuratime.ui.MainScreen()
        }
        composable("user_management") {
            com.azura.azuratime.ui.admin.UserManagementScreen(onBack = { navController.popBackStack() })
        }
        composable("face_management") {
            com.azura.azuratime.ui.admin.FaceManagementScreen(onBack = { navController.popBackStack() })
        }
        composable("register") {
            RegisterUserScreen(userViewModel,
                onUserRegistered = { navController.popBackStack() },
                onBackToLogin = { navController.navigate("login") }
            )
        }
    }
}
