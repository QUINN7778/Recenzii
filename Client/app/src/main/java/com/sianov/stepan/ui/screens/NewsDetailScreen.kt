package com.sianov.stepan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sianov.stepan.ui.components.NetworkImage
import com.sianov.stepan.ui.viewmodel.NewsDetailViewModel

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    url: String,
    title: String,
    date: String,
    imageUrl: String,
    onBack: () -> Unit,
    viewModel: NewsDetailViewModel = hiltViewModel()
) {
    val content by viewModel.content.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(url) {
        viewModel.loadNewsDetail(url)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новость", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. ИЗОБРАЖЕНИЕ (если есть)
                if (imageUrl.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                        NetworkImage(
                            url = imageUrl,
                            repository = viewModel.repository,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Тень внизу картинки для мягкого перехода
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            androidx.compose.ui.graphics.Color.Transparent,
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )
                    }
                }

                // 2. КОНТЕНТНАЯ ЧАСТЬ
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Дата в виде чипа
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Заголовок
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 34.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(24.dp))

                    // Основной текст
                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                        }
                    } else if (content.isNullOrEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Не удалось загрузить детали",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.loadNewsDetail(url) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Попробовать снова")
                                }
                            }
                        }
                    } else {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val annotatedContent = remember(content, primaryColor) {
                            val text = content ?: ""
                            val urlRegex = Regex("(https?://[\\w-]+(\\.[\\w-]+)+(:\\d+)?(/[^\\s]*)?)")
                            buildAnnotatedString {
                                var lastIndex = 0
                                urlRegex.findAll(text).forEach { match ->
                                    append(text.substring(lastIndex, match.range.first))
                                    val url = match.value
                                    withLink(LinkAnnotation.Url(url)) {
                                        append(url)
                                        addStyle(
                                            style = SpanStyle(
                                                color = primaryColor,
                                                textDecoration = TextDecoration.Underline,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            start = this.length - url.length,
                                            end = this.length
                                        )
                                    }
                                    lastIndex = match.range.last + 1
                                }
                                if (lastIndex < text.length) {
                                    append(text.substring(lastIndex))
                                }
                            }
                        }

                        Text(
                            text = annotatedContent,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 28.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}
