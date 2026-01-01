package com.jrlabs.baura.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrlabs.baura.ui.components.dialogs.EdgeToEdgeDialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.jrlabs.baura.ui.components.cards.PerfumeCompactCard
import com.jrlabs.baura.ui.components.cards.RatingBadgeType
import com.jrlabs.baura.ui.theme.*

/**
 * LibraryScreen - Mi Colección tab
 * Equivalent to FragranceLibraryTabView.swift
 *
 * Layout:
 * - Editorial header "MI COLECCIÓN"
 * - Horizontal section: "Tus Perfumes Probados" with scroll
 * - "Añadir Perfume Probado" button
 * - Divider
 * - Horizontal section: "Tu Lista de Deseos" with scroll
 */
@Composable
fun LibraryScreen(
    onNavigateToPerfumeDetail: (String) -> Unit,
    onNavigateToEvaluation: (String, Boolean) -> Unit = { _, _ -> },
    onNavigateToTriedList: () -> Unit = {},
    onNavigateToWishlist: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // State for showing AddTriedPerfume modal
    var showAddTriedPerfume by remember { mutableStateOf(false) }

    // Full screen modal for adding tried perfume (edge-to-edge)
    if (showAddTriedPerfume) {
        EdgeToEdgeDialog(
            onDismissRequest = { showAddTriedPerfume = false },
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ) {
            AddTriedPerfumeScreen(
                onDismiss = { showAddTriedPerfume = false },
                onPerfumeSelected = { perfumeId ->
                    showAddTriedPerfume = false
                    // Navigate to evaluation flow (isEditing = false for new perfume)
                    onNavigateToEvaluation(perfumeId, false)
                }
            )
        }
    }

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
            LibraryHeader()

            Spacer(modifier = Modifier.height(25.dp))

            // Tried Perfumes Section
            HorizontalPerfumeSection(
                title = "TUS PERFUMES PROBADOS",
                count = uiState.triedPerfumes.size,
                isEmpty = uiState.triedPerfumes.isEmpty(),
                emptyMessage = "Aún no has probado ningún perfume.\n¡Añade tu primer perfume probado!",
                onViewAll = onNavigateToTriedList,
                content = {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(
                            items = uiState.triedPerfumes.take(5),
                            key = { it.perfumeId }
                        ) { item ->
                            PerfumeCompactCard(
                                name = item.perfumeName,
                                brand = item.perfumeBrand,
                                imageUrl = item.perfumeImageURL,
                                family = item.family,
                                rating = item.personalRating,
                                showRating = true,
                                ratingBadgeType = RatingBadgeType.HEART,
                                onClick = { onNavigateToPerfumeDetail(item.perfumeId) }
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add Tried Perfume Button
            AddPerfumeButton(onClick = { showAddTriedPerfume = true })

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                color = AppColors.dividerPrimary,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Wishlist Section
            HorizontalPerfumeSection(
                title = "TU LISTA DE DESEOS",
                count = uiState.wishlistItems.size,
                isEmpty = uiState.wishlistItems.isEmpty(),
                emptyMessage = "Tu lista de deseos está vacía.\nBusca un perfume y pulsa el botón de carrito para añadirlo.",
                onViewAll = onNavigateToWishlist,
                content = {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(
                            items = uiState.wishlistItems.take(5),
                            key = { it.perfumeId }
                        ) { item ->
                            PerfumeCompactCard(
                                name = item.perfumeName,
                                brand = item.perfumeBrand,
                                imageUrl = item.perfumeImageURL,
                                family = item.family,
                                rating = item.globalRating,
                                showRating = item.globalRating != null,
                                ratingBadgeType = RatingBadgeType.STAR,
                                onClick = { onNavigateToPerfumeDetail(item.perfumeId) }
                            )
                        }
                    }
                }
            )

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
}

@Composable
private fun LibraryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(top = AppSpacing.spacing16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "COLECCIÓN",
            fontFamily = FontFamily.Serif,
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 1.5.sp,
            color = AppColors.textPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HorizontalPerfumeSection(
    title: String,
    count: Int,
    isEmpty: Boolean,
    emptyMessage: String,
    onViewAll: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = AppTypography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    ),
                    color = AppColors.textSecondary
                )
                if (count > 0) {
                    Text(
                        text = "($count)",
                        style = AppTypography.labelSmall.copy(fontSize = 11.sp),
                        color = AppColors.textSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            if (!isEmpty) {
                TextButton(
                    onClick = onViewAll,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ver todos",
                        style = AppTypography.labelSmall.copy(fontSize = 12.sp),
                        color = AppColors.brandAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AppColors.brandAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content or empty state
        if (isEmpty) {
            EmptyStateBox(message = emptyMessage)
        } else {
            content()
        }
    }
}

@Composable
private fun EmptyStateBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = AppTypography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            ),
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddPerfumeButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.brandAccent,
            contentColor = AppColors.textOnAccent
        )
    ) {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Añadir Perfume Probado",
            style = AppTypography.labelMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
