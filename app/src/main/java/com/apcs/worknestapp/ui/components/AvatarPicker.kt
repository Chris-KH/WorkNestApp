package com.apcs.worknestapp.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPicker(
    imageUrl: String?,
    isLoading: Boolean,
    snackbarHost: SnackbarHostState,
) {
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Launcher for photo picker
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                println("Selected from library: $uri")
                // TODO: Lưu URI này, hiển thị ảnh, tải lên server, v.v.
                // Persist media file access:
                // context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                println("No media selected from library")
            }
        }
    )

    // Launcher for Legacy Gallery (fallback Android < 11 or Photo Picker unavailable)
    val pickImageLegacyLauncher = rememberLauncherForActivityResult(
        contract = GetContent(), // "image/*" open gallery
        onResult = { uri: Uri? ->
            if (uri != null) {
                println("Selected from legacy gallery: $uri")
                // TODO: Lưu URI này, hiển thị ảnh, tải lên server, v.v.
            } else {
                println("No image selected from legacy gallery")
            }
        }
    )

    val requestReadPermissionsLauncher = rememberLauncherForActivityResult(
        contract = RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            pickImageLegacyLauncher.launch("image/*")
        } else {
            coroutineScope.launch {
                snackbarHost.showSnackbar(
                    message = "Fail: Permission is denied",
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    val onChooseFromLibrary: () -> Unit = {
        if (PickVisualMedia.isPhotoPickerAvailable(context)) {
            pickMediaLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        } else {
            val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(
                    context,
                    readImagePermission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickImageLegacyLauncher.launch("image/*")
            } else {
                // Request permission
                requestReadPermissionsLauncher.launch(
                    arrayOf(readImagePermission)
                )
            }
        }
    }

    // Launcher take photo
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                // Ảnh đã được chụp và lưu vào tempImageUri
                println("Photo taken: $tempImageUri")
                // TODO: Xử lý tempImageUri (hiển thị, tải lên, xóa file tạm)
            } else {
                tempImageUri?.let { uri ->
                    context.contentResolver.delete(uri, null, null)
                    tempImageUri = null
                }
            }
        }
    )

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            tempImageUri = createImageFileUri(context)
            tempImageUri?.let { takePhotoLauncher.launch(it) }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.CAMERA
                )
            ) {
                // TODO: Hiển thị SnackBar/AlertDialog giải thích tại sao cần quyền
                println("Camera permission denied (rationale should be shown)")
            } else {
                // TODO: Hiển thị SnackBar/AlertDialog hướng dẫn người dùng vào cài đặt
                println("Camera permission denied permanently. Direct user to settings.")
                // Mở cài đặt ứng dụng
//                val intent = Intent(
//                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                    Uri.fromParts("package", context.packageName, null)
//                )
//                context.startActivity(intent)
            }
        }
    }

    val onTakePhoto: () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                tempImageUri = createImageFileUri(context)
                tempImageUri?.let { takePhotoLauncher.launch(it) }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity, Manifest.permission.CAMERA
            ) -> {
                // TODO: Hiển thị SnackBar/AlertDialog giải thích trước khi yêu cầu lại
                println("Show rationale for camera permission before asking again.")
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        AvatarBottomSheet(
            onDismiss = { showBottomSheet = false },
            onTakePhoto = onTakePhoto,
            onChooseFromLibrary = onChooseFromLibrary,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.fade_avatar_fallback),
            contentDescription = "Avatar place holder",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .border(
                    width = (0.5).dp,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                .clip(CircleShape)
                .clickable(
                    onClick = { showBottomSheet = true },
                ),
        )
        Text(
            text = "Edit avatar",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Roboto,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = (0.5).dp,
        )
    }
}

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
