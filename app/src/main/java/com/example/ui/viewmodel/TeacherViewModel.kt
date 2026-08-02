package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiSyllabusService
import com.example.ai.GeneratedSyllabusResult
import com.example.data.database.AppDatabase
import com.example.data.entity.AssignmentEntity
import com.example.data.entity.StudentSubmissionEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TimetableSlotEntity
import com.example.data.entity.TopicEntity
import com.example.data.entity.TopicSourceEntity
import com.example.data.repository.TeacherRepository
import com.example.security.AppSecurityManager
import com.example.util.GoogleDriveManager
import com.example.util.TeacherStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface AiGeneratorUiState {
    object Idle : AiGeneratorUiState
    object Loading : AiGeneratorUiState
    data class Success(val result: GeneratedSyllabusResult) : AiGeneratorUiState
    data class Error(val message: String) : AiGeneratorUiState
}

class TeacherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = TeacherRepository(db)

    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTopics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val timetableSlots: StateFlow<List<TimetableSlotEntity>> = repository.timetableSlots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    private val _aiState = MutableStateFlow<AiGeneratorUiState>(AiGeneratorUiState.Idle)
    val aiState: StateFlow<AiGeneratorUiState> = _aiState.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    fun selectSubject(subjectId: Long) {
        _selectedSubjectId.value = subjectId
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // --- Subject Operations ---
    fun addSubject(name: String, gradeClass: String, colorHex: String, totalRolls: Int, description: String) {
        viewModelScope.launch {
            val cleanName = AppSecurityManager.sanitizeInput(name, 100)
            if (cleanName.isBlank()) {
                _userMessage.value = "Subject name cannot be empty"
                return@launch
            }
            val cleanGrade = AppSecurityManager.sanitizeInput(gradeClass, 50).ifEmpty { "General" }
            val subject = SubjectEntity(
                name = cleanName,
                gradeClass = cleanGrade,
                colorHex = colorHex,
                totalRolls = if (totalRolls > 0) totalRolls else 60,
                description = AppSecurityManager.sanitizeInput(description, 500)
            )
            val id = repository.insertSubject(subject)
            _selectedSubjectId.value = id

            // Parallel Google Drive Folder Creation for this specific subject
            GoogleDriveManager.createSubjectDriveFolder(
                context = getApplication(),
                subjectId = id,
                subjectName = cleanName,
                gradeClass = cleanGrade
            )

            _userMessage.value = "Subject '${subject.name}' added & Google Drive Folder created!"
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            val sanitized = subject.copy(
                name = AppSecurityManager.sanitizeInput(subject.name, 100),
                gradeClass = AppSecurityManager.sanitizeInput(subject.gradeClass, 50),
                description = AppSecurityManager.sanitizeInput(subject.description, 500)
            )
            repository.updateSubject(sanitized)
            
            // Sync updated subject drive folder name
            GoogleDriveManager.syncSubjectToClassroomDrive(
                context = getApplication(),
                subjectId = sanitized.id,
                subjectName = sanitized.name,
                gradeClass = sanitized.gradeClass
            )
            _userMessage.value = "Subject updated & Drive folder synced"
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            if (_selectedSubjectId.value == subject.id) {
                _selectedSubjectId.value = null
            }

            // Parallel Google Drive Folder Deletion / Archiving for this specific subject
            GoogleDriveManager.deleteSubjectDriveFolder(
                context = getApplication(),
                subjectId = subject.id,
                subjectName = subject.name,
                gradeClass = subject.gradeClass
            )

            _userMessage.value = "Subject '${subject.name}' & Drive folder deleted"
        }
    }

    // --- Topic Operations ---
    fun addTopic(
        subjectId: Long,
        unitTitle: String,
        topicName: String,
        estimatedHours: Float,
        targetDate: String,
        learningObjectives: String,
        notes: String
    ) {
        viewModelScope.launch {
            val cleanTopicName = AppSecurityManager.sanitizeInput(topicName, 150)
            if (cleanTopicName.isBlank()) {
                _userMessage.value = "Topic name cannot be empty"
                return@launch
            }
            val topic = TopicEntity(
                subjectId = subjectId,
                unitTitle = AppSecurityManager.sanitizeInput(unitTitle, 100).ifEmpty { "General Unit" },
                topicName = cleanTopicName,
                estimatedHours = estimatedHours,
                targetDate = AppSecurityManager.sanitizeInput(targetDate, 50),
                isCovered = false,
                coveragePercentage = 0,
                learningObjectives = AppSecurityManager.sanitizeInput(learningObjectives, 1000),
                notes = AppSecurityManager.sanitizeInput(notes, 1000)
            )
            repository.insertTopic(topic)
            _userMessage.value = "Topic '$cleanTopicName' added!"
        }
    }

    fun updateTopicCoverage(topic: TopicEntity, isCovered: Boolean, coveragePercentage: Int) {
        viewModelScope.launch {
            val updated = topic.copy(
                isCovered = isCovered,
                coveragePercentage = if (isCovered) 100 else coveragePercentage
            )
            repository.updateTopic(updated)
        }
    }

    fun updateTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.updateTopic(topic)
            _userMessage.value = "Topic updated"
        }
    }

    fun deleteTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
            _userMessage.value = "Topic deleted"
        }
    }

    // --- Topic Sources ---
    fun addTopicSource(topicId: Long, title: String, sourceType: String, uriOrContent: String) {
        viewModelScope.launch {
            val cleanTitle = AppSecurityManager.sanitizeInput(title, 100)
            val cleanContent = AppSecurityManager.sanitizeInput(uriOrContent, 1000)
            if (cleanTitle.isBlank() || cleanContent.isBlank()) {
                _userMessage.value = "Title and link/file content are required"
                return@launch
            }
            val source = TopicSourceEntity(
                topicId = topicId,
                title = cleanTitle,
                sourceType = sourceType,
                uriOrContent = cleanContent
            )
            repository.insertSource(source)

            // Auto-upload attached file/resource to exact subject Google Drive folder
            val topic = repository.getTopicByIdOneShot(topicId)
            if (topic != null) {
                val subject = repository.getSubjectByIdOneShot(topic.subjectId)
                if (subject != null) {
                    GoogleDriveManager.uploadAttachmentToSubjectDriveFolder(
                        context = getApplication(),
                        subjectName = subject.name,
                        gradeClass = subject.gradeClass,
                        subfolderCategory = "4. Reference Materials & Resources",
                        fileName = cleanTitle
                    )
                }
            }

            _userMessage.value = "Attachment added & uploaded to Subject's Drive folder!"
        }
    }

    fun deleteSource(source: TopicSourceEntity) {
        viewModelScope.launch {
            repository.deleteSource(source)
            _userMessage.value = "Attachment removed"
        }
    }

    // --- Assignment & Student Roll Submissions ---
    fun createAssignment(subjectId: Long, title: String, description: String, dueDate: String, maxMarks: Int, totalRolls: Int = 60) {
        viewModelScope.launch {
            val cleanTitle = AppSecurityManager.sanitizeInput(title, 120)
            if (cleanTitle.isBlank()) {
                _userMessage.value = "Assignment title is required"
                return@launch
            }
            val assignment = AssignmentEntity(
                subjectId = subjectId,
                title = cleanTitle,
                description = AppSecurityManager.sanitizeInput(description, 500),
                dueDate = AppSecurityManager.sanitizeInput(dueDate, 50),
                maxMarks = if (maxMarks > 0) maxMarks else 100
            )
            repository.createAssignmentWithRolls(assignment, totalRolls)

            // Auto-upload assignment file to exact subject Google Drive folder
            val subject = repository.getSubjectByIdOneShot(subjectId)
            if (subject != null) {
                GoogleDriveManager.uploadAttachmentToSubjectDriveFolder(
                    context = getApplication(),
                    subjectName = subject.name,
                    gradeClass = subject.gradeClass,
                    subfolderCategory = "2. Classwork & Assignments",
                    fileName = "$cleanTitle.pdf"
                )
            }

            _userMessage.value = "Assignment created with $totalRolls student rolls & uploaded to Drive!"
        }
    }

    fun toggleSubmissionDone(submission: StudentSubmissionEntity) {
        viewModelScope.launch {
            val newDone = !submission.isDone
            val currentDate = if (newDone) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) else ""
            val updated = submission.copy(
                isDone = newDone,
                submissionDate = currentDate
            )
            repository.updateSubmission(updated)
        }
    }

    fun updateSubmissionMarks(submission: StudentSubmissionEntity, marks: Float?, remarks: String) {
        viewModelScope.launch {
            val updated = submission.copy(
                marksObtained = marks,
                remarks = remarks,
                isDone = if (marks != null) true else submission.isDone
            )
            repository.updateSubmission(updated)
            _userMessage.value = "Roll #${submission.rollNumber} marks updated!"
        }
    }

    fun markAllRollsCompletion(assignmentId: Long, isDone: Boolean) {
        viewModelScope.launch {
            repository.updateAllRollsCompletion(assignmentId, isDone)
            _userMessage.value = if (isDone) "Marked all roll numbers as Submitted!" else "Cleared all submission marks!"
        }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
            _userMessage.value = "Assignment deleted"
        }
    }

    // --- Timetable Operations ---
    fun autoSaveTimetableSlot(
        dayOfWeek: String,
        timeSlot: String,
        subjectId: Long?,
        customSubjectName: String = "",
        roomOrNote: String = "",
        isLunchBreak: Boolean = false
    ) {
        viewModelScope.launch {
            repository.saveTimetableSlot(
                dayOfWeek = dayOfWeek,
                timeSlot = timeSlot,
                subjectId = subjectId,
                customSubjectName = customSubjectName,
                roomOrNote = roomOrNote,
                isLunchBreak = isLunchBreak
            )
        }
    }

    fun clearTimetableSlot(dayOfWeek: String, timeSlot: String) {
        viewModelScope.launch {
            repository.clearTimetableSlot(dayOfWeek, timeSlot)
        }
    }

    fun clearAllTimetableSlots() {
        viewModelScope.launch {
            repository.clearAllTimetableSlots()
            _userMessage.value = "Timetable reset successfully"
        }
    }

    // --- AI Integration: Lesson Plan from Syllabus ---
    fun generateAiLessonPlan(syllabusInput: String, subjectName: String, gradeLevel: String) {
        if (syllabusInput.isBlank() || subjectName.isBlank()) {
            _userMessage.value = "Please enter subject name and syllabus text/prompt"
            return
        }
        viewModelScope.launch {
            _aiState.value = AiGeneratorUiState.Loading
            val result = GeminiSyllabusService.generateLessonPlanFromSyllabus(
                syllabusContent = syllabusInput,
                subjectName = subjectName,
                gradeLevel = gradeLevel
            )
            result.fold(
                onSuccess = { generated ->
                    _aiState.value = AiGeneratorUiState.Success(generated)
                },
                onFailure = { err ->
                    _aiState.value = AiGeneratorUiState.Error(err.localizedMessage ?: "AI Generation Error")
                }
            )
        }
    }

    fun applyAiGeneratedTopicsToSubject(subjectId: Long, generated: GeneratedSyllabusResult) {
        viewModelScope.launch {
            val topicsToInsert = generated.topics.mapIndexed { idx, item ->
                TopicEntity(
                    subjectId = subjectId,
                    unitTitle = item.unitTitle,
                    topicName = item.topicName,
                    estimatedHours = item.estimatedHours,
                    targetDate = "",
                    isCovered = false,
                    coveragePercentage = 0,
                    learningObjectives = item.learningObjectives,
                    notes = item.teachingNotes,
                    orderIndex = idx + 1
                )
            }
            db.topicDao().insertTopics(topicsToInsert)

            // Auto-upload AI generated lesson plan document to subject's Google Drive folder
            val subject = repository.getSubjectByIdOneShot(subjectId)
            if (subject != null) {
                GoogleDriveManager.uploadAttachmentToSubjectDriveFolder(
                    context = getApplication(),
                    subjectName = subject.name,
                    gradeClass = subject.gradeClass,
                    subfolderCategory = "1. Syllabus & Lesson Plans",
                    fileName = "${subject.name}_AI_Generated_Lesson_Plan.pdf"
                )
            }

            _aiState.value = AiGeneratorUiState.Idle
            _userMessage.value = "Successfully imported ${topicsToInsert.size} AI lesson topics & uploaded to Drive!"
        }
    }

    fun resetAiState() {
        _aiState.value = AiGeneratorUiState.Idle
    }

    // --- Storage File Reading ---
    fun readDocumentContentFromUri(uri: Uri, onRead: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val content = TeacherStorageManager.readTextFromUri(getApplication(), uri)
                onRead(content)
                _userMessage.value = "Loaded text document from storage successfully!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to read file: ${e.message}"
            }
        }
    }
}
