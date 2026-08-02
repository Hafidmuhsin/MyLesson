package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CloudSync
import com.example.util.GoogleDriveManager
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.example.data.entity.StudentSubmissionEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassmorphicCanvas
import com.example.ui.theme.CollegeBlue
import com.example.ui.theme.CollegeNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentRollScreen(
    assignment: AssignmentEntity,
    submissions: List<StudentSubmissionEntity>,
    onBack: () -> Unit,
    onToggleDone: (StudentSubmissionEntity) -> Unit,
    onUpdateMarks: (StudentSubmissionEntity, Float?, String) -> Unit,
    onMarkAllDone: (Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("ALL") } // ALL, DONE, PENDING
    var isGridView by remember { mutableStateOf(true) }
    var editingSubmission by remember { mutableStateOf<StudentSubmissionEntity?>(null) }

    val totalRolls = submissions.size
    val doneSubmissions = submissions.filter { it.isDone }
    val doneCount = doneSubmissions.size
    val pendingCount = totalRolls - doneCount
    val completionPercent = if (totalRolls > 0) (doneCount.toFloat() / totalRolls.toFloat()) else 0f

    val filteredSubmissions = remember(submissions, searchQuery, filterMode) {
        submissions.filter { sub ->
            val matchesQuery = searchQuery.isEmpty() ||
                    sub.rollNumber.toString().contains(searchQuery) ||
                    sub.studentName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filterMode) {
                "DONE" -> sub.isDone
                "PENDING" -> !sub.isDone
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    GlassmorphicCanvas {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = assignment.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                            Text(
                                text = "Coursework Submissions & Student Grades Register",
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
                                val uploaded = GoogleDriveManager.uploadAttachmentToSubjectDriveFolder(
                                    context = context,
                                    subjectName = "Coursework Grades",
                                    gradeClass = "Submissions",
                                    subfolderCategory = "3. Student Submissions & Grades",
                                    fileName = "Grades_${assignment.title}_Export.csv"
                                )
                                if (uploaded) {
                                    Toast.makeText(context, "Exported grades matrix & synced to Google Drive!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Grades matrix is already up to date on Google Drive", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("sync_assignment_drive_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Sync to Google Drive", tint = CollegeBlue)
                        }
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier.testTag("toggle_view_button")
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                                contentDescription = "Toggle Grid/List View"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White.copy(alpha = 0.85f)
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Stats Header
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White.copy(alpha = 0.88f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Submitted: $doneCount / $totalRolls Students (${(completionPercent * 100).toInt()}%)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavy
                                )
                                Text(
                                    text = "$pendingCount Pending • Max Marks: ${assignment.maxMarks}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row {
                                TextButton(
                                    onClick = { onMarkAllDone(true) },
                                    modifier = Modifier.testTag("mark_all_done_button")
                                ) {
                                    Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark All")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { completionPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CollegeBlue,
                            trackColor = CollegeBlue.copy(alpha = 0.15f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Controls: Search & Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Roll # or Student") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("roll_search_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = filterMode == "ALL",
                        onClick = { filterMode = "ALL" },
                        label = { Text("All ($totalRolls)") },
                        modifier = Modifier.testTag("filter_all_chip")
                    )
                    FilterChip(
                        selected = filterMode == "DONE",
                        onClick = { filterMode = "DONE" },
                        label = { Text("Submitted ($doneCount)") },
                        modifier = Modifier.testTag("filter_done_chip")
                    )
                    FilterChip(
                        selected = filterMode == "PENDING",
                        onClick = { filterMode = "PENDING" },
                        label = { Text("Pending ($pendingCount)") },
                        modifier = Modifier.testTag("filter_pending_chip")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Submissions Matrix Grid or List
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 75.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSubmissions, key = { it.id }) { sub ->
                            RollGridCell(
                                submission = sub,
                                onToggleDone = { onToggleDone(sub) },
                                onLongClick = { editingSubmission = sub }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSubmissions, key = { it.id }) { sub ->
                            RollListItem(
                                submission = sub,
                                maxMarks = assignment.maxMarks,
                                onToggleDone = { onToggleDone(sub) },
                                onEditMarks = { editingSubmission = sub }
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingSubmission != null) {
        EditSubmissionMarksDialog(
            submission = editingSubmission!!,
            maxMarks = assignment.maxMarks,
            onDismiss = { editingSubmission = null },
            onConfirm = { marks, remarks ->
                onUpdateMarks(editingSubmission!!, marks, remarks)
                editingSubmission = null
            }
        )
    }
}

@Composable
private fun RollGridCell(
    submission: StudentSubmissionEntity,
    onToggleDone: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = if (submission.isDone) EmeraldSuccess else Color.White.copy(alpha = 0.88f)
    val textColor = if (submission.isDone) Color.White else CollegeNavy

    GlassCard(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onToggleDone)
            .testTag("roll_cell_${submission.rollNumber}"),
        containerColor = if (submission.isDone) EmeraldSuccess else Color.White.copy(alpha = 0.88f),
        borderColor = if (submission.isDone) EmeraldSuccess else Color(0x332563EB)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#${submission.rollNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (submission.marksObtained != null) {
                    Text(
                        text = "${submission.marksObtained.toInt()}m",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (submission.isDone) Color.White.copy(alpha = 0.9f) else CollegeBlue
                    )
                } else if (submission.isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RollListItem(
    submission: StudentSubmissionEntity,
    maxMarks: Int,
    onToggleDone: () -> Unit,
    onEditMarks: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleDone)
            .testTag("roll_list_item_${submission.rollNumber}"),
        containerColor = Color.White.copy(alpha = 0.88f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = submission.isDone,
                    onCheckedChange = { onToggleDone() },
                    modifier = Modifier.testTag("roll_checkbox_${submission.rollNumber}")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Student Roll #${submission.rollNumber} ${submission.studentName.ifEmpty { "" }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavy
                    )
                    if (submission.isDone && submission.submissionDate.isNotEmpty()) {
                        Text(
                            text = "Submitted: ${submission.submissionDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (submission.marksObtained != null) {
                    Text(
                        text = "${submission.marksObtained.toInt()} / $maxMarks Marks",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CollegeBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = onEditMarks) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Marks", tint = CollegeBlue)
                }
            }
        }
    }
}

@Composable
private fun EditSubmissionMarksDialog(
    submission: StudentSubmissionEntity,
    maxMarks: Int,
    onDismiss: () -> Unit,
    onConfirm: (marks: Float?, remarks: String) -> Unit
) {
    var marksText by remember { mutableStateOf(submission.marksObtained?.toInt()?.toString() ?: "") }
    var remarks by remember { mutableStateOf(submission.remarks) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Student Roll #${submission.rollNumber} Evaluation", fontWeight = FontWeight.Bold, color = CollegeNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = marksText,
                    onValueChange = { marksText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Marks / Score Obtained (Max $maxMarks)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("marks_obtained_input")
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Faculty Evaluation Notes / Feedback") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marks = marksText.toFloatOrNull()
                    onConfirm(marks, remarks)
                },
                modifier = Modifier.testTag("save_marks_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue)
            ) {
                Text("Save Evaluation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
