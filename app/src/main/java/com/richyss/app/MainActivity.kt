package com.richyss.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.richyss.app.core.AppContainer
import com.richyss.app.core.LocalTranslations
import com.richyss.app.core.Translations
import com.richyss.app.core.resolveTranslations
import com.richyss.app.data.prefs.AppSettings
import com.richyss.app.ui.editor.EditorScreen
import com.richyss.app.ui.home.HomeScreen
import com.richyss.app.ui.patches.PatchesScreen
import com.richyss.app.ui.presets.PresetsScreen
import com.richyss.app.ui.profiles.ProfilesScreen
import com.richyss.app.ui.settings.SettingsScreen
import com.richyss.app.ui.theme.RichyssAccent
import com.richyss.app.ui.theme.RichyssTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RichyssApplication).container

        setContent {
            val settings by container.settings.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())
            val translations = resolveTranslations(settings.language)
            CompositionLocalProvider(LocalTranslations provides translations) {
                RichyssTheme(themeMode = settings.theme) {
                    RichyssRoot(container = container, settings = settings, translations = translations)
                }
            }
        }
    }
}

internal const val ROUTE_HOME = "home"
internal const val ROUTE_PROFILES = "profiles"
internal const val ROUTE_PRESETS = "presets"
internal const val ROUTE_PATCHES = "patches"
internal const val ROUTE_SETTINGS = "settings"
internal const val ROUTE_EDITOR = "editor?profileId={profileId}"

internal fun editorRoute(profileId: String? = null): String =
    if (profileId.isNullOrBlank()) "editor" else "editor?profileId=$profileId"

private data class Destination(
    val route: String,
    val icon: ImageVector,
    val label: (Translations) -> String
)

private val destinations = listOf(
    Destination(ROUTE_HOME, Icons.Filled.Home) { it.navHome },
    Destination(ROUTE_PROFILES, Icons.Filled.Person) { it.navProfiles },
    Destination(ROUTE_PRESETS, Icons.Filled.LocalOffer) { it.navPresets },
    Destination(ROUTE_PATCHES, Icons.Filled.Extension) { it.navPatches },
    Destination(ROUTE_SETTINGS, Icons.Filled.Settings) { it.navSettings }
)

@Composable
private fun RichyssRoot(
    container: AppContainer,
    settings: AppSettings,
    translations: Translations
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(container) {
        container.messages.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute?.startsWith("editor") != true

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                RichyssNavigationBar(
                    currentRoute = currentRoute,
                    translations = translations,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_HOME) {
                HomeScreen(
                    container = container,
                    onOpenProfiles = { navController.navigateToTab(ROUTE_PROFILES) },
                    onOpenPatches = { navController.navigateToTab(ROUTE_PATCHES) },
                    onCreateRpc = { navController.navigate(editorRoute()) },
                    onEditProfile = { id -> navController.navigate(editorRoute(id)) }
                )
            }
            composable(ROUTE_PROFILES) {
                ProfilesScreen(
                    container = container,
                    onEditProfile = { id -> navController.navigate(editorRoute(id)) },
                    onCreateProfile = { navController.navigate(editorRoute()) }
                )
            }
            composable(ROUTE_PRESETS) {
                PresetsScreen(
                    container = container,
                    onEditCopy = { id -> navController.navigate(editorRoute(id)) }
                )
            }
            composable(ROUTE_PATCHES) {
                PatchesScreen(container = container)
            }
            composable(ROUTE_SETTINGS) {
                SettingsScreen(container = container, settings = settings)
            }
            composable(
                route = ROUTE_EDITOR,
                arguments = listOf(
                    navArgument("profileId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                EditorScreen(
                    container = container,
                    profileId = entry.arguments?.getString("profileId"),
                    onFinished = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun RichyssNavigationBar(
    currentRoute: String?,
    translations: Translations,
    navController: NavHostController
) {
    NavigationBar(containerColor = com.richyss.app.ui.theme.RichyssSurface) {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { navController.navigateToTab(destination.route) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(destination.label(translations)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RichyssAccent,
                    selectedTextColor = RichyssAccent,
                    unselectedTextColor = com.richyss.app.ui.theme.RichyssTextSecondary,
                    indicatorColor = RichyssAccent.copy(alpha = 0.16f)
                )
            )
        }
    }
}
