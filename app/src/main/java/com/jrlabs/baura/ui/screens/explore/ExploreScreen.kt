package com.jrlabs.baura.ui.screens.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jrlabs.baura.R
import com.jrlabs.baura.ui.components.cards.PerfumeCompactCard
import com.jrlabs.baura.ui.components.cards.RatingBadgeType
import com.jrlabs.baura.ui.components.feedback.ShimmerCard
import com.jrlabs.baura.ui.theme.*

/**
 * ExploreScreen - Search and filter perfumes
 * Equivalent to ExploreTabView.swift in iOS
 *
 * Features:
 * - Editorial header with title and sort menu
 * - Search bar for text search
 * - Expandable filter section with accordions
 * - Empty state when no search/filters applied
 * - Grid results with perfume cards
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    onNavigateToPerfumeDetail: (String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

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
        ) {
            // Header
            ExploreHeader(
                onSortClick = { showSortMenu = true }
            )

            // Search Bar
            SearchBarSection(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) }
            )

            // Filter Toggle
            FilterToggleSection(
                isFilterExpanded = uiState.isFilterExpanded,
                onToggleFilters = { viewModel.toggleFilterExpanded(!uiState.isFilterExpanded) }
            )

            // Lazy grid content - loads images as they become visible
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Filter Section (expandable) - full width
                item(span = { GridItemSpan(2) }) {
                    AnimatedVisibility(
                        visible = uiState.isFilterExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        ExploreFilterSection(
                            selectedFilters = uiState.selectedFilters,
                            expandedAccordions = uiState.expandedAccordions,
                            onToggleFilter = { category, option -> viewModel.toggleFilter(category, option) },
                            onToggleAccordion = { accordion, expanded -> viewModel.toggleAccordion(accordion, expanded) }
                        )
                    }
                }

                // Results header or empty states - full width
                item(span = { GridItemSpan(2) }) {
                    when {
                        uiState.isLoading -> {
                            // Loading shimmer will be shown in grid items below
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        !uiState.hasActiveSearchOrFilters && uiState.perfumes.isEmpty() -> {
                            ExploreEmptyState(
                                title = stringResource(R.string.explore_empty_title),
                                message = stringResource(R.string.explore_empty_message)
                            )
                        }
                        uiState.hasActiveSearchOrFilters && uiState.perfumes.isEmpty() -> {
                            ExploreEmptyState(
                                title = stringResource(R.string.explore_no_results_title),
                                message = stringResource(R.string.explore_no_results_message)
                            )
                        }
                        uiState.perfumes.isNotEmpty() -> {
                            Text(
                                text = stringResource(R.string.perfumes_found, uiState.perfumes.size),
                                fontSize = 12.sp,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }

                // Loading shimmer grid
                if (uiState.isLoading) {
                    items(6) {
                        ShimmerCard(modifier = Modifier.fillMaxWidth())
                    }
                }

                // Perfume cards - lazy loaded as they become visible
                if (!uiState.isLoading && uiState.perfumes.isNotEmpty()) {
                    items(
                        items = uiState.perfumes,
                        key = { it.id }
                    ) { perfume ->
                        // Map family to display name
                        val familyDisplayName = when (perfume.family.lowercase()) {
                            "woody" -> "Woody"
                            "oriental" -> "Oriental"
                            "floral" -> "Floral"
                            "fresh" -> "Fresh"
                            "citrus" -> "Citrus"
                            "aromatic" -> "Aromatic"
                            "fougere" -> "Fougère"
                            "chypre" -> "Chypre"
                            "aquatic" -> "Aquatic"
                            "gourmand" -> "Gourmand"
                            else -> perfume.family.replaceFirstChar { it.uppercase() }
                        }

                        // Use popularity or default to 10.0 like iOS
                        val displayRating = perfume.popularity ?: 10.0

                        PerfumeCompactCard(
                            name = perfume.name,
                            brand = perfume.brand.replace("_", " ")
                                .split(" ")
                                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                            imageUrl = perfume.imageURL,
                            onClick = { onNavigateToPerfumeDetail(perfume.id) },
                            modifier = Modifier.fillMaxWidth(),
                            rating = displayRating,
                            showRating = true,
                            ratingBadgeType = RatingBadgeType.STAR,
                            family = familyDisplayName,
                            imageHeight = 130
                        )
                    }
                }
            }
        }

        // Sort Menu Dropdown
        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_relevance)) },
                onClick = {
                    viewModel.updateSortOrder(SortOrder.NONE)
                    showSortMenu = false
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_popularity_desc)) },
                onClick = {
                    viewModel.updateSortOrder(SortOrder.POPULARITY_DESC)
                    showSortMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_popularity_asc)) },
                onClick = {
                    viewModel.updateSortOrder(SortOrder.POPULARITY_ASC)
                    showSortMenu = false
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_name_asc)) },
                onClick = {
                    viewModel.updateSortOrder(SortOrder.NAME_ASC)
                    showSortMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_name_desc)) },
                onClick = {
                    viewModel.updateSortOrder(SortOrder.NAME_DESC)
                    showSortMenu = false
                }
            )
        }
    }
}

/**
 * Header - Editorial style matching iOS
 * "ENCUENTRA TU PERFUME" with sort icon
 */
@Composable
private fun ExploreHeader(
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.explore_header_title),
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif,
            letterSpacing = 1.sp,
            color = AppColors.textPrimary
        )

        // Sort/Filter icon (like iOS)
        IconButton(
            onClick = onSortClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = stringResource(R.string.sort),
                tint = AppColors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Search Bar Section - Matches iOS search bar
 */
@Composable
private fun SearchBarSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    val placeholderText = stringResource(R.string.search_placeholder)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 12.dp)
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
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = AppColors.textPrimary
                ),
                cursorBrush = SolidColor(AppColors.brandAccent),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = placeholderText,
                            fontSize = 15.sp,
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
 * Filter Toggle Section - Matches iOS "Ocultar Filtros" toggle
 */
@Composable
private fun FilterToggleSection(
    isFilterExpanded: Boolean,
    onToggleFilters: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = AppSpacing.screenHorizontal)
            .clickable { onToggleFilters() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isFilterExpanded) stringResource(R.string.hide_filters) else stringResource(R.string.show_filters),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.brandAccent
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = if (isFilterExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = AppColors.brandAccent,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Filter chip - Editorial style matching iOS
 */
@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(17.dp))
            .background(
                if (isSelected) AppColors.brandAccent
                else Color.White.copy(alpha = 0.8f)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) Color.White else AppColors.textPrimary
        )
    }
}

/**
 * Filter Section - Accordion filters matching iOS
 * Shows category rows with chevrons, chips appear when expanded
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExploreFilterSection(
    selectedFilters: Map<String, List<String>>,
    expandedAccordions: Set<FilterAccordion>,
    onToggleFilter: (String, String) -> Unit,
    onToggleAccordion: (FilterAccordion, Boolean) -> Unit
) {
    // Get localized strings for filter options
    val genderDisplayNames = mapOf(
        "male" to stringResource(R.string.filter_male),
        "female" to stringResource(R.string.filter_female),
        "unisex" to stringResource(R.string.filter_unisex)
    )
    val familyDisplayNames = mapOf(
        "woody" to stringResource(R.string.filter_woody),
        "oriental" to stringResource(R.string.filter_oriental),
        "floral" to stringResource(R.string.filter_floral),
        "fresh" to stringResource(R.string.filter_fresh),
        "citrus" to stringResource(R.string.filter_citrus),
        "aromatic" to stringResource(R.string.filter_aromatic),
        "fougere" to stringResource(R.string.filter_fougere),
        "chypre" to stringResource(R.string.filter_chypre),
        "aquatic" to stringResource(R.string.filter_aquatic),
        "gourmand" to stringResource(R.string.filter_gourmand)
    )
    val seasonDisplayNames = mapOf(
        "spring" to stringResource(R.string.filter_spring),
        "summer" to stringResource(R.string.filter_summer),
        "fall" to stringResource(R.string.filter_fall),
        "winter" to stringResource(R.string.filter_winter)
    )
    val projectionDisplayNames = mapOf(
        "intimate" to stringResource(R.string.filter_intimate),
        "moderate" to stringResource(R.string.filter_moderate),
        "notable" to stringResource(R.string.filter_notable),
        "powerful" to stringResource(R.string.filter_powerful),
        "enormous" to stringResource(R.string.filter_enormous)
    )
    val durationDisplayNames = mapOf(
        "very_short" to stringResource(R.string.filter_very_short),
        "short" to stringResource(R.string.filter_short),
        "moderate" to stringResource(R.string.filter_duration_moderate),
        "long_lasting" to stringResource(R.string.filter_long_lasting),
        "eternal" to stringResource(R.string.filter_eternal)
    )
    val priceDisplayNames = mapOf(
        "cheap" to stringResource(R.string.filter_cheap),
        "affordable" to stringResource(R.string.filter_affordable),
        "mid_range" to stringResource(R.string.filter_mid_range),
        "premium" to stringResource(R.string.filter_premium),
        "luxury" to stringResource(R.string.filter_luxury)
    )
    val originDisplayNames = mapOf(
        "france" to stringResource(R.string.filter_france),
        "italy" to stringResource(R.string.filter_italy),
        "usa" to stringResource(R.string.filter_usa),
        "spain" to stringResource(R.string.filter_spain),
        "uk" to stringResource(R.string.filter_uk),
        "germany" to stringResource(R.string.filter_germany),
        "middle_east" to stringResource(R.string.filter_middle_east),
        "other" to stringResource(R.string.filter_other)
    )
    val segmentDisplayNames = mapOf(
        "designer" to stringResource(R.string.filter_designer),
        "niche" to stringResource(R.string.filter_niche),
        "indie" to stringResource(R.string.filter_indie),
        "celebrity" to stringResource(R.string.filter_celebrity),
        "classic" to stringResource(R.string.filter_classic)
    )
    val popularityDisplayNames = mapOf(
        "very_popular" to stringResource(R.string.filter_very_popular),
        "popular" to stringResource(R.string.filter_popular),
        "moderate" to stringResource(R.string.filter_pop_moderate),
        "niche" to stringResource(R.string.filter_pop_niche),
        "rare" to stringResource(R.string.filter_rare)
    )

    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Género
        FilterAccordionItem(
            title = stringResource(R.string.filter_gender),
            accordion = FilterAccordion.GENDER,
            options = listOf("male", "female", "unisex"),
            displayNames = genderDisplayNames,
            selectedOptions = selectedFilters["Género"] ?: emptyList(),
            isExpanded = FilterAccordion.GENDER in expandedAccordions,
            onToggleExpanded = { onToggleAccordion(FilterAccordion.GENDER, it) },
            onToggleOption = { onToggleFilter("Género", it) }
        )

        // Familia Olfativa
        FilterAccordionItem(
            title = stringResource(R.string.filter_family),
            accordion = FilterAccordion.FAMILY,
            options = listOf("woody", "oriental", "floral", "fresh", "citrus", "aromatic", "fougere", "chypre", "aquatic", "gourmand"),
            displayNames = familyDisplayNames,
            selectedOptions = selectedFilters["Familia Olfativa"] ?: emptyList(),
            isExpanded = FilterAccordion.FAMILY in expandedAccordions,
            onToggleExpanded = { onToggleAccordion(FilterAccordion.FAMILY, it) },
            onToggleOption = { onToggleFilter("Familia Olfativa", it) }
        )

        // Temporada Recomendada
        FilterAccordionItem(
            title = stringResource(R.string.filter_season),
            accordion = FilterAccordion.SEASON,
            options = listOf("spring", "summer", "fall", "winter"),
            displayNames = seasonDisplayNames,
            selectedOptions = selectedFilters["Temporada"] ?: emptyList(),
            isExpanded = FilterAccordion.SEASON in expandedAccordions,
            onToggleExpanded = { onToggleAccordion(FilterAccordion.SEASON, it) },
            onToggleOption = { onToggleFilter("Temporada", it) }
        )

        // Proyección (sillage)
        FilterAccordionItem(
            title = stringResource(R.string.filter_projection),
            accordion = FilterAccordion.PRESENCE,
            options = listOf("intimate", "moderate", "notable", "powerful", "enormous"),
            displayNames = projectionDisplayNames,
            selectedOptions = selectedFilters["Proyección"] ?: emptyList(),
            isExpanded = FilterAccordion.PRESENCE in expandedAccordions,
            onToggleExpanded = { onToggleAccordion(FilterAccordion.PRESENCE, it) },
            onToggleOption = { onToggleFilter("Proyección", it) }
        )

        // Duración
        FilterAccordionItem(
            title = stringResource(R.string.filter_duration),
            accordion = FilterAccordion.DURATION,
            options = listOf("very_short", "short", "moderate", "long_lasting", "eternal"),
            displayNames = durationDisplayNames,
            selectedOptions = selectedFilters["Duración"] ?: emptyList(),
            isExpanded = FilterAccordion.DURATION in expandedAccordions,
            onToggleExpanded = { onToggleAccordion(FilterAccordion.DURATION, it) },
            onToggleOption = { onToggleFilter("Duración", it) }
        )

        // Precio
        FilterAccordionItem(
            title = stringResource(R.string.filter_price),
            accordion = FilterAccordion.PRICE,
            options = listOf("cheap", "affordable", "mid_range", "premium", "luxury"),
            displayNames = priceDisplayNames,
            selectedOptions = selectedFilters["Precio"] ?: emptyList(),
            isExpanded = FilterAccordion.PRICE in expandedAccordions,
            onToggleExpanded = { onToggleAccordion(FilterAccordion.PRICE, it) },
            onToggleOption = { onToggleFilter("Precio", it) }
        )

        // Origen
        FilterAccordionItem(
            title = stringResource(R.string.filter_origin),
            accordion = FilterAccordion.ORIGIN,
            options = listOf("france", "italy", "usa", "spain", "uk", "germany", "middle_east", "other"),
            displayNames = originDisplayNames,
            selectedOptions = selectedFilters["Origen"] ?: emptyList(),
            isExpanded = FilterAccordion.ORIGIN in expandedAccordions,
            onToggleExpanded = { onToggleAccordion(FilterAccordion.ORIGIN, it) },
            onToggleOption = { onToggleFilter("Origen", it) }
        )

        // Segmento de Mercado
        FilterAccordionItem(
            title = stringResource(R.string.filter_segment),
            accordion = FilterAccordion.SEGMENT,
            options = listOf("designer", "niche", "indie", "celebrity", "classic"),
            displayNames = segmentDisplayNames,
            selectedOptions = selectedFilters["Segmento"] ?: emptyList(),
            isExpanded = FilterAccordion.SEGMENT in expandedAccordions,
            onToggleExpanded = { onToggleAccordion(FilterAccordion.SEGMENT, it) },
            onToggleOption = { onToggleFilter("Segmento", it) }
        )

        // Popularidad
        FilterAccordionItem(
            title = stringResource(R.string.filter_popularity),
            accordion = FilterAccordion.POPULARITY,
            options = listOf("very_popular", "popular", "moderate", "niche", "rare"),
            displayNames = popularityDisplayNames,
            selectedOptions = selectedFilters["Popularidad"] ?: emptyList(),
            isExpanded = FilterAccordion.POPULARITY in expandedAccordions,
            onToggleExpanded = { onToggleAccordion(FilterAccordion.POPULARITY, it) },
            onToggleOption = { onToggleFilter("Popularidad", it) }
        )
    }
}

/**
 * Single filter accordion item - matching iOS style
 * Shows title with chevron ">" that expands to show filter chips
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterAccordionItem(
    title: String,
    accordion: FilterAccordion,
    options: List<String>,
    displayNames: Map<String, String>,
    selectedOptions: List<String>,
    isExpanded: Boolean,
    onToggleExpanded: (Boolean) -> Unit,
    onToggleOption: (String) -> Unit
) {
    Column {
        // Accordion header - title with chevron on the right (like iOS)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpanded(!isExpanded) }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.textPrimary
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = AppColors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        // Accordion content - chips
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option in selectedOptions
                    FilterChip(
                        text = displayNames[option] ?: option,
                        isSelected = isSelected,
                        onClick = { onToggleOption(option) }
                    )
                }
            }
        }
    }
}


/**
 * Empty state component - centered in available space
 */
@Composable
private fun ExploreEmptyState(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 26.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = 15.sp,
            fontWeight = FontWeight.Light,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
