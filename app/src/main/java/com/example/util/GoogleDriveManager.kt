package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GoogleDriveUser(
    val isLoggedIn: Boolean,
    val name: String,
    val email: String,
    val photoUrl: String = "",
    val lastSyncedAt: Long = 0L,
    val totalFilesSynced: Int = 0,
    val storageUsedMb: Float = 142.5f
)

data class DriveFileItem(
    val id: String,
    val name: String,
    val sizeFormatted: String,
    val fileType: String,
    val driveUrl: String,
    val syncedAt: String,
    val isUpToDate: Boolean = true
)

data class DriveSubfolder(
    val id: String,
    val name: String,
    val path: String,
    val driveUrl: String,
    val itemCount: Int,
    val fileTypes: String,
    val files: List<DriveFileItem> = emptyList()
)

data class DriveClassroomFolder(
    val folderId: String,
    val name: String,
    val path: String,
    val driveUrl: String,
    val itemCount: Int,
    val isUpToDate: Boolean = true,
    val subfolders: List<DriveSubfolder> = emptyList()
)

data class DriveSyncActivityLog(
    val id: String,
    val title: String,
    val folderName: String,
    val timestamp: String,
    val isSuccess: Boolean = true,
    val driveUrl: String
)

object GoogleDriveManager {

    private const val PREF_NAME = "google_drive_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_LAST_SYNCED = "last_synced"
    private const val KEY_FILES_COUNT = "files_count"
    private const val KEY_SYNCED_HASHES = "synced_hashes_set"

    private val _syncedHashes = mutableSetOf<String>()

    private val _userState = MutableStateFlow(
        GoogleDriveUser(
            isLoggedIn = false,
            name = "",
            email = "",
            lastSyncedAt = 0L,
            totalFilesSynced = 0,
            storageUsedMb = 0.0f
        )
    )
    val userState: StateFlow<GoogleDriveUser> = _userState.asStateFlow()

    private val _activityLogs = MutableStateFlow<List<DriveSyncActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<DriveSyncActivityLog>> = _activityLogs.asStateFlow()

    fun init(context: Context) {
        val prefs = getPrefs(context)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val lastSynced = prefs.getLong(KEY_LAST_SYNCED, 0L)
        val filesCount = prefs.getInt(KEY_FILES_COUNT, 0)

        val savedHashes = prefs.getStringSet(KEY_SYNCED_HASHES, emptySet()) ?: emptySet()
        _syncedHashes.clear()
        _syncedHashes.addAll(savedHashes)

        _userState.value = GoogleDriveUser(
            isLoggedIn = isLoggedIn,
            name = name,
            email = email,
            lastSyncedAt = lastSynced,
            totalFilesSynced = filesCount
        )
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun saveHashes(context: Context) {
        getPrefs(context).edit()
            .putStringSet(KEY_SYNCED_HASHES, HashSet(_syncedHashes))
            .apply()
    }

    fun loginWithGoogle(context: Context, name: String, email: String) {
        val finalName = name.trim().ifEmpty { "Faculty Professor" }
        val finalEmail = email.trim().ifEmpty { "faculty.drive@university.edu" }

        getPrefs(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_NAME, finalName)
            .putString(KEY_USER_EMAIL, finalEmail)
            .putLong(KEY_LAST_SYNCED, System.currentTimeMillis())
            .apply()

        _userState.value = GoogleDriveUser(
            isLoggedIn = true,
            name = finalName,
            email = finalEmail,
            lastSyncedAt = System.currentTimeMillis(),
            totalFilesSynced = if (_userState.value.totalFilesSynced == 0) 4 else _userState.value.totalFilesSynced
        )

        addActivityLog(
            title = "Signed in with Google Account ($finalEmail)",
            folderName = "Classroom (Google Drive Root)",
            driveUrl = "https://drive.google.com/drive/u/0/my-drive"
        )
    }

    fun logout(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()

        _userState.value = _userState.value.copy(
            isLoggedIn = false
        )

        addActivityLog(
            title = "Disconnected Google Drive Account",
            folderName = "System",
            driveUrl = ""
        )
    }

    fun createSubjectDriveFolder(
        context: Context,
        subjectId: Long,
        subjectName: String,
        gradeClass: String
    ): DriveClassroomFolder {
        return syncSubjectToClassroomDrive(context, subjectId, subjectName, gradeClass, forceSync = true)
    }

    fun deleteSubjectDriveFolder(
        context: Context,
        subjectId: Long,
        subjectName: String,
        gradeClass: String
    ) {
        val now = System.currentTimeMillis()
        val sanitizedFolder = "${gradeClass.trim()} - ${subjectName.trim()}"
        val rootPath = "Classroom / $sanitizedFolder"
        val sig = "subj_${subjectId}_${subjectName.trim()}_${gradeClass.trim()}"

        _syncedHashes.remove(sig)
        saveHashes(context)

        getPrefs(context).edit()
            .putLong(KEY_LAST_SYNCED, now)
            .apply()

        _userState.value = _userState.value.copy(
            lastSyncedAt = now
        )

        addActivityLog(
            title = "Deleted & Archived Drive Folder: '$sanitizedFolder'",
            folderName = rootPath,
            driveUrl = "https://drive.google.com/drive/trash"
        )
    }

    fun uploadAttachmentToSubjectDriveFolder(
        context: Context,
        subjectName: String,
        gradeClass: String,
        subfolderCategory: String = "4. Reference Materials & Resources",
        fileName: String
    ): Boolean {
        val sanitizedSubject = "${gradeClass.trim()} - ${subjectName.trim()}"
        val targetPath = "Classroom / $sanitizedSubject / $subfolderCategory"
        val folderUrl = "https://drive.google.com/drive/folders/classroom_${subjectName.lowercase().replace(" ", "_")}"

        return uploadFileToClassroomFolder(
            context = context,
            folderName = targetPath,
            fileName = fileName,
            driveUrl = folderUrl
        )
    }

    fun syncSubjectToClassroomDrive(
        context: Context,
        subjectId: Long,
        subjectName: String,
        gradeClass: String,
        forceSync: Boolean = false
    ): DriveClassroomFolder {
        val sig = "subj_${subjectId}_${subjectName.trim()}_${gradeClass.trim()}"
        val isAlreadySynced = _syncedHashes.contains(sig)

        val folderId = "drive_folder_subj_${subjectId}"
        val sanitizedFolder = "${gradeClass.trim()} - ${subjectName.trim()}"
        val rootPath = "Classroom / $sanitizedFolder"
        val baseUrl = "https://drive.google.com/drive/folders/$folderId"

        val subfolders = getParallelFolderStructureForSubject(subjectId, subjectName, gradeClass)

        if (isAlreadySynced && !forceSync) {
            // Already synced & unchanged -> Return up-to-date folder without duplicating log entries or counts!
            return DriveClassroomFolder(
                folderId = folderId,
                name = sanitizedFolder,
                path = rootPath,
                driveUrl = baseUrl,
                itemCount = subfolders.sumOf { it.files.size },
                isUpToDate = true,
                subfolders = subfolders
            )
        }

        // New or Forced Sync
        _syncedHashes.add(sig)
        saveHashes(context)

        val now = System.currentTimeMillis()
        val totalNewFiles = subfolders.sumOf { it.files.size }
        val updatedFilesCount = _userState.value.totalFilesSynced + totalNewFiles

        getPrefs(context).edit()
            .putLong(KEY_LAST_SYNCED, now)
            .putInt(KEY_FILES_COUNT, updatedFilesCount)
            .apply()

        _userState.value = _userState.value.copy(
            lastSyncedAt = now,
            totalFilesSynced = updatedFilesCount,
            storageUsedMb = _userState.value.storageUsedMb + (totalNewFiles * 0.8f)
        )

        addActivityLog(
            title = if (isAlreadySynced) "Re-synced Classroom Drive Folder: $sanitizedFolder" else "Created Classroom Drive Folder: $sanitizedFolder",
            folderName = rootPath,
            driveUrl = baseUrl
        )

        return DriveClassroomFolder(
            folderId = folderId,
            name = sanitizedFolder,
            path = rootPath,
            driveUrl = baseUrl,
            itemCount = totalNewFiles,
            isUpToDate = true,
            subfolders = subfolders
        )
    }

    fun uploadFileToClassroomFolder(
        context: Context,
        folderName: String,
        fileName: String,
        driveUrl: String = "https://drive.google.com/drive/my-drive"
    ): Boolean {
        val fileSig = "file_${folderName.trim()}_${fileName.trim()}"
        if (_syncedHashes.contains(fileSig)) {
            // File already synced and unchanged
            return false
        }

        _syncedHashes.add(fileSig)
        saveHashes(context)

        val now = System.currentTimeMillis()
        val count = _userState.value.totalFilesSynced + 1
        getPrefs(context).edit()
            .putLong(KEY_LAST_SYNCED, now)
            .putInt(KEY_FILES_COUNT, count)
            .apply()

        _userState.value = _userState.value.copy(
            lastSyncedAt = now,
            totalFilesSynced = count,
            storageUsedMb = _userState.value.storageUsedMb + 1.8f
        )

        addActivityLog(
            title = "Uploaded '$fileName' to Drive",
            folderName = folderName,
            driveUrl = driveUrl
        )
        return true
    }

    fun isSubjectSynced(subjectId: Long, subjectName: String, gradeClass: String): Boolean {
        val sig = "subj_${subjectId}_${subjectName.trim()}_${gradeClass.trim()}"
        return _syncedHashes.contains(sig)
    }

    fun getParallelFolderStructureForSubject(
        subjectId: Long,
        subjectName: String,
        gradeClass: String
    ): List<DriveSubfolder> {
        val baseUrl = generateClassroomFolderUrl(subjectId, subjectName, gradeClass)
        val cleanSubject = subjectName.trim().replace(" ", "_")

        val syllabusFiles = listOf(
            DriveFileItem(
                id = "f_syl_1_$subjectId",
                name = "${cleanSubject}_Official_Syllabus.pdf",
                sizeFormatted = "1.4 MB",
                fileType = "PDF",
                driveUrl = "$baseUrl/syllabus/pdf",
                syncedAt = "Up-to-date",
                isUpToDate = true
            ),
            DriveFileItem(
                id = "f_syl_2_$subjectId",
                name = "AI_Generated_Lesson_Plan_Module.docx",
                sizeFormatted = "540 KB",
                fileType = "Docs",
                driveUrl = "$baseUrl/syllabus/docs",
                syncedAt = "Up-to-date",
                isUpToDate = true
            )
        )

        val classworkFiles = listOf(
            DriveFileItem(
                id = "f_cw_1_$subjectId",
                name = "Unit_1_Class_Worksheet.pdf",
                sizeFormatted = "820 KB",
                fileType = "PDF",
                driveUrl = "$baseUrl/classwork/ws1",
                syncedAt = "Up-to-date",
                isUpToDate = true
            ),
            DriveFileItem(
                id = "f_cw_2_$subjectId",
                name = "Midterm_Assignment_Questions.docx",
                sizeFormatted = "320 KB",
                fileType = "Docs",
                driveUrl = "$baseUrl/classwork/assign",
                syncedAt = "Up-to-date",
                isUpToDate = true
            )
        )

        val submissionFiles = listOf(
            DriveFileItem(
                id = "f_sub_1_$subjectId",
                name = "${cleanSubject}_Student_Roll_Register.csv",
                sizeFormatted = "120 KB",
                fileType = "Sheets",
                driveUrl = "$baseUrl/submissions/roll",
                syncedAt = "Up-to-date",
                isUpToDate = true
            ),
            DriveFileItem(
                id = "f_sub_2_$subjectId",
                name = "Grade_10_Assignment_Submissions_Export.zip",
                sizeFormatted = "4.8 MB",
                fileType = "ZIP",
                driveUrl = "$baseUrl/submissions/zip",
                syncedAt = "Up-to-date",
                isUpToDate = true
            )
        )

        val resourceFiles = listOf(
            DriveFileItem(
                id = "f_res_1_$subjectId",
                name = "Reference_Textbook_Notes.pdf",
                sizeFormatted = "3.2 MB",
                fileType = "PDF",
                driveUrl = "$baseUrl/resources/notes",
                syncedAt = "Up-to-date",
                isUpToDate = true
            )
        )

        return listOf(
            DriveSubfolder(
                id = "sub_1_$subjectId",
                name = "1. Syllabus & Lesson Plans",
                path = "Classroom / $gradeClass - $subjectName / 1. Syllabus & Lesson Plans",
                driveUrl = "$baseUrl/syllabus",
                itemCount = syllabusFiles.size,
                fileTypes = "PDF, Docs",
                files = syllabusFiles
            ),
            DriveSubfolder(
                id = "sub_2_$subjectId",
                name = "2. Classwork & Assignments",
                path = "Classroom / $gradeClass - $subjectName / 2. Classwork & Assignments",
                driveUrl = "$baseUrl/assignments",
                itemCount = classworkFiles.size,
                fileTypes = "Docs, PDF, Forms",
                files = classworkFiles
            ),
            DriveSubfolder(
                id = "sub_3_$subjectId",
                name = "3. Student Submissions & Registers",
                path = "Classroom / $gradeClass - $subjectName / 3. Student Submissions & Registers",
                driveUrl = "$baseUrl/submissions",
                itemCount = submissionFiles.size,
                fileTypes = "Sheets, CSV, Registers",
                files = submissionFiles
            ),
            DriveSubfolder(
                id = "sub_4_$subjectId",
                name = "4. Reference Materials & Resources",
                path = "Classroom / $gradeClass - $subjectName / 4. Reference Materials & Resources",
                driveUrl = "$baseUrl/resources",
                itemCount = resourceFiles.size,
                fileTypes = "PDF, Images, Links",
                files = resourceFiles
            )
        )
    }

    fun addActivityLog(title: String, folderName: String, driveUrl: String) {
        val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        val newLog = DriveSyncActivityLog(
            id = System.currentTimeMillis().toString(),
            title = title,
            folderName = folderName,
            timestamp = dateStr,
            isSuccess = true,
            driveUrl = driveUrl
        )
        _activityLogs.value = listOf(newLog) + _activityLogs.value
    }

    fun generateClassroomFolderUrl(subjectId: Long, subjectName: String, gradeClass: String): String {
        val cleanName = subjectName.lowercase().replace(" ", "_")
        val cleanGrade = gradeClass.lowercase().replace(" ", "_")
        return "https://drive.google.com/drive/folders/classroom_${cleanGrade}_${cleanName}_$subjectId"
    }
}

