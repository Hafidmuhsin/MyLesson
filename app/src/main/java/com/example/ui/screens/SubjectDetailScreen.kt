package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.People
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FolderShared
import com.example.util.GoogleDriveManager
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AssignmentEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TopicEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassmorphicCanvas
import com.example.ui.theme.CollegeBlue
import com.example.ui.theme.CollegeNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subject: SubjectEntity,
    topics: List<TopicEntity>,
    assignments: List<AssignmentEntity>,
    onBack: () -> Unit,
    onTopicClick: (TopicEntity) -> Unit,
    onAssignmentClick: (AssignmentEntity) -> Unit,
    onAddTopic: (unit: String, name: String, hrs: Float, date: String, obj: String, notes: String) -> Unit,
    onAddAssignment: (title: String, desc: String, date: String, marks: Int) -> Unit,
    onToggleTopicCovered: (TopicEntity, Boolean) -> Unit,
    onOpenAiSyllabus: () -> Unit,
    onDeleteSubject: (SubjectEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val accentColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        CollegeBlue
    }

    val coveredCount = topics.count { it.isCovered }
    val totalTopics = topics.size
    val overallProgress = if (totalTopics > 0) coveredCount.toFloat() / totalTopics else 0f

    GlassmorphicCanvas {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                            Text(
                                text = "${subject.gradeClass} • Enrolled: Roll 1 to ${subject.totalRolls}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        val context = LocalContext.current
                        IconButton(
                            onClick = {
                                val url = GoogleDriveManager.generateClassroomFolderUrl(subject.id, subject.name, subject.gradeClass)
                                GoogleDriveManager.syncSubjectToClassroomDrive(context, subject.id, subject.name, subject.gradeClass)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.testTag("open_subject_drive_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Course Drive Folder",
                                tint = CollegeBlue
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Course",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White.copy(alpha = 0.85f)
                    )
                )
            },
            floatingActionButton = {
                when (selectedTab) {
                    0 -> FloatingActionButton(
                        onClick = { showAddTopicDialog = true },
                        containerColor = CollegeBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_topic_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Lecture Topic")
                    }
                    1 -> FloatingActionButton(
                        onClick = { showAddAssignmentDialog = true },
                        containerColor = CollegeBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_assignment_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create Coursework")
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Course Progress Banner
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    containerColor = Color.White.copy(alpha = 0.88f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Syllabus Completed: $coveredCount / $totalTopics Lecture Modules (${(overallProgress * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CollegeBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${subject.totalRolls} Students Enrolled",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CollegeBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CollegeBlue,
                            trackColor = CollegeBlue.copy(alpha = 0.15f)
                        )
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White.copy(alpha = 0.80f),
                    contentColor = CollegeBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Syllabus ($totalTopics)", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Outlined.Book, contentDescription = null) },
                        modifier = Modifier.testTag("topics_tab")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Coursework (${assignments.size})", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.AutoMirrored.Outlined.Assignment, contentDescription = null) },
                        modifier = Modifier.testTag("assignments_tab")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("AI Syllabus", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = EmeraldSuccess) },
                        modifier = Modifier.testTag("ai_tab")
                    )
                }

                when (selectedTab) {
                    0 -> TopicsTabContent(
                        topics = topics,
                        accentColor = accentColor,
                        onTopicClick = onTopicClick,
                        onToggleCovered = onToggleTopicCovered,
                        onAddTopicClick = { showAddTopicDialog = true }
                    )
                    1 -> AssignmentsTabContent(
                        assignments = assignments,
                        accentColor = accentColor,
                        onAssignmentClick = onAssignmentClick,
                        onCreateAssignmentClick = { showAddAssignmentDialog = true }
                    )
                    2 -> AiSyllabusTabContent(
                        subject = subject,
                        onOpenAiSyllabus = onOpenAiSyllabus
                    )
                }
            }
        }
    }

    if (showAddTopicDialog) {
        AddTopicDialog(
            onDismiss = { showAddTopicDialog = false },
            onConfirm = { unit, name, hrs, date, obj, notes ->
                onAddTopic(unit, name, hrs, date, obj, notes)
                showAddTopicDialog = false
            }
        )
    }

    if (showAddAssignmentDialog) {
        AddAssignmentDialog(
            totalRolls = subject.totalRolls,
            onDismiss = { showAddAssignmentDialog = false },
            onConfirm = { title, desc, date, maxMarks ->
                onAddAssignment(title, desc, date, maxMarks)
                showAddAssignmentDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Course?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${subject.name}'? This will archive its syllabus topics, coursework roll records, and Google Drive folder structure.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSubject(subject)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Course")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TopicsTabContent(
    topics: List<TopicEntity>,
    accentColor: Color,
    onTopicClick: (TopicEntity) -> Unit,
    onToggleCovered: (TopicEntity, Boolean) -> Unit,
    onAddTopicClick: () -> Unit
) {
    if (topics.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White.copy(alpha = 0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = CollegeBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Lecture Topics Added",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add syllabus units, lecture slides, and reading materials for students.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddTopicClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Lecture Topic")
                    }
                }
            }
        }
    } else {
        val groupedTopics = topics.groupBy { it.unitTitle.ifEmpty { "General Module" } }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedTopics.forEach { (unitName, unitTopics) ->
                item {
                    Text(
                        text = unitName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavy,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                items(unitTopics, key = { it.id }) { topic ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTopicClick(topic) }
                            .testTag("topic_card_${topic.id}"),
                        containerColor = Color.White.copy(alpha = 0.88f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = topic.isCovered,
                                onCheckedChange = { checked -> onToggleCovered(topic, checked) },
                                modifier = Modifier.testTag("topic_checkbox_${topic.id}")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topic.topicName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (topic.isCovered) MaterialTheme.colorScheme.onSurfaceVariant else CollegeNavy
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${topic.estimatedHours} hrs lecture",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (topic.targetDate.isNotEmpty()) {
                                        Text(
                                            text = " • Target: ${topic.targetDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = CollegeBlue
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun AssignmentsTabContent(
    assignments: List<AssignmentEntity>,
    accentColor: Color,
    onAssignmentClick: (AssignmentEntity) -> Unit,
    onCreateAssignmentClick: () -> Unit
) {
    if (assignments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White.copy(alpha = 0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = CollegeBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Coursework Created",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create lab assignments, term projects, or midterms with automatic student roll submission tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onCreateAssignmentClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Coursework")
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(assignments, key = { it.id }) { assignment ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAssignmentClick(assignment) }
                        .testTag("assignment_card_${assignment.id}"),
                    containerColor = Color.White.copy(alpha = 0.88f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = assignment.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavy
                                )
                                if (assignment.dueDate.isNotEmpty()) {
                                    Text(
                                        text = "Due Date: ${assignment.dueDate} • Max Marks: ${assignment.maxMarks}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = CollegeBlue.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = CollegeBlue
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Student Roll Register",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CollegeBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (assignment.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = assignment.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tap to open Submission & Grades Roll Register",
                                style = MaterialTheme.typography.labelSmall,
                                color = CollegeBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = CollegeBlue
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun AiSyllabusTabContent(
    subject: SubjectEntity,
    onOpenAiSyllabus: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White.copy(alpha = 0.88f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = EmeraldSuccess
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AI Course Curriculum Planner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CollegeNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Paste course outline or syllabus text for '${subject.name}'. Gemini AI will format units, lecture topics, estimated hours, learning outcomes, and auto-sync with Google Drive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onOpenAiSyllabus,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("launch_ai_planner_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Launch AI Curriculum Generator")
                }
            }
        }
    }
}

@Composable
private fun AddTopicDialog(
    onDismiss: () -> Unit,
    onConfirm: (unit: String, name: String, hrs: Float, date: String, obj: String, notes: String) -> Unit
) {
    var unitTitle by remember { mutableStateOf("Module 1: Fundamentals") }
    var topicName by remember { mutableStateOf("") }
    var estimatedHoursText by remember { mutableStateOf("2.0") }
    var targetDate by remember { mutableStateOf("2026-08-15") }
    var learningObjectives by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Syllabus Topic", fontWeight = FontWeight.Bold, color = CollegeNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = unitTitle,
                    onValueChange = { unitTitle = it },
                    label = { Text("Syllabus Module / Unit (e.g. Module 1)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unit_title_input")
                )
                OutlinedTextField(
                    value = topicName,
                    onValueChange = { topicName = it },
                    label = { Text("Lecture Topic (e.g. Tree Traversal Algorithms)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = estimatedHoursText,
                        onValueChange = { estimatedHoursText = it },
                        label = { Text("Lecture Hrs") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetDate,
                        onValueChange = { targetDate = it },
                        label = { Text("Target Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = learningObjectives,
                    onValueChange = { learningObjectives = it },
                    label = { Text("Learning Objectives") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Lecture Notes & Reference Bibliography") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hrs = estimatedHoursText.toFloatOrNull() ?: 2.0f
                    onConfirm(unitTitle, topicName, hrs, targetDate, learningObjectives, notes)
                },
                modifier = Modifier.testTag("confirm_add_topic_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue)
            ) {
                Text("Save Topic")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddAssignmentDialog(
    totalRolls: Int,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, date: String, maxMarks: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-08-15") }
    var maxMarksText by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Coursework / Exam Roll Register", fontWeight = FontWeight.Bold, color = CollegeNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Will automatically generate submission matrix for Roll #1 to Roll #$totalRolls.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CollegeBlue
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Coursework Title (e.g. Lab Project 1)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assignment_title_input")
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Coursework Instructions & Problem Statement") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxMarksText,
                        onValueChange = { maxMarksText = it.filter { c -> c.isDigit() } },
                        label = { Text("Max Marks") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marks = maxMarksText.toIntOrNull() ?: 20
                    onConfirm(title, desc, dueDate, marks)
                },
                modifier = Modifier.testTag("confirm_add_assignment_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue)
            ) {
                Text("Create Coursework")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
