package com.jrlabs.baura.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.jrlabs.baura.ui.components.feedback.ShimmerBox
import com.jrlabs.baura.ui.theme.*

/**
 * Rating badge type for PerfumeCompactCard
 */
enum class RatingBadgeType {
    HEART,  // For tried perfumes (personal rating)
    STAR    // For wishlist (global/match rating)
}

/**
 * PerfumeCard - Displays a perfume in card format
 * Equivalent to PerfumeCarouselItem.swift / PerfumeCard.swift
 */
@Composable
fun PerfumeCard(
    name: String,
    brand: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rating: Double? = null,
    showRating: Boolean = true,
    family: String? = null
) {
    // Simple column without card elevation - matches iOS style
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        // Image with shimmer loading and rounded corners - like iOS with padding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.9f)  // Square-ish like iOS
                .clip(RoundedCornerShape(AppCornerRadius.medium))
                .background(AppColors.backgroundSecondary)
        ) {
            if (imageUrl != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),  // Padding to show image smaller like iOS
                    contentScale = ContentScale.Fit,  // Fit instead of Crop to maintain aspect ratio
                    loading = {
                        ShimmerBox(modifier = Modifier.fillMaxSize())
                    },
                    error = {
                        // Fallback placeholder on error
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AppColors.backgroundTertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase(),
                                style = AppTypography.headlineMedium,
                                color = AppColors.textTertiary
                            )
                        }
                    }
                )
            } else {
                // Placeholder when no URL
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.backgroundTertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = AppTypography.headlineMedium,
                        color = AppColors.textTertiary
                    )
                }
            }

            // Rating badge
            if (showRating && rating != null) {
                Surface(
                    modifier = Modifier
                        .padding(AppSpacing.spacing8)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(AppCornerRadius.small),
                    color = AppColors.brandAccent
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = AppSpacing.spacing6,
                            vertical = AppSpacing.spacing2
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(AppIconSize.small),
                            tint = AppColors.textOnAccent
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", rating),
                            style = AppTypography.labelSmall,
                            color = AppColors.textOnAccent
                        )
                    }
                }
            }
        }

        // Info - compact padding like iOS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.spacing8)
        ) {
            Text(
                text = brand,
                style = AppTypography.overline,
                color = AppColors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(AppSpacing.spacing2))

            Text(
                text = name,
                style = AppTypography.labelMedium,
                color = AppColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Family - shown below name like iOS
            if (family != null) {
                Spacer(modifier = Modifier.height(AppSpacing.spacing2))
                Text(
                    text = family,
                    style = AppTypography.labelSmall,
                    color = AppColors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Compact perfume card for horizontal scrolling sections
 * Equivalent to PerfumeCard.swift with style: .compact, size: .small
 * Matches iOS design with brand, name, family display and heart/star rating badges
 * Full card with shadow containing image and text info
 */
@Composable
fun PerfumeCompactCard(
    name: String,
    brand: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    family: String? = null,
    rating: Double? = null,
    showRating: Boolean = true,
    ratingBadgeType: RatingBadgeType = RatingBadgeType.HEART,
    // Legacy parameter for backwards compatibility
    personalRating: Double? = null
) {
    // Use rating if provided, otherwise fall back to personalRating for backwards compatibility
    val displayRating = rating ?: personalRating

    // Badge colors matching iOS design
    val badgeBackgroundColor = Color(0xFFFAF7F2) // Cream/beige background
    val heartColor = Color(0xFFE57373) // Soft red for heart
    val starColor = AppColors.brandAccent // Gold for star

    // Full card with shadow - iOS style
    Card(
        modifier = modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(Color.White)
            ) {
                if (imageUrl != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit,
                        loading = {
                            ShimmerBox(modifier = Modifier.fillMaxSize())
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppColors.backgroundTertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.take(1).uppercase(),
                                    style = AppTypography.headlineMedium,
                                    color = AppColors.textTertiary
                                )
                            }
                        }
                    )
                } else {
                    // Placeholder when no URL
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.backgroundTertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1).uppercase(),
                            style = AppTypography.headlineMedium,
                            color = AppColors.textTertiary
                        )
                    }
                }

                // Rating badge (top right) - iOS style
                if (showRating && displayRating != null && displayRating > 0) {
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBackgroundColor,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (ratingBadgeType == RatingBadgeType.HEART)
                                    Icons.Default.Favorite else Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (ratingBadgeType == RatingBadgeType.HEART)
                                    heartColor else starColor
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format("%.1f", displayRating),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.textPrimary
                            )
                        }
                    }
                }
            }

            // Text info section inside card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand (centered, small)
                Text(
                    text = brand.replace("_", " "),
                    fontSize = 11.sp,
                    color = AppColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Name (centered, bold)
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Family (centered, small, below name)
                if (family != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = family,
                        fontSize = 11.sp,
                        color = AppColors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Large perfume card for detail/feature display
 */
@Composable
fun PerfumeLargeCard(
    name: String,
    brand: String,
    family: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppCornerRadius.medium),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surfaceCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppElevation.small
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding)
        ) {
            // Image with shimmer loading
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(AppCornerRadius.small))
                    .background(AppColors.backgroundSecondary)
            ) {
                if (imageUrl != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            ShimmerBox(modifier = Modifier.fillMaxSize())
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.take(1).uppercase(),
                                    style = AppTypography.titleLarge,
                                    color = AppColors.textTertiary
                                )
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1).uppercase(),
                            style = AppTypography.titleLarge,
                            color = AppColors.textTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.spacing16))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = brand,
                    style = AppTypography.overline,
                    color = AppColors.textTertiary
                )

                Spacer(modifier = Modifier.height(AppSpacing.spacing4))

                Text(
                    text = name,
                    style = AppTypography.titleSmall,
                    color = AppColors.textPrimary
                )

                Spacer(modifier = Modifier.height(AppSpacing.spacing8))

                Surface(
                    shape = RoundedCornerShape(AppCornerRadius.small),
                    color = AppColors.brandAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = family,
                        style = AppTypography.labelSmall,
                        color = AppColors.brandAccent,
                        modifier = Modifier.padding(
                            horizontal = AppSpacing.spacing8,
                            vertical = AppSpacing.spacing4
                        )
                    )
                }
            }
        }
    }
}
