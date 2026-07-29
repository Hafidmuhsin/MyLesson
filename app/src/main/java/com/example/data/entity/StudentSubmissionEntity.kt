package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "student_submissions",
    foreignKeys = [
        ForeignKey(
            entity = AssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("assignmentId"), Index(value = ["assignmentId", "rollNumber"], unique = true)]
)
data class StudentSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assignmentId: Long,
    val rollNumber: Int,
    val studentName: String = "",
    val isDone: Boolean = false,
    val marksObtained: Float? = null,
    val submissionDate: String = "",
    val remarks: String = ""
)
