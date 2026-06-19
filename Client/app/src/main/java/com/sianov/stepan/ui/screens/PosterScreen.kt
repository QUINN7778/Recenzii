package com.sianov.stepan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sianov.stepan.R
import com.sianov.stepan.data.model.AppItem
import com.sianov.stepan.ui.components.AppItemCard
import com.sianov.stepan.ui.components.SkeletonAppItemCard
import com.sianov.stepan.ui.viewmodel.PosterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosterScreen(
    onItemClick: (AppItem) -> Unit = {},
    viewModel: PosterViewModel = hiltViewModel()
) {
    val posters by viewModel.filteredPosters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val availableMonths by viewModel.availableMonths.collectAsState()
    val genres = viewModel.genres
    
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = { 
                        Text(
                            text = stringResource(R.string.screen_posters),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        ) 
                    },
                    actions = {
                        IconButton(onClick = { showFilterSheet = true }) {
                            val isFilterActive = selectedMonth != null || selectedGenre != null
                            if (isFilterActive) {
                                BadgedBox(badge = { Badge() }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Фильтр")
                                }
                            } else {
                                Icon(Icons.Default.FilterList, contentDescription = "Фильтр")
                            }
                        }
                        IconButton(onClick = { viewModel.loadPosters() }) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.retry))
                        }
                    }
                )
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_posters)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(5) { SkeletonAppItemCard() }
            }
        } else if (posters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedMonth != null || selectedGenre != null) 
                               stringResource(R.string.nothing_found)
                               else stringResource(R.string.error_posters_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(onClick = { 
                        viewModel.onSearchQueryChange("")
                        if (selectedMonth != null) viewModel.toggleMonth(selectedMonth!!)
                        if (selectedGenre != null) viewModel.toggleGenre(selectedGenre!!)
                        viewModel.loadPosters() 
                    }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posters) { poster ->
                    AppItemCard(poster, viewModel.repository, onClick = { onItemClick(poster) })
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Фильтры", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                if (availableMonths.isNotEmpty()) {
                    Text("Месяц", fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableMonths) { month ->
                            FilterChip(
                                selected = selectedMonth == month,
                                onClick = { viewModel.toggleMonth(month) },
                                label = { Text(month) },
                                leadingIcon = if (selectedMonth == month) {
                                    { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                Text("Жанр", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genres.forEach { genre ->
                        FilterChip(
                            selected = selectedGenre == genre,
                            onClick = { viewModel.toggleGenre(genre) },
                            label = { Text(genre) },
                            leadingIcon = if (selectedGenre == genre) {
                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Показать результаты")
                }
                
                if (selectedMonth != null || selectedGenre != null) {
                    TextButton(
                        onClick = { 
                            if (selectedMonth != null) viewModel.toggleMonth(selectedMonth!!)
                            if (selectedGenre != null) viewModel.toggleGenre(selectedGenre!!)
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Сбросить все")
                    }
                }
            }
        }
    }
}
