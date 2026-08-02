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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassmorphicCanvas
import com.example.ui.theme.CollegeBlue
import com.example.ui.theme.CollegeNavy
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
    var gradeLevel by remember(currentTargetSubject) { mutableStateOf(currentTargetSubject?.gradeClass ?: "Computer Science Dept") }
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

    GlassmorphicCanvas {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Faculty AI Course Planner", fontWeight = FontWeight.Bold, color = CollegeNavy)
                            Text("Curriculum Structuring & Lesson Plan Generation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.85f))
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
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.White.copy(alpha = 0.88f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CollegeBlue,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Gemini AI Curriculum Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CollegeNavy)
                                Text(
                                    "Paste university course curriculum or upload syllabus PDF. AI auto-structures lecture units, learning outcomes, and recommended readings.",
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
                            value = currentTargetSubject?.name ?: "Select Target Course",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Course / Subject") },
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
                        Text("Course Syllabus Input", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CollegeNavy)

                        Button(
                            onClick = { docPickerLauncher.launch(arrayOf("*/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue),
                            modifier = Modifier.testTag("pick_file_button")
                        ) {
                            Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload Syllabus PDF", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = syllabusText,
                        onValueChange = { syllabusText = it },
                        placeholder = { Text("Paste university course curriculum, unit breakdown, course code, or weekly lecture topics here...") },
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
                        colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue),
                        enabled = aiState !is AiGeneratorUiState.Loading && syllabusText.isNotBlank()
                    ) {
                        if (aiState is AiGeneratorUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Gemini AI is Structuring Course Plan...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Course Plan with Gemini AI", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                when (aiState) {
                    is AiGeneratorUiState.Error -> {
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
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
                                    text = "Generated Lecture Topics (${result.topics.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavy
                                )

                                currentTargetSubject?.let { subj ->
                                    Button(
                                        onClick = { onApplyToSubject(subj.id, result) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue),
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
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Color.White.copy(alpha = 0.88f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = topic.unitTitle, style = MaterialTheme.typography.labelMedium, color = CollegeBlue, fontWeight = FontWeight.Bold)
                                    Text(text = topic.topicName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CollegeNavy)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Learning Objectives:\n${topic.learningObjectives}", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Faculty Notes: ${topic.teachingNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
