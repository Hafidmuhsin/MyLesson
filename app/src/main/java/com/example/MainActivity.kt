package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.view.WindowManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.security.AppSecurityManager
import com.example.ui.components.AppLockOverlay
import com.example.ui.screens.AiSyllabusGeneratorScreen
import com.example.ui.screens.AssignmentRollScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SubjectDetailScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.screens.TopicDetailScreen
import com.example.ui.theme.TeacherPlanTheme
import com.example.ui.viewmodel.TeacherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TeacherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (AppSecurityManager.isFlagSecureEnabled(this)) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        setContent {
            TeacherPlanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TeacherPlanApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TeacherPlanApp(viewModel: TeacherViewModel) {
    val navController = rememberNavController()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val timetableSlots by viewModel.timetableSlots.collectAsStateWithLifecycle()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    var isAppUnlocked by remember { mutableStateOf(!AppSecurityManager.isPinEnabled(context)) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    if (!isAppUnlocked && AppSecurityManager.isPinEnabled(context)) {
        AppLockOverlay(onUnlocked = { isAppUnlocked = true })
    }

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        // 1. Dashboard Screen
        composable("dashboard") {
            DashboardScreen(
                subjects = subjects,
                onSelectSubject = { subjectId ->
                    viewModel.selectSubject(subjectId)
                    navController.navigate("subject_detail/$subjectId")
                },
                onOpenAiPlanner = {
                    navController.navigate("ai_planner")
                },
                onOpenTimetable = {
                    navController.navigate("timetable")
                },
                onAddSubject = { name, grade, color, rolls, desc ->
                    viewModel.addSubject(name, grade, color, rolls, desc)
                }
            )
        }

        // 2. Subject Detail Screen
        composable(
            route = "subject_detail/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: 0L
            val subjectFlow = remember(subjectId) { viewModel.repository.getSubjectById(subjectId) }
            val subject by subjectFlow.collectAsStateWithLifecycle(initialValue = null)

            val topicsFlow = remember(subjectId) { viewModel.repository.getTopicsForSubject(subjectId) }
            val topics by topicsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

            val assignmentsFlow = remember(subjectId) { viewModel.repository.getAssignmentsForSubject(subjectId) }
            val assignments by assignmentsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

            subject?.let { currentSubject ->
                SubjectDetailScreen(
                    subject = currentSubject,
                    topics = topics,
                    assignments = assignments,
                    onBack = { navController.popBackStack() },
                    onTopicClick = { topic ->
                        navController.navigate("topic_detail/${currentSubject.id}/${topic.id}")
                    },
                    onAssignmentClick = { assignment ->
                        navController.navigate("assignment_rolls/${currentSubject.id}/${assignment.id}")
                    },
                    onAddTopic = { unit, name, hrs, date, obj, notes ->
                        viewModel.addTopic(currentSubject.id, unit, name, hrs, date, obj, notes)
                    },
                    onAddAssignment = { title, desc, date, maxMarks ->
                        viewModel.createAssignment(
                            subjectId = currentSubject.id,
                            title = title,
                            description = desc,
                            dueDate = date,
                            maxMarks = maxMarks,
                            totalRolls = currentSubject.totalRolls
                        )
                    },
                    onToggleTopicCovered = { topic, isCovered ->
                        viewModel.updateTopicCoverage(topic, isCovered, if (isCovered) 100 else topic.coveragePercentage)
                    },
                    onOpenAiSyllabus = {
                        navController.navigate("ai_planner")
                    },
                    onDeleteSubject = { subj ->
                        viewModel.deleteSubject(subj)
                        navController.popBackStack()
                    }
                )
            }
        }

        // 3. Topic Detail Screen
        composable(
            route = "topic_detail/{subjectId}/{topicId}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.LongType },
                navArgument("topicId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: 0L
            val topicFlow = remember(topicId) { viewModel.repository.getTopicById(topicId) }
            val topic by topicFlow.collectAsStateWithLifecycle(initialValue = null)

            val sourcesFlow = remember(topicId) { viewModel.repository.getSourcesForTopic(topicId) }
            val sources by sourcesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

            topic?.let { currentTopic ->
                TopicDetailScreen(
                    topic = currentTopic,
                    sources = sources,
                    onBack = { navController.popBackStack() },
                    onUpdateCoverage = { t, covered, pct ->
                        viewModel.updateTopicCoverage(t, covered, pct)
                    },
                    onAddSource = { title, type, content ->
                        viewModel.addTopicSource(currentTopic.id, title, type, content)
                    },
                    onDeleteSource = { src ->
                        viewModel.deleteSource(src)
                    },
                    onDeleteTopic = { t ->
                        viewModel.deleteTopic(t)
                        navController.popBackStack()
                    }
                )
            }
        }

        // 4. Assignment Roll Matrix Screen
        composable(
            route = "assignment_rolls/{subjectId}/{assignmentId}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.LongType },
                navArgument("assignmentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getLong("assignmentId") ?: 0L
            val assignmentFlow = remember(assignmentId) { viewModel.repository.getAssignmentById(assignmentId) }
            val assignment by assignmentFlow.collectAsStateWithLifecycle(initialValue = null)

            val submissionsFlow = remember(assignmentId) { viewModel.repository.getSubmissionsForAssignment(assignmentId) }
            val submissions by submissionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

            assignment?.let { currentAssignment ->
                AssignmentRollScreen(
                    assignment = currentAssignment,
                    submissions = submissions,
                    onBack = { navController.popBackStack() },
                    onToggleDone = { sub ->
                        viewModel.toggleSubmissionDone(sub)
                    },
                    onUpdateMarks = { sub, marks, remarks ->
                        viewModel.updateSubmissionMarks(sub, marks, remarks)
                    },
                    onMarkAllDone = { isDone ->
                        viewModel.markAllRollsCompletion(currentAssignment.id, isDone)
                    }
                )
            }
        }

        // 5. AI Syllabus Generator Screen
        composable("ai_planner") {
            val currentSubject = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()
            AiSyllabusGeneratorScreen(
                subjects = subjects,
                selectedSubject = currentSubject,
                aiState = aiState,
                onBack = {
                    viewModel.resetAiState()
                    navController.popBackStack()
                },
                onGenerate = { input, subjName, grade ->
                    viewModel.generateAiLessonPlan(input, subjName, grade)
                },
                onApplyToSubject = { subjId, result ->
                    viewModel.applyAiGeneratedTopicsToSubject(subjId, result)
                    navController.popBackStack()
                },
                onReadDocumentFromStorage = { uri, onRead ->
                    viewModel.readDocumentContentFromUri(uri, onRead)
                }
            )
        }

        // 6. My Timetable Screen
        composable("timetable") {
            TimetableScreen(
                subjects = subjects,
                timetableSlots = timetableSlots,
                onBack = { navController.popBackStack() },
                onAutoSaveSlot = { day, time, subjId, customName, roomNote, isLunch ->
                    viewModel.autoSaveTimetableSlot(day, time, subjId, customName, roomNote, isLunch)
                },
                onClearSlot = { day, time ->
                    viewModel.clearTimetableSlot(day, time)
                },
                onClearAll = {
                    viewModel.clearAllTimetableSlots()
                }
            )
        }
    }
}
