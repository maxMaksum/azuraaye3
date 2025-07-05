package com.azura.azuratime.db

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun getUsersByRole(role: String): List<UserEntity>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Delete
    suspend fun deleteUser(user: UserEntity)
}
