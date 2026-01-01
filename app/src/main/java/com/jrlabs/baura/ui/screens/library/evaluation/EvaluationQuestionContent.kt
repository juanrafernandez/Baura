package com.jrlabs.baura.ui.screens.library.evaluation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrlabs.baura.data.model.Question
import com.jrlabs.baura.data.model.QuestionOption
import com.jrlabs.baura.ui.theme.*

/**
 * EvaluationQuestionContent - Display a single evaluation question with options
 * Equivalent to EvaluationQuestionView.swift
 */
@Composable
fun EvaluationQuestionContent(
    question: Question,
    selectedOption: QuestionOption?,
    isMultiSelect: Boolean,
    selectedOptions: List<QuestionOption>,
    onSelectOption: (QuestionOption) -> Unit,
    onToggleOption: (QuestionOption, Int) -> Unit,
    onNext: () -> Unit,
    maxSelections: Int = 1,
    minSelections: Int = 0
) {
    val canContinue = if (isMultiSelect) {
        selectedOptions.size >= minSelections
    } else {
        minSelections == 0 || selectedOption != null
    }

    val showButton = if (isMultiSelect) {
        canContinue
    } else {
        // Single-select: show only if optional and nothing selected (to skip)
        minSelections == 0 && selectedOption == null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = AppSpacing.screenHorizontal,
                vertical = AppSpacing.spacing16
            )
        ) {
            // Question text
            item {
                Text(
                    text = question.getLocalizedText(),
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    color = AppColors.textPrimary,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(AppSpacing.spacing16))

                // Multi-select hint
                if (isMultiSelect) {
                    Text(
                        text = "Puedes seleccionar hasta $maxSelections opciones",
                        fontSize = 14.sp,
                        color = AppColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.spacing12))
                }
            }

            // Options
            items(question.options) { option ->
                val isSelected = if (isMultiSelect) {
                    selectedOptions.any { it.id == option.id }
                } else {
                    selectedOption?.id == option.id
                }

                val canSelect = if (isMultiSelect) {
                    selectedOptions.size < maxSelections || isSelected
                } else {
                    true
                }

                OptionButton(
                    option = option,
                    isSelected = isSelected,
                    enabled = canSelect,
                    onClick = {
                        if (canSelect) {
                            if (isMultiSelect) {
                                onToggleOption(option, maxSelections)
                            } else {
                                onSelectOption(option)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.spacing12))
            }

            // Bottom padding for button
            if (showButton) {
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Skip/Continue button
        if (showButton) {
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
                            text = if (isMultiSelect && selectedOptions.isEmpty() && minSelections == 0) {
                                "Saltar"
                            } else if (isMultiSelect && selectedOptions.isNotEmpty()) {
                                "Continuar"
                            } else {
                                "Saltar"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionButton(
    option: QuestionOption,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(AppCornerRadius.medium),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) AppColors.brandAccent.copy(alpha = 0.1f) else Color.White,
            contentColor = if (isSelected) AppColors.brandAccent else AppColors.textPrimary,
            disabledContainerColor = Color.White.copy(alpha = 0.5f),
            disabledContentColor = AppColors.textTertiary
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) AppColors.brandAccent else AppColors.borderPrimary
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = option.getLocalizedLabel(),
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Start
            )

            val localizedDescription = option.getLocalizedDescription()
            if (localizedDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localizedDescription,
                    fontSize = 13.sp,
                    color = if (isSelected) AppColors.brandAccent.copy(alpha = 0.8f) else AppColors.textSecondary,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
