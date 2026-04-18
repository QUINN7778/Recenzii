package com.sianov.stepan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sianov.stepan.R
import com.sianov.stepan.data.model.AppItem
import com.sianov.stepan.ui.components.AppItemCard
import com.sianov.stepan.ui.viewmodel.AuthViewModel
import com.sianov.stepan.ui.viewmodel.PosterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTheatreScreen(
    onNavigateToDetail: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    posterViewModel: PosterViewModel = hiltViewModel()
) {
    val favorites by authViewModel.favorites.collectAsState()
    val reminders by authViewModel.reminders.collectAsState()
    val visited by authViewModel.visited.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    // Используем filteredPosters из PosterViewModel
    val allPosters by posterViewModel.filteredPosters.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Избранное", "Напоминания", "Я посетил")
    val tabIcons = listOf(Icons.Default.Favorite, Icons.Default.Notifications, Icons.Default.CheckCircle)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мой театр", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (!isLoggedIn) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                    Text("Войдите в аккаунт", style = MaterialTheme.typography.titleLarge)
                    Text("Чтобы пользоваться личным кабинетом", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            icon = { Icon(tabIcons[index], contentDescription = null) }
                        )
                    }
                }

                val currentList = when (selectedTab) {
                    0 -> allPosters.filter { favorites.contains(it.detailUrl) }
                    1 -> allPosters.filter { reminders.contains(it.detailUrl) }
                    2 -> allPosters.filter { visited.contains(it.detailUrl) }
                    else -> emptyList()
                }

                if (currentList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = tabIcons[selectedTab],
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Здесь пока пусто",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Отмечайте спектакли в афише",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentList) { item ->
                            AppItemCard(
                                item = item,
                                onClick = { onNavigateToDetail(item.detailUrl) },
                                repository = posterViewModel.repository
                            )
                        }
                    }
                }
            }
        }
    }
}
