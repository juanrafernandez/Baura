package com.jrlabs.baura.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jrlabs.baura.R
import com.jrlabs.baura.ui.screens.explore.ExploreScreen
import com.jrlabs.baura.ui.screens.library.LibraryScreen
import com.jrlabs.baura.ui.screens.settings.SettingsScreen
import com.jrlabs.baura.ui.screens.test.TestScreen
import com.jrlabs.baura.ui.theme.AppColors

/**
 * Tab definitions matching iOS MainTabView exactly
 * Order: Home (Inicio), Explore (Explorar), Test, Library (Mi Colección), Settings (Ajustes)
 */
sealed class MainTab(
    val route: String,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    // Tab 0: Inicio - house.fill
    data object Home : MainTab(
        route = "home",
        titleRes = R.string.tab_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    // Tab 1: Explorar - magnifyingglass
    data object Explore : MainTab(
        route = "explore",
        titleRes = R.string.tab_explore,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )
    // Tab 2: Test - drop.fill (using WaterDrop)
    data object Test : MainTab(
        route = "test",
        titleRes = R.string.tab_test,
        selectedIcon = Icons.Filled.WaterDrop,
        unselectedIcon = Icons.Outlined.WaterDrop
    )
    // Tab 3: Mi Colección - books.vertical.fill (using MenuBook)
    data object Library : MainTab(
        route = "library",
        titleRes = R.string.tab_library,
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    )
    // Tab 4: Ajustes - gearshape.fill
    data object Settings : MainTab(
        route = "settings",
        titleRes = R.string.tab_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

// Order matches iOS: Home, Explore, Test, Library, Settings
val tabs = listOf(
    MainTab.Home,
    MainTab.Explore,
    MainTab.Test,
    MainTab.Library,
    MainTab.Settings
)

/**
 * MainTabScreen - Main screen with bottom navigation
 * Equivalent to MainTabView.swift
 */
@Composable
fun MainTabScreen(
    onNavigateToPerfumeDetail: (String) -> Unit,
    onNavigateToGiftRecommendation: () -> Unit,
    onNavigateToProfileManagement: () -> Unit = {},
    onNavigateToGiftProfileManagement: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToCacheStats: () -> Unit = {},
    onNavigateToEvaluation: (String, Boolean) -> Unit = { _, _ -> },
    onNavigateToTriedList: () -> Unit = {},
    onNavigateToWishlist: () -> Unit = {},
    onLogout: () -> Unit,
    onShowOnboarding: () -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            MainBottomNavigation(navController = navController)
        },
        contentWindowInsets = WindowInsets(0) // Allow edge-to-edge, screens handle their own insets
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainTab.Home.route) {
                HomeScreen(
                    onNavigateToPerfumeDetail = onNavigateToPerfumeDetail,
                    onNavigateToGiftRecommendation = onNavigateToGiftRecommendation
                )
            }
            composable(MainTab.Test.route) {
                TestScreen(
                    onNavigateToPerfumeDetail = onNavigateToPerfumeDetail,
                    onNavigateToProfileManagement = onNavigateToProfileManagement,
                    onNavigateToGiftProfileManagement = onNavigateToGiftProfileManagement
                )
            }
            composable(MainTab.Library.route) {
                LibraryScreen(
                    onNavigateToPerfumeDetail = onNavigateToPerfumeDetail,
                    onNavigateToEvaluation = onNavigateToEvaluation,
                    onNavigateToTriedList = onNavigateToTriedList,
                    onNavigateToWishlist = onNavigateToWishlist
                )
            }
            composable(MainTab.Explore.route) {
                ExploreScreen(
                    onNavigateToPerfumeDetail = onNavigateToPerfumeDetail
                )
            }
            composable(MainTab.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout,
                    onShowOnboarding = onShowOnboarding,
                    onEditProfile = onNavigateToEditProfile,
                    onCacheStats = onNavigateToCacheStats
                )
            }
        }
    }
}

@Composable
private fun MainBottomNavigation(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = AppColors.backgroundPrimary,
        contentColor = AppColors.textPrimary
    ) {
        tabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = stringResource(tab.titleRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.titleRes),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.textPrimary,
                    selectedTextColor = AppColors.textPrimary,
                    unselectedIconColor = AppColors.iconSecondary,
                    unselectedTextColor = AppColors.textSecondary,
                    indicatorColor = AppColors.backgroundSecondary
                )
            )
        }
    }
}
