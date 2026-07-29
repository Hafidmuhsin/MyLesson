package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AssignmentDao
import com.example.data.dao.StudentSubmissionDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TimetableDao
import com.example.data.dao.TopicDao
import com.example.data.dao.TopicSourceDao
import com.example.data.entity.AssignmentEntity
import com.example.data.entity.StudentSubmissionEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TimetableSlotEntity
import com.example.data.entity.TopicEntity
import com.example.data.entity.TopicSourceEntity

@Database(
    entities = [
        SubjectEntity::class,
        TopicEntity::class,
        TopicSourceEntity::class,
        AssignmentEntity::class,
        StudentSubmissionEntity::class,
        TimetableSlotEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun topicSourceDao(): TopicSourceDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun studentSubmissionDao(): StudentSubmissionDao
    abstract fun timetableDao(): TimetableDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teacher_plan_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
