package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxHeight
import com.example.ui.components.SecuritySettingsDialog
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TopicEntity
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassmorphicCanvas
import com.example.ui.theme.CollegeBlue
import com.example.ui.theme.CollegeNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.util.GoogleDriveManager
import com.example.util.UserProfileManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    subjects: List<SubjectEntity>,
    topics: List<TopicEntity> = emptyList(),
    onSelectSubject: (Long) -> Unit,
    onOpenAiPlanner: () -> Unit,
    onOpenTimetable: () -> Unit,
    onOpenGoogleDrive: () -> Unit,
    onAddSubject: (String, String, String, Int, String) -> Unit
) {
    val context = LocalContext.current
    val username by UserProfileManager.username.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White.copy(alpha = 0.96f),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
            ) {
                SidebarDrawerContent(
                    username = username,
                    onUpdateUsername = { newHandle ->
                        UserProfileManager.updateUsername(context, newHandle)
                        Toast.makeText(context, "Handle updated to @$newHandle", Toast.LENGTH_SHORT).show()
                    },
                    onOpenDashboard = {
                        scope.launch { drawerState.close() }
                    },
                    onOpenGoogleDrive = {
                        scope.launch { drawerState.close() }
                        onOpenGoogleDrive()
                    },
                    onOpenTimetable = {
                        scope.launch { drawerState.close() }
                        onOpenTimetable()
                    },
                    onOpenAiPlanner = {
                        scope.launch { drawerState.close() }
                        onOpenAiPlanner()
                    },
                    onOpenSecurity = {
                        scope.launch { drawerState.close() }
                        showSecurityDialog = true
                    }
                )
            }
        }
    ) {
        GlassmorphicCanvas {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_sidebar_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Navigation Menu",
                                    tint = CollegeNavy
                                )
                            }
                        },
                        title = {
                            Column {
                                Text(
                                    text = "Welcome, @$username",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavy
                                )
                                Text(
                                    text = "${subjects.size} Active Courses • Tap ☰ for Navigation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White.copy(alpha = 0.85f)
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = CollegeBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_subject_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Course"
                        )
                    }
                }
            ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    HeroWelcomeBanner(
                        subjectsCount = subjects.size,
                        onOpenTimetable = onOpenTimetable,
                        onOpenAi = onOpenAiPlanner,
                        onOpenSecurity = { showSecurityDialog = true }
                    )
                }

                item {
                    val driveUser by GoogleDriveManager.userState.collectAsState()
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenGoogleDrive() }
                            .testTag("dashboard_google_drive_card"),
                        containerColor = Color.White.copy(alpha = 0.88f),
                        borderColor = CollegeBlue.copy(alpha = 0.30f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(CollegeBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Classroom Google Drive Workspace",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CollegeNavy
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = if (driveUser.isLoggedIn) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                                        ) {
                                            Text(
                                                text = if (driveUser.isLoggedIn) "Connected" else "Sync Pending",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (driveUser.isLoggedIn) EmeraldSuccess else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (driveUser.isLoggedIn) "${driveUser.email} • ${driveUser.totalFilesSynced} files synced" else "Tap to log in & auto-sync department course folders",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Drive Workspace",
                                tint = CollegeBlue
                            )
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
                            text = "Active Department Courses (${subjects.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavy
                        )
                        TextButton(onClick = { showAddDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Course")
                        }
                    }
                }

                if (subjects.isEmpty()) {
                    item {
                        EmptySubjectsCard(onAdd = { showAddDialog = true })
                    }
                } else {
                    items(subjects, key = { it.id }) { subject ->
                        SubjectCard(
                            subject = subject,
                            topics = topics,
                            onClick = { onSelectSubject(subject.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

    if (showAddDialog) {
        AddSubjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, grade, color, rolls, desc ->
                onAddSubject(name, grade, color, rolls, desc)
                showAddDialog = false
            }
        )
    }

    if (showSecurityDialog) {
        SecuritySettingsDialog(
            onDismiss = { showSecurityDialog = false }
        )
    }
}

@Composable
private fun HeroWelcomeBanner(
    subjectsCount: Int,
    onOpenTimetable: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenSecurity: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White.copy(alpha = 0.88f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CollegeBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = CollegeBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Department Faculty Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavy
                    )
                    Text(
                        text = "$subjectsCount Active Courses • Semester Timetable & Auto Drive Backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenTimetable,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CollegeBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Timetable", fontSize = 13.sp)
                }

                Button(
                    onClick = onOpenAi,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Syllabus", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectEntity,
    topics: List<TopicEntity> = emptyList(),
    onClick: () -> Unit
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        CollegeBlue
    }

    val subjectTopics = remember(subject.id, topics) {
        topics.filter { it.subjectId == subject.id }
    }

    val totalTopics = subjectTopics.size
    val coveredTopics = subjectTopics.count { it.isCovered || it.coveragePercentage >= 100 }

    val progressFraction = if (totalTopics > 0) {
        val sumPct = subjectTopics.sumOf { if (it.isCovered) 100 else it.coveragePercentage }
        (sumPct.toFloat() / (totalTopics * 100f)).coerceIn(0f, 1f)
    } else {
        0.0f
    }
    val progressPctInt = (progressFraction * 100).toInt()

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("subject_card_${subject.id}"),
        containerColor = Color.White.copy(alpha = 0.88f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavy
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = subject.gradeClass,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (subject.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subject.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- Review of Progress Bar Widget ---
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = accentColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Syllabus Progress Review",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (progressPctInt >= 100) EmeraldSuccess.copy(alpha = 0.15f) else accentColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$progressPctInt% Covered",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (progressPctInt >= 100) EmeraldSuccess else accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Visual Progress Bar Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = progressFraction.coerceIn(0.02f, 1f))
                                .clip(CircleShape)
                                .background(
                                    if (progressPctInt >= 100) EmeraldSuccess else accentColor
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (totalTopics > 0) "$coveredTopics of $totalTopics Topics Covered" else "No topics added yet",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val statusText = when {
                            totalTopics == 0 -> "Syllabus Pending"
                            progressPctInt >= 100 -> "Completed"
                            progressPctInt >= 50 -> "On Track"
                            progressPctInt > 0 -> "In Progress"
                            else -> "Not Started"
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (progressPctInt >= 100) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Enrolled Students: Roll 1 to ${subject.totalRolls}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Course Workspace",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySubjectsCard(onAdd: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        containerColor = Color.White.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = CollegeBlue
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Active Courses Added",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CollegeNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Create your first university course to manage syllabus modules, lecture slides, research papers, and student roll assignments.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAdd,
                modifier = Modifier.testTag("empty_add_subject_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add First Course")
            }
        }
    }
}

@Composable
private fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, grade: String, colorHex: String, totalRolls: Int, desc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("CS Dept • Semester 5 - Sec A") }
    var description by remember { mutableStateOf("") }
    var totalRollsText by remember { mutableStateOf("60") }
    var selectedColor by remember { mutableStateOf("#2563EB") }

    val colorOptions = listOf("#2563EB", "#059669", "#7C3AED", "#0284C7", "#D97706", "#DB2777")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add New Course", fontWeight = FontWeight.Bold, color = CollegeNavy)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Title & Code (e.g. CS-301 Data Structures)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_input")
                )

                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Department & Semester (e.g. CS Dept • Sem 5)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_grade_input")
                )

                OutlinedTextField(
                    value = totalRollsText,
                    onValueChange = { totalRollsText = it.filter { char -> char.isDigit() } },
                    label = { Text("Total Enrolled Students (Default 60)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_rolls_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Course Overview / Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Course Tag Color", style = MaterialTheme.typography.labelMedium, color = CollegeNavy)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .padding(2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rolls = totalRollsText.toIntOrNull() ?: 60
                    onConfirm(name, grade, selectedColor, rolls, description)
                },
                modifier = Modifier.testTag("confirm_add_subject_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue)
            ) {
                Text("Create Course & Drive Workspace")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SidebarDrawerContent(
    username: String,
    onUpdateUsername: (String) -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenGoogleDrive: () -> Unit,
    onOpenTimetable: () -> Unit,
    onOpenAiPlanner: () -> Unit,
    onOpenSecurity: () -> Unit
) {
    var handleInput by remember(username) { mutableStateOf(username) }
    var isEditingHandle by remember { mutableStateOf(false) }
    val driveUser by GoogleDriveManager.userState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1. Profile Header
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = CollegeBlue.copy(alpha = 0.08f),
            borderColor = CollegeBlue.copy(alpha = 0.25f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(CollegeBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (driveUser.isLoggedIn && driveUser.name.isNotEmpty()) driveUser.name else "Faculty Member",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavy
                        )
                        Text(
                            text = if (driveUser.isLoggedIn) driveUser.email else "Local Academic Session",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile Handle Badge & Editor Section
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Profile Session Handle",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = { isEditingHandle = !isEditingHandle },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit handle",
                                    tint = CollegeBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (isEditingHandle) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = handleInput,
                                    onValueChange = { handleInput = it },
                                    label = { Text("Handle") },
                                    prefix = { Text("@") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sidebar_handle_input")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        onUpdateUsername(handleInput)
                                        isEditingHandle = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("save_sidebar_handle_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Save",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "@$username",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = CollegeBlue
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "PORTAL NAVIGATION",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = CollegeNavy.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Navigation Items
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null, tint = CollegeBlue) },
            label = { Text("Home Dashboard", fontWeight = FontWeight.SemiBold, color = CollegeNavy) },
            selected = true,
            onClick = onOpenDashboard,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .testTag("sidebar_nav_home"),
            colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CollegeBlue.copy(alpha = 0.12f))
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Cloud, contentDescription = null, tint = CollegeBlue) },
            label = { Text("Google Drive Workspace", fontWeight = FontWeight.SemiBold, color = CollegeNavy) },
            badge = {
                Surface(
                    shape = CircleShape,
                    color = if (driveUser.isLoggedIn) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = if (driveUser.isLoggedIn) "Linked" else "Connect",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (driveUser.isLoggedIn) EmeraldSuccess else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            },
            selected = false,
            onClick = onOpenGoogleDrive,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .testTag("sidebar_nav_drive")
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = CollegeNavy) },
            label = { Text("Lecture Timetable", fontWeight = FontWeight.SemiBold, color = CollegeNavy) },
            selected = false,
            onClick = onOpenTimetable,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .testTag("sidebar_nav_timetable")
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CollegeBlue) },
            label = { Text("AI Syllabus Planner", fontWeight = FontWeight.SemiBold, color = CollegeNavy) },
            selected = false,
            onClick = onOpenAiPlanner,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .testTag("sidebar_nav_ai")
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldSuccess) },
            label = { Text("Security Settings", fontWeight = FontWeight.SemiBold, color = CollegeNavy) },
            selected = false,
            onClick = onOpenSecurity,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .testTag("sidebar_nav_security")
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(12.dp))

        // Footer info
        Text(
            text = "Faculty Academic Portal • Session @$username",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
