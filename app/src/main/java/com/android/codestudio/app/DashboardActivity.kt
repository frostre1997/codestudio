package com.android.codestudio.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.android.codestudio.app.data.Extension
import com.android.codestudio.app.data.PreferencesManager
import com.android.codestudio.app.data.Repository
import com.android.codestudio.app.navigation.DashboardNavHost
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DashboardActivity : ComponentActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private var isDarkTheme by mutableStateOf(false)

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

        isDarkTheme = PreferencesManager.getDarkTheme(prefs)

        setContent {
            val colorScheme = if (isDarkTheme) {
                dynamicDarkColorScheme(this)
            } else {
                dynamicLightColorScheme(this)
            }

            MaterialTheme(
                colorScheme = colorScheme,
                shapes = Shapes(
                    extraSmall = RoundedCornerShape(4.dp),
                    small = RoundedCornerShape(8.dp),
                    medium = RoundedCornerShape(12.dp),
                    large = RoundedCornerShape(16.dp),
                    extraLarge = RoundedCornerShape(24.dp)
                )
            ) {
                val repositories = getRecentRepositories()
                var extensions by remember {
                    mutableStateOf(PreferencesManager.getExtensions(prefs))
                }

                DashboardNavHost(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDark ->
                        isDarkTheme = isDark
                        PreferencesManager.setDarkTheme(prefs, isDark)
                    },
                    onOpenFolder = { folderPickerLauncher.launch(null) },
                    onCloneRepo = { Toast.makeText(this, "Clone dialog coming soon", Toast.LENGTH_SHORT).show() },
                    onOpenFile = { Toast.makeText(this, "Open File (coming soon)", Toast.LENGTH_SHORT).show() },
                    onNewFile = { Toast.makeText(this, "New File (coming soon)", Toast.LENGTH_SHORT).show() },
                    onConnect = { Toast.makeText(this, "Connect (coming soon)", Toast.LENGTH_SHORT).show() },
                    repositories = repositories,
                    extensions = extensions,
                    onExtensionStateChanged = { updated ->
                        extensions = updated
                        PreferencesManager.saveExtensions(prefs, updated)
                    },
                    showWelcome = PreferencesManager.getShowWelcome(prefs),
                    onShowWelcomeChange = { show ->
                        PreferencesManager.setShowWelcome(prefs, show)
                    },
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
                        finish()
                    }
                )
            }
        }
    }

    private fun getRecentRepositories(): List<Repository> {
        return listOf(
            Repository("codestudio", "github.com/frostre1997/codestudio", "dev", "No Changes", "a few seconds ago"),
            Repository("python-flask-example", "github.com/gitpod-io/python-flask-example", "main", "No Changes", "a few seconds ago"),
            Repository("sveltejs-template", "github.com/gitpod-io/sveltejs-template", "master", "No Changes", "2 minutes ago")
        )
    }

    private fun addRecentFolder(uri: String) {
        val json = prefs.getString("recent_folders", "[]")
        val type = object : TypeToken<List<String>>() {}.type
        val list = Gson().fromJson<List<String>>(json, type) ?: emptyList()
        val updated = list.toMutableList().apply {
            remove(uri)
            add(0, uri)
            if (size > 10) removeAt(size - 1)
        }
        prefs.edit().putString("recent_folders", Gson().toJson(updated)).apply()
    }
}
