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
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState(initial = false)
    val user by authViewModel.user.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text(
                        text = "Настройки",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    ) 
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

            // 2. Оформление (Task 5)
            SettingsSection("Оформление")
            Card(
                shape = cardShape, 
                modifier = cardModifier.clickable { onNavigateToAppearance() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, null)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Внешний вид", fontWeight = FontWeight.Bold)
                        Text("Тема, цвета, размер шрифта", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null)
                }
            }

            // 3. Безопасность
            SettingsSection("Безопасность")
            Card(
                shape = cardShape, 
                modifier = cardModifier.clickable { onNavigateToPermissions() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, null)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Разрешения", fontWeight = FontWeight.Bold)
                        Text("Управление доступом приложения", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null)
                }
            }

            // 4. О приложении
            SettingsSection("Приложение")
            Card(
                shape = cardShape, 
                modifier = cardModifier.clickable { onNavigateToAbout() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("О приложении", fontWeight = FontWeight.Bold)
                        Text("Версия 1.0.1.", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null)
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
