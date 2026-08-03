package com.android.codestudio.app.ui

import android.provider.DocumentsContract
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.android.codestudio.app.data.Repository

@Composable
fun RepositoriesScreen(
    repositories: List<Repository>,
    onOpenRecent: (String) -> Unit,
    onCloneRepo: () -> Unit,
    onOpenFile: () -> Unit,
    onNewFile: () -> Unit,
    onConnect: () -> Unit,
    onOpenFolder: () -> Unit,
    showWelcome: Boolean,
    onShowWelcomeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Text(
            text = "Visual Studio Code",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Editing evolved",
            fontSize = 18.sp,
            color = onSurfaceColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            // Left panel: Start
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Start",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                WelcomeActionButton("New File...", onNewFile)
                WelcomeActionButton("Open File...", onOpenFile)
                WelcomeActionButton("Open Folder...", onOpenFolder)
                WelcomeActionButton("Clone Git...", onCloneRepo)
                WelcomeActionButton("Connect to...", onConnect)
            }

            // Right panel: Recent + Walkthroughs
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Recent
                Text(
                    text = "Recent",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                if (repositories.isEmpty()) {
                    Text(
                        text = "You have no recent folders, open a folder to start.",
                        color = onSurfaceColor.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                } else {
                    repositories.forEach { repo ->
                        RecentItem(repo, onClick = { onOpenRecent(repo.context) })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Walkthroughs
                Text(
                    text = "Walkthroughs",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                WalkthroughItem(
                    "Get Started with VS Code",
                    "Customize your editor, learn the basics, and start coding"
                )
                WalkthroughItem(
                    "Learn the Fundamentals",
                    "Core concepts of Android development"
                )
                WalkthroughItem(
                    "GitHub Copilot [Updated]",
                    "AI‑assisted coding"
                )
                WalkthroughItem(
                    "Get Started with Python Development [Updated]",
                    "Python support in CodeStudio"
                )
                WalkthroughItem(
                    "More...",
                    "Additional guides and resources"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showWelcome,
                onCheckedChange = onShowWelcomeChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = primaryColor,
                    uncheckedColor = onSurfaceColor.copy(alpha = 0.4f)
                )
            )
            Text(
                text = "Show welcome page on startup",
                color = onSurfaceColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun WelcomeActionButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 16.sp,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    )
}

@Composable
fun RecentItem(repo: Repository, onClick: () -> Unit) {
    val context = LocalContext.current
    val name = remember(repo) {
        try {
            val uri = repo.context.toUri()
            val doc = DocumentsContract.Document.COLUMN_DISPLAY_NAME
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(doc)
                    if (nameIndex >= 0) it.getString(nameIndex) else repo.name
                } else repo.name
            } ?: repo.name
        } catch (e: Exception) { repo.name }
    }
    Text(
        text = name ?: repo.name,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 16.sp,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    )
}

@Composable
fun WalkthroughItem(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
    }
}
