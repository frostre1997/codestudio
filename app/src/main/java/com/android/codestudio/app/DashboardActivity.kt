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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
                    onCloneRepo = { /* later */ },
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

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Activity Bar
        ActivityBar(
            selectedIndex = selectedActivity,
            onItemSelected = { selectedActivity = it }
        )

        // Main content area
        Column(modifier = Modifier.weight(1f)) {
            TopBar()
            Box(modifier = Modifier.weight(1f)) {
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
    // Use only icons that exist in androidx.compose.material.icons.filled.*
    val icons = listOf(
        Folder to "Explorer",
        Search to "Search",
        Code to "Source Control",
        PlayArrow to "Run",
        Build to "Extensions"  // Build exists; you can also use Settings, but we already have that below
    )

    Column(
        modifier = Modifier
            .width(56.dp)
            .fillMaxHeight()
            .background(Color(0xFF2D2D30))
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icons.forEachIndexed { index, (icon, _) ->
            IconButton(
                onClick = { onItemSelected(index) },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (selectedIndex == index) Color(0xFF37373D) else Color.Transparent
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selectedIndex == index) Color.White else Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /* settings */ }) {
            Icon(Settings, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFF3C3C3C)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "CodeStudio",
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp),
                fontWeight = FontWeight.Bold
            )
            // Menu items (simplified)
            Text(text = "File", color = Color.LightGray, modifier = Modifier.padding(start = 16.dp))
            Text(text = "Edit", color = Color.LightGray, modifier = Modifier.padding(start = 16.dp))
            Text(text = "View", color = Color.LightGray, modifier = Modifier.padding(start = 16.dp))
            Text(text = "Go", color = Color.LightGray, modifier = Modifier.padding(start = 16.dp))
            Text(text = "Run", color = Color.LightGray, modifier = Modifier.padding(start = 16.dp))
            Text(text = "Terminal", color = Color.LightGray, modifier = Modifier.padding(start = 16.dp))
            Text(text = "Help", color = Color.LightGray, modifier = Modifier.padding(start = 16.dp))
        }
        // Window controls using text to avoid missing icons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("—", color = Color.White, fontSize = 18.sp, modifier = Modifier.clickable { /* minimize */ })
            Text("☐", color = Color.White, fontSize = 18.sp, modifier = Modifier.clickable { /* maximize */ })
            Text("✕", color = Color.White, fontSize = 18.sp, modifier = Modifier.clickable { /* close */ })
        }
    }
}

@Composable
fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Color(0xFF007ACC))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("master", color = Color.White, fontSize = 12.sp)
            Text("0 ✕", color = Color.White, fontSize = 12.sp)
            Text("UTF-8", color = Color.White, fontSize = 12.sp)
            Text("LF", color = Color.White, fontSize = 12.sp)
            Text("Kotlin", color = Color.White, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(24.dp)
    ) {
        Row(modifier = Modifier.weight(1f)) {
            // Left panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Start",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF007ACC)
                )
                Spacer(modifier = Modifier.height(8.dp))
                WelcomeActionButton("New File...", onNewFile)
                WelcomeActionButton("Open File...", onOpenFile)
                WelcomeActionButton("Open Folder...", onOpenFolder)
                WelcomeActionButton("Clone Git Repository...", onCloneRepo)
                WelcomeActionButton("Connect to...", onConnect)
            }

            // Right panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Recent",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF007ACC)
                )
                if (recentFolders.isEmpty()) {
                    Text(
                        text = "You have no recent folders, open a folder to start.",
                        color = Color.Gray
                    )
                } else {
                    recentFolders.forEach { uri ->
                        RecentItem(uri, onClick = { onOpenRecent(uri) })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Walkthroughs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF007ACC)
                )
                WalkthroughItem("Get Started with CodeStudio", "Customize your editor, learn the basics, and start coding")
                WalkthroughItem("Learn the Fundamentals", "Core concepts of Android development")
                WalkthroughItem("GitHub Copilot [Updated]", "AI‑assisted coding")
                WalkthroughItem("Get Started with Python Development [Updated]", "Python support in CodeStudio")
                WalkthroughItem("More...", "Additional guides and resources")
            }
        }

        // Bottom checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = showWelcome,
                onCheckedChange = onShowWelcomeChange
            )
            Text(
                text = "Show welcome page on startup",
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun WelcomeActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        elevation = null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text, fontWeight = FontWeight.Normal)
    }
}

@Composable
fun RecentItem(uri: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val name = remember(uri) {
        try {
            val doc = DocumentsContract.Document.COLUMN_DISPLAY_NAME
            val cursor = context.contentResolver.query(uri.toUri(), null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(doc)
                    if (nameIndex >= 0) it.getString(nameIndex) else uri
                } else uri
            } ?: uri
        } catch (e: Exception) { uri }
    }
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = name ?: uri, color = Color.White)
    }
}

@Composable
fun WalkthroughItem(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = description, fontSize = 14.sp, color = Color.Gray)
    }
}
