package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    fun getAssignmentsForSubject(subjectId: Long): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE id = :assignmentId")
    fun getAssignmentById(assignmentId: Long): Flow<AssignmentEntity?>

    @Query("SELECT * FROM assignments WHERE id = :assignmentId")
    suspend fun getAssignmentByIdOneShot(assignmentId: Long): AssignmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity): Long

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)
}
