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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
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
            val matchesSearch = searchQuery.isBlank() ||
                    sub.rollNumber.toString().contains(searchQuery) ||
                    sub.studentName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filterMode) {
                "DONE" -> sub.isDone
                "PENDING" -> !sub.isDone
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = assignment.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Due: ${assignment.dueDate.ifEmpty { "No date" }} • Max Marks: ${assignment.maxMarks}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stats Header Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Roll Completion: $doneCount / $totalRolls",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap Roll Chip to mark Done/Pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${(completionPercent * 100).toInt()}% Done",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { completionPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldSuccess,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Batch Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onMarkAllDone(true) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mark_all_done_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark All Done", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onMarkAllDone(false) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clear_all_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Filters & Search Bar
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Roll Number (e.g. 15)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("roll_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            }

            // Main Content: Grid Matrix or Vertical List
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5), // 5 columns matrix for quick attendance / submission tapping
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSubmissions, key = { it.id }) { sub ->
                        RollMatrixChip(
                            submission = sub,
                            onToggle = { onToggleDone(sub) },
                            onLongClick = { editingSubmission = sub }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSubmissions, key = { it.id }) { sub ->
                        RollListItemCard(
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

    editingSubmission?.let { sub ->
        EditSubmissionMarksDialog(
            submission = sub,
            maxMarks = assignment.maxMarks,
            onDismiss = { editingSubmission = null },
            onConfirm = { marks, remarks ->
                onUpdateMarks(sub, marks, remarks)
                editingSubmission = null
            }
        )
    }
}

@Composable
private fun RollMatrixChip(
    submission: StudentSubmissionEntity,
    onToggle: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = if (submission.isDone) EmeraldSuccess else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (submission.isDone) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .testTag("roll_chip_${submission.rollNumber}"),
        color = bgColor,
        tonalElevation = if (submission.isDone) 4.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#${submission.rollNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (submission.isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    if (submission.marksObtained != null) {
                        Text(
                            text = "${submission.marksObtained.toInt()}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                } else {
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RollListItemCard(
    submission: StudentSubmissionEntity,
    maxMarks: Int,
    onToggleDone: () -> Unit,
    onEditMarks: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggleDone)
            .testTag("roll_list_item_${submission.rollNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        text = "Roll #${submission.rollNumber} ${submission.studentName.ifEmpty { "" }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
                        color = IndigoPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = onEditMarks) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Marks")
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
        title = { Text("Roll #${submission.rollNumber} Marks & Remarks", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = marksText,
                    onValueChange = { marksText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Marks Obtained (Max $maxMarks)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("marks_obtained_input")
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Teacher Remarks / Feedback") },
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
                modifier = Modifier.testTag("save_marks_button")
            ) {
                Text("Save Marks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
