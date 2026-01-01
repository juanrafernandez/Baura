package com.jrlabs.baura.ui.screens.library.evaluation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrlabs.baura.data.model.Perfume
import com.jrlabs.baura.ui.theme.*

/**
 * PerceivedNotesContent - Select notes the user perceived from the perfume
 * Equivalent to PerceivedNotesView.swift
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerceivedNotesContent(
    perfume: Perfume?,
    selectedNotes: Set<String>,
    onToggleNote: (String) -> Unit,
    onNext: () -> Unit,
    onAddCustomNotes: (() -> Unit)? = null
) {
    if (perfume == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay perfume seleccionado",
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary
            )
        }
        return
    }

    // Group notes by category
    val noteCategories = listOf(
        NoteCategory("NOTAS DE SALIDA", perfume.topNotes ?: emptyList()),
        NoteCategory("NOTAS DE CORAZÓN", perfume.heartNotes ?: emptyList()),
        NoteCategory("NOTAS DE FONDO", perfume.baseNotes ?: emptyList())
    ).filter { it.notes.isNotEmpty() }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.spacing16)
        ) {
            // Question title
            Text(
                text = "¿Qué notas has percibido?",
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                color = AppColors.textPrimary,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(AppSpacing.spacing8))

            // Subtitle
            Text(
                text = "Selecciona las notas que has identificado en este perfume",
                fontSize = 14.sp,
                color = AppColors.textSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(AppSpacing.spacing24))

            // Note categories with chips
            noteCategories.forEach { category ->
                // Category title
                Text(
                    text = category.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textTertiary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(AppSpacing.spacing12))

                // Horizontal flow of chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.spacing8),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.spacing8)
                ) {
                    category.notes.forEach { note ->
                        val isSelected = selectedNotes.contains(note)
                        NoteChip(
                            note = note,
                            isSelected = isSelected,
                            onClick = { onToggleNote(note) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.spacing24))
            }

            // "Add notes not in list" button
            if (onAddCustomNotes != null) {
                AddCustomNotesButton(onClick = onAddCustomNotes)
                Spacer(modifier = Modifier.height(AppSpacing.spacing24))
            }

            // Bottom padding for button
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Continue button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column {
                HorizontalDivider(color = AppColors.dividerPrimary)
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenHorizontal)
                        .padding(vertical = AppSpacing.spacing12)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.brandAccent
                    ),
                    shape = RoundedCornerShape(AppCornerRadius.medium)
                ) {
                    Text(
                        text = "Continuar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private data class NoteCategory(
    val title: String,
    val notes: List<String>
)

@Composable
private fun NoteChip(
    note: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppCornerRadius.full),
        color = if (isSelected) AppColors.textPrimary else AppColors.backgroundSecondary,
        border = if (!isSelected) BorderStroke(1.dp, AppColors.borderPrimary) else null
    ) {
        Text(
            text = note.replaceFirstChar { it.uppercase() },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else AppColors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun AddCustomNotesButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppCornerRadius.medium),
        color = Color.Transparent,
        border = BorderStroke(1.dp, AppColors.brandAccent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = AppColors.brandAccent,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "He identificado notas que no están en la lista",
                fontSize = 14.sp,
                color = AppColors.brandAccent,
                modifier = Modifier.weight(1f),
                lineHeight = 20.sp
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.brandAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
