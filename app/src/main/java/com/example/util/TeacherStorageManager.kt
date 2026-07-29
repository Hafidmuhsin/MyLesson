package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.database.AppDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream

data class BackupExportData(
    val exportDate: String,
    val appVersion: String = "1.0",
    val subjectsCount: Int,
    val summaryText: String
)

object TeacherStorageManager {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun readTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append("\n")
                    line = reader.readLine()
                }
            }
        }
        stringBuilder.toString()
    }

    suspend fun generateExportReport(db: AppDatabase): String = withContext(Dispatchers.IO) {
        val subjects = db.subjectDao().getAllSubjects()
        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("      TEACHER PLANNER DATA REPORT       \n")
        sb.append("=========================================\n\n")

        val subjectList = db.subjectDao().getAllSubjects()
        // Fetch snapshot
        val subjectEntities = db.subjectDao().getSubjectCount()
        sb.append("Total Subjects: $subjectEntities\n\n")

        Result.runCatching {
            val allSubjects = db.subjectDao().getSubjectCount()
            // Format report
        }
        sb.toString()
    }

    suspend fun writeStringToOutputStream(outputStream: OutputStream, content: String) = withContext(Dispatchers.IO) {
        outputStream.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
            stream.flush()
        }
    }
}
