package com.sianov.stepan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sianov.stepan.R
import com.sianov.stepan.ui.viewmodel.SettingsViewModel
import com.sianov.stepan.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val fontSizeMultiplier by viewModel.fontSizeMultiplier.collectAsState()
    val themeColorIndex by viewModel.themeColorIndex.collectAsState()
    val user by authViewModel.user.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val themes = listOf(
        stringResource(R.string.theme_default) to 0,
        stringResource(R.string.theme_theatre) to 1,
        stringResource(R.string.theme_ocean) to 2,
        stringResource(R.string.theme_forest) to 3
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.screen_settings)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLoggedIn) MaterialTheme.colorScheme.primaryContainer 
                                   else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (isLoggedIn && user != null) {
                            Text(user!!.name, style = MaterialTheme.typography.titleLarge)
                            Text(user!!.email, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text("Вы зашли как гость", style = MaterialTheme.typography.titleLarge)
                            Text("Войдите, чтобы покупать билеты", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Button(
                        onClick = { 
                            authViewModel.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoggedIn) MaterialTheme.colorScheme.error 
                                           else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isLoggedIn) "Выйти" else "Войти")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.dark_theme), style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isDarkTheme ?: false,
                    onCheckedChange = { viewModel.setDarkTheme(it) }
                )
            }

            Column {
                Text(
                    stringResource(R.string.font_size, fontSizeMultiplier), 
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = fontSizeMultiplier,
                    onValueChange = { viewModel.setFontSize(it) },
                    valueRange = 0.8f..1.5f,
                    steps = 6
                )
            }

            Column {
                Text(stringResource(R.string.theme_color), style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                themes.forEach { (name, index) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeColorIndex == index,
                            onClick = { viewModel.setThemeColor(index) }
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            HorizontalDivider()

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.app_info), style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.version))
                    Text(stringResource(R.string.developer))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.app_description))
                }
            }
        }
    }
}
