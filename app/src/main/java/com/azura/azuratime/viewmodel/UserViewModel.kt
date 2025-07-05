package com.azura.azuratime.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azura.azuratime.db.AppDatabase
import com.azura.azuratime.db.UserEntity
import com.azura.azuratime.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getInstance(application).userDao()
    private val repository = UserRepository(userDao)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    fun login(username: String, passwordHash: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUsername(username)
            if (user != null && user.passwordHash == passwordHash) {
                withContext(Dispatchers.Main) {
                    _currentUser.value = user
                    onSuccess()
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Username atau password salah")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.Main) {
            _currentUser.value = null
        }
    }

    fun registerUser(user: UserEntity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertUser(user)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Gagal mendaftar user")
                }
            }
        }
    }

    fun getUsersByRole(role: String, onResult: (List<UserEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val users = repository.getUsersByRole(role)
            withContext(Dispatchers.Main) {
                onResult(users)
            }
        }
    }

    fun deleteUser(user: UserEntity, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(user)
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
