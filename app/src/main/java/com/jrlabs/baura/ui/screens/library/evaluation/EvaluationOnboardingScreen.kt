package com.jrlabs.baura.ui.screens.library.evaluation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jrlabs.baura.data.model.EvaluationStep
import com.jrlabs.baura.ui.theme.*

/**
 * EvaluationOnboardingScreen - Multi-step perfume evaluation flow
 * Equivalent to AddPerfumeOnboardingView.swift in iOS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationOnboardingScreen(
    onDismiss: () -> Unit,
    viewModel: EvaluationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back button
    BackHandler {
        if (!viewModel.goToPreviousStep()) {
            onDismiss()
        }
    }

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.currentStep.navigationTitle,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        color = AppColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!viewModel.goToPreviousStep()) {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.currentStepIndex > 0) {
                                Icons.AutoMirrored.Filled.ArrowBack
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = "Atrás",
                            tint = AppColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White) // Solid base background
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.gradientBeigeWarm.copy(alpha = 0.3f),
                            AppColors.gradientChampagne.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Progress bar
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenHorizontal)
                        .height(4.dp),
                    color = AppColors.brandAccent,
                    trackColor = AppColors.borderPrimary,
                )

                Spacer(modifier = Modifier.height(AppSpacing.spacing16))

                // Content
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        uiState.isLoading -> {
                            LoadingContent()
                        }
                        uiState.error != null -> {
                            ErrorContent(
                                message = uiState.error!!,
                                onRetry = { viewModel.clearError() }
                            )
                        }
                        else -> {
                            StepContent(
                                step = uiState.currentStep,
                                viewModel = viewModel,
                                uiState = uiState
                            )
                        }
                    }
                }

                // Bottom button (for last step)
                if (uiState.isLastStep && !uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.screenHorizontal)
                            .padding(bottom = AppSpacing.spacing16)
                    ) {
                        Button(
                            onClick = { viewModel.saveTriedPerfume { /* handled by LaunchedEffect */ } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.brandAccent
                            ),
                            shape = RoundedCornerShape(AppCornerRadius.medium)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Guardar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.brandAccent)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando pregunta...",
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Error",
                style = AppTypography.titleLarge,
                color = AppColors.feedbackError
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text("Reintentar", color = AppColors.brandAccent)
            }
        }
    }
}

@Composable
private fun StepContent(
    step: EvaluationStep,
    viewModel: EvaluationViewModel,
    uiState: EvaluationUiState
) {
    when (step) {
        EvaluationStep.PRESENCE,
        EvaluationStep.DURATION,
        EvaluationStep.PRICE,
        EvaluationStep.GENDER -> {
            // Single-select Firebase question
            val question = viewModel.getQuestion(step.stepType)
            if (question != null) {
                EvaluationQuestionContent(
                    question = question,
                    selectedOption = uiState.singleAnswers[step.stepType],
                    isMultiSelect = false,
                    selectedOptions = emptyList(),
                    onSelectOption = { option ->
                        viewModel.selectSingleOption(step.stepType, option)
                        // Auto-advance after short delay
                        viewModel.goToNextStep()
                    },
                    onToggleOption = { _, _ -> },
                    onNext = { viewModel.goToNextStep() }
                )
            } else if (uiState.isQuestionsLoading) {
                LoadingContent()
            } else {
                QuestionNotFoundContent(stepType = step.stepType)
            }
        }

        EvaluationStep.OCCASION,
        EvaluationStep.SEASON -> {
            // Multi-select Firebase question
            val question = viewModel.getQuestion(step.stepType)
            if (question != null) {
                val maxSelections = question.maxSelections ?: 3
                EvaluationQuestionContent(
                    question = question,
                    selectedOption = null,
                    isMultiSelect = true,
                    selectedOptions = uiState.multiAnswers[step.stepType] ?: emptyList(),
                    onSelectOption = { },
                    onToggleOption = { option, max -> viewModel.toggleMultiOption(step.stepType, option, max) },
                    onNext = { viewModel.goToNextStep() },
                    maxSelections = maxSelections,
                    minSelections = question.minSelections ?: 0
                )
            } else if (uiState.isQuestionsLoading) {
                LoadingContent()
            } else {
                QuestionNotFoundContent(stepType = step.stepType)
            }
        }

        EvaluationStep.PERCEIVED_NOTES -> {
            // Local step: Perceived notes selection
            var showAddNotesSheet by remember { mutableStateOf(false) }

            PerceivedNotesContent(
                perfume = uiState.perfume,
                selectedNotes = uiState.perceivedNotes,
                onToggleNote = { viewModel.togglePerceivedNote(it) },
                onNext = { viewModel.goToNextStep() },
                onAddCustomNotes = { showAddNotesSheet = true }
            )

            if (showAddNotesSheet) {
                AddNotesBottomSheet(
                    onDismiss = { showAddNotesSheet = false },
                    addedTopNotes = uiState.addedTopNotes,
                    addedHeartNotes = uiState.addedHeartNotes,
                    addedBaseNotes = uiState.addedBaseNotes,
                    onAddNote = { noteType, note ->
                        when (noteType) {
                            NoteType.TOP -> viewModel.addTopNote(note)
                            NoteType.HEART -> viewModel.addHeartNote(note)
                            NoteType.BASE -> viewModel.addBaseNote(note)
                        }
                    },
                    onRemoveNote = { noteType, note ->
                        when (noteType) {
                            NoteType.TOP -> viewModel.removeTopNote(note)
                            NoteType.HEART -> viewModel.removeHeartNote(note)
                            NoteType.BASE -> viewModel.removeBaseNote(note)
                        }
                    }
                )
            }
        }

        EvaluationStep.IMPRESSIONS_AND_RATING -> {
            // Local step: Final impressions and rating
            ImpressionsContent(
                impressions = uiState.impressions,
                rating = uiState.rating,
                onImpressionsChange = { viewModel.updateImpressions(it) },
                onRatingChange = { viewModel.updateRating(it) }
            )
        }
    }
}

@Composable
private fun QuestionNotFoundContent(stepType: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Pregunta no encontrada",
                style = AppTypography.titleMedium,
                color = AppColors.feedbackWarning
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "stepType: $stepType",
                style = AppTypography.bodySmall,
                color = AppColors.textTertiary
            )
        }
    }
}
