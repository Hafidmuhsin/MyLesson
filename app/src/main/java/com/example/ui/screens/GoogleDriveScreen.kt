package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.entity.SubjectEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassmorphicCanvas
import com.example.ui.theme.CollegeBlue
import com.example.ui.theme.CollegeNavy
import com.example.ui.theme.CollegeTextPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.util.DriveClassroomFolder
import com.example.util.DriveSyncActivityLog
import com.example.util.GoogleDriveManager
import com.example.util.GoogleDriveUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveScreen(
    subjects: List<SubjectEntity>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userState by GoogleDriveManager.userState.collectAsState()
    val activityLogs by GoogleDriveManager.activityLogs.collectAsState()

    var showLoginDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedFolderForUpload by remember { mutableStateOf("Faculty Classroom Drive") }
    var isSyncingAll by remember { mutableStateOf(false) }

    // Map of subjectId to expanded state for UI tree view
    val expandedSubjectMap = remember { mutableStateMapOf<Long, Boolean>() }

    GlassmorphicCanvas {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Faculty Classroom Google Drive",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                            Text(
                                text = "Department Workspace & Course Materials Auto-Sync",
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
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/my-drive"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.testTag("open_web_drive_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Web Drive",
                                tint = CollegeBlue
                            )
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    GoogleAccountCard(
                        userState = userState,
                        onSignInClick = { showLoginDialog = true },
                        onSignOutClick = {
                            GoogleDriveManager.logout(context)
                            Toast.makeText(context, "Logged out from Google Drive", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    ClassroomStorageOverviewCard(
                        userState = userState,
                        subjectsCount = subjects.size,
                        isSyncing = isSyncingAll,
                        onSyncAllClick = {
                            if (!userState.isLoggedIn) {
                                showLoginDialog = true
                            } else {
                                isSyncingAll = true
                                subjects.forEach { subject ->
                                    GoogleDriveManager.syncSubjectToClassroomDrive(
                                        context = context,
                                        subjectId = subject.id,
                                        subjectName = subject.name,
                                        gradeClass = subject.gradeClass
                                    )
                                }
                                isSyncingAll = false
                                Toast.makeText(context, "All ${subjects.size} Department Course Folders Synced to Google Drive!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onUploadClick = {
                            if (!userState.isLoggedIn) {
                                showLoginDialog = true
                            } else {
                                showUploadDialog = true
                            }
                        }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Department Course Drive Folders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                            Text(
                                text = "Automated subfolders for syllabus, coursework, and grades",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = CollegeBlue.copy(alpha = 0.12f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${subjects.size} Active Folders",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CollegeBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Root Classroom Folder Tree Header
                item {
                    ClassroomRootFolderHeaderCard(
                        userState = userState,
                        onOpenRootDrive = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/classroom_root_workspace"))
                            context.startActivity(intent)
                        }
                    )
                }

            // Subjects List as Classroom Folders
            if (subjects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Subjects Added Yet",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Add subjects from the Dashboard to automatically create their Google Classroom Drive folders.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(subjects, key = { it.id }) { subject ->
                    val isExpanded = expandedSubjectMap[subject.id] ?: false
                    SubjectClassroomDriveFolderCard(
                        subject = subject,
                        isExpanded = isExpanded,
                        isLoggedIn = userState.isLoggedIn,
                        onToggleExpand = { expandedSubjectMap[subject.id] = !isExpanded },
                        onSyncSubject = { force ->
                            if (!userState.isLoggedIn) {
                                showLoginDialog = true
                            } else {
                                val wasAlreadySynced = GoogleDriveManager.isSubjectSynced(subject.id, subject.name, subject.gradeClass)
                                GoogleDriveManager.syncSubjectToClassroomDrive(
                                    context = context,
                                    subjectId = subject.id,
                                    subjectName = subject.name,
                                    gradeClass = subject.gradeClass,
                                    forceSync = force
                                )
                                if (wasAlreadySynced && !force) {
                                    Toast.makeText(context, "Drive folder '${subject.name}' is up to date (No changes detected)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Synced '${subject.name}' Classroom Drive folder!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onUploadToSubject = { folderName ->
                            if (!userState.isLoggedIn) {
                                showLoginDialog = true
                            } else {
                                selectedFolderForUpload = folderName
                                showUploadDialog = true
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Drive Sync Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CollegeNavy
                )
            }

            if (activityLogs.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.White.copy(alpha = 0.85f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Recent Sync Activity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Connect your Google Drive account and sync course folders to view automated activity logs here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(activityLogs, key = { it.id }) { log ->
                    DriveActivityLogRow(log = log)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

    if (showLoginDialog) {
        GoogleDriveLoginDialog(
            currentEmail = userState.email,
            currentName = userState.name,
            onDismiss = { showLoginDialog = false },
            onConfirmLogin = { name, email ->
                GoogleDriveManager.loginWithGoogle(context, name, email)
                subjects.forEach { subject ->
                    GoogleDriveManager.syncSubjectToClassroomDrive(
                        context = context,
                        subjectId = subject.id,
                        subjectName = subject.name,
                        gradeClass = subject.gradeClass
                    )
                }
                showLoginDialog = false
                Toast.makeText(context, "Successfully connected Google Drive Account & synced course folders!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showUploadDialog) {
        UploadToDriveDialog(
            folderName = selectedFolderForUpload,
            onDismiss = { showUploadDialog = false },
            onConfirmUpload = { fileName ->
                val uploaded = GoogleDriveManager.uploadFileToClassroomFolder(
                    context = context,
                    folderName = selectedFolderForUpload,
                    fileName = fileName
                )
                showUploadDialog = false
                if (uploaded) {
                    Toast.makeText(context, "File '$fileName' uploaded to Google Drive!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "File '$fileName' is already up to date in Google Drive", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
private fun GoogleAccountCard(
    userState: GoogleDriveUser,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White.copy(alpha = 0.88f),
        borderColor = if (userState.isLoggedIn) EmeraldSuccess.copy(alpha = 0.4f) else Color(0x332563EB)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (userState.isLoggedIn) CollegeBlue else MaterialTheme.colorScheme.outline
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (userState.isLoggedIn) userState.name.ifEmpty { "Faculty Account" } else "Google Account Not Connected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (userState.isLoggedIn) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (userState.isLoggedIn) userState.email.ifEmpty { "drive.file connected" } else "Sign in to enable Classroom Drive Sync",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (userState.isLoggedIn) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (userState.isLoggedIn) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (userState.isLoggedIn) EmeraldSuccess else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userState.isLoggedIn) "Connected" else "Offline",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (userState.isLoggedIn) EmeraldSuccess else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!userState.isLoggedIn) {
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CollegeBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign in with Google Account", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = CollegeBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scope: drive.file active",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        TextButton(
                            onClick = onSignInClick,
                            modifier = Modifier.testTag("switch_account_button")
                        ) {
                            Text("Switch Account")
                        }
                        TextButton(
                            onClick = onSignOutClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("google_logout_button")
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassroomStorageOverviewCard(
    userState: GoogleDriveUser,
    subjectsCount: Int,
    isSyncing: Boolean,
    onSyncAllClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Google Drive Classroom Workspace",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val lastSyncStr = if (userState.lastSyncedAt > 0) {
                        SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(userState.lastSyncedAt))
                    } else "Never"
                    Text(
                        text = "Last synced: $lastSyncStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${userState.totalFilesSynced} Synced Files",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress indicator
            val usedMb = userState.storageUsedMb
            val percent = (usedMb / 15000f).coerceIn(0f, 1f)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Drive Storage Usage",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f MB of 15 GB", usedMb),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { percent.coerceAtLeast(0.02f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = IndigoPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSyncAllClick,
                    enabled = !isSyncing,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sync_all_drive_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSyncing) "Syncing..." else "Sync All Folders")
                }

                OutlinedButton(
                    onClick = onUploadClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("upload_file_drive_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload File")
                }
            }
        }
    }
}

@Composable
private fun ClassroomRootFolderHeaderCard(
    userState: GoogleDriveUser,
    onOpenRootDrive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IndigoPrimary.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(IndigoPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderShared,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "📁 Classroom (Root Drive Folder)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "path: My Drive / Classroom",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onOpenRootDrive) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open Root Folder in Drive",
                    tint = IndigoPrimary
                )
            }
        }
    }
}

@Composable
private fun SubjectClassroomDriveFolderCard(
    subject: SubjectEntity,
    isExpanded: Boolean,
    isLoggedIn: Boolean,
    onToggleExpand: () -> Unit,
    onSyncSubject: (Boolean) -> Unit,
    onUploadToSubject: (String) -> Unit
) {
    val context = LocalContext.current
    val isSynced = GoogleDriveManager.isSubjectSynced(subject.id, subject.name, subject.gradeClass)
    val subfolders = GoogleDriveManager.getParallelFolderStructureForSubject(subject.id, subject.name, subject.gradeClass)
    val driveFolderUrl = GoogleDriveManager.generateClassroomFolderUrl(subject.id, subject.name, subject.gradeClass)

    // Map of subfolder id to expanded state
    val expandedSubfolders = remember { mutableStateMapOf<String, Boolean>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${subject.gradeClass} - ${subject.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isSynced) EmeraldSuccess.copy(alpha = 0.15f) else CollegeBlue.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSynced) Icons.Default.CheckCircle else Icons.Default.Sync,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = if (isSynced) EmeraldSuccess else CollegeBlue
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSynced) "Synced & Up to Date" else "Sync Required",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSynced) EmeraldSuccess else CollegeBlue
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Classroom Path: My Drive / Classroom / ${subject.gradeClass} - ${subject.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onSyncSubject(false) },
                        modifier = Modifier.testTag("sync_subject_${subject.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Check Sync",
                            tint = if (isSynced) EmeraldSuccess else MaterialTheme.colorScheme.primary
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Google Classroom Parallel Subfolders:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavy
                        )
                        TextButton(onClick = { onSyncSubject(true) }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Force Re-sync", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    subfolders.forEach { subfolder ->
                        val isSubExpanded = expandedSubfolders[subfolder.id] ?: false

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.06f))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedSubfolders[subfolder.id] = !isSubExpanded }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isSubExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = CollegeBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = subfolder.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CollegeNavy
                                            )
                                            Text(
                                                text = "${subfolder.files.size} items • ${subfolder.fileTypes}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onUploadToSubject("Classroom / ${subject.gradeClass} - ${subject.name} / ${subfolder.name}") },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add File to Subfolder",
                                                modifier = Modifier.size(18.dp),
                                                tint = CollegeBlue
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(subfolder.driveUrl))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = "Open Subfolder in Drive",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Icon(
                                            imageVector = if (isSubExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                            contentDescription = "Expand Subfolder Files",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isSubExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (subfolder.files.isEmpty()) {
                                            Text(
                                                text = "No files in this subfolder yet.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        } else {
                                            subfolder.files.forEach { file ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color.White,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.08f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.InsertDriveFile,
                                                                contentDescription = null,
                                                                tint = CollegeBlue,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column {
                                                                Text(
                                                                    text = file.name,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = CollegeTextPrimary
                                                                )
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Text(
                                                                        text = "${file.fileType} • ${file.sizeFormatted}",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = "• ${file.syncedAt}",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = EmeraldSuccess,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(file.driveUrl))
                                                                context.startActivity(intent)
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                                contentDescription = "View File in Google Drive",
                                                                modifier = Modifier.size(16.dp),
                                                                tint = CollegeBlue
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(driveFolderUrl))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Course Root in Google Drive")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveActivityLogRow(log: DriveSyncActivityLog) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EmeraldSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = log.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.folderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.driveUrl.isNotEmpty()) {
                    Text(
                        text = "Open Link",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(log.driveUrl))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleDriveLoginDialog(
    currentEmail: String,
    currentName: String,
    onDismiss: () -> Unit,
    onConfirmLogin: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = CollegeBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google Sign-In for Drive", fontWeight = FontWeight.Bold, color = CollegeNavy)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Sign in with your Google Account to connect TeacherPlan with Google Drive Classroom folders.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Faculty Name / Display Name") },
                    placeholder = { Text("Enter your full name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("google_name_input")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Google Account Email") },
                    placeholder = { Text("Enter your account email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("google_email_input")
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OAuth Scope requested: https://www.googleapis.com/auth/drive.file (Allows creating and managing Classroom files).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmLogin(name, email) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                modifier = Modifier.testTag("confirm_google_login_button")
            ) {
                Text("Authorize & Sign In")
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
private fun UploadToDriveDialog(
    folderName: String,
    onDismiss: () -> Unit,
    onConfirmUpload: (String) -> Unit
) {
    var fileName by remember { mutableStateOf("Lesson_Plan_Unit_1.pdf") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, tint = IndigoPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload to Drive", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Destination Folder:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = folderName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name to Upload") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("drive_filename_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmUpload(fileName) },
                modifier = Modifier.testTag("confirm_drive_upload_button")
            ) {
                Text("Upload Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
