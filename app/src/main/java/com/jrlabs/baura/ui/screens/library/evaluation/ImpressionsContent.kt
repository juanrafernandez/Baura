package com.jrlabs.baura.ui.screens.library.evaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrlabs.baura.ui.theme.*
import kotlin.math.roundToInt

/**
 * ImpressionsContent - Final step for impressions and rating
 * Equivalent to AddPerfumeStep9View.swift
 */
@Composable
fun ImpressionsContent(
    impressions: String,
    rating: Double,
    onImpressionsChange: (String) -> Unit,
    onRatingChange: (Double) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(top = AppSpacing.spacing16)
    ) {
        // Title
        Text(
            text = "¿Qué te pareció?",
            fontFamily = FontFamily.Serif,
            fontSize = 24.sp,
            color = AppColors.textPrimary,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(AppSpacing.spacing8))

        Text(
            text = "Comparte tu experiencia con este perfume",
            fontSize = 14.sp,
            color = AppColors.textSecondary
        )

        Spacer(modifier = Modifier.height(AppSpacing.spacing32))

        // Rating section
        Text(
            text = "PUNTUACIÓN",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textTertiary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(AppSpacing.spacing16))

        // Star rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val isFilled = i <= rating.roundToInt()
                IconButton(
                    onClick = { onRatingChange(i.toDouble()) },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Estrella $i",
                        modifier = Modifier.size(40.dp),
                        tint = if (isFilled) AppColors.brandAccent else AppColors.borderPrimary
                    )
                }
            }
        }

        // Rating value display
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    rating >= 5 -> "Excelente"
                    rating >= 4 -> "Muy bueno"
                    rating >= 3 -> "Bueno"
                    rating >= 2 -> "Regular"
                    rating >= 1 -> "Malo"
                    else -> "Sin puntuar"
                },
                fontSize = 14.sp,
                color = if (rating > 0) AppColors.brandAccent else AppColors.textTertiary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.spacing32))

        // Slider rating (more precise)
        Column {
            Text(
                text = "Ajuste fino: ${String.format("%.1f", rating)}",
                fontSize = 12.sp,
                color = AppColors.textSecondary
            )
            Spacer(modifier = Modifier.height(AppSpacing.spacing8))
            Slider(
                value = rating.toFloat(),
                onValueChange = { onRatingChange(it.toDouble()) },
                valueRange = 0f..5f,
                steps = 9, // 0.5 increments
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.brandAccent,
                    activeTrackColor = AppColors.brandAccent,
                    inactiveTrackColor = AppColors.borderPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.spacing32))

        // Impressions section
        Text(
            text = "IMPRESIONES",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textTertiary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(AppSpacing.spacing12))

        OutlinedTextField(
            value = impressions,
            onValueChange = onImpressionsChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            placeholder = {
                Text(
                    text = "Escribe tus impresiones sobre este perfume...\n\n¿Cómo te hizo sentir? ¿En qué situación lo usaste? ¿Lo recomendarías?",
                    color = AppColors.textTertiary,
                    fontSize = 14.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.brandAccent,
                unfocusedBorderColor = AppColors.borderPrimary,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(AppCornerRadius.medium)
        )

        // Character count
        Text(
            text = "${impressions.length} caracteres",
            fontSize = 12.sp,
            color = AppColors.textTertiary,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom button
    }
}
