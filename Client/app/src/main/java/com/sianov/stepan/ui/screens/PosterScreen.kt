package com.sianov.stepan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.screen_posters)) },
                    actions = {
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
                items(5) {
                    SkeletonAppItemCard()
                }
            }
        } else if (posters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) stringResource(R.string.nothing_found) 
                               else stringResource(R.string.error_site_unavailable),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (searchQuery.isEmpty()) {
                        Button(onClick = { viewModel.loadPosters() }) {
                            Text(stringResource(R.string.retry))
                        }
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
}
