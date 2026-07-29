package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.TopicSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicSourceDao {
    @Query("SELECT * FROM topic_sources WHERE topicId = :topicId ORDER BY id DESC")
    fun getSourcesForTopic(topicId: Long): Flow<List<TopicSourceEntity>>

    @Query("SELECT * FROM topic_sources WHERE topicId = :topicId ORDER BY id DESC")
    suspend fun getSourcesForTopicOneShot(topicId: Long): List<TopicSourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: TopicSourceEntity): Long

    @Delete
    suspend fun deleteSource(source: TopicSourceEntity)
}
