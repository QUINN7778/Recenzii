package com.sianov.stepan.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.sianov.stepan.data.repository.AppRepository
import android.graphics.Bitmap

@Composable
fun NetworkImage(
    url: String,
    repository: AppRepository,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var isLoading by remember(url) { mutableStateOf(true) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .crossfade(150)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Максимальное качество без сжатия
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build(),

            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            onSuccess = { isLoading = false },
            onError = { isLoading = false },
            onLoading = { isLoading = true }
        )

        if (isLoading && url.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().shimmer())
        }
    }
}
