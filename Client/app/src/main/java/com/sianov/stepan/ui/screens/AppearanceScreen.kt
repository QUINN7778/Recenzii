package com.sianov.stepan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sianov.stepan.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState(initial = false)
    val fontSizeMultiplier by viewModel.fontSizeMultiplier.collectAsState(initial = 1.0f)
    val themeColorIndex by viewModel.themeColorIndex.collectAsState(initial = 0)
    val dynamicColor by viewModel.dynamicColor.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Внешний вид", fontWeight = FontWeight.ExtraBold) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. ПРЕВЬЮ
            Text("Предпросмотр", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ThemePreviewCard()

            // 2. ОСНОВНЫЕ НАСТРОЙКИ
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BrightnessMedium, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Тёмная тема", fontWeight = FontWeight.Bold)
                            Text("Использовать темные оттенки", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = isDarkTheme ?: false, onCheckedChange = { viewModel.setDarkTheme(it) })
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Динамические цвета", fontWeight = FontWeight.Bold)
                            Text("Цвета на основе обоев (Android 12+)", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = dynamicColor, 
                            onCheckedChange = { viewModel.setDynamicColor(it) }
                        )
                    }
                }
            }

            // 3. ЦВЕТОВЫЕ СХЕМЫ
            if (!dynamicColor) {
                Text("Цветовая схема", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val colorThemes = listOf(
                    "Стандарт" to Color(0xFF6750A4),
                    "Театральный" to Color(0xFFB71C1C),
                    "Океан" to Color(0xFF01579B),
                    "Лесной" to Color(0xFF1B5E20),
                    "Минимализм" to Color(0xFF455A64)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colorThemes.forEachIndexed { index, pair ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(pair.second)
                                .clickable { viewModel.setThemeColor(index) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (themeColorIndex == index) {
                                Icon(Icons.Default.Check, null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            // 4. ТЕКСТ
            Text("Текст", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("А", fontSize = 14.sp)
                        Slider(
                            value = fontSizeMultiplier, 
                            onValueChange = { viewModel.setFontSize(it) }, 
                            valueRange = 0.8f..1.4f, 
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text("А", fontSize = 24.sp)
                    }
                    Text(
                        "Размер шрифта: ${(fontSizeMultiplier * 100).toInt()}%",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ThemePreviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Пример заголовка", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Это пример того, как будет выглядеть текст в приложении.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {}, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)) {
                        Text("Кнопка", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
