package com.jrlabs.baura.ui.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.jrlabs.baura.ui.components.dialogs.EdgeToEdgeDialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jrlabs.baura.R
import com.jrlabs.baura.data.model.Note
import com.jrlabs.baura.data.model.UnifiedProfile
import com.jrlabs.baura.ui.components.buttons.AppButton
import com.jrlabs.baura.ui.components.buttons.AppButtonStyle
import com.jrlabs.baura.ui.theme.*

/**
 * TestScreen - Olfactory test tab
 * Equivalent to TestOlfativoTabView.swift
 *
 * Layout:
 * - Editorial header "DESCUBRE TU FRAGANCIA IDEAL"
 * - Segmented control: "Perfiles Personales" / "Búsquedas de Regalo"
 * - Content based on selected tab
 */
@Composable
fun TestScreen(
    onNavigateToPerfumeDetail: (String) -> Unit,
    onNavigateToProfileManagement: () -> Unit = {},
    onNavigateToGiftProfileManagement: () -> Unit = {},
    viewModel: TestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isTestInProgress -> {
            QuestionFlowScreen(
                viewModel = viewModel,
                onComplete = { viewModel.finishTest() }
            )
        }
        uiState.showResults && uiState.calculatedProfile != null -> {
            ProfileResultsScreen(
                profile = uiState.calculatedProfile!!,
                onDismiss = { viewModel.resetTest() },
                onPerfumeClick = onNavigateToPerfumeDetail,
                isFromTest = true,
                onSaveProfile = { viewModel.saveProfile() },
                onRetakeTest = {
                    viewModel.resetTest()
                    viewModel.startTest(uiState.currentTestType ?: TestType.PERSONAL)
                }
            )
        }
        else -> {
            TestHomeScreen(
                uiState = uiState,
                onTabSelected = { viewModel.selectTab(it) },
                onStartPersonalTest = { viewModel.startTest(TestType.PERSONAL) },
                onStartGiftTest = { viewModel.startTest(TestType.GIFT) },
                onNavigateToPerfumeDetail = onNavigateToPerfumeDetail,
                onViewAllPersonalProfiles = onNavigateToProfileManagement,
                onViewAllGiftProfiles = onNavigateToGiftProfileManagement
            )
        }
    }
}

@Composable
private fun TestHomeScreen(
    uiState: TestUiState,
    onTabSelected: (TestTabSection) -> Unit,
    onStartPersonalTest: () -> Unit,
    onStartGiftTest: () -> Unit,
    onNavigateToPerfumeDetail: (String) -> Unit,
    onViewAllPersonalProfiles: () -> Unit,
    onViewAllGiftProfiles: () -> Unit
) {
    val scrollState = rememberScrollState()

    // State for showing profile results
    var selectedProfile by remember { mutableStateOf<UnifiedProfile?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8D9C0),  // Warm golden/beige at top (same as Home)
                        Color(0xFFF2E9DC),  // Lighter beige
                        Color(0xFFFFFFFF)   // White
                    ),
                    startY = 0f,
                    endY = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Add status bar padding
                .verticalScroll(scrollState)
        ) {
            // Editorial Header
            TestHeader()

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Control
            EditorialSegmentedControl(
                selectedTab = uiState.selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Content
            when (uiState.selectedTab) {
                TestTabSection.PERSONAL_PROFILES -> {
                    PersonalProfilesContent(
                        profiles = uiState.personalProfiles,
                        isLoading = uiState.isLoading,
                        onProfileClick = { profile ->
                            selectedProfile = profile
                        },
                        onViewAll = onViewAllPersonalProfiles,
                        onStartTest = onStartPersonalTest
                    )
                }
                TestTabSection.GIFT_SEARCHES -> {
                    GiftSearchesContent(
                        profiles = uiState.giftProfiles,
                        isLoading = uiState.isLoading,
                        onProfileClick = { profile ->
                            selectedProfile = profile
                        },
                        onViewAll = onViewAllGiftProfiles,
                        onStartTest = onStartGiftTest
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
        }

        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AppColors.brandAccent
            )
        }
    }

    // Profile Results Dialog (fullscreen edge-to-edge)
    selectedProfile?.let { profile ->
        EdgeToEdgeDialog(
            onDismissRequest = { selectedProfile = null },
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ) {
            ProfileResultsScreen(
                profile = profile,
                onDismiss = { selectedProfile = null },
                onPerfumeClick = { perfumeId ->
                    selectedProfile = null
                    onNavigateToPerfumeDetail(perfumeId)
                },
                isFromTest = false
            )
        }
    }
}

@Composable
private fun TestHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(top = AppSpacing.spacing16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DESCUBRE TU FRAGANCIA IDEAL",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 1.sp,
            color = AppColors.textPrimary
        )
    }
}

@Composable
private fun EditorialSegmentedControl(
    selectedTab: TestTabSection,
    onTabSelected: (TestTabSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppColors.backgroundSecondary,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TestTabSection.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) },
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) Color.White else Color.Transparent,
                shadowElevation = if (isSelected) 1.dp else 0.dp
            ) {
                Text(
                    text = tab.displayName,
                    style = AppTypography.labelMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    ),
                    color = if (isSelected) AppColors.textPrimary else AppColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PersonalProfilesContent(
    profiles: List<UnifiedProfile>,
    isLoading: Boolean,
    onProfileClick: (UnifiedProfile) -> Unit,
    onViewAll: () -> Unit,
    onStartTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Intro text
        Text(
            text = "Crea un nuevo perfil personal o consulta tus perfiles guardados.",
            style = AppTypography.bodyMedium.copy(
                fontWeight = FontWeight.Light,
                fontSize = 15.sp
            ),
            color = AppColors.textSecondary
        )

        // Saved Profiles Section
        SavedProfilesSection(
            title = "PERFILES GUARDADOS",
            profiles = profiles,
            maxVisible = 5,
            emptyMessage = "Aun no tienes perfiles guardados.\nInicia el test olfativo para crear tu primer perfil.",
            onProfileClick = onProfileClick,
            onViewAll = onViewAll
        )

        // Start Test Button
        ActionButton(
            text = "Iniciar Test Olfativo",
            icon = Icons.Default.Search,
            isLoading = isLoading,
            onClick = onStartTest
        )
    }
}

@Composable
private fun GiftSearchesContent(
    profiles: List<UnifiedProfile>,
    isLoading: Boolean,
    onProfileClick: (UnifiedProfile) -> Unit,
    onViewAll: () -> Unit,
    onStartTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Intro text
        Text(
            text = "Encuentra el perfume perfecto para regalar. Guarda tus busquedas para consultarlas despues.",
            style = AppTypography.bodyMedium.copy(
                fontWeight = FontWeight.Light,
                fontSize = 15.sp
            ),
            color = AppColors.textSecondary
        )

        // Saved Gift Profiles Section
        SavedProfilesSection(
            title = "PERFILES DE REGALO GUARDADOS",
            profiles = profiles,
            maxVisible = 5,
            emptyMessage = "Aun no has guardado busquedas de regalos.\nPulsa el boton 'Buscar un Regalo' para empezar.",
            onProfileClick = onProfileClick,
            onViewAll = onViewAll
        )

        // Start Gift Search Button
        ActionButton(
            text = "Buscar un Regalo",
            icon = Icons.Default.PlayArrow, // Would use gift icon
            isLoading = isLoading,
            onClick = onStartTest
        )
    }
}

@Composable
private fun SavedProfilesSection(
    title: String,
    profiles: List<UnifiedProfile>,
    maxVisible: Int,
    emptyMessage: String,
    onProfileClick: (UnifiedProfile) -> Unit,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = AppTypography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                color = AppColors.textPrimary
            )

            if (profiles.isNotEmpty()) {
                TextButton(
                    onClick = onViewAll,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ver todos",
                        style = AppTypography.labelSmall.copy(fontSize = 13.sp),
                        color = AppColors.brandAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AppColors.brandAccent
                    )
                }
            }
        }

        // Subtitle with count
        if (profiles.size > maxVisible) {
            Text(
                text = "Mostrando $maxVisible de ${profiles.size} perfiles",
                style = AppTypography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light
                ),
                color = AppColors.textSecondary
            )
        }

        // Profiles or empty state
        if (profiles.isEmpty()) {
            EmptyProfilesState(message = emptyMessage)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                profiles.take(maxVisible).forEachIndexed { index, profile ->
                    val isLastVisible = index == minOf(maxVisible - 1, profiles.size - 1)
                    val showFade = isLastVisible && profiles.size > maxVisible

                    ProfileCard(
                        profile = profile,
                        onClick = { onProfileClick(profile) },
                        showFadeOverlay = showFade
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyProfilesState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = AppTypography.bodySmall.copy(
                fontWeight = FontWeight.Light
            ),
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Family color map - hex colors for each olfactory family
 * Equivalent to family.familyColor in iOS
 */
private val familyColorMap = mapOf(
    "aquatic" to Color(0xFF4A90D9),
    "citrus" to Color(0xFFFFD700),
    "floral" to Color(0xFFFF69B4),
    "fruity" to Color(0xFFFF6B6B),
    "gourmand" to Color(0xFF8B4513),
    "green" to Color(0xFF228B22),
    "oriental" to Color(0xFF8B0000),
    "spicy" to Color(0xFFFF4500),
    "woody" to Color(0xFF8B7355),
    // Spanish variations
    "acuaticos" to Color(0xFF4A90D9),
    "citricos" to Color(0xFFFFD700),
    "florales" to Color(0xFFFF69B4),
    "frutales" to Color(0xFFFF6B6B),
    "orientales" to Color(0xFF8B0000),
    "especiados" to Color(0xFFFF4500),
    "amaderados" to Color(0xFF8B7355),
    "verdes" to Color(0xFF228B22)
)

/**
 * Get color for a family key
 */
private fun getFamilyColor(familyKey: String): Color {
    return familyColorMap[familyKey.lowercase()] ?: Color(0xFFCCCCCC)
}

/**
 * ProfileCardView - Editorial style profile card
 * Equivalent to ProfileCardView.swift in iOS
 */
@Composable
private fun ProfileCard(
    profile: UnifiedProfile,
    onClick: () -> Unit,
    showFadeOverlay: Boolean = false
) {
    // Get family colors: primary + up to 2 subfamilies
    val familyKeys = listOf(profile.primaryFamily) + profile.subfamilies.take(2)

    // Calculate filter count
    val filterCount = (profile.dismissedPerfumeIds?.size ?: 0)
    val viewFilterCount = profile.viewFilters?.let { filters ->
        listOfNotNull(
            filters.gender?.takeIf { it.isNotEmpty() },
            filters.season?.takeIf { it.isNotEmpty() },
            filters.occasion?.takeIf { it.isNotEmpty() },
            filters.price?.takeIf { it.isNotEmpty() },
            filters.marketSegment?.takeIf { it.isNotEmpty() }
        ).size
    } ?: 0
    val totalFilterCount = filterCount + viewFilterCount

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left content: Title and description
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Title row with optional filter indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = profile.name,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = AppColors.textPrimary
                        )

                        // Filter indicator badge
                        if (totalFilterCount > 0) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        color = AppColors.brandAccent.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search, // Using search as filter icon
                                    contentDescription = null,
                                    modifier = Modifier.size(8.dp),
                                    tint = AppColors.brandAccent.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$totalFilterCount",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.brandAccent.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Description - use descriptionProfile from Firestore, or fall back to summary
                    val description = profile.descriptionProfile?.takeIf { it.isNotBlank() }
                        ?: profile.summary
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        color = AppColors.textSecondary,
                        lineHeight = 16.sp,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right content: Three family color circles
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    familyKeys.take(3).forEach { familyKey ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = getFamilyColor(familyKey),
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }

            // Fade overlay for last item when there are more
            if (showFadeOverlay) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.6f)
                                )
                            )
                        )
                )
            }
        }
    }
}

/**
 * Get description for a family key
 */
private fun getFamilyDescription(familyKey: String): String {
    return when (familyKey.lowercase()) {
        "aquatic", "acuaticos" -> "Fragancias frescas y marinas"
        "citrus", "citricos" -> "Fragancias energizantes"
        "floral", "florales" -> "El corazon de la perfumeria"
        "fruity", "frutales" -> "Dulzura y vitalidad"
        "gourmand" -> "Fragancias golosas"
        "green", "verdes" -> "Frescura natural"
        "oriental", "orientales" -> "Opulencia y misterio"
        "spicy", "especiados" -> "Calidez y caracter"
        "woody", "amaderados" -> "Elegancia clasica"
        else -> familyKey.replaceFirstChar { it.uppercase() }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.brandAccent,
            contentColor = AppColors.textOnAccent
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppColors.textOnAccent,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = AppTypography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

// QuestionFlowScreen - Matches iOS TestOlfativoFlowView design
@Composable
private fun QuestionFlowScreen(
    viewModel: TestViewModel,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentQuestion = uiState.currentQuestion

    // Auto-advance for single-select questions
    LaunchedEffect(uiState.shouldAutoAdvance) {
        if (uiState.shouldAutoAdvance) {
            // Small delay to show the selection before advancing
            kotlinx.coroutines.delay(300)
            viewModel.clearAutoAdvance()
            if (uiState.isLastQuestion) {
                onComplete()
            } else {
                viewModel.nextQuestion()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.gradientBeigeWarm.copy(alpha = 0.4f),
                        AppColors.gradientChampagne.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header with back button, title, and close button
            QuestionFlowHeader(
                title = when (uiState.currentTestType) {
                    TestType.PERSONAL -> "Test Olfativo Personal"
                    TestType.GIFT -> "Buscar un Regalo"
                },
                canGoBack = uiState.canGoBack,
                onBack = { viewModel.previousQuestion() },
                onClose = { viewModel.resetTest() }
            )

            // Segmented Progress Bar
            SegmentedProgressBar(
                currentStep = uiState.currentQuestionIndex + 1,
                totalSteps = uiState.totalQuestions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(top = 12.dp, bottom = 20.dp)
            )

            if (currentQuestion != null) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = AppSpacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Question text
                    item {
                        Text(
                            text = currentQuestion.getLocalizedText(),
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = AppColors.textPrimary,
                            lineHeight = 28.sp
                        )
                    }

                    // Subtitle - selection instructions
                    item {
                        val maxSelections = currentQuestion.maxSelections ?: 1
                        val subtitle = if (maxSelections > 1) {
                            "Puedes seleccionar hasta $maxSelections opciones"
                        } else {
                            currentQuestion.getLocalizedSubtitle() ?: "Selecciona una opción"
                        }
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            color = AppColors.textSecondary
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Show different UI based on question type
                    if (uiState.isNotesQuestion) {
                        // Notes autocomplete search
                        item {
                            NotesSearchField(
                                query = uiState.noteSearchQuery,
                                onQueryChange = { viewModel.updateNoteSearchQuery(it) },
                                placeholder = currentQuestion.placeholder ?: "Oud, iris, ambroxan, iso e super..."
                            )
                        }

                        // Selection counter (iOS style)
                        item {
                            val maxSelections = currentQuestion.maxSelections ?: 10
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (uiState.selectedNotes.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "${uiState.selectedNotes.size}/$maxSelections notas seleccionadas",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = AppColors.textSecondary
                                )
                            }
                        }

                        // Autocomplete suggestions
                        if (uiState.filteredNotes.isNotEmpty()) {
                            item {
                                NotesSuggestionsList(
                                    notes = uiState.filteredNotes,
                                    onNoteSelected = { viewModel.selectNote(it) }
                                )
                            }
                        }

                        // Selected notes list (iOS style - vertical cards)
                        if (uiState.selectedNotes.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Notas seleccionadas:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(uiState.selectedNotes) { note ->
                                SelectedNoteCard(
                                    note = note,
                                    onRemove = { viewModel.removeNote(note) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    } else {
                        // Standard options with iOS-style left border accent
                        currentQuestion.options.forEach { option ->
                            item {
                                TestOptionCard(
                                    label = option.getLocalizedLabel(),
                                    description = option.getLocalizedDescription().takeIf { it.isNotEmpty() },
                                    isSelected = uiState.selectedAnswers.contains(option.id),
                                    showDescription = currentQuestion.uiConfig?.showDescriptions ?: true,
                                    onClick = { viewModel.selectAnswer(option.id) }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(120.dp)) // Space for bottom button
                    }
                }
            }
        }

        // Bottom navigation - Continuar button (for multi-select and notes questions)
        if (uiState.isMultiSelect || uiState.isNotesQuestion) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.9f),
                                Color.White
                            )
                        )
                    )
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(bottom = 8.dp, top = 24.dp)
                    .navigationBarsPadding()
            ) {
                // Continue/Finish button
                val isEnabled = uiState.selectedAnswers.isNotEmpty()
                Button(
                    onClick = {
                        if (uiState.isLastQuestion) {
                            onComplete()
                        } else {
                            viewModel.nextQuestion()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = isEnabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabled) AppColors.brandAccent else AppColors.borderSecondary,
                        contentColor = if (isEnabled) AppColors.textOnAccent else AppColors.textDisabled,
                        disabledContainerColor = AppColors.borderSecondary,
                        disabledContentColor = AppColors.textDisabled
                    )
                ) {
                    Text(
                        text = if (uiState.isLastQuestion)
                            stringResource(R.string.finish_test)
                        else
                            stringResource(R.string.continue_test),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Header for QuestionFlowScreen with back button, centered title, and close button
 * Matches iOS TestOlfativoFlowView header exactly
 */
@Composable
private fun QuestionFlowHeader(
    title: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val circleBackground = AppColors.gradientBeigeWarm.copy(alpha = 0.5f)
    val buttonSize = 44.dp
    val iconSize = 22.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        // Back button (left) - only visible if can go back
        if (canGoBack) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(circleBackground)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.back),
                    tint = AppColors.textPrimary,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        // Centered title
        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.3.sp,
            color = AppColors.textPrimary,
            modifier = Modifier.align(Alignment.Center)
        )

        // Close button (right)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(buttonSize)
                .clip(CircleShape)
                .background(circleBackground)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                tint = AppColors.textPrimary,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Segmented Progress Bar with gradient fill
 * Matches iOS segmented progress style
 */
@Composable
private fun SegmentedProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    if (totalSteps <= 0) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (step in 1..totalSteps) {
            val isFilled = step <= currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        brush = if (isFilled) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AppColors.gradientChampagneDark,
                                    AppColors.gradientChampagne
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AppColors.borderSecondary,
                                    AppColors.borderSecondary
                                )
                            )
                        },
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * TestOptionCard - Option button that fills with accent color when selected
 * Matches iOS StandardOptionButton style exactly
 */
@Composable
private fun TestOptionCard(
    label: String,
    description: String? = null,
    isSelected: Boolean,
    showDescription: Boolean = true,
    onClick: () -> Unit
) {
    // Colors based on selection state
    val backgroundColor = if (isSelected) AppColors.brandAccent else Color.White
    val textColor = if (isSelected) Color.White else AppColors.textPrimary
    val descriptionColor = if (isSelected) Color.White.copy(alpha = 0.9f) else AppColors.textSecondary
    val borderColor = if (isSelected) Color.Transparent else AppColors.borderSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (!isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Label - Georgia font (editorial style)
                Text(
                    text = label,
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )

                // Description - System font, light
                if (showDescription && !description.isNullOrEmpty()) {
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = descriptionColor,
                        lineHeight = 20.sp
                    )
                }
            }

            // Checkmark indicator when selected
            if (isSelected) {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * StandardOptionButton - Editorial style option button with description
 * Equivalent to StandardOptionButton.swift in iOS
 *
 * Visual Design:
 * - Label: Georgia font (editorial style), 16pt
 * - Description: System font, light weight, 13pt
 * - Background: White 0.7 opacity (unselected) or brandAccent tint (selected)
 * - Padding: 20h × 16v
 * - Corner Radius: 16pt
 * - Shadow: Dynamic (stronger when selected)
 */
@Composable
private fun StandardOptionButton(
    label: String,
    description: String? = null,
    isSelected: Boolean,
    showDescription: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) AppColors.brandAccent.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.7f),
        shadowElevation = if (isSelected) 4.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) AppColors.brandAccent.copy(alpha = 0.5f) else AppColors.borderSecondary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Label - Georgia font (editorial style)
                Text(
                    text = label,
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isSelected) AppColors.brandAccent else AppColors.textPrimary
                )

                // Description - System font, light
                if (showDescription && !description.isNullOrEmpty()) {
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        color = if (isSelected) AppColors.brandAccent.copy(alpha = 0.8f) else AppColors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            // Checkmark indicator when selected
            if (isSelected) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AppColors.brandAccent
                )
            }
        }
    }
}

@Composable
private fun TestResultsScreen(
    viewModel: TestViewModel,
    onNavigateToPerfumeDetail: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onStartNewTest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundPrimary),
        contentPadding = PaddingValues(AppSpacing.screenHorizontal)
    ) {
        item {
            Spacer(modifier = Modifier.height(AppSpacing.spacing32))
            Text(
                text = stringResource(R.string.your_recommendations),
                style = AppTypography.headlineMedium,
                color = AppColors.textPrimary
            )
        }

        item {
            Spacer(modifier = Modifier.height(AppSpacing.spacing24))
            Text(
                text = "Tu perfil olfativo ha sido calculado",
                style = AppTypography.bodyLarge,
                color = AppColors.textSecondary
            )
        }

        item {
            Spacer(modifier = Modifier.height(AppSpacing.spacing32))
            AppButton(
                text = stringResource(R.string.save_profile),
                onClick = onSaveProfile,
                style = AppButtonStyle.Accent,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(AppSpacing.spacing16))
            AppButton(
                text = "Hacer otro test",
                onClick = onStartNewTest,
                style = AppButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(AppSpacing.spacing32))
        }
    }
}

/**
 * Search field for notes autocomplete
 */
@Composable
private fun NotesSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = AppColors.borderSecondary,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AppColors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = AppColors.textPrimary
                ),
                cursorBrush = SolidColor(AppColors.brandAccent),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = AppColors.textTertiary
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

/**
 * List of autocomplete suggestions for notes
 */
@Composable
private fun NotesSuggestionsList(
    notes: List<Note>,
    onNoteSelected: (Note) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = AppColors.borderSecondary,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        notes.forEachIndexed { index, note ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNoteSelected(note) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.getLocalizedName("es"),
                    fontSize = 15.sp,
                    color = AppColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                note.family?.let { family ->
                    Text(
                        text = family.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        color = AppColors.textTertiary
                    )
                }
            }
            if (index < notes.lastIndex) {
                HorizontalDivider(
                    color = AppColors.borderSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

/**
 * Card for a selected note (iOS style - rectangular card with X button)
 */
@Composable
private fun SelectedNoteCard(
    note: Note,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AppColors.brandAccent.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.getLocalizedName("es"),
                fontSize = 15.sp,
                color = AppColors.brandAccent,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AppColors.textTertiary.copy(alpha = 0.2f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = AppColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
