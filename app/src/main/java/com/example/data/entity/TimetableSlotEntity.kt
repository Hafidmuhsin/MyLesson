package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timetable_slots",
    indices = [Index(value = ["dayOfWeek", "timeSlot"], unique = true)]
)
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: String,       // e.g. "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    val timeSlot: String,        // e.g. "09:00 AM - 10:00 AM"
    val subjectId: Long? = null, // ID from SubjectEntity
    val customSubjectName: String = "",
    val roomOrNote: String = "",
    val isLunchBreak: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
