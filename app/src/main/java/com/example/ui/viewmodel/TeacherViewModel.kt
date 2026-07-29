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
            if (name.isBlank()) {
                _userMessage.value = "Subject name cannot be empty"
                return@launch
            }
            val subject = SubjectEntity(
                name = name.trim(),
                gradeClass = gradeClass.trim().ifEmpty { "General" },
                colorHex = colorHex,
                totalRolls = if (totalRolls > 0) totalRolls else 60,
                description = description.trim()
            )
            val id = repository.insertSubject(subject)
            _selectedSubjectId.value = id
            _userMessage.value = "Subject '${subject.name}' added successfully!"
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            _userMessage.value = "Subject updated"
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            if (_selectedSubjectId.value == subject.id) {
                _selectedSubjectId.value = null
            }
            _userMessage.value = "Subject deleted"
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
            if (topicName.isBlank()) {
                _userMessage.value = "Topic name cannot be empty"
                return@launch
            }
            val topic = TopicEntity(
                subjectId = subjectId,
                unitTitle = unitTitle.trim().ifEmpty { "General Unit" },
                topicName = topicName.trim(),
                estimatedHours = estimatedHours,
                targetDate = targetDate.trim(),
                isCovered = false,
                coveragePercentage = 0,
                learningObjectives = learningObjectives.trim(),
                notes = notes.trim()
            )
            repository.insertTopic(topic)
            _userMessage.value = "Topic '$topicName' added!"
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
            if (title.isBlank() || uriOrContent.isBlank()) {
                _userMessage.value = "Title and link/file content are required"
                return@launch
            }
            val source = TopicSourceEntity(
                topicId = topicId,
                title = title.trim(),
                sourceType = sourceType,
                uriOrContent = uriOrContent.trim()
            )
            repository.insertSource(source)
            _userMessage.value = "Source attachment added!"
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
            if (title.isBlank()) {
                _userMessage.value = "Assignment title is required"
                return@launch
            }
            val assignment = AssignmentEntity(
                subjectId = subjectId,
                title = title.trim(),
                description = description.trim(),
                dueDate = dueDate.trim(),
                maxMarks = if (maxMarks > 0) maxMarks else 100
            )
            repository.createAssignmentWithRolls(assignment, totalRolls)
            _userMessage.value = "Assignment created with $totalRolls student roll numbers!"
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
            _aiState.value = AiGeneratorUiState.Idle
            _userMessage.value = "Successfully imported ${topicsToInsert.size} AI lesson topics into Subject!"
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
