package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CloudUpload
import com.example.util.GoogleDriveManager
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import com.example.data.entity.TopicEntity
import com.example.data.entity.TopicSourceEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassmorphicCanvas
import com.example.ui.theme.CollegeBlue
import com.example.ui.theme.CollegeNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topic: TopicEntity,
    sources: List<TopicSourceEntity>,
    onBack: () -> Unit,
    onUpdateCoverage: (TopicEntity, Boolean, Int) -> Unit,
    onAddSource: (title: String, sourceType: String, uriOrContent: String) -> Unit,
    onDeleteSource: (TopicSourceEntity) -> Unit,
    onDeleteTopic: (TopicEntity) -> Unit
) {
    val context = LocalContext.current
    var showAddSourceDialog by remember { mutableStateOf(false) }
    var coveragePercent by remember { mutableStateOf(topic.coveragePercentage.toFloat()) }
    var isCovered by remember { mutableStateOf(topic.isCovered) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            onAddSource("Lecture Slides (${it.lastPathSegment ?: "Document"})", "FILE_URI", it.toString())
            val uploaded = GoogleDriveManager.uploadAttachmentToSubjectDriveFolder(
                context = context,
                subjectName = "Classroom Materials",
                gradeClass = "Reference",
                subfolderCategory = "4. Reference Materials & Resources",
                fileName = "Lecture_Slide_${it.lastPathSegment ?: "Doc"}.pdf"
            )
            if (uploaded) {
                Toast.makeText(context, "Attached document and uploaded to Google Drive!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Attached document (Already up to date on Google Drive)", Toast.LENGTH_SHORT).show()
            }
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
                                text = topic.topicName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                            Text(
                                text = "${topic.unitTitle.ifEmpty { "Module Topic" }} • ${topic.estimatedHours} hrs lecture",
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
                        IconButton(onClick = { onDeleteTopic(topic) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Topic", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White.copy(alpha = 0.85f)
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddSourceDialog = true },
                    containerColor = CollegeBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_source_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Attach Course Materials")
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.White.copy(alpha = 0.88f)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lecture Coverage Progress",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavy
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isCovered,
                                        onCheckedChange = { checked ->
                                            isCovered = checked
                                            coveragePercent = if (checked) 100f else 0f
                                            onUpdateCoverage(topic, checked, coveragePercent.toInt())
                                        }
                                    )
                                    Text(
                                        text = if (isCovered) "Covered" else "In Progress",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCovered) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${coveragePercent.toInt()}% Syllabus Covered",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Slider(
                                value = coveragePercent,
                                onValueChange = { value ->
                                    coveragePercent = value
                                    isCovered = value >= 100f
                                },
                                onValueChangeFinished = {
                                    onUpdateCoverage(topic, isCovered, coveragePercent.toInt())
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (topic.learningObjectives.isNotEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color.White.copy(alpha = 0.88f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Course Objectives & Learning Outcomes",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavy
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = topic.learningObjectives,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                if (topic.notes.isNotEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color.White.copy(alpha = 0.88f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Lecture Notes & Recommended Reading",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavy
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = topic.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Course Slides & References (${sources.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavy
                        )
                        TextButton(onClick = { showAddSourceDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Attach Material")
                        }
                    }
                }

                if (sources.isEmpty()) {
                    item {
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
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = CollegeBlue
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No Reference Materials Attached",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavy
                                )
                                Text(
                                    text = "Attach lecture slide PDFs, research links, or textbook references.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(sources, key = { it.id }) { source ->
                        SourceItemCard(
                            source = source,
                            onOpen = {
                                if (source.sourceType == "URL" && source.uriOrContent.startsWith("http")) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.uriOrContent))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "Reference: ${source.uriOrContent}", Toast.LENGTH_LONG).show()
                                }
                            },
                            onDelete = { onDeleteSource(source) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddSourceDialog) {
        AddSourceDialog(
            onDismiss = { showAddSourceDialog = false },
            onPickDocument = {
                showAddSourceDialog = false
                try {
                    documentPickerLauncher.launch(arrayOf("application/pdf", "image/*", "text/*"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Storage picker open error", Toast.LENGTH_SHORT).show()
                }
            },
            onConfirmCustom = { title, type, contentStr ->
                onAddSource(title, type, contentStr)
                val uploaded = GoogleDriveManager.uploadAttachmentToSubjectDriveFolder(
                    context = context,
                    subjectName = "Classroom Reference",
                    gradeClass = "Materials",
                    subfolderCategory = "4. Reference Materials & Resources",
                    fileName = title
                )
                if (uploaded) {
                    Toast.makeText(context, "Saved '$title' & uploaded to Google Drive!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Saved '$title' (Drive copy is already up to date)", Toast.LENGTH_SHORT).show()
                }
                showAddSourceDialog = false
            }
        )
    }
}

@Composable
private fun SourceItemCard(
    source: TopicSourceEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White.copy(alpha = 0.88f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CollegeBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (source.sourceType == "URL") Icons.Default.Link else Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = CollegeBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = source.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavy
                    )
                    Text(
                        text = source.uriOrContent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        val uploaded = GoogleDriveManager.uploadFileToClassroomFolder(
                            context = context,
                            folderName = "Classroom / Reference Materials",
                            fileName = source.title
                        )
                        if (uploaded) {
                            Toast.makeText(context, "Saved '${source.title}' to Google Drive Folder!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "'${source.title}' is already up to date in Google Drive", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload to Google Drive",
                        tint = CollegeBlue
                    )
                }
                if (source.sourceType == "URL") {
                    IconButton(onClick = onOpen) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open Link")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddSourceDialog(
    onDismiss: () -> Unit,
    onPickDocument: () -> Unit,
    onConfirmCustom: (title: String, type: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("URL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attach Course Material", fontWeight = FontWeight.Bold, color = CollegeNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPickDocument,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_file_from_storage_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue)
                ) {
                    Icon(imageVector = Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Slide Deck / PDF File")
                }

                Text("— OR Add Web URL / IEEE Bibliography —", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Lecture 5 Slide Deck)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("URL / DOI Link / Textbook Chapter") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { selectedType = "URL" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "URL") CollegeBlue else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == "URL") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Web Link")
                    }

                    Button(
                        onClick = { selectedType = "BOOK_REF" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "BOOK_REF") CollegeBlue else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == "BOOK_REF") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Textbook Ref")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCustom(title, selectedType, content) },
                modifier = Modifier.testTag("confirm_add_source_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue)
            ) {
                Text("Attach Reference")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
