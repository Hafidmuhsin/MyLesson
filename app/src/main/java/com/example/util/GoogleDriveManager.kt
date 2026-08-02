package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile
import com.google.api.services.drive.DriveScopes

/**
 * Utility to help integrate Google Drive: creates sign-in intent, builds Drive service,
 * and offers simple list/upload helpers.
 *
 * Usage (high-level):
 * 1. Create sign-in intent: val intent = GoogleDriveManager.getSignInIntent(context)
 * 2. Start activity for result from an Activity (use ActivityResult APIs preferably)
 * 3. On result, call GoogleDriveManager.getSignedInAccountFromIntent(data)
 * 4. Build Drive service: val drive = GoogleDriveManager.createDriveService(context, account)
 * 5. Use listFiles/uploadTextFile helpers on the Drive service.
 *
 * Notes:
 * - This example uses DRIVE_FILE scope (app can access files it creates or user explicitly opens),
 *   which is a safer, least-privilege option for typical classroom-like flows.
 * - If server-side long-lived access is required, request server auth code and exchange it on backend.
 */
object GoogleDriveManager {

    const val REQ_SIGN_IN = 9001

    fun getSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            // Request DRIVE_FILE scope so the app can create and manage files it creates
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        val client = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    fun getSignedInAccountFromIntent(data: Intent?): GoogleSignInAccount? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            task.getResult(Exception::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    /**
     * Builds a com.google.api.services.drive.Drive instance using GoogleAccountCredential
     * from the signed-in account and DRIVE_FILE scope.
     */
    fun createDriveService(context: Context, account: GoogleSignInAccount): Drive {
        val scopes = listOf(DriveScopes.DRIVE_FILE)
        val credential = GoogleAccountCredential.usingOAuth2(context, scopes)
        credential.selectedAccount = account.account

        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()

        return Drive.Builder(transport, jsonFactory, credential)
            .setApplicationName(context.applicationInfo.loadLabel(context.packageManager).toString())
            .build()
    }

    /** Lists files visible to the app (Drive API /files list). Returns at most pageSize items. */
    suspend fun listFiles(drive: Drive, pageSize: Int = 50): List<DriveFile> {
        return try {
            val result = drive.files().list()
                .setPageSize(pageSize)
                .setFields("files(id,name,mimeType,parents)")
                .execute()
            result.files ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Uploads a small text file to Drive; returns the new file id or null on failure. */
    suspend fun uploadTextFile(drive: Drive, name: String, content: String, mimeType: String = "text/plain", parentFolderId: String? = null): String? {
        return try {
            val metadata = DriveFile().apply {
                this.name = name
                parentFolderId?.let { parents = listOf(it) }
            }
            val byteContent = ByteArrayContent.fromString(mimeType, content)
            val file = drive.files().create(metadata, byteContent)
                .setFields("id")
                .execute()
            file?.id
        } catch (e: Exception) {
            null
        }
    }

    /** Helper: sign out the currently signed-in account (Activity context required). */
    fun signOut(activity: Activity, onComplete: (() -> Unit)? = null) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        val client = GoogleSignIn.getClient(activity, gso)
        client.signOut().addOnCompleteListener {
            onComplete?.invoke()
        }
    }
}
