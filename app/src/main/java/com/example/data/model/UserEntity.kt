package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val avatarColorHex: String = "#A855F7"
)
