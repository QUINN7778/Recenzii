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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.sianov.stepan.utils.DateUtils

@Composable
fun AppItemCard(
    item: AppItem, 
    repository: AppRepository, 
    isPoster: Boolean = true,
    onClick: () -> Unit = {}
) {
    val isPast = if (isPoster) DateUtils.isPast(item.date) else false

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column {
            Box {
                NetworkImage(
                    url = item.imageUrl,
                    repository = repository,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
                
                if (isPoster && isPast) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "НЕ АКТУАЛЬНО",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isPoster && isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Скрываем технические теги для красоты
                val cleanDescription = item.description.replace(Regex("\\[.*?\\] "), "")

                Text(
                    text = cleanDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
