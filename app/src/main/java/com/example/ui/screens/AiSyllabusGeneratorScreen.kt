package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeneratedSyllabusResult
import com.example.data.entity.SubjectEntity
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.viewmodel.AiGeneratorUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSyllabusGeneratorScreen(
    subjects: List<SubjectEntity>,
    selectedSubject: SubjectEntity?,
    aiState: AiGeneratorUiState,
    onBack: () -> Unit,
    onGenerate: (syllabusInput: String, subjectName: String, grade: String) -> Unit,
    onApplyToSubject: (subjectId: Long, result: GeneratedSyllabusResult) -> Unit,
    onReadDocumentFromStorage: (Uri, (String) -> Unit) -> Unit
) {
    var syllabusText by remember { mutableStateOf("") }
    var targetSubject by remember(selectedSubject, subjects) { mutableStateOf(selectedSubject ?: subjects.firstOrNull()) }
    val currentTargetSubject = targetSubject ?: selectedSubject ?: subjects.firstOrNull()
    var gradeLevel by remember(currentTargetSubject) { mutableStateOf(currentTargetSubject?.gradeClass ?: "Grade 10") }
    var expandedSubjectDropdown by remember { mutableStateOf(false) }

    // SAF File Picker
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            onReadDocumentFromStorage(it) { text ->
                syllabusText = text
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Syllabus & Lesson Planner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Gemini AI Curriculum Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Paste syllabus text or select a document file from phone storage. AI will auto-create structured unit topics, objectives, and teaching notes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                // Target Subject Selection
                ExposedDropdownMenuBox(
                    expanded = expandedSubjectDropdown,
                    onExpandedChange = { expandedSubjectDropdown = !expandedSubjectDropdown }
                ) {
                    OutlinedTextField(
                        value = currentTargetSubject?.name ?: "Select Target Subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubjectDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("subject_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedSubjectDropdown,
                        onDismissRequest = { expandedSubjectDropdown = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text("${subject.name} (${subject.gradeClass})") },
                                onClick = {
                                    targetSubject = subject
                                    gradeLevel = subject.gradeClass
                                    expandedSubjectDropdown = false
                                }
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
                    Text("Syllabus / Curriculum Input", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = { docPickerLauncher.launch(arrayOf("*/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("pick_file_button")
                    ) {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick File", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = syllabusText,
                    onValueChange = { syllabusText = it },
                    placeholder = { Text("Paste syllabus chapters, course topics, or learning outcomes here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("syllabus_text_input")
                )
            }

            item {
                Button(
                    onClick = {
                        val subjName = currentTargetSubject?.name ?: "General Subject"
                        onGenerate(syllabusText, subjName, gradeLevel)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("generate_ai_plan_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    enabled = aiState !is AiGeneratorUiState.Loading && syllabusText.isNotBlank()
                ) {
                    if (aiState is AiGeneratorUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gemini AI is Structuring Lesson Plan...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Lesson Plan with Gemini AI", fontWeight = FontWeight.Bold)
                    }
                }
            }

            when (aiState) {
                is AiGeneratorUiState.Error -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "AI Error: ${aiState.message}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                is AiGeneratorUiState.Success -> {
                    val result = aiState.result
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Generated Topics (${result.topics.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            currentTargetSubject?.let { subj ->
                                Button(
                                    onClick = { onApplyToSubject(subj.id, result) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("import_topics_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Import into ${subj.name}")
                                }
                            }
                        }
                    }

                    items(result.topics) { topic ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = topic.unitTitle, style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                Text(text = topic.topicName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Objectives:\n${topic.learningObjectives}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Notes: ${topic.teachingNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
