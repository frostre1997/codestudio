package com.android.codestudio.app.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.codestudio.app.data.Extension
import com.android.codestudio.app.data.Repository
import com.android.codestudio.app.ui.ExtensionsScreen
import com.android.codestudio.app.ui.GitScreen
import com.android.codestudio.app.ui.RepositoriesScreen
import com.android.codestudio.app.ui.SettingsScreen

object Routes {
    const val REPOS = "repositories"
    const val EXTENSIONS = "extensions"
    const val GIT = "git"
    const val SETTINGS = "settings"
}

private data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardNavHost(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onOpenFolder: () -> Unit,
    onCloneRepo: () -> Unit,
    onOpenFile: () -> Unit,
    onNewFile: () -> Unit,
    onConnect: () -> Unit,
    repositories: List<Repository>,
    extensions: List<Extension>,
    onExtensionStateChanged: (List<Extension>) -> Unit,
    showWelcome: Boolean,
    onShowWelcomeChange: (Boolean) -> Unit,
    onOpenRecent: (String) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    val navItems = listOf(
        NavItem(Routes.REPOS, Icons.Filled.Folder, "Repositories"),
        NavItem(Routes.EXTENSIONS, Icons.Filled.Extension, "Extensions"),
        NavItem(Routes.GIT, Icons.Filled.Code, "Git"),
        NavItem(Routes.SETTINGS, Icons.Filled.Settings, "Settings")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CodeStudio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = onSurfaceColor
                ),
                modifier = Modifier.clip(
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = surfaceColor,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                modifier = Modifier
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            unselectedIconColor = onSurfaceColor.copy(alpha = 0.6f),
                            unselectedTextColor = onSurfaceColor.copy(alpha = 0.6f),
                            indicatorColor = primaryColor.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.REPOS,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.REPOS) {
                RepositoriesScreen(
                    repositories = repositories,
                    onOpenRecent = onOpenRecent,
                    onCloneRepo = onCloneRepo,
                    onOpenFile = onOpenFile,
                    onNewFile = onNewFile,
                    onConnect = onConnect,
                    onOpenFolder = onOpenFolder,
                    showWelcome = showWelcome,
                    onShowWelcomeChange = onShowWelcomeChange
                )
            }
            composable(Routes.EXTENSIONS) {
                ExtensionsScreen(
                    extensions = extensions,
                    onExtensionStateChanged = onExtensionStateChanged
                )
            }
            composable(Routes.GIT) {
                val context = LocalContext.current
                val prefs = context.getSharedPreferences("codestudio_prefs", android.content.Context.MODE_PRIVATE)
                val workspaceUri = prefs.getString("workspace_uri", null)
                val path = workspaceUri?.let { Uri.parse(it).path } ?: ""
                GitScreen(repoPath = path)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
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
