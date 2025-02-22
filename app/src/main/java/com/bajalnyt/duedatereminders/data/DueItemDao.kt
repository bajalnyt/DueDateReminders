package com.bajalnyt.duedatereminders.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DueItemDao {
    @Query("SELECT * FROM due_items ORDER BY dueDate ASC")
    fun getAllItems(): Flow<List<DueItem>>

    @Insert
    suspend fun insert(item: DueItem)
    
    @Delete
    suspend fun delete(item: DueItem)
} 