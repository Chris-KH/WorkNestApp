package com.apcs.worknestapp.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.cloudinary.MyCloudinary.uploadAvatarToCloudinary
import com.apcs.worknestapp.utils.FileUtils.copyUriToTempFile
import com.apcs.worknestapp.utils.FileUtils.createImageFileUri
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPicker(
    userId: String?,
    imageUrl: String?,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    var showBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun uploadImage(uri: Uri, isFromCamera: Boolean = false) {
        coroutineScope.launch {
            val file = copyUriToTempFile(context, uri)
            if (file != null) {
                uploadAvatarToCloudinary(
                    context = context,
                    file = file,
                    folder = "/$userId",
                    onSuccess = { url ->
                        if (isFromCamera) {
                            tempImageUri?.let { uri ->
                                context.contentResolver.delete(uri, null, null)
                                tempImageUri = null
                            }
                        }
                        coroutineScope.launch {
                            val isSuccess = authViewModel.updateUserAvatar(url)
                            if (isSuccess) {
                                snackbarHost.showSnackbar(
                                    message = "Upload successful",
                                    withDismissAction = true,
                                )
                            } else {
                                snackbarHost.showSnackbar(
                                    message = "Upload avatar failed",
                                    withDismissAction = true,
                                )
                            }
                        }
                    },
                    onError = { error ->
                        if (isFromCamera) {
                            tempImageUri?.let { uri ->
                                context.contentResolver.delete(uri, null, null)
                                tempImageUri = null
                            }
                        }
                        coroutineScope.launch {
                            snackbarHost.showSnackbar(
                                message = "Upload avatar failed: $error",
                                withDismissAction = true,
                            )
                        }
                    })
            } else {
                snackbarHost.showSnackbar(
                    message = "Fail: Cannot read image file",
                    withDismissAction = true,
                )
            }
        }
    }

    // Launcher for photo picker
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(), onResult = { uri: Uri? ->
            // Persist media file access: context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (uri != null) {
                uploadImage(uri)
            } else {
                coroutineScope.launch {
                    snackbarHost.showSnackbar(
                        message = "No photos selected",
                        withDismissAction = true,
                    )
                }
            }
        })

    // Launcher for Legacy Gallery (fallback Android < 11 or Photo Picker unavailable)
    val pickImageLegacyLauncher = rememberLauncherForActivityResult(
        contract = GetContent(), // "image/*" open gallery
        onResult = { uri: Uri? ->
            if (uri != null) {
                uploadImage(uri)
            } else {
                coroutineScope.launch {
                    snackbarHost.showSnackbar(
                        message = "No photos selected",
                        withDismissAction = true,
                    )
                }
            }
        })

    val requestReadPermissionsLauncher = rememberLauncherForActivityResult(
        contract = RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.entries.all { it.value }
        if (isGranted) {
            pickImageLegacyLauncher.launch("image/*")
        } else {
            val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    readImagePermission,
                )
            ) {
                coroutineScope.launch {
                    snackbarHost.showSnackbar(
                        message = "Media permission is required",
                        withDismissAction = true,
                    )
                }
            } else {
                coroutineScope.launch {
                    val isClicked = snackbarHost.showSnackbar(
                        message = "Media permission is required",
                        withDismissAction = true,
                        actionLabel = "Settings"
                    )

                    if (isClicked == SnackbarResult.ActionPerformed) {
                        val intent = Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                }
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
                    context, readImagePermission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickImageLegacyLauncher.launch("image/*")
            } else {
                requestReadPermissionsLauncher.launch(arrayOf(readImagePermission))
            }
        }
    }

    // Launcher take photo
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = TakePicture(), onResult = { success: Boolean ->
            if (success) {
                uploadImage(tempImageUri!!, true)
            } else {
                tempImageUri?.let { uri ->
                    context.contentResolver.delete(uri, null, null)
                    tempImageUri = null
                }
                coroutineScope.launch {
                    snackbarHost.showSnackbar(
                        message = "Take photo is cancelled",
                        withDismissAction = true,
                    )
                }
            }
        })

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            tempImageUri = createImageFileUri(context)
            tempImageUri?.let { takePhotoLauncher.launch(it) }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity, Manifest.permission.CAMERA
                )
            ) {
                coroutineScope.launch {
                    snackbarHost.showSnackbar(
                        message = "Camera permission is required",
                        withDismissAction = true,
                    )
                }
            } else {
                coroutineScope.launch {
                    val isClicked = snackbarHost.showSnackbar(
                        message = "Camera permission is required",
                        withDismissAction = true,
                        actionLabel = "Settings"
                    )

                    if (isClicked == SnackbarResult.ActionPerformed) {
                        val intent = Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                }
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

            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showBottomSheet) {
        AvatarBottomSheet(
            onDismiss = { showBottomSheet = false },
            onTakePhoto = onTakePhoto,
            onChooseFromLibrary = onChooseFromLibrary,
        )
    }
    AsyncImage(
        model = ImageRequest.Builder(context).data(imageUrl).crossfade(true).build(),
        placeholder = painterResource(R.drawable.fade_avatar_fallback),
        error = painterResource(R.drawable.fade_avatar_fallback),
        contentDescription = "Avatar",
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Medium,
        modifier = modifier.clickable(onClick = { showBottomSheet = true }),
    )
}
