package com.jrlabs.baura.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jrlabs.baura.R
import com.jrlabs.baura.data.model.Perfume
import com.jrlabs.baura.data.model.UnifiedProfile
import com.jrlabs.baura.ui.components.feedback.ShimmerBox
import com.jrlabs.baura.ui.screens.perfume.AllPerfumesScreen
import com.jrlabs.baura.ui.screens.perfume.PerfumeWithScore
import com.jrlabs.baura.ui.components.feedback.ShimmerCircle
import com.jrlabs.baura.ui.components.feedback.ShimmerRowItem
import com.jrlabs.baura.ui.components.feedback.ShimmerTextLine
import com.jrlabs.baura.ui.theme.*
import java.util.Calendar

/**
 * HomeScreen - Main home tab
 * Equivalent to HomeTabView.swift in iOS
 *
 * Structure:
 * - Gradient background (champan)
 * - State-based UI (loading/loaded/error)
 * - GreetingSection with time-based greeting
 * - Swipeable ProfileCard pager
 * - Empty state with CTA button
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToPerfumeDetail: (String) -> Unit,
    onNavigateToGiftRecommendation: () -> Unit,
    onNavigateToTest: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Filter personal profiles only (exclude gift profiles) and sort by order
    val personalProfiles = uiState.profiles
        .filter { it.profileType == "personal" }
        .sortedBy { it.order }

    // Background extends edge-to-edge (behind status bar)
    // Gradient matching iOS: golden/beige at top, transitioning to white around 35% of screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)  // Base white
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8D9C0),  // Warm golden/beige at top
                        Color(0xFFF2E9DC),  // Lighter beige
                        Color(0xFFFFFFFF)   // White
                    ),
                    startY = 0f,
                    endY = 800f  // End gradient earlier (around 35% of screen)
                )
            )
    ) {
        // Content with status bar padding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            when {
                uiState.isLoading -> {
                    // Estado 1: Loading - mostrar skeleton
                    HomeLoadingSkeleton()
                }
                uiState.error != null -> {
                // Estado 2: Error
                HomeErrorView(
                    message = uiState.error ?: "",
                    onRetry = { viewModel.refresh() }
                )
            }
            personalProfiles.isEmpty() -> {
                // Estado 3: Empty state - no hay perfiles
                HomeEmptyState(
                    onCreateProfile = onNavigateToTest
                )
            }
            else -> {
                // Estado 4: Loaded - mostrar perfiles
                HomeLoadedContent(
                    userName = uiState.userName?.takeIf { it.isNotBlank() } ?: "Usuario",
                    profiles = personalProfiles,
                    perfumesCache = uiState.perfumesCache,
                    onNavigateToPerfumeDetail = onNavigateToPerfumeDetail
                )
            }
            }
        } // Close content Box with statusBarsPadding
    } // Close background Box
}

/**
 * GreetingSection - Time-based greeting
 * Equivalent to GreetingSection.swift
 */
@Composable
private fun GreetingSection(
    userName: String,
    modifier: Modifier = Modifier
) {
    val greeting = getGreetingMessage(userName)

    Text(
        text = greeting,
        fontSize = 18.sp,
        fontFamily = FontFamily.Serif,
        color = AppColors.textSecondary,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun getGreetingMessage(userName: String): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val capitalizedName = userName.replaceFirstChar { it.uppercase() }

    return when (hour) {
        in 6..11 -> stringResource(R.string.greeting_morning, capitalizedName)
        in 12..17 -> stringResource(R.string.greeting_afternoon, capitalizedName)
        else -> stringResource(R.string.greeting_evening, capitalizedName)
    }
}

/**
 * Home content when profiles are loaded
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeLoadedContent(
    userName: String,
    profiles: List<UnifiedProfile>,
    perfumesCache: Map<String, Perfume>,
    onNavigateToPerfumeDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Greeting section
        GreetingSection(
            userName = userName,
            modifier = Modifier
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.spacing16)
        )

        // Profile pager (swipeable cards)
        if (profiles.size > 1) {
            val pagerState = rememberPagerState(pageCount = { profiles.count() })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                ProfileCardContent(
                    profile = profiles[page],
                    perfumesCache = perfumesCache,
                    onPerfumeClick = onNavigateToPerfumeDetail,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Page indicator
            if (profiles.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(profiles.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) AppColors.textPrimary
                                    else AppColors.textSecondary.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        } else {
            // Single profile - no pager needed
            ProfileCardContent(
                profile = profiles.first(),
                perfumesCache = perfumesCache,
                onPerfumeClick = onNavigateToPerfumeDetail,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

/**
 * Layout constants matching iOS ProfileCard.LayoutConstants
 */
private object ProfileCardLayout {
    val headerHeight = 160.dp      // Profile header with name (fixed for 2 lines)
    val sectionTitleHeight = 35.dp // "RECOMENDADOS" + padding
    val perfumeItemHeight = 68.dp  // Each item (fixed height for consistent layout)
    val separatorHeight = 17.dp    // Separator between items (divider + padding)
    val viewAllButtonHeight = 60.dp // "Ver todos" button
    val topPadding = 8.dp          // Top padding
}

/**
 * Calculate how many perfumes fit on screen
 * Equivalent to calculateVisiblePerfumes in iOS
 */
@Composable
private fun calculateVisiblePerfumes(availableHeight: Float): Int {
    val density = LocalDensity.current
    with(density) {
        val headerPx = ProfileCardLayout.headerHeight.toPx()
        val sectionTitlePx = ProfileCardLayout.sectionTitleHeight.toPx()
        val viewAllButtonPx = ProfileCardLayout.viewAllButtonHeight.toPx()
        val topPaddingPx = ProfileCardLayout.topPadding.toPx()
        val itemHeightPx = ProfileCardLayout.perfumeItemHeight.toPx() + ProfileCardLayout.separatorHeight.toPx()

        val usableHeight = availableHeight - headerPx - sectionTitlePx - viewAllButtonPx - topPaddingPx
        val count = (usableHeight / itemHeightPx).toInt()

        // Minimum 2, maximum 6
        return count.coerceIn(2, 6)
    }
}

/**
 * ProfileCard content - Shows profile details and recommendations
 * Equivalent to ProfileCard.swift
 */
@Composable
private fun ProfileCardContent(
    profile: UnifiedProfile,
    perfumesCache: Map<String, Perfume>,
    onPerfumeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val availableHeightPx = with(density) { maxHeight.toPx() }
        val visibleCount = calculateVisiblePerfumes(availableHeightPx)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Separator superior elegante
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(AppColors.textSecondary.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profile name (uppercase with letter spacing and generous line height)
            Text(
                text = profile.name.uppercase(),
                fontSize = 32.sp,
                fontFamily = FontFamily.Serif,
                letterSpacing = 6.sp,
                lineHeight = 44.sp,  // Generous spacing between lines like iOS
                color = AppColors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Family names with bullet separator (capitalized)
            val familyNames = buildList {
                if (profile.primaryFamily.isNotEmpty()) add(capitalizeFamilyName(profile.primaryFamily))
                profile.subfamilies.take(2).forEach { add(capitalizeFamilyName(it)) }
            }

            if (familyNames.isNotEmpty()) {
                Text(
                    text = familyNames.joinToString("  •  "),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Separator inferior
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(AppColors.textSecondary.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recommendations section title
            Text(
                text = stringResource(R.string.recommended),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = AppColors.textSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List of recommended perfumes (dynamic count based on screen size)
            val allRecommendations = profile.recommendedPerfumes ?: emptyList()
            val recommendations = allRecommendations.take(visibleCount)

            if (recommendations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_recommendations_yet),
                        fontSize = 14.sp,
                        color = AppColors.textTertiary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    recommendations.forEachIndexed { index, rec ->
                        val perfume = perfumesCache[rec.perfumeId]
                        RecommendedPerfumeRow(
                            perfume = perfume,
                            perfumeId = rec.perfumeId,
                            matchPercentage = rec.matchPercentage,
                            onClick = { onPerfumeClick(rec.perfumeId) }
                        )

                        if (index < recommendations.size - 1) {
                            HorizontalDivider(
                                color = AppColors.textSecondary.copy(alpha = 0.1f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // "Ver todos" button (only if there are more perfumes than visible)
                if (allRecommendations.size > visibleCount) {
                    TextButton(
                        onClick = { /* Navigate to all perfumes */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.see_all_arrow),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "→",
                                fontSize = 14.sp,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single recommended perfume row
 * Equivalent to PerfumeListItemView.swift in iOS
 */
@Composable
private fun RecommendedPerfumeRow(
    perfume: Perfume?,
    perfumeId: String,
    matchPercentage: Double,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Perfume image container (60x60 like iOS)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = perfume?.imageURL
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .size(coil.size.Size.ORIGINAL)  // Load original size, don't downsample
                            .crossfade(true)
                            .build(),
                        contentDescription = perfume?.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Placeholder when no image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.backgroundTertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (perfume?.name ?: perfumeId).take(1).uppercase(),
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Serif,
                            color = AppColors.textTertiary
                        )
                    }
                }
            }

            // Perfume info (name and brand)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Perfume name
                Text(
                    text = perfume?.name ?: perfumeId,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    color = AppColors.textPrimary,
                    maxLines = 2
                )

                // Brand (uppercase with letter spacing)
                val brandDisplay = perfume?.let {
                    it.brandName.ifEmpty {
                        // Convert brand key to display name (e.g., "carolina_herrera" -> "Carolina Herrera")
                        it.brand.replace("_", " ").split(" ")
                            .joinToString(" ") { word -> word.replaceFirstChar { c -> c.uppercase() } }
                    }
                } ?: ""
                Text(
                    text = brandDisplay.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = AppColors.textSecondary,
                    maxLines = 1
                )
            }

            // Match percentage
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${matchPercentage.toInt()}%",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    color = AppColors.textPrimary
                )
                Text(
                    text = stringResource(R.string.match),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    color = AppColors.textSecondary
                )
            }
        }
    }
}

/**
 * Empty state when no profiles exist
 */
@Composable
private fun HomeEmptyState(
    onCreateProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.brandAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🌸",
                fontSize = 60.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = stringResource(R.string.home_welcome_title),
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = stringResource(R.string.home_welcome_description),
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CTA Button
        Button(
            onClick = onCreateProfile,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.brandAccent
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.create_olfactory_profile),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Loading skeleton with shimmer animation
 * Equivalent to iOS shimmer loading effect
 */
@Composable
private fun HomeLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(top = AppSpacing.spacing16)
    ) {
        // Greeting skeleton with shimmer
        ShimmerTextLine(
            modifier = Modifier.width(180.dp),
            height = 24.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        ShimmerTextLine(
            modifier = Modifier.width(140.dp),
            height = 20.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile card skeleton with shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Profile title shimmer
                ShimmerTextLine(
                    modifier = Modifier.width(120.dp),
                    height = 32.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                ShimmerTextLine(
                    modifier = Modifier.width(180.dp),
                    height = 14.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section title
                ShimmerTextLine(
                    modifier = Modifier.width(100.dp),
                    height = 12.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Perfume list items with shimmer
                repeat(4) {
                    ShimmerRowItem(imageSize = 60.dp)
                    if (it < 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Button placeholder
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * Error view
 */
@Composable
private fun HomeErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠️",
            fontSize = 60.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.error_loading_profiles),
            fontSize = 20.sp,
            fontFamily = FontFamily.Serif,
            color = AppColors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.brandAccent
            )
        ) {
            Text(stringResource(R.string.retry))
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Capitalize family name for display
 * Maps family keys to Spanish display names
 */
private fun capitalizeFamilyName(familyKey: String): String {
    return when (familyKey.lowercase()) {
        "aquatic", "acuaticos" -> "Acuáticos"
        "citrus", "citricos" -> "Cítricos"
        "floral", "florales" -> "Florales"
        "fruity", "frutales" -> "Frutales"
        "gourmand" -> "Gourmand"
        "green", "verdes" -> "Verdes"
        "oriental", "orientales" -> "Orientales"
        "spicy", "especiados" -> "Especiados"
        "woody", "amaderados" -> "Amaderados"
        else -> familyKey.replaceFirstChar { it.uppercase() }
    }
}
