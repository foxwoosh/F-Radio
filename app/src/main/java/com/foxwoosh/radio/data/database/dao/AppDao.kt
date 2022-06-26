package com.foxwoosh.radio.data.database.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface AppDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(db: T): Long

    @Update
    fun update(db: T): Int
}