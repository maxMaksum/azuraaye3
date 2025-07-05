package com.azura.azuratime.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String, // Simpan hash bukan plaintext!
    val name: String,
    val role: String, // admin, guru, siswa
    val createdAt: Long
)
