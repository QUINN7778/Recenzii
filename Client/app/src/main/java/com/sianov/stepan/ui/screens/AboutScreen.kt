package com.sianov.stepan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О приложении", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Блок Автора
            AuthorBlock()

            Text(
                "Технологический стек",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Сетка блоков технологий
            TechBlock(
                title = "Frontend / UI",
                description = "Kotlin & Jetpack Compose, Material Design 3",
                icon = Icons.Default.Smartphone,
                gradient = Brush.linearGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3)))
            )

            TechBlock(
                title = "Архитектура & DI",
                description = "MVVM, Hilt, StateFlow, Navigation Compose",
                icon = Icons.Default.Architecture,
                gradient = Brush.linearGradient(listOf(Color(0xFF03DAC6), Color(0xFF018786)))
            )

            TechBlock(
                title = "Данные & Сеть",
                description = "Retrofit, OkHttp, Room, DataStore, Jsoup",
                icon = Icons.Default.Storage,
                gradient = Brush.linearGradient(listOf(Color(0xFFFFB300), Color(0xFFFFA000)))
            )

            TechBlock(
                title = "Backend (API)",
                description = "Python & Django, BeautifulSoup4",
                icon = Icons.Default.Cloud,
                gradient = Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF388E3C)))
            )

            TechBlock(
                title = "Функционал",
                description = "Уведомления, Скачивание билетов, Скрапинг афиши",
                icon = Icons.Default.Extension,
                gradient = Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFFC2185B)))
            )

            Spacer(Modifier.height(24.dp))
            Text(
                "Данное приложение является дипломным проектом и предназначено для ознакомления с репертуаром Ивановского музыкального театра.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun AuthorBlock() {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    "Сиянов Степан",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Автор и разработчик",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TechBlock(title: String, description: String, icon: ImageVector, gradient: Brush) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.background(gradient).fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
