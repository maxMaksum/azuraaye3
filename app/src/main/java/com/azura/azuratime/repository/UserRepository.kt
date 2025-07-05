package com.azura.azuratime.repository

import com.azura.azuratime.db.UserDao
import com.azura.azuratime.db.UserEntity

class UserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun getUserByUsername(username: String) = userDao.getUserByUsername(username)
    suspend fun getUsersByRole(role: String) = userDao.getUsersByRole(role)
    suspend fun getAllUsers() = userDao.getAllUsers()
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
}
