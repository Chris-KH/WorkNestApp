package com.apcs.worknestapp.data.remote.cloudinary

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.File

object MyCloudinary {
    private const val PRESET = "my_unsigned_upload"

    fun uploadAvatarToCloudinary(
        context: Context,
        file: File,
        folder: String = "",
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        MediaManager.get().upload(file.absolutePath)
            .unsigned(PRESET)
            .option("connect_timeout", 10000)
            .option("read_timeout", 10000)
            .option("folder", "unsigned$folder")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) onSuccess(url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    onError(error.description)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch(context)
    }
}
