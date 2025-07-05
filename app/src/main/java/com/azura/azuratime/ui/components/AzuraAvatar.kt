package com.azura.azuratime.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File

private const val TAG = "AzuraAvatar"

/**
 * AzuraAvatar - Composable function for displaying user avatars (face or fallback)
 * Loads images from local storage or URL using Coil with fallback to default icon.
 * Displays images in circular format with proper error handling.
 */
@Composable
fun AzuraAvatar(
    photoPath: String?,
    modifier: Modifier = Modifier,
    size: Int = 64
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isUrl = !photoPath.isNullOrEmpty() && (photoPath.startsWith("http://") || photoPath.startsWith("https://"))
    val isLocalFile = !photoPath.isNullOrEmpty() && !isUrl && File(photoPath).exists()
    val hasValidPhoto = isUrl || isLocalFile

    if (hasValidPhoto) {
        val imageData = if (isUrl) photoPath!! else File(photoPath!!)
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(imageData)
                    .crossfade(true)
                    .listener(
                        onStart = { Log.d(TAG, "Image loading started: $photoPath") },
                        onSuccess = { _, _ -> Log.d(TAG, "Image loaded successfully: $photoPath") },
                        onError = { _, error ->
                            Log.e(TAG, "Image loading failed: $photoPath")
                            Log.e(TAG, "  Error: ${error.throwable?.message}")
                        }
                    )
                    .build()
            ),
            contentDescription = "User Photo",
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Card(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Avatar",
                    modifier = Modifier.size((size * 0.6).dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun AzuraSmallAvatar(
    photoPath: String?,
    modifier: Modifier = Modifier
) {
    AzuraAvatar(photoPath = photoPath, modifier = modifier, size = 48)
}

@Composable
fun AzuraLargeAvatar(
    photoPath: String?,
    modifier: Modifier = Modifier
) {
    AzuraAvatar(photoPath = photoPath, modifier = modifier, size = 96)
}

fun isValidAzuraPhotoPath(photoPath: String?): Boolean {
    return !photoPath.isNullOrEmpty() && File(photoPath).exists().also { exists ->
        Log.d(TAG, "Photo path validation - Path: $photoPath, Exists: $exists")
    }
}
