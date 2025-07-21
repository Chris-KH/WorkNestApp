package com.apcs.worknestapp.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    fun copyUriToTempFile(context: Context, uri: Uri): File? {
        return try {
            val suffix = when(context.contentResolver.getType(uri)) {
                "image/png" -> ".png"
                "image/webp" -> ".webp"
                else -> ".jpg"
            }
            val file = File.createTempFile("upload_", suffix, context.cacheDir)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            file
        } catch(e: Exception) {
            Log.e("File Utils", e.message, e)
            null
        }
    }

    fun createImageFileUri(context: Context): Uri {
        val format = SimpleDateFormat("yyyyMMdd_HH:mm:ss", Locale.getDefault())
        val timestamp = format.format(Date())

        val imageFileName = "JPEG_${timestamp}_"

        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Authority match AndroidManifest.xml
            imageFile
        )
    }
}
