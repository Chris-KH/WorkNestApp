package com.apcs.worknestapp.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    fun createImageFileUri(context: Context): Uri {
        val format = SimpleDateFormat("yyyyMMdd_HH:mm:ss", Locale.getDefault())
        val timestamp = format.format(Date())

        val imageFileName = "JPEG_${timestamp}"

        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Authority match AndroidManifest.xml
            imageFile
        )
    }
}
