package com.android.codestudio.app.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.codestudio.app.data.Repository

@Composable
fun RepositoriesScreen(
    repositories: List<Repository>,
    onOpenRecent: (String) -> Unit,
    onCloneRepo: () -> Unit
) {
    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(repositories) { repo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenRecent(repo.context) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = repo.name, color = onSurfaceColor, fontWeight = FontWeight.Bold)
                            Text(text = repo.context, color = onSurfaceColor.copy(alpha = 0.7f), fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Branch: ${repo.branch}", color = onSurfaceColor.copy(alpha = 0.5f), fontSize = 13.sp)
                                Text(text = repo.status, color = primaryColor, fontSize = 13.sp)
                            }
                        }
                        Text(
                            text = repo.lastActive,
                            color = onSurfaceColor.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {
                                Toast.makeText(context, "Share ${repo.name}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = onSurfaceColor)
                            }
                            IconButton(onClick = {
                                Toast.makeText(context, "Delete ${repo.name}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = onSurfaceColor)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCloneRepo,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = primaryColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Clone Repository")
        }
    }
}
