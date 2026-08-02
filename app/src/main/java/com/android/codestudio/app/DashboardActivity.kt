package com.android.codestudio.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class DashboardActivity : ComponentActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("codestudio_prefs", MODE_PRIVATE)

        setContent {
            MaterialTheme {
                WelcomeScreen(
                    onOpenFolder = { openFolderPicker() },
                    onCloneRepo = { showCloneDialog() },
                    onOpenFile = { /* future */ },
                    onNewFile = { /* future */ },
                    onConnect = { /* future */ },
                    recentFolders = getRecentFolders(),
                    onShowWelcomeChange = { show ->
                        prefs.edit().putBoolean("show_welcome", show).apply()
                    },
                    showWelcome = prefs.getBoolean("show_welcome", true)
                )
            }
        }
    }

    private fun openFolderPicker() {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri: Uri? ->
            uri?.let {
                // Persist permission
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                // Save to recent list
                addRecentFolder(it.toString())
                // Open editor with this folder
                val intent = Intent(this, EditorActivity::class.java).apply {
                    putExtra("workspace_uri", it.toString())
                }
                startActivity(intent)
                finish()
            }
        }
        launcher.launch(null)
    }

    private fun showCloneDialog() {
        // We'll show a simple dialog to input Git URL and destination folder
        setContent {
            CloneDialog(
                onDismiss = { /* close dialog */ },
                onClone = { url, dest ->
                    // Save destination to recent
                    // Start clone with JGit (later)
                    Toast.makeText(this, "Clone not yet implemented", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun getRecentFolders(): List<String> {
        val json = prefs.getString("recent_folders", "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    private fun addRecentFolder(uri: String) {
        val list = getRecentFolders().toMutableList()
        list.remove(uri) // avoid duplicates
        list.add(0, uri)
        if (list.size > 10) list.removeAt(list.size - 1) // keep max 10
        val json = Gson().toJson(list)
        prefs.edit().putString("recent_folders", json).apply()
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
    showWelcome: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Left panel (Start)
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

        // Right panel (Recent + Walkthroughs)
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
                    RecentItem(uri) {
                        // Open that folder
                        val intent = Intent(androidx.core.content.ContextWrapper(androidx.compose.ui.platform.LocalContext.current), EditorActivity::class.java).apply {
                            putExtra("workspace_uri", uri)
                        }
                        androidx.compose.ui.platform.LocalContext.current.startActivity(intent)
                        (androidx.compose.ui.platform.LocalContext.current as? ComponentActivity)?.finish()
                    }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Checkbox(
            checked = showWelcome,
            onCheckedChange = onShowWelcomeChange
        )
        Text(
            text = "Show welcome page on startup",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun WelcomeActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black
        ),
        elevation = null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text, fontWeight = FontWeight.Normal)
    }
}

@Composable
fun RecentItem(uri: String, onClick: () -> Unit) {
    // Extract folder name from URI
    val name = try {
        val doc = DocumentsContract.Document.COLUMN_DISPLAY_NAME
        val cursor = androidx.compose.ui.platform.LocalContext.current.contentResolver.query(
            uri.toUri(), null, null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(doc)
                if (nameIndex >= 0) it.getString(nameIndex) else uri
            } else uri
        } ?: uri
    } catch (e: Exception) { uri }
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = name ?: uri, color = Color.Black)
    }
}

@Composable
fun WalkthroughItem(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold)
        Text(text = description, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun CloneDialog(onDismiss: () -> Unit, onClone: (String, String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Clone Git Repository", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                // Input fields: URL and destination (we'll just use a default folder for now)
                // For simplicity, we'll just show a toast.
                Text("Coming soon...")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}
