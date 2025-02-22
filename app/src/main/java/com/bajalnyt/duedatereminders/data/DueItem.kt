package com.bajalnyt.duedatereminders.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "due_items")
data class DueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dueDate: LocalDate
) 