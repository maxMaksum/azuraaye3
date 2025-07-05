package com.azura.azuratime.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azura.azuratime.viewmodel.UserViewModel
import com.azura.azuratime.db.UserEntity
import com.azura.azuratime.utils.sha256
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.azura.azuratime.ui.components.AzuraButton
import com.azura.azuratime.ui.components.AzuraOutlinedButton
import com.azura.azuratime.ui.components.AzuraInput
import com.azura.azuratime.ui.components.AzuraCard
import com.azura.azuratime.ui.components.AzuraFormField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    userViewModel: UserViewModel = viewModel(),
    onBack: () -> Unit
) {
    var users by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<UserEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<UserEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    val roles = listOf("", "admin", "guru", "siswa")

    LaunchedEffect(Unit) {
        userViewModel.getUsersByRole("") { allUsers ->
            users = allUsers
            loading = false
        }
    }

    // Add User Dialog
    if (showAddDialog) {
        UserEditDialog(
            title = "Add User",
            initialUser = null,
            onDismiss = { showAddDialog = false },
            onSave = { user ->
                userViewModel.registerUser(user, onSuccess = { showAddDialog = false }, onError = { /* handle error */ })
            }
        )
    }
    // Edit User Dialog
    showEditDialog?.let { userToEdit ->
        UserEditDialog(
            title = "Edit User",
            initialUser = userToEdit,
            onDismiss = { showEditDialog = null },
            onSave = { user ->
                // For simplicity, just re-register (replace) user
                userViewModel.registerUser(user, onSuccess = { showEditDialog = null }, onError = { /* handle error */ })
            }
        )
    }
    // Delete User Dialog
    showDeleteDialog?.let { userToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${userToDelete.username}?") },
            confirmButton = {
                AzuraButton(
                    onClick = {
                        userViewModel.deleteUser(userToDelete, onComplete = { showDeleteDialog = null })
                    },
                    text = "Delete"
                )
            },
            dismissButton = {
                AzuraOutlinedButton(onClick = { showDeleteDialog = null }, text = "Cancel")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add User")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AzuraFormField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search username or name",
                    modifier = Modifier.weight(1f)
                )
                AzuraButton(
                    onClick = {
                        userViewModel.getUsersByRole(selectedRole) { allUsers ->
                            users = if (searchQuery.isBlank()) allUsers else allUsers.filter {
                                it.username.contains(searchQuery, true) || it.name.contains(searchQuery, true)
                            }
                        }
                    },
                    modifier = Modifier.alignByBaseline(),
                    text = "Search"
                )
                DropdownMenuBox(
                    selectedRole = selectedRole,
                    onRoleChange = { selectedRole = it },
                    roles = roles
                )
            }
            Spacer(Modifier.height(8.dp))
            if (loading) {
                CircularProgressIndicator()
            } else {
                ReusableLazyList(users.filter {
                    (searchQuery.isBlank() || it.username.contains(searchQuery, true) || it.name.contains(searchQuery, true)) &&
                    (selectedRole.isBlank() || it.role == selectedRole)
                }) { user ->
                    AzuraCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Username: ${user.username}", style = MaterialTheme.typography.bodyLarge)
                            Text("Name: ${user.name}")
                            Text("Role: ${user.role}")
                            Text("Created: ${user.createdAt}")
                            Row(Modifier.padding(top = 8.dp)) {
                                AzuraButton(onClick = { showEditDialog = user }, text = "Edit")
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(onClick = { showDeleteDialog = user }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserEditDialog(title: String, initialUser: UserEntity?, onDismiss: () -> Unit, onSave: (UserEntity) -> Unit) {
    var username by remember { mutableStateOf(initialUser?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf(initialUser?.name ?: "") }
    var role by remember { mutableStateOf(initialUser?.role ?: "admin") }
    val isEdit = initialUser != null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                AzuraInput(value = username, onValueChange = { username = it }, label = "Username")
                AzuraInput(value = password, onValueChange = { password = it }, label = if (isEdit) "New Password (optional)" else "Password")
                AzuraInput(value = name, onValueChange = { name = it }, label = "Name")
                DropdownMenuBox(selectedRole = role, onRoleChange = { role = it })
            }
        },
        confirmButton = {
            AzuraButton(onClick = {
                val hash = if (password.isNotBlank()) password.sha256() else (initialUser?.passwordHash ?: "")
                onSave(UserEntity(username, hash, name, role, initialUser?.createdAt ?: System.currentTimeMillis()))
            }, text = "Save")
        },
        dismissButton = {
            AzuraOutlinedButton(onClick = onDismiss, text = "Cancel")
        }
    )
}

@Composable
fun DropdownMenuBox(selectedRole: String, onRoleChange: (String) -> Unit, roles: List<String> = listOf("admin", "guru", "siswa")) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(if (selectedRole.isBlank()) "All Roles" else selectedRole) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(if (role.isBlank()) "All Roles" else role) },
                    onClick = {
                        onRoleChange(role)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun <T> ReusableLazyList(
    items: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    LazyColumn {
        items(items) { item ->
            itemContent(item)
        }
    }
}
