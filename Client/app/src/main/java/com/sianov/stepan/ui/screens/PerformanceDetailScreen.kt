package com.sianov.stepan.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.sianov.stepan.data.model.CastMember
import com.sianov.stepan.data.model.ReviewResponse
import com.sianov.stepan.data.repository.AppRepository
import com.sianov.stepan.ui.components.NetworkImage
import com.sianov.stepan.ui.components.SkeletonPerformanceDetail
import com.sianov.stepan.ui.viewmodel.PerformanceDetailViewModel
import com.sianov.stepan.ui.viewmodel.AuthViewModel
import android.content.Intent
import android.net.Uri

import android.Manifest
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

import com.sianov.stepan.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceDetailScreen(
    url: String,
    performanceDate: String,
    onBack: () -> Unit,
    onNavigateToWebView: (String) -> Unit,
    viewModel: PerformanceDetailViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isPast = remember(performanceDate) { DateUtils.isPast(performanceDate) }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    
    val favorites by authViewModel.favorites.collectAsState()
    val reminders by authViewModel.reminders.collectAsState()
    val visited by authViewModel.visited.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var showReviewDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableIntStateOf(5) }
    var userComment by remember { mutableStateOf("") }
    var selectedFullScreenIndex by remember { mutableStateOf<Int?>(null) }

    val message by viewModel.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(url) {
        viewModel.loadDetail(url)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val allImages = remember(detail) {
            detail?.let { d ->
                val gallery = d.galleryImages
                if (gallery.isEmpty()) {
                    listOf(d.imageUrl)
                } else {
                    // Переставляем постер (последний в галерее) в начало списка для просмотра
                    val poster = gallery.last()
                    val others = gallery.dropLast(1)
                    listOf(poster) + others
                }
            } ?: emptyList()
        }

        Scaffold(
            topBar = {
                if (selectedFullScreenIndex == null) {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    CircleShape
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            bottomBar = {
                if (detail != null && !isLoading && selectedFullScreenIndex == null) {
                    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
                        Button(
                            onClick = { onNavigateToWebView(detail?.buyTicketUrl ?: url) },
                            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Купить билет на сайте", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { padding ->
            if (isLoading) {
                Box(Modifier.padding(padding).fillMaxSize()) { SkeletonPerformanceDetail() }
            } else if (detail != null) {
                detail?.let { performance ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
                    ) {
                        item {
                            // 1. ГЕРОЙ-ИЗОБРАЖЕНИЕ (Статичный постер)
                            Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                                val mainImg = performance.galleryImages.lastOrNull() ?: performance.imageUrl
                                NetworkImage(
                                    url = mainImg,
                                    repository = viewModel.repository,
                                    modifier = Modifier.fillMaxSize().clickable { 
                                        selectedFullScreenIndex = 0 
                                    },
                                    contentScale = ContentScale.Crop
                                )
                                // Градиент снизу
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                            startY = 300f
                                        )
                                    )
                                )
                            }

                            Column(Modifier.padding(16.dp)) {
                                // 2. ЗАГОЛОВОК
                                Text(
                                    text = performance.title,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 38.sp
                                )

                                Spacer(Modifier.height(24.dp))

                                // 3. КОЛОНКА ИНФОРМАЦИИ
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (!performance.duration.isNullOrEmpty()) {
                                        InfoRow(Icons.Outlined.Timer, "Продолжительность", performance.duration!!)
                                    }
                                    if (!performance.acts.isNullOrEmpty()) {
                                        InfoRow(Icons.Outlined.TheaterComedy, "Постановка", performance.acts!!)
                                    }
                                    InfoRow(
                                        Icons.Outlined.CalendarMonth, 
                                        "Дата сеанса", 
                                        if (isPast) "$performanceDate (НЕ АКТУАЛЬНО)" else performanceDate
                                    )
                                }

                                Spacer(Modifier.height(32.dp))

                                // КНОПКИ ДЕЙСТВИЙ
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    ActionButton(
                                        icon = if (favorites.contains(url)) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        label = "Избранное",
                                        isActive = favorites.contains(url),
                                        activeColor = Color(0xFFE91E63),
                                        onClick = { if (isLoggedIn) authViewModel.toggleFavorite(url) }
                                    )
                                    ActionButton(
                                        icon = if (reminders.contains(url)) Icons.Default.NotificationsActive else Icons.Outlined.NotificationsNone,
                                        label = "Напомнить",
                                        isActive = reminders.contains(url),
                                        activeColor = Color(0xFFFF9800),
                                        onClick = { 
                                            if (isLoggedIn) {
                                                if (reminders.contains(url)) {
                                                    authViewModel.toggleReminder(url, performance.title, performanceDate)
                                                    return@ActionButton
                                                }
                                                val hasNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                                                } else true
                                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                                val hasAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                    alarmManager.canScheduleExactAlarms()
                                                } else true
                                                if (!hasNotify && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                } else if (!hasAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply { data = Uri.fromParts("package", context.packageName, null) })
                                                } else {
                                                    authViewModel.toggleReminder(url, performance.title, performanceDate)
                                                }
                                            } 
                                        }
                                    )
                                    ActionButton(
                                        icon = if (visited.contains(url)) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                        label = "Я был",
                                        isActive = visited.contains(url),
                                        activeColor = Color(0xFF4CAF50),
                                        onClick = { if (isLoggedIn) authViewModel.toggleVisited(url) }
                                    )
                                }

                                // СЮЖЕТ
                                if (performance.description.isNotEmpty()) {
                                    Spacer(Modifier.height(40.dp))
                                    Text(text = "О спектакле", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(12.dp))
                                    Text(text = performance.description, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // ГАЛЕРЕЯ
                                if (performance.galleryImages.isNotEmpty()) {
                                    Spacer(Modifier.height(40.dp))
                                    Text(text = "Галерея", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(16.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(end = 16.dp)
                                    ) {
                                        items(performance.galleryImages.size) { index ->
                                            val img = performance.galleryImages[index]
                                            Card(
                                                modifier = Modifier.width(180.dp).height(120.dp).clickable { 
                                                    val fullIndex = allImages.indexOf(img)
                                                    selectedFullScreenIndex = if (fullIndex != -1) fullIndex else 0
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                NetworkImage(url = img, repository = viewModel.repository, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            }
                                        }
                                    }
                                }

                                // АКТЕРЫ
                                Spacer(Modifier.height(40.dp))
                                Text(text = "Действующие лица", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(16.dp))
                                if (performance.cast.isNotEmpty()) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(end = 16.dp)
                                    ) {
                                        items(performance.cast) { actor -> CastMemberCard(actor, viewModel.repository) }
                                    }
                                }
                                
                                // ОТЗЫВЫ
                                Spacer(Modifier.height(40.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Отзывы", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    if (isLoggedIn) {
                                        TextButton(onClick = { showReviewDialog = true }) {
                                            Icon(Icons.Default.Edit, null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Написать")
                                        }
                                    }
                                }
                            }
                        }
                        items(reviews) { review -> ReviewCard(review) }
                        item { Spacer(Modifier.height(120.dp)) }
                    }
                }
            }
        }

        // ПОЛНОЭКРАННЫЙ ПРОСМОТР ФОТО (Свайп работает здесь!)
        AnimatedVisibility(
            visible = selectedFullScreenIndex != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            val fullScreenPagerState = rememberPagerState(
                initialPage = selectedFullScreenIndex ?: 0,
                pageCount = { allImages.size }
            )
            
            LaunchedEffect(selectedFullScreenIndex) {
                selectedFullScreenIndex?.let { fullScreenPagerState.scrollToPage(it) }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { selectedFullScreenIndex = null },
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = fullScreenPagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    NetworkImage(
                        url = allImages[page],
                        repository = viewModel.repository,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Fit
                    )
                }

                if (allImages.size > 1) {
                    PagerIndicator(
                        pageCount = allImages.size,
                        currentPage = fullScreenPagerState.currentPage,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp),
                        activeColor = Color.White,
                        inactiveColor = Color.White.copy(alpha = 0.3f)
                    )
                }

                IconButton(
                    onClick = { selectedFullScreenIndex = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Ваш отзыв") },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        repeat(5) { index ->
                            IconButton(onClick = { userRating = index + 1 }) {
                                Icon(imageVector = if (userRating > index) Icons.Default.Star else Icons.Outlined.StarBorder, contentDescription = null, tint = if (userRating > index) Color(0xFFFFB300) else Color.Gray)
                            }
                        }
                    }
                    OutlinedTextField(value = userComment, onValueChange = { userComment = it }, modifier = Modifier.fillMaxWidth().height(120.dp), placeholder = { Text("Поделитесь впечатлениями...") })
                }
            },
            confirmButton = { Button(onClick = { viewModel.addReview(url, userRating, userComment); showReviewDialog = false; userComment = "" }, enabled = userComment.isNotBlank()) { Text("Отправить") } },
            dismissButton = { TextButton(onClick = { showReviewDialog = false }) { Text("Отмена") } }
        )
    }
}

@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val color = if (currentPage == index) activeColor else inactiveColor
            Box(
                modifier = Modifier
                    .size(if (currentPage == index) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ReviewCard(review: ReviewResponse) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(review.username, fontWeight = FontWeight.Bold)
                Text(review.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Row(Modifier.padding(vertical = 4.dp)) {
                repeat(5) { i -> Icon(if (review.rating > i) Icons.Default.Star else Icons.Outlined.StarBorder, null, Modifier.size(14.dp), tint = if (review.rating > i) Color(0xFFFFB300) else Color.Gray) }
            }
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, label: String, isActive: Boolean, activeColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(8.dp)) {
        Icon(icon, label, tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CastMemberCard(castMember: CastMember, repository: AppRepository) {
    Column(modifier = Modifier.width(120.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (!castMember.imageUrl.isNullOrEmpty()) NetworkImage(castMember.imageUrl, repository, Modifier.fillMaxSize(), ContentScale.Crop)
            else Icon(Icons.Default.Person, null, Modifier.size(50.dp).align(Alignment.Center), tint = MaterialTheme.colorScheme.outline)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            castMember.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(2.dp))
        Text(
            castMember.role,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
