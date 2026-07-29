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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TopicEntity
import com.example.data.entity.TopicSourceEntity
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

    // Storage Access Framework Document Picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            onAddSource("Storage Document (${it.lastPathSegment ?: "File"})", "FILE_URI", it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = topic.topicName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = topic.unitTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onDeleteTopic(topic) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Topic",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSourceDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_source_fab")
            ) {
                Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Attach Source")
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
            // Coverage & Progress Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Coverage Progress",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isCovered,
                                    onCheckedChange = { checked ->
                                        isCovered = checked
                                        val newPercent = if (checked) 100 else coveragePercent.toInt()
                                        onUpdateCoverage(topic, checked, newPercent)
                                    },
                                    modifier = Modifier.testTag("topic_detail_covered_checkbox")
                                )
                                Text(
                                    text = if (isCovered) "Covered (100%)" else "${coveragePercent.toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isCovered) EmeraldSuccess else IndigoPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!isCovered) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = coveragePercent,
                                onValueChange = { coveragePercent = it },
                                onValueChangeFinished = {
                                    onUpdateCoverage(topic, isCovered, coveragePercent.toInt())
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier.testTag("coverage_slider")
                            )
                        }
                    }
                }
            }

            // Learning Objectives
            if (topic.learningObjectives.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Learning Objectives",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = topic.learningObjectives,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Teacher Notes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Teacher Lesson Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = topic.notes.ifEmpty { "No lesson notes added yet." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (topic.notes.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Attached Sources & References Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attached Sources & References (${sources.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showAddSourceDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Attach")
                    }
                }
            }

            if (sources.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Sources Attached", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Attach syllabus PDFs, textbook references, or web links to this topic.",
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
                        onDelete = { onDeleteSource(source) },
                        onOpen = {
                            if (source.sourceType == "URL" && source.uriOrContent.startsWith("http")) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.uriOrContent))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // fallback
                                }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddSourceDialog) {
        AddSourceDialog(
            onDismiss = { showAddSourceDialog = false },
            onPickDocument = {
                documentPickerLauncher.launch(arrayOf("*/*"))
                showAddSourceDialog = false
            },
            onConfirmCustom = { title, type, content ->
                onAddSource(title, type, content)
                showAddSourceDialog = false
            }
        )
    }
}

@Composable
private fun SourceItemCard(
    source: TopicSourceEntity,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .testTag("source_item_${source.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                val icon = when (source.sourceType) {
                    "URL" -> Icons.Default.Link
                    "FILE_URI" -> Icons.Default.PictureAsPdf
                    "BOOK_REF" -> Icons.Default.Book
                    else -> Icons.Outlined.Bookmark
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = source.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = source.uriOrContent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (source.sourceType == "URL") {
                    IconButton(onClick = onOpen) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open Link")
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
        title = { Text("Attach Source / Reference", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPickDocument,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_file_from_storage_button")
                ) {
                    Icon(imageVector = Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select PDF/Document from Storage")
                }

                Text("— OR Add Web Link / Book Ref —", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g., Chapter 4 PDF Link)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("URL / Book Reference / Note") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { selectedType = "URL" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "URL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == "URL") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Web URL")
                    }

                    Button(
                        onClick = { selectedType = "BOOK_REF" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "BOOK_REF") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == "BOOK_REF") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Book Ref")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCustom(title, selectedType, content) },
                modifier = Modifier.testTag("confirm_add_source_button")
            ) {
                Text("Attach Source")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
