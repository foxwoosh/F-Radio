package com.foxwoosh.radio.data.database

import com.foxwoosh.radio.data.database.models.CurrentUserDb
import com.foxwoosh.radio.domain.models.CurrentUser

fun CurrentUserDb.map() = CurrentUser(id, login, name, email)

fun CurrentUser.map() = CurrentUserDb(
    id = id,
    login = login,
    name = name,
    email = email
)