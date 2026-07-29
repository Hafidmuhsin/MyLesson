package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.StudentSubmissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentSubmissionDao {
    @Query("SELECT * FROM student_submissions WHERE assignmentId = :assignmentId ORDER BY rollNumber ASC")
    fun getSubmissionsForAssignment(assignmentId: Long): Flow<List<StudentSubmissionEntity>>

    @Query("SELECT * FROM student_submissions WHERE assignmentId = :assignmentId ORDER BY rollNumber ASC")
    suspend fun getSubmissionsForAssignmentOneShot(assignmentId: Long): List<StudentSubmissionEntity>

    @Query("SELECT * FROM student_submissions WHERE assignmentId = :assignmentId AND rollNumber = :rollNumber LIMIT 1")
    suspend fun getSubmissionByRoll(assignmentId: Long, rollNumber: Int): StudentSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: StudentSubmissionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<StudentSubmissionEntity>)

    @Update
    suspend fun updateSubmission(submission: StudentSubmissionEntity)

    @Query("UPDATE student_submissions SET isDone = :isDone, submissionDate = :date WHERE assignmentId = :assignmentId")
    suspend fun updateAllDoneStatus(assignmentId: Long, isDone: Boolean, date: String)

    @Delete
    suspend fun deleteSubmission(submission: StudentSubmissionEntity)
}
