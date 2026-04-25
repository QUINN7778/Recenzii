package com.sianov.stepan.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sianov.stepan.ui.viewmodel.SettingsViewModel
import com.sianov.stepan.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState(initial = false)
    val fontSizeMultiplier by viewModel.fontSizeMultiplier.collectAsState(initial = 1.0f)
    val themeColorIndex by viewModel.themeColorIndex.collectAsState(initial = 0)
    val user by authViewModel.user.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Настройки") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Единый стиль карточек для всех разделов
            val cardShape = RoundedCornerShape(16.dp)
            val cardModifier = Modifier.fillMaxWidth()

            // 1. Аккаунт
            SettingsSection("Профиль")
            Card(shape = cardShape, modifier = cardModifier) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user?.name ?: "Гость", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(user?.email ?: "Войдите для доступа", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { if (isLoggedIn) { authViewModel.logout(); onLogout() } else { onLogout() } }) {
                        Icon(if (isLoggedIn) Icons.Default.ExitToApp else Icons.Default.Login, null)
                    }
                }
            }

            // 2. Оформление
            SettingsSection("Оформление")
            Card(shape = cardShape, modifier = cardModifier) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BrightnessMedium, null)
                        Spacer(Modifier.width(16.dp))
                        Text("Тёмная тема", Modifier.weight(1f))
                        Switch(checked = isDarkTheme ?: false, onCheckedChange = { viewModel.setDarkTheme(it) })
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Размер шрифта: ${(fontSizeMultiplier * 100).toInt()}%")
                    Slider(value = fontSizeMultiplier, onValueChange = { viewModel.setFontSize(it) }, valueRange = 0.8f..1.4f, steps = 5)
                }
            }

            // 3. Цветовой стиль
            SettingsSection("Цветовой стиль")
            Card(shape = cardShape, modifier = cardModifier) {
                Column(modifier = Modifier.padding(8.dp)) {
                    val themes = listOf("Стандарт", "Театральный", "Океан", "Лесной", "Минимализм")
                    themes.forEachIndexed { index, name ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.setThemeColor(index) }.padding(8.dp)
                        ) {
                            RadioButton(selected = themeColorIndex == index, onClick = { viewModel.setThemeColor(index) })
                            Text(name)
                        }
                    }
                }
            }

            // 4. О приложении
            SettingsSection("О приложении")
            Card(shape = cardShape, modifier = cardModifier) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Версия 1.2.0 (Stable)", style = MaterialTheme.typography.bodyMedium)
                    Text("Разработчик: Сиянов Степан", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
}
