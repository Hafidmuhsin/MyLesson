package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.entity.AssignmentEntity
import com.example.data.entity.StudentSubmissionEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TimetableSlotEntity
import com.example.data.entity.TopicEntity
import com.example.data.entity.TopicSourceEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TeacherRepository(private val db: AppDatabase) {

    val allSubjects: Flow<List<SubjectEntity>> = db.subjectDao().getAllSubjects()

    fun getSubjectById(subjectId: Long): Flow<SubjectEntity?> = db.subjectDao().getSubjectById(subjectId)

    fun getTopicsForSubject(subjectId: Long): Flow<List<TopicEntity>> = db.topicDao().getTopicsForSubject(subjectId)

    fun getTopicById(topicId: Long): Flow<TopicEntity?> = db.topicDao().getTopicById(topicId)

    fun getSourcesForTopic(topicId: Long): Flow<List<TopicSourceEntity>> = db.topicSourceDao().getSourcesForTopic(topicId)

    fun getAssignmentsForSubject(subjectId: Long): Flow<List<AssignmentEntity>> = db.assignmentDao().getAssignmentsForSubject(subjectId)

    fun getAssignmentById(assignmentId: Long): Flow<AssignmentEntity?> = db.assignmentDao().getAssignmentById(assignmentId)

    fun getSubmissionsForAssignment(assignmentId: Long): Flow<List<StudentSubmissionEntity>> =
        db.studentSubmissionDao().getSubmissionsForAssignment(assignmentId)

    suspend fun insertSubject(subject: SubjectEntity): Long = db.subjectDao().insertSubject(subject)

    suspend fun updateSubject(subject: SubjectEntity) = db.subjectDao().updateSubject(subject)

    suspend fun deleteSubject(subject: SubjectEntity) = db.subjectDao().deleteSubject(subject)

    suspend fun insertTopic(topic: TopicEntity): Long = db.topicDao().insertTopic(topic)

    suspend fun updateTopic(topic: TopicEntity) = db.topicDao().updateTopic(topic)

    suspend fun deleteTopic(topic: TopicEntity) = db.topicDao().deleteTopic(topic)

    suspend fun insertSource(source: TopicSourceEntity): Long = db.topicSourceDao().insertSource(source)

    suspend fun deleteSource(source: TopicSourceEntity) = db.topicSourceDao().deleteSource(source)

    suspend fun createAssignmentWithRolls(assignment: AssignmentEntity, totalRolls: Int = 60): Long {
        val assignmentId = db.assignmentDao().insertAssignment(assignment)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val submissions = (1..totalRolls).map { roll ->
            StudentSubmissionEntity(
                assignmentId = assignmentId,
                rollNumber = roll,
                studentName = "Roll #$roll",
                isDone = false,
                marksObtained = null,
                submissionDate = "",
                remarks = ""
            )
        }
        db.studentSubmissionDao().insertSubmissions(submissions)
        return assignmentId
    }

    suspend fun updateSubmission(submission: StudentSubmissionEntity) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val updated = if (submission.isDone && submission.submissionDate.isEmpty()) {
            submission.copy(submissionDate = currentDate)
        } else {
            submission
        }
        db.studentSubmissionDao().updateSubmission(updated)
    }

    suspend fun updateAllRollsCompletion(assignmentId: Long, isDone: Boolean) {
        val currentDate = if (isDone) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) else ""
        db.studentSubmissionDao().updateAllDoneStatus(assignmentId, isDone, currentDate)
    }

    suspend fun deleteAssignment(assignment: AssignmentEntity) = db.assignmentDao().deleteAssignment(assignment)

    // Timetable operations
    val timetableSlots: Flow<List<TimetableSlotEntity>> = db.timetableDao().getAllSlots()

    suspend fun saveTimetableSlot(
        dayOfWeek: String,
        timeSlot: String,
        subjectId: Long?,
        customSubjectName: String = "",
        roomOrNote: String = "",
        isLunchBreak: Boolean = false
    ) {
        val existing = db.timetableDao().getSlotByDayAndTime(dayOfWeek, timeSlot)
        val newSlot = TimetableSlotEntity(
            id = existing?.id ?: 0,
            dayOfWeek = dayOfWeek,
            timeSlot = timeSlot,
            subjectId = subjectId,
            customSubjectName = customSubjectName,
            roomOrNote = roomOrNote,
            isLunchBreak = isLunchBreak,
            updatedAt = System.currentTimeMillis()
        )
        db.timetableDao().insertOrUpdateSlot(newSlot)
    }

    suspend fun clearTimetableSlot(dayOfWeek: String, timeSlot: String) {
        db.timetableDao().deleteSlotByDayAndTime(dayOfWeek, timeSlot)
    }

    suspend fun clearAllTimetableSlots() {
        db.timetableDao().clearAllSlots()
    }

    // Seed default sample data if database is empty (Disabled per request - user starts clean)
    suspend fun prepopulateIfEmpty() {
        // No dummy data inserted. Users start with clean app state.
    }
}
