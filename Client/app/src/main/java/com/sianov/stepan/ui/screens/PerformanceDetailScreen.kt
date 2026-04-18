package com.sianov.stepan.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceDetailScreen(
    url: String,
    onBack: () -> Unit,
    viewModel: PerformanceDetailViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    
    val favorites by authViewModel.favorites.collectAsState()
    val reminders by authViewModel.reminders.collectAsState()
    val visited by authViewModel.visited.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var currentMainImageUrl by remember(detail) { 
        mutableStateOf(detail?.imageUrl ?: "") 
    }

    // Состояние для написания отзыва
    var showReviewDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableIntStateOf(5) }
    var userComment by remember { mutableStateOf("") }

    val context = LocalContext.current
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = detail?.title ?: "",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            if (detail != null && !isLoading) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    item {
                        // 1. ГЛАВНАЯ КАРТИНКА
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            NetworkImage(
                                url = currentMainImageUrl.ifEmpty { performance.imageUrl },
                                repository = viewModel.repository,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(Modifier.padding(horizontal = 16.dp)) {
                            // Название и Автор
                            Text(
                                text = performance.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            if (!performance.author.isNullOrEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = performance.author!!,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontStyle = FontStyle.Italic
                                )
                            }

                            // ACTION BUTTONS ROW
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ActionButton(
                                    icon = if (favorites.contains(url)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    label = "Избранное",
                                    isActive = favorites.contains(url),
                                    activeColor = Color(0xFFE91E63),
                                    onClick = { if (isLoggedIn) authViewModel.toggleFavorite(url) }
                                )
                                ActionButton(
                                    icon = if (reminders.contains(url)) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                    label = "Напомнить",
                                    isActive = reminders.contains(url),
                                    activeColor = Color(0xFFFF9800),
                                    onClick = { if (isLoggedIn) authViewModel.toggleReminder(url) }
                                )
                                ActionButton(
                                    icon = if (visited.contains(url)) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                    label = "Я был",
                                    isActive = visited.contains(url),
                                    activeColor = Color(0xFF4CAF50),
                                    onClick = { if (isLoggedIn) authViewModel.toggleVisited(url) }
                                )
                            }

                            if (!performance.acts.isNullOrEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = performance.acts!!,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            // 2. ГАЛЕРЕЯ
                            if (performance.galleryImages.isNotEmpty()) {
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    text = "Галерея",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(12.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(end = 16.dp)
                                ) {
                                    items(performance.galleryImages) { img ->
                                        Card(
                                            modifier = Modifier
                                                .width(140.dp)
                                                .height(90.dp)
                                                .clickable { currentMainImageUrl = img },
                                            shape = RoundedCornerShape(12.dp),
                                            border = if (currentMainImageUrl == img) 
                                                BorderStroke(3.dp, MaterialTheme.colorScheme.primary) 
                                                else null,
                                            elevation = CardDefaults.cardElevation(2.dp)
                                        ) {
                                            NetworkImage(
                                                url = img,
                                                repository = viewModel.repository,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. ПРОДОЛЖИТЕЛЬНОСТЬ
                            if (!performance.duration.isNullOrEmpty()) {
                                Spacer(Modifier.height(24.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Продолжительность: ",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = performance.duration!!,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }

                            // 4. СЮЖЕТ
                            if (performance.description.isNotEmpty()) {
                                Spacer(Modifier.height(32.dp))
                                Text(
                                    text = "О спектакле",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = performance.description,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = 24.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // 5. АКТЕРЫ
                            if (performance.cast.isNotEmpty()) {
                                Spacer(Modifier.height(32.dp))
                                Text(
                                    text = "Действующие лица и исполнители",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(16.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(end = 16.dp)
                                ) {
                                    items(performance.cast) { actor ->
                                        CastMemberCard(actor, viewModel.repository)
                                    }
                                }
                            }

                            // 6. ОТЗЫВЫ (REVIEW SECTION)
                            Spacer(Modifier.height(40.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Отзывы зрителей",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isLoggedIn) {
                                    TextButton(onClick = { showReviewDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Написать")
                                    }
                                }
                            }

                            if (reviews.isEmpty()) {
                                Text(
                                    text = "Будьте первым, кто оставит отзыв!",
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    // Список отзывов
                    items(reviews) { review ->
                        ReviewCard(review)
                    }

                    item {
                        Spacer(Modifier.height(120.dp))
                    }
                }
            }
        }
    }

    // Диалог написания отзыва
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Ваш отзыв") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) { index ->
                            val rating = index + 1
                            IconButton(onClick = { userRating = rating }) {
                                Icon(
                                    imageVector = if (userRating >= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (userRating >= rating) Color(0xFFFFB300) else Color.Gray
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = userComment,
                        onValueChange = { userComment = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Поделитесь впечатлениями...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addReview(url, userRating, userComment)
                        showReviewDialog = false
                        userComment = ""
                    },
                    enabled = userComment.isNotBlank()
                ) {
                    Text("Отправить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun ReviewCard(review: ReviewResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (review.rating > index) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (review.rating > index) Color(0xFFFFB300) else Color.Gray
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CastMemberCard(castMember: CastMember, repository: AppRepository) {
    ElevatedCard(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!castMember.imageUrl.isNullOrEmpty()) {
                    NetworkImage(
                        url = castMember.imageUrl,
                        repository = repository,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = castMember.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                textAlign = TextAlign.Center,
                minLines = 2,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = castMember.role,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                textAlign = TextAlign.Center,
                minLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}
