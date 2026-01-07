package com.jrlabs.baura.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jrlabs.baura.app.AppLaunchCoordinator
import com.jrlabs.baura.app.LaunchPhase
import com.jrlabs.baura.ui.screens.auth.AuthViewModel
import com.jrlabs.baura.ui.screens.auth.ForgotPasswordScreen
import com.jrlabs.baura.ui.screens.auth.LoginScreen
import com.jrlabs.baura.ui.screens.auth.RegisterScreen
import com.jrlabs.baura.ui.screens.home.MainTabScreen
import com.jrlabs.baura.ui.screens.library.AddTriedPerfumeScreen
import com.jrlabs.baura.ui.screens.library.AllTriedPerfumesScreen
import com.jrlabs.baura.ui.screens.library.AllWishlistScreen
import com.jrlabs.baura.ui.screens.library.evaluation.EvaluationOnboardingScreen
import com.jrlabs.baura.ui.screens.onboarding.OnboardingScreen
import com.jrlabs.baura.ui.screens.perfume.PerfumeDetailScreen
import com.jrlabs.baura.ui.screens.perfume.PerfumeDetailUiState
import com.jrlabs.baura.ui.screens.perfume.PerfumeDetailViewModel
import com.jrlabs.baura.ui.screens.test.GiftProfileManagementScreen
import com.jrlabs.baura.ui.screens.test.ProfileManagementScreen
import com.jrlabs.baura.ui.screens.settings.CacheStatsScreen
import com.jrlabs.baura.ui.screens.settings.EditProfileScreen
import com.jrlabs.baura.ui.screens.splash.AnimatedSplashScreen
import com.jrlabs.baura.data.local.MetadataIndexManager
import com.jrlabs.baura.ui.theme.AppColors
import com.jrlabs.baura.utils.AppLogger
import kotlinx.coroutines.launch

/**
 * Navigation Routes - equivalent to routing in iOS ContentView
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    data object Main : Screen("main")
    data object PerfumeDetail : Screen("perfume_detail/{perfumeId}") {
        fun createRoute(perfumeId: String) = "perfume_detail/$perfumeId"
    }
    data object GiftRecommendation : Screen("gift_recommendation")
    data object ProfileManagement : Screen("profile_management")
    data object GiftProfileManagement : Screen("gift_profile_management")
    data object EditProfile : Screen("edit_profile")
    data object CacheStats : Screen("cache_stats")
    data object PerfumeEvaluation : Screen("perfume_evaluation/{perfumeId}?isEditing={isEditing}") {
        fun createRoute(perfumeId: String, isEditing: Boolean = false) =
            "perfume_evaluation/$perfumeId?isEditing=$isEditing"
    }
    data object AllTriedPerfumes : Screen("all_tried_perfumes")
    data object AllWishlist : Screen("all_wishlist")
    data object AddTriedPerfume : Screen("add_tried_perfume")
}

/**
 * Main Navigation Host
 * Handles routing based on launch phase and authentication state
 * Equivalent to ContentView's launch-based routing in iOS
 */
@Composable
fun BauraNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    launchCoordinator: AppLaunchCoordinator,
    metadataIndexManager: MetadataIndexManager,
    pendingDeepLink: DeepLinkHandler.DeepLinkDestination? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    val currentPhase by launchCoordinator.currentPhase.collectAsState()
    val isDataLoaded by launchCoordinator.isDataLoaded.collectAsState()
    val scope = rememberCoroutineScope()

    // Initialize launch coordinator and pre-load essential data during splash
    LaunchedEffect(Unit) {
        AppLogger.debug("BauraNavHost", ">>> LaunchedEffect START at ${System.currentTimeMillis()}")
        launchCoordinator.initialize()

        // PRE-LOAD MetadataIndex during splash (like iOS)
        // This ensures HomeViewModel has instant access to perfume metadata
        val startTime = System.currentTimeMillis()
        try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                metadataIndexManager.getAllMetadata()
            }
            val elapsed = System.currentTimeMillis() - startTime
            AppLogger.debug("BauraNavHost", ">>> MetadataIndex PRE-LOADED in ${elapsed}ms")
        } catch (e: Exception) {
            AppLogger.error("BauraNavHost", ">>> MetadataIndex pre-load failed", e)
        }

        // Mark data as loaded AFTER metadata is in memory
        launchCoordinator.dataLoadDidComplete()
        AppLogger.debug("BauraNavHost", ">>> LaunchedEffect DONE")
    }

    // Show content based on launch phase
    AnimatedContent(
        targetState = currentPhase,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "launchPhaseTransition"
    ) { phase ->
        when (phase) {
            is LaunchPhase.Splash -> {
                AnimatedSplashScreen(
                    isDataLoaded = isDataLoaded,
                    onAnimationComplete = {
                        scope.launch {
                            launchCoordinator.splashAnimationDidComplete()
                        }
                    }
                )
            }

            is LaunchPhase.Onboarding -> {
                OnboardingScreen(
                    onComplete = {
                        scope.launch {
                            launchCoordinator.onboardingDidComplete()
                        }
                    }
                )
            }

            is LaunchPhase.Loading -> {
                // Loading screen while data loads
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.brandAccent)
                }
            }

            is LaunchPhase.Ready -> {
                // Main app navigation
                MainAppNavigation(
                    navController = navController,
                    authViewModel = authViewModel,
                    authState = authState,
                    launchCoordinator = launchCoordinator,
                    pendingDeepLink = pendingDeepLink,
                    onDeepLinkHandled = onDeepLinkHandled
                )
            }
        }
    }
}

/**
 * Main App Navigation - handles auth-based routing after launch
 */
@Composable
private fun MainAppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    authState: com.jrlabs.baura.ui.screens.auth.AuthState,
    launchCoordinator: AppLaunchCoordinator,
    pendingDeepLink: DeepLinkHandler.DeepLinkDestination? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    // Handle pending deep link after authentication
    LaunchedEffect(pendingDeepLink, authState.isAuthenticated) {
        if (pendingDeepLink != null && authState.isAuthenticated && !authState.isLoading) {
            when (pendingDeepLink) {
                is DeepLinkHandler.DeepLinkDestination.PerfumeDetail -> {
                    navController.navigate(Screen.PerfumeDetail.createRoute(pendingDeepLink.perfumeId))
                }
                is DeepLinkHandler.DeepLinkDestination.Wishlist -> {
                    navController.navigate(Screen.AllWishlist.route)
                }
                is DeepLinkHandler.DeepLinkDestination.TriedPerfumes -> {
                    navController.navigate(Screen.AllTriedPerfumes.route)
                }
                else -> {
                    // Tab-based deep links are handled by MainTabScreen
                }
            }
            onDeepLinkHandled()
        }
    }
    // Show loading while checking auth state
    if (authState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppColors.brandAccent)
        }
        return
    }

    // Determine start destination based on auth state
    val startDestination = if (authState.isAuthenticated) {
        Screen.Main.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Main app screens (after auth)
        composable(Screen.Main.route) {
            MainTabScreen(
                onNavigateToPerfumeDetail = { perfumeId ->
                    navController.navigate(Screen.PerfumeDetail.createRoute(perfumeId))
                },
                onNavigateToGiftRecommendation = {
                    navController.navigate(Screen.GiftRecommendation.route)
                },
                onNavigateToProfileManagement = {
                    navController.navigate(Screen.ProfileManagement.route)
                },
                onNavigateToGiftProfileManagement = {
                    navController.navigate(Screen.GiftProfileManagement.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToCacheStats = {
                    navController.navigate(Screen.CacheStats.route)
                },
                onNavigateToEvaluation = { perfumeId, isEditing ->
                    navController.navigate(Screen.PerfumeEvaluation.createRoute(perfumeId, isEditing))
                },
                onNavigateToTriedList = {
                    navController.navigate(Screen.AllTriedPerfumes.route)
                },
                onNavigateToWishlist = {
                    navController.navigate(Screen.AllWishlist.route)
                },
                onNavigateToAddTriedPerfume = {
                    navController.navigate(Screen.AddTriedPerfume.route)
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onShowOnboarding = {
                    launchCoordinator.showOnboarding()
                }
            )
        }

        composable(
            route = Screen.PerfumeDetail.route,
            arguments = listOf(
                navArgument("perfumeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val viewModel: PerfumeDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // Refresh tried perfume data when returning from evaluation
            val lifecycleOwner = backStackEntry.lifecycle
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        viewModel.refreshTriedPerfume()
                    }
                }
                lifecycleOwner.addObserver(observer)
                onDispose {
                    lifecycleOwner.removeObserver(observer)
                }
            }

            when (val state = uiState) {
                is PerfumeDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.brandAccent)
                    }
                }
                is PerfumeDetailUiState.Success -> {
                    PerfumeDetailScreen(
                        perfume = state.perfume,
                        matchPercentage = state.matchPercentage,
                        triedPerfume = state.triedPerfume,
                        onDismiss = { navController.popBackStack() },
                        onEditOpinion = { perfumeId ->
                            navController.navigate(Screen.PerfumeEvaluation.createRoute(perfumeId, isEditing = true))
                        }
                    )
                }
                is PerfumeDetailUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = state.message,
                            color = AppColors.feedbackError
                        )
                    }
                }
            }
        }

        composable(Screen.GiftRecommendation.route) {
            // Not used - gift profiles accessed from Test tab (iOS parity)
        }

        // Profile Management (personal profiles)
        composable(Screen.ProfileManagement.route) {
            ProfileManagementScreen(
                onDismiss = { navController.popBackStack() },
                onNavigateToPerfumeDetail = { perfumeId ->
                    navController.navigate(Screen.PerfumeDetail.createRoute(perfumeId))
                }
            )
        }

        // Gift Profile Management
        composable(Screen.GiftProfileManagement.route) {
            GiftProfileManagementScreen(
                onDismiss = { navController.popBackStack() },
                onNavigateToPerfumeDetail = { perfumeId ->
                    navController.navigate(Screen.PerfumeDetail.createRoute(perfumeId))
                }
            )
        }

        // Edit Profile
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onDismiss = { navController.popBackStack() }
            )
        }

        // Cache Stats
        composable(Screen.CacheStats.route) {
            CacheStatsScreen(
                onDismiss = { navController.popBackStack() }
            )
        }

        // Perfume Evaluation
        composable(
            route = Screen.PerfumeEvaluation.route,
            arguments = listOf(
                navArgument("perfumeId") { type = NavType.StringType },
                navArgument("isEditing") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            EvaluationOnboardingScreen(
                onDismiss = { navController.popBackStack() }
            )
        }

        // All Tried Perfumes
        composable(Screen.AllTriedPerfumes.route) {
            AllTriedPerfumesScreen(
                onDismiss = { navController.popBackStack() },
                onNavigateToPerfumeDetail = { perfumeId ->
                    navController.navigate(Screen.PerfumeDetail.createRoute(perfumeId))
                }
            )
        }

        // All Wishlist
        composable(Screen.AllWishlist.route) {
            AllWishlistScreen(
                onDismiss = { navController.popBackStack() },
                onNavigateToPerfumeDetail = { perfumeId ->
                    navController.navigate(Screen.PerfumeDetail.createRoute(perfumeId))
                }
            )
        }

        // Add Tried Perfume
        composable(Screen.AddTriedPerfume.route) {
            AddTriedPerfumeScreen(
                onDismiss = { navController.popBackStack() },
                onPerfumeSelected = { perfumeId ->
                    // Navigate to evaluation, replacing this screen
                    navController.navigate(Screen.PerfumeEvaluation.createRoute(perfumeId, false)) {
                        popUpTo(Screen.AddTriedPerfume.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
