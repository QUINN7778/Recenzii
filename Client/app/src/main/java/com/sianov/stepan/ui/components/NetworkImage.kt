package com.sianov.stepan.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.sianov.stepan.data.repository.AppRepository

@Composable
fun NetworkImage(
    url: String,
    repository: AppRepository,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val imageBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, url) {
        val bytes = repository.getImageBytes(url)
        if (bytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            value = bitmap?.asImageBitmap()
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            CircularProgressIndicator()
        }
    }
}
