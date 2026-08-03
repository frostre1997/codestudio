package com.android.codestudio.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ---------- Data Classes ----------
data class Repository(
    val name: String,
    val context: String,
    val branch: String = "main",
    val status: String = "No Changes",
    val lastActive: String = "a few seconds ago"
)

data class Extension(
    val name: String,
    val description: String,
    val installed: Boolean = false
)

// ---------- Routes ----------
object Routes {
    const val REPOS = "repositories"
    const val EXTENSIONS = "extensions"
    const val SETTINGS = "settings"
}

// ---------- Main Activity ----------
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

        // Read saved dark theme preference (default: false = light)
        val savedDarkTheme = prefs.getBoolean("dark_theme", false)
        var isDarkTheme by mutableStateOf(savedDarkTheme)

        setContent {
            // 🌈 Material You: Dynamic color scheme based on system wallpaper
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
                DashboardNavHost(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDark ->
                        isDarkTheme = isDark
                        prefs.edit().putBoolean("dark_theme", isDark).apply()
                    },
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

// ---------- Navigation Host ----------
@Composable
fun DashboardNavHost(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onOpenFolder: () -> Unit,
    onCloneRepo: () -> Unit,
    recentFolders: List<String>,
    onShowWelcomeChange: (Boolean) -> Unit,
    showWelcome: Boolean,
    onOpenRecent: (String) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    val context = LocalContext.current

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

    // Use dynamic colors for the drawer background
    val drawerBgColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = drawerBgColor,
                drawerShape = RoundedCornerShape(end = 16.dp)
            ) {
                Text(
                    text = "CodeStudio",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 24.sp,
                    color = onSurfaceColor,
                    fontWeight = FontWeight.Bold
                )
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                NavigationDrawerItem(
                    label = { Text("Repositories") },
                    selected = currentDestination == Routes.REPOS,
                    onClick = {
                        navController.navigate(Routes.REPOS) { popUpTo(Routes.REPOS) { inclusive = true } }
                        drawerState.close()
                    },
                    icon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                        unselectedContainerColor = Color.Transparent
                    )
                )
                NavigationDrawerItem(
                    label = { Text("Extensions") },
                    selected = currentDestination == Routes.EXTENSIONS,
                    onClick = {
                        navController.navigate(Routes.EXTENSIONS)
                        drawerState.close()
                    },
                    icon = { Icon(Icons.Filled.Extension, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                        unselectedContainerColor = Color.Transparent
                    )
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = currentDestination == Routes.SETTINGS,
                    onClick = {
                        navController.navigate(Routes.SETTINGS)
                        drawerState.close()
                    },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                        unselectedContainerColor = Color.Transparent
                    )
                )
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        drawerState.close()
                        onLogout()
                    },
                    icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    )
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("CodeStudio") },
                    navigationIcon = {
                        IconButton(onClick = { drawerState.open() }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open drawer")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.clip(
                        RoundedCornerShape(
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Routes.REPOS,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Routes.REPOS) {
                    RepositoriesTab(
                        repositories = repositories,
                        onOpenRecent = onOpenRecent,
                        onCloneRepo = onCloneRepo
                    )
                }
                composable(Routes.EXTENSIONS) {
                    ExtensionsTab(extensions = extensions)
                }
                composable(Routes.SETTINGS) {
                    SettingsTab(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle,
                        showWelcome = showWelcome,
                        onShowWelcomeChange = onShowWelcomeChange,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

// ---------- Repositories Tab ----------
@Composable
fun RepositoriesTab(
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

// ---------- Extensions Tab ----------
@Composable
fun ExtensionsTab(extensions: List<Extension>) {
    val context = LocalContext.current
    var extList by remember { mutableStateOf(extensions) }
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val successColor = MaterialTheme.colorScheme.primary // or use a custom green

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(extList) { ext ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Column {
                        Text(text = ext.name, color = onSurfaceColor, fontWeight = FontWeight.Bold)
                        Text(text = ext.description, color = onSurfaceColor.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                    if (ext.installed) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Uninstalling ${ext.name}", Toast.LENGTH_SHORT).show()
                                extList = extList.map {
                                    if (it.name == ext.name) it.copy(installed = false) else it
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = successColor
                            )
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = successColor)
                            Text("Installed", color = successColor, modifier = Modifier.padding(start = 4.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Installing ${ext.name}", Toast.LENGTH_SHORT).show()
                                extList = extList.map {
                                    if (it.name == ext.name) it.copy(installed = true) else it
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Install")
                        }
                    }
                }
            }
        }
    }
}

// ---------- Settings Tab ----------
@Composable
fun SettingsTab(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    showWelcome: Boolean,
    onShowWelcomeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            color = onSurfaceColor,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Theme", color = onSurfaceColor)
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeToggle,
                        colors = SwitchDefaults.colors(checkedTrackColor = primaryColor)
                    )
                }
                Divider(color = onSurfaceColor.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show welcome on startup", color = onSurfaceColor)
                    Switch(
                        checked = showWelcome,
                        onCheckedChange = onShowWelcomeChange,
                        colors = SwitchDefaults.colors(checkedTrackColor = primaryColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = errorColor),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50)
        ) {
            Text("Logout")
        }

        Text(
            text = "CodeStudio v0.10.0-alpha.1",
            color = onSurfaceColor.copy(alpha = 0.5f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
