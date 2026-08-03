package com.android.codestudio.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.codestudio.app.data.Extension
import com.android.codestudio.app.data.Repository
import com.android.codestudio.app.ui.ExtensionsScreen
import com.android.codestudio.app.ui.RepositoriesScreen
import com.android.codestudio.app.ui.SettingsScreen
import kotlinx.coroutines.launch

object Routes {
    const val REPOS = "repositories"
    const val EXTENSIONS = "extensions"
    const val GIT = "git"
    const val SETTINGS = "settings"
}

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val drawerBgColor = surfaceColor.copy(alpha = 0.95f)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = drawerBgColor,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                // Drawer header
                Text(
                    text = "CodeStudio",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 24.sp,
                    color = onSurfaceColor,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Divider(color = onSurfaceColor.copy(alpha = 0.12f))

                // Navigation items
                NavigationDrawerItem(
                    label = { Text("Repositories") },
                    selected = currentDestination == Routes.REPOS,
                    onClick = {
                        navController.navigate(Routes.REPOS) { popUpTo(Routes.REPOS) { inclusive = true } }
                        coroutineScope.launch { drawerState.close() }
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
                        coroutineScope.launch { drawerState.close() }
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
                        coroutineScope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                        unselectedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Logout at bottom
                Divider(color = onSurfaceColor.copy(alpha = 0.12f))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
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
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open drawer")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = surfaceColor,
                        titleContentColor = onSurfaceColor,
                        navigationIconContentColor = onSurfaceColor
                    ),
                    modifier = Modifier.clip(
                        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
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
}
