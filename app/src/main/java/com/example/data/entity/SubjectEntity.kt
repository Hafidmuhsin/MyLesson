package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gradeClass: String,
    val colorHex: String = "#4F46E5",
    val totalRolls: Int = 60,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
