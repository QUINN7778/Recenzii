package com.sianov.stepan.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sianov.stepan.data.model.AppItem
import com.sianov.stepan.data.repository.AppRepository

@Composable
fun AppItemCard(item: AppItem, repository: AppRepository, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column {
            NetworkImage(
                url = item.imageUrl,
                repository = repository,
                modifier = Modifier.Companion.fillMaxWidth().height(200.dp)
            )
            Column(modifier = Modifier.Companion.padding(16.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Companion.Ellipsis
                )
                Spacer(modifier = Modifier.Companion.height(4.dp))
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.Companion.height(8.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Companion.Ellipsis
                )
            }
        }
    }
}