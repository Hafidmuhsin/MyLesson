package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val unitTitle: String,
    val topicName: String,
    val estimatedHours: Float = 2.0f,
    val targetDate: String = "",
    val isCovered: Boolean = false,
    val coveragePercentage: Int = 0,
    val learningObjectives: String = "",
    val notes: String = "",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
