package com.jrlabs.baura.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jrlabs.baura.ui.components.filters.FilterAccordion
import com.jrlabs.baura.ui.components.filters.FilterToggleButton
import com.jrlabs.baura.ui.components.filters.PerfumeFilterSection
import com.jrlabs.baura.ui.components.filters.SortOrder
import com.jrlabs.baura.ui.theme.*

/**
 * AllTriedPerfumesScreen - Shows all tried perfumes in a list
 * Equivalent to TriedPerfumesListView.swift in iOS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTriedPerfumesScreen(
    onDismiss: () -> Unit,
    onNavigateToPerfumeDetail: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Local filter state
    var isFilterExpanded by remember { mutableStateOf(false) }
    var selectedFilters by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var expandedAccordions by remember { mutableStateOf<Set<FilterAccordion>>(emptySet()) }
    var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter and sort the list
    val filteredPerfumes = remember(uiState.triedPerfumes, selectedFilters, sortOrder) {
        var result = uiState.triedPerfumes

        // Apply filters
        selectedFilters.forEach { (category, values) ->
            if (values.isNotEmpty()) {
                result = when (category) {
                    "Género" -> result.filter { it.perfumeGender in values }
                    "Familia Olfativa" -> result.filter { it.family in values }
                    else -> result
                }
            }
        }

        // Apply sorting
        when (sortOrder) {
            SortOrder.NAME_ASC -> result.sortedBy { it.perfumeName }
            SortOrder.NAME_DESC -> result.sortedByDescending { it.perfumeName }
            SortOrder.RATING_ASC -> result.sortedBy { it.personalRating }
            SortOrder.RATING_DESC -> result.sortedByDescending { it.personalRating }
            else -> result
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8D9C0),
                        Color(0xFFF2E9DC),
                        Color(0xFFFFFFFF)
                    ),
                    startY = 0f,
                    endY = 800f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "PERFUMES PROBADOS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = AppColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = AppColors.textPrimary
                        )
                    }
                },
                actions = {
                    // Share button
                    IconButton(onClick = { /* TODO: Share functionality */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = AppColors.textPrimary
                        )
                    }
                    // Sort button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Ordenar",
                                tint = AppColors.textPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Relevancia") },
                                onClick = {
                                    sortOrder = SortOrder.NONE
                                    showSortMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Valoración (Mayor a Menor)") },
                                onClick = {
                                    sortOrder = SortOrder.RATING_DESC
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Valoración (Menor a Mayor)") },
                                onClick = {
                                    sortOrder = SortOrder.RATING_ASC
                                    showSortMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Nombre (A - Z)") },
                                onClick = {
                                    sortOrder = SortOrder.NAME_ASC
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Nombre (Z - A)") },
                                onClick = {
                                    sortOrder = SortOrder.NAME_DESC
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Filter toggle button
            FilterToggleButton(
                isExpanded = isFilterExpanded,
                onClick = { isFilterExpanded = !isFilterExpanded },
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)
            )

            // Expandable filters
            AnimatedVisibility(
                visible = isFilterExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                PerfumeFilterSection(
                    selectedFilters = selectedFilters,
                    expandedAccordions = expandedAccordions,
                    onToggleFilter = { category, option ->
                        val currentList = selectedFilters[category] ?: emptyList()
                        val newList = if (option in currentList) {
                            currentList - option
                        } else {
                            currentList + option
                        }
                        selectedFilters = if (newList.isEmpty()) {
                            selectedFilters - category
                        } else {
                            selectedFilters + (category to newList)
                        }
                    },
                    onToggleAccordion = { accordion, expanded ->
                        expandedAccordions = if (expanded) {
                            expandedAccordions + accordion
                        } else {
                            expandedAccordions - accordion
                        }
                    },
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)
                )
            }

            // Perfume list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.brandAccent)
                }
            } else if (filteredPerfumes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedFilters.isNotEmpty())
                            "No hay perfumes con estos filtros"
                        else
                            "No has probado ningún perfume",
                        color = AppColors.textSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AppSpacing.screenHorizontal,
                        vertical = 8.dp
                    )
                ) {
                    items(
                        items = filteredPerfumes,
                        key = { it.perfumeId }
                    ) { item ->
                        TriedPerfumeListItem(
                            name = item.perfumeName,
                            brand = item.perfumeBrand,
                            imageUrl = item.perfumeImageURL,
                            rating = item.personalRating,
                            onClick = { onNavigateToPerfumeDetail(item.perfumeId) }
                        )
                        HorizontalDivider(
                            color = AppColors.dividerPrimary,
                            modifier = Modifier.padding(start = 90.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TriedPerfumeListItem(
    name: String,
    brand: String,
    imageUrl: String?,
    rating: Double,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Perfume image
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            } else {
                Text(
                    text = "\uD83E\uDDF4",
                    fontSize = 32.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name and brand
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )
            Text(
                text = brand,
                fontSize = 14.sp,
                color = AppColors.textSecondary
            )
        }

        // Rating with heart
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color(0xFFE53935)
            )
            Text(
                text = String.format("%.1f", rating),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )
        }
    }
}
