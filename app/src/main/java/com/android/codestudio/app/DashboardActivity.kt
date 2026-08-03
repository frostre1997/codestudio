package com.android.codestudio.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Repository(
    val name: String,
    val context: String,
    val branch: String = "main",
    val status: String = "No Changes",
    val lastActive: String = "a few seconds ago"
)

class DashboardActivity : ComponentActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            addRecentFolder(it.toString())
            val intent = Intent(this, EditorActivity::class.java).apply {
                putExtra("workspace_uri", it.toString())
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("codestudio_prefs", MODE_PRIVATE)

        setContent {
            MaterialTheme {
                CodeStudioLayout(
                    onOpenFolder = { folderPickerLauncher.launch(null) },
                    onCloneRepo = { Toast.makeText(this, "Clone dialog coming soon", Toast.LENGTH_SHORT).show() },
                    onOpenFile = { Toast.makeText(this, "Open File (coming soon)", Toast.LENGTH_SHORT).show() },
                    onNewFile = { Toast.makeText(this, "New File (coming soon)", Toast.LENGTH_SHORT).show() },
                    onConnect = { Toast.makeText(this, "Connect (coming soon)", Toast.LENGTH_SHORT).show() },
                    recentFolders = getRecentFolders(),
                    onShowWelcomeChange = { show ->
                        prefs.edit().putBoolean("show_welcome", show).apply()
                    },
                    showWelcome = prefs.getBoolean("show_welcome", true),
                    onOpenRecent = { uri ->
                        val intent = Intent(this, EditorActivity::class.java).apply {
                            putExtra("workspace_uri", uri)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    private fun getRecentFolders(): List<String> {
        val json = prefs.getString("recent_folders", "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    private fun addRecentFolder(uri: String) {
        val list = getRecentFolders().toMutableList()
        list.remove(uri)
        list.add(0, uri)
        if (list.size > 10) list.removeAt(list.size - 1)
        val json = Gson().toJson(list)
        prefs.edit().putString("recent_folders", json).apply()
    }
}

@Composable
fun CodeStudioLayout(
    onOpenFolder: () -> Unit,
    onCloneRepo: () -> Unit,
    onOpenFile: () -> Unit,
    onNewFile: () -> Unit,
    onConnect: () -> Unit,
    recentFolders: List<String>,
    onShowWelcomeChange: (Boolean) -> Unit,
    showWelcome: Boolean,
    onOpenRecent: (String) -> Unit
) {
    var selectedActivity by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        ActivityBar(
            selectedIndex = selectedActivity,
            onItemSelected = { selectedActivity = it }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            TopBar()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                WelcomeScreen(
                    onOpenFolder = onOpenFolder,
                    onCloneRepo = onCloneRepo,
                    onOpenFile = onOpenFile,
                    onNewFile = onNewFile,
                    onConnect = onConnect,
                    recentFolders = recentFolders,
                    onShowWelcomeChange = onShowWelcomeChange,
                    showWelcome = showWelcome,
                    onOpenRecent = onOpenRecent
                )
            }
            StatusBar()
        }
    }
}

@Composable
fun ActivityBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val icons = listOf(
        Icons.Filled.Folder to "Explorer",
        Icons.Filled.Search to "Search",
        Icons.Filled.Code to "Source Control",
        Icons.Filled.PlayArrow to "Run",
        Icons.Filled.Build to "Extensions"
    )

    Column(
        modifier = Modifier
            .width(50.dp)
            .fillMaxHeight()
            .background(Color(0xFF2D2D30))
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icons.forEachIndexed { index, (icon, _) ->
            IconButton(
                onClick = { onItemSelected(index) },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selectedIndex == index) Color(0xFF37373D) else Color.Transparent
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selectedIndex == index) Color.White else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /* settings */ }) {
            Icon(Icons.Filled.Settings, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)
            .background(Color(0xFF3C3C3C)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = "CodeStudio",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text("File", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp))
            Text("Edit", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp))
            Text("View", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp))
            Text("Go", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp))
            Text("Run", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp))
            Text("Terminal", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp))
            Text("Help", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(end = 12.dp)) {
            Text("—", color = Color.White, fontSize = 16.sp, modifier = Modifier.clickable { /* minimize */ })
            Text("☐", color = Color.White, fontSize = 16.sp, modifier = Modifier.clickable { /* maximize */ })
            Text("✕", color = Color.White, fontSize = 16.sp, modifier = Modifier.clickable { /* close */ })
        }
    }
}

@Composable
fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(Color(0xFF007ACC))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("master", color = Color.White, fontSize = 12.sp)
            Text("0 ✕", color = Color.White, fontSize = 12.sp)
            Text("UTF-8", color = Color.White, fontSize = 12.sp)
            Text("LF", color = Color.White, fontSize = 12.sp)
            Text("Kotlin", color = Color.White, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Spaces: 4", color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
fun WelcomeScreen(
    onOpenFolder: () -> Unit,
    onCloneRepo: () -> Unit,
    onOpenFile: () -> Unit,
    onNewFile: () -> Unit,
    onConnect: () -> Unit,
    recentFolders: List<String>,
    onShowWelcomeChange: (Boolean) -> Unit,
    showWelcome: Boolean,
    onOpenRecent: (String) -> Unit
) {
    val context = LocalContext.current

    val repositories = remember {
        listOf(
            Repository("codestudio", "github.com/frostre1997/codestudio", "dev", "No Changes", "a few seconds ago"),
            Repository("python-flask-example", "github.com/gitpod-io/python-flask-example", "main", "No Changes", "a few seconds ago"),
            Repository("sveltejs-template", "github.com/gitpod-io/sveltejs-template", "master", "No Changes", "a few seconds ago"),
            Repository("spring-petclinic", "github.com/gitpod-io/spring-petclinic", "master", "No Changes", "2 minutes ago")
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Active") }
    var limit by remember { mutableStateOf(50) }

    val filteredRepos = repositories.filter { repo ->
        repo.name.contains(searchQuery, ignoreCase = true) ||
                repo.context.contains(searchQuery, ignoreCase = true)
    }.take(limit)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(24.dp)
    ) {
        Text(
            text = "CodeStudio",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Your IDE on Android",
            fontSize = 16.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Repositories", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF2D2D30)),
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            var filterExpanded by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { filterExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2D30), contentColor = Color.White),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Filter: $selectedFilter")
                }
                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false }
                ) {
                    listOf("Active", "All", "Stopped").forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(filter) },
                            onClick = {
                                selectedFilter = filter
                                filterExpanded = false
                            }
                        )
                    }
                }
            }

            var limitExpanded by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { limitExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2D30), contentColor = Color.White),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Limit: $limit")
                }
                DropdownMenu(
                    expanded = limitExpanded,
                    onDismissRequest = { limitExpanded = false }
                ) {
                    listOf(10, 20, 50, 100).forEach { lim ->
                        DropdownMenuItem(
                            text = { Text(lim.toString()) },
                            onClick = {
                                limit = lim
                                limitExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { onCloneRepo() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC), contentColor = Color.White),
                modifier = Modifier.height(48.dp)
            ) {
                Text("New Repository")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Repository List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredRepos) { repo ->
                RepositoryItem(
                    repo = repo,
                    onStop = { Toast.makeText(context, "Stop ${repo.name}", Toast.LENGTH_SHORT).show() },
                    onDownload = { Toast.makeText(context, "Download ${repo.name}", Toast.LENGTH_SHORT).show() },
                    onShare = { Toast.makeText(context, "Share ${repo.name}", Toast.LENGTH_SHORT).show() },
                    onPin = { Toast.makeText(context, "Pin ${repo.name}", Toast.LENGTH_SHORT).show() },
                    onDelete = { Toast.makeText(context, "Delete ${repo.name}", Toast.LENGTH_SHORT).show() },
                    onClick = { onOpenRecent(repo.context) }
                )
            }
        }

        // Bottom: Filter chips + checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("Docs", selected = false) { /* filter by Docs */ }
                FilterChip("Community", selected = false) { /* filter by Community */ }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = showWelcome,
                    onCheckedChange = onShowWelcomeChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF007ACC),
                        uncheckedColor = Color.Gray
                    )
                )
                Text(
                    text = "Show welcome page on startup",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun RepositoryItem(
    repo: Repository,
    onStop: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D30)),
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
                Text(text = repo.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = repo.context, color = Color.LightGray, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Branch: ${repo.branch}", color = Color.Gray, fontSize = 13.sp)
                    Text(text = repo.status, color = Color(0xFF4CAF50), fontSize = 13.sp)
                }
            }
            Text(
                text = repo.lastActive,
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onStop) {
                    Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = Color.White)
                }
                IconButton(onClick = onDownload) {
                    Icon(Icons.Filled.Download, contentDescription = "Download", tint = Color.White)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                }
                IconButton(onClick = onPin) {
                    Icon(Icons.Filled.PushPin, contentDescription = "Pin", tint = Color.White)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, color = if (selected) Color.White else Color.Gray) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color(0xFF007ACC) else Color(0xFF2D2D30)
        )
    )
}
