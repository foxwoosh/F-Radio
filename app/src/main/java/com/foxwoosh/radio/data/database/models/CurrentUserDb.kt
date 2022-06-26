package com.foxwoosh.radio.data.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_user")
data class CurrentUserDb(
    @PrimaryKey
    val key: Int = 0,

    val id: Long,
    val name: String,
    val login: String,
    val email: String
)