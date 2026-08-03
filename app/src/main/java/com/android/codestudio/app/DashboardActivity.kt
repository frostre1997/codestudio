package com.android.codestudio.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.android.codestudio.app.data.Extension
import com.android.codestudio.app.data.Repository
import com.android.codestudio.app.ui.ExtensionsTab
import com.android.codestudio.app.ui.RepositoriesTab
import com.android.codestudio.app.ui.SettingsTab
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
            // Custom theme with rounded corners
            MaterialTheme(
                shapes = Shapes(
                    extraSmall = RoundedCornerShape(4.dp),
                    small = RoundedCornerShape(8.dp),
                    medium = RoundedCornerShape(12.dp),
                    large = RoundedCornerShape(16.dp),
                    extraLarge = RoundedCornerShape(24.dp)
                )
            ) {
                DashboardScreen(
                    onOpenFolder = { folderPickerLauncher.launch(null) },
                    onCloneRepo = { Toast.makeText(this, "Clone dialog coming soon", Toast.LENGTH_SHORT).show() },
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
                    },
                    onLogout = {
                        prefs.edit().clear().apply()
                        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    },
                    onThemeToggle = { isDark ->
                        prefs.edit().putBoolean("dark_theme", isDark).apply()
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
fun DashboardScreen(
    onOpenFolder: () -> Unit,
    onCloneRepo: () -> Unit,
    recentFolders: List<String>,
    onShowWelcomeChange: (Boolean) -> Unit,
    showWelcome: Boolean,
    onOpenRecent: (String) -> Unit,
    onLogout: () -> Unit,
    onThemeToggle: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    // Sample data
    val repositories = listOf(
        Repository("codestudio", "github.com/frostre1997/codestudio", "dev", "No Changes", "a few seconds ago"),
        Repository("python-flask-example", "github.com/gitpod-io/python-flask-example", "main", "No Changes", "a few seconds ago"),
        Repository("sveltejs-template", "github.com/gitpod-io/sveltejs-template", "master", "No Changes", "2 minutes ago")
    )

    val extensions = listOf(
        Extension("Kotlin", "Kotlin language support", installed = true),
        Extension("Python", "Python language support", installed = false),
        Extension("GitLens", "Supercharge the Git capabilities", installed = false),
        Extension("Prettier", "Code formatter", installed = false)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CodeStudio") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                // Apply rounded corners to the top of the app bar (if desired)
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            )
        },
        bottomBar = {
            // Bottom navigation with rounded top corners
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    listOf("Repositories", "Extensions", "Settings").forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                when (index) {
                                    0 -> Icon(Icons.Filled.Folder, contentDescription = null)
                                    1 -> Icon(Icons.Filled.Extension, contentDescription = null)
                                    2 -> Icon(Icons.Filled.Settings, contentDescription = null)
                                }
                            },
                            label = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> RepositoriesTab(
                    repositories = repositories,
                    onOpenRecent = onOpenRecent,
                    onCloneRepo = onCloneRepo
                )
                1 -> ExtensionsTab(extensions = extensions)
                2 -> SettingsTab(
                    showWelcome = showWelcome,
                    onShowWelcomeChange = onShowWelcomeChange,
                    onThemeToggle = onThemeToggle,
                    onLogout = onLogout
                )
            }
        }
    }
}
