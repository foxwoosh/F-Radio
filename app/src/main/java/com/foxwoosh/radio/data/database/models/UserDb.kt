package com.foxwoosh.radio.data.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserDb(
    @PrimaryKey
    val id: Long = 0L,

    val login: String,
    val name: String,
    val email: String
)