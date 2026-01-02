package com.jrlabs.baura.ui.screens.library.evaluation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrlabs.baura.ui.theme.*

/**
 * ImpressionsContent - Final step for impressions and rating
 * Equivalent to AddPerfumeStep9View.swift in iOS
 */
@Composable
fun ImpressionsContent(
    impressions: String,
    rating: Double,
    onImpressionsChange: (String) -> Unit,
    onRatingChange: (Double) -> Unit,
    onSave: () -> Unit = {},
    isSaving: Boolean = false,
    isEditing: Boolean = false
) {
    val maxCharacters = 2000
    val minCharacters = 30

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.spacing16)
        ) {
            // Title
            Text(
                text = "Tus impresiones",
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                color = AppColors.textPrimary,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(AppSpacing.spacing16))

            // Subtitle
            Text(
                text = "Describe tus impresiones del perfume",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )

            // Character limits hint
            Text(
                text = "Mínimo $minCharacters, máximo $maxCharacters caracteres",
                fontSize = 14.sp,
                color = AppColors.textSecondary
            )

            Spacer(modifier = Modifier.height(AppSpacing.spacing12))

            // Text field
            OutlinedTextField(
                value = impressions,
                onValueChange = {
                    if (it.length <= maxCharacters) {
                        onImpressionsChange(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp),
                placeholder = {
                    Text(
                        text = "",
                        color = AppColors.textTertiary,
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.borderPrimary,
                    unfocusedBorderColor = AppColors.borderPrimary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(AppCornerRadius.medium)
            )

            // Character count
            Text(
                text = "${impressions.length}/$maxCharacters",
                fontSize = 12.sp,
                color = AppColors.textTertiary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(AppSpacing.spacing24))

            // Rating section
            Text(
                text = "Tu valoración",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.spacing16))

            // Large rating number
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%.1f", rating),
                    fontFamily = FontFamily.Serif,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    color = AppColors.brandAccent
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.spacing8))

            // Slider
            Slider(
                value = rating.toFloat(),
                onValueChange = { onRatingChange((it * 10).toInt() / 10.0) },
                valueRange = 0f..10f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = AppColors.brandAccent,
                    inactiveTrackColor = AppColors.borderPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Min/Max labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0",
                    fontSize = 14.sp,
                    color = AppColors.textSecondary
                )
                Text(
                    text = "10",
                    fontSize = 14.sp,
                    color = AppColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Bottom button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column {
                HorizontalDivider(color = AppColors.dividerPrimary)
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenHorizontal)
                        .padding(vertical = AppSpacing.spacing12)
                        .height(52.dp),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.brandAccent
                    ),
                    shape = RoundedCornerShape(AppCornerRadius.medium)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) "Actualizar" else "Guardar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
