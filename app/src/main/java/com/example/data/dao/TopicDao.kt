package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY orderIndex ASC, id ASC")
    fun getTopicsForSubject(subjectId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY orderIndex ASC, id ASC")
    suspend fun getTopicsForSubjectOneShot(subjectId: Long): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun getTopicById(topicId: Long): Flow<TopicEntity?>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicByIdOneShot(topicId: Long): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>): List<Long>

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE subjectId = :subjectId")
    suspend fun deleteTopicsForSubject(subjectId: Long)
}
