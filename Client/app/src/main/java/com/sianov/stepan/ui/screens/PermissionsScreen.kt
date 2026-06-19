package com.sianov.stepan.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    // Вспомогательная функция для получения текущего статуса (без side-effects)
    fun getNotifyStatus() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    fun getAlarmStatus() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else true

    // Состояния инициализируются СРАЗУ актуальными значениями, чтобы не было "красной вспышки"
    var isNotifyGranted by remember { mutableStateOf(getNotifyStatus()) }
    var isAlarmGranted by remember { mutableStateOf(getAlarmStatus()) }

    fun checkPermissions() {
        isNotifyGranted = getNotifyStatus()
        isAlarmGranted = getAlarmStatus()
    }

    // Автоматическая проверка при каждом возврате в приложение
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Лаунчеры для запросов
    val notifyLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { checkPermissions() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление разрешениями", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Для полноценной работы приложения необходимы следующие разрешения. Вы можете включить их здесь или в настройках системы.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 1. Уведомления
            PermissionBlock(
                title = "Уведомления",
                description = "Позволяет присылать важные новости и обновления театра.",
                icon = Icons.Default.Notifications,
                isGranted = isNotifyGranted,
                activeBrush = Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))),
                inactiveBrush = Brush.linearGradient(listOf(Color(0xFFF44336), Color(0xFFD32F2F))),
                onAction = {
                    if (!isNotifyGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifyLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // Прямой переход в настройки уведомлений
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                }
            )

            // 2. Напоминания
            PermissionBlock(
                title = "Напоминания о спектаклях",
                description = "Позволяет приложению будить телефон для отправки напоминания точно в срок.",
                icon = Icons.Default.Alarm,
                isGranted = isAlarmGranted,
                activeBrush = Brush.linearGradient(listOf(Color(0xFF2196F3), Color(0xFF1976D2))),
                inactiveBrush = Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2))),
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        android.widget.Toast.makeText(context, "Разрешено системой", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { openSettings(context) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Settings, null)
                Spacer(Modifier.width(8.dp))
                Text("Открыть настройки системы")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun PermissionBlock(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    activeBrush: Brush,
    inactiveBrush: Brush,
    onAction: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(if (isGranted) activeBrush else inactiveBrush)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        if (isGranted) "Разрешено" else "Требуется действие",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = { Text(description + "\n\nВы можете дать разрешение прямо сейчас или перейти в настройки системы.") },
            confirmButton = {
                Button(onClick = { 
                    showDialog = false
                    onAction()
                }) {
                    Text(if (isGranted) "В настройки" else "Разрешить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

private fun openSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
