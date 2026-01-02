package com.jrlabs.baura.ui.screens.library.evaluation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.jrlabs.baura.data.model.EvaluationConfiguration
import com.jrlabs.baura.data.model.EvaluationContext
import com.jrlabs.baura.data.model.EvaluationStep
import com.jrlabs.baura.data.model.Perfume
import com.jrlabs.baura.data.model.Question
import com.jrlabs.baura.data.model.QuestionOption
import com.jrlabs.baura.data.model.TriedPerfume
import com.jrlabs.baura.data.remote.NotesService
import com.jrlabs.baura.data.remote.QuestionService
import com.jrlabs.baura.data.remote.TriedPerfumeService
import com.jrlabs.baura.data.repository.PerfumeRepository
import com.jrlabs.baura.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the evaluation flow
 */
data class EvaluationUiState(
    val isLoading: Boolean = true,
    val isQuestionsLoading: Boolean = true,
    val isSaving: Boolean = false,
    val currentStepIndex: Int = 0,
    val perfume: Perfume? = null,
    val questions: List<Question> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false,

    // Firestore answers (stepType -> selected QuestionOption)
    val singleAnswers: Map<String, QuestionOption> = emptyMap(),
    // Multi-select answers (stepType -> List of selected QuestionOptions)
    val multiAnswers: Map<String, List<QuestionOption>> = emptyMap(),

    // Perceived notes (local step)
    val perceivedNotes: Set<String> = emptySet(),

    // User added notes (crowdsourcing)
    val addedTopNotes: List<String> = emptyList(),
    val addedHeartNotes: List<String> = emptyList(),
    val addedBaseNotes: List<String> = emptyList(),

    // Final step: impressions and rating
    val impressions: String = "",
    val rating: Double = 0.0,

    // Available notes from Firebase for search suggestions
    val availableNotes: List<String> = emptyList()
) {
    val configuration: EvaluationConfiguration = EvaluationConfiguration(EvaluationContext.FULL_EVALUATION)

    val currentStep: EvaluationStep
        get() = if (currentStepIndex < configuration.steps.size) {
            configuration.steps[currentStepIndex]
        } else {
            configuration.steps.last()
        }

    val isLastStep: Boolean
        get() = currentStepIndex == configuration.steps.lastIndex

    val progress: Float
        get() = (currentStepIndex + 1).toFloat() / configuration.totalSteps
}

@HiltViewModel
class EvaluationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val perfumeRepository: PerfumeRepository,
    private val questionService: QuestionService,
    private val triedPerfumeService: TriedPerfumeService,
    private val notesService: NotesService
) : ViewModel() {

    private val perfumeId: String = savedStateHandle.get<String>("perfumeId") ?: ""
    private val isEditing: Boolean = savedStateHandle.get<Boolean>("isEditing") ?: false

    private val _uiState = MutableStateFlow(EvaluationUiState())
    val uiState: StateFlow<EvaluationUiState> = _uiState.asStateFlow()

    private var existingTriedPerfume: TriedPerfume? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load perfume - try by ID first, then by key
                var perfume = perfumeRepository.getPerfumeById(perfumeId)
                if (perfume == null) {
                    // Fallback to key lookup for backwards compatibility
                    perfume = perfumeRepository.getPerfumeByKey(perfumeId)
                }
                if (perfume == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Perfume no encontrado") }
                    return@launch
                }

                // Load evaluation questions from Firestore
                val questions = questionService.fetchEvaluationQuestions()
                AppLogger.info("EvaluationVM", "Loaded ${questions.size} evaluation questions")

                // Load available notes for search suggestions
                val availableNotes = notesService.getNoteNames()
                AppLogger.info("EvaluationVM", "Loaded ${availableNotes.size} available notes")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isQuestionsLoading = false,
                        perfume = perfume,
                        questions = questions,
                        availableNotes = availableNotes
                    )
                }

                // If editing, load existing tried perfume data
                if (isEditing) {
                    loadExistingTriedPerfume()
                }

            } catch (e: Exception) {
                AppLogger.error("EvaluationVM", "Error loading data", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun loadExistingTriedPerfume() {
        try {
            val tried = triedPerfumeService.getTriedPerfume(perfumeId)
            if (tried != null) {
                existingTriedPerfume = tried
                preloadAnswers(tried)
            }
        } catch (e: Exception) {
            AppLogger.error("EvaluationVM", "Error loading existing tried perfume", e)
        }
    }

    private fun preloadAnswers(triedPerfume: TriedPerfume) {
        val questions = _uiState.value.questions

        val singleAnswers = mutableMapOf<String, QuestionOption>()
        val multiAnswers = mutableMapOf<String, List<QuestionOption>>()

        // Presence
        triedPerfume.userPresence?.let { value ->
            findOption(questions, "presence", value)?.let { singleAnswers["presence"] = it }
        }

        // Duration
        triedPerfume.userDuration?.let { value ->
            findOption(questions, "duration", value)?.let { singleAnswers["duration"] = it }
        }

        // Price
        if (triedPerfume.userPrice.isNotBlank()) {
            findOption(questions, "price", triedPerfume.userPrice)?.let { singleAnswers["price"] = it }
        }

        // Gender
        triedPerfume.userGender?.let { value ->
            findOption(questions, "gender", value)?.let { singleAnswers["gender"] = it }
        }

        // Occasions (multi-select)
        triedPerfume.userOccasions?.let { values ->
            if (values.isNotEmpty()) {
                val options = findOptions(questions, "occasion", values)
                if (options.isNotEmpty()) multiAnswers["occasion"] = options
            }
        }

        // Seasons (multi-select)
        if (triedPerfume.userSeasons.isNotEmpty()) {
            val options = findOptions(questions, "season", triedPerfume.userSeasons)
            if (options.isNotEmpty()) multiAnswers["season"] = options
        }

        _uiState.update {
            it.copy(
                singleAnswers = singleAnswers,
                multiAnswers = multiAnswers,
                perceivedNotes = triedPerfume.perceivedNotes?.toSet() ?: emptySet(),
                addedTopNotes = triedPerfume.addedTopNotes ?: emptyList(),
                addedHeartNotes = triedPerfume.addedHeartNotes ?: emptyList(),
                addedBaseNotes = triedPerfume.addedBaseNotes ?: emptyList(),
                impressions = triedPerfume.notes,
                rating = triedPerfume.rating
            )
        }
    }

    private fun findOption(questions: List<Question>, stepType: String, value: String): QuestionOption? {
        val question = questions.find { it.stepType == stepType }
        return question?.options?.find { it.value == value }
    }

    private fun findOptions(questions: List<Question>, stepType: String, values: List<String>): List<QuestionOption> {
        val question = questions.find { it.stepType == stepType }
        return question?.options?.filter { values.contains(it.value) } ?: emptyList()
    }

    fun getQuestion(stepType: String): Question? {
        return _uiState.value.questions.find { it.stepType == stepType }
    }

    fun selectSingleOption(stepType: String, option: QuestionOption) {
        _uiState.update {
            it.copy(singleAnswers = it.singleAnswers + (stepType to option))
        }
    }

    fun toggleMultiOption(stepType: String, option: QuestionOption, maxSelections: Int) {
        _uiState.update { state ->
            val currentOptions = state.multiAnswers[stepType] ?: emptyList()
            val newOptions = if (currentOptions.any { it.id == option.id }) {
                // Deselect
                currentOptions.filter { it.id != option.id }
            } else if (currentOptions.size < maxSelections) {
                // Select (if not exceeding max)
                currentOptions + option
            } else {
                currentOptions
            }
            state.copy(multiAnswers = state.multiAnswers + (stepType to newOptions))
        }
    }

    fun togglePerceivedNote(noteKey: String) {
        _uiState.update { state ->
            val notes = state.perceivedNotes.toMutableSet()
            if (notes.contains(noteKey)) {
                notes.remove(noteKey)
            } else {
                notes.add(noteKey)
            }
            state.copy(perceivedNotes = notes)
        }
    }

    fun updateImpressions(impressions: String) {
        _uiState.update { it.copy(impressions = impressions) }
    }

    fun updateRating(rating: Double) {
        _uiState.update { it.copy(rating = rating) }
    }

    fun goToNextStep() {
        val currentState = _uiState.value
        if (currentState.currentStepIndex < currentState.configuration.steps.lastIndex) {
            _uiState.update { it.copy(currentStepIndex = it.currentStepIndex + 1) }
        }
    }

    fun goToPreviousStep(): Boolean {
        val currentState = _uiState.value
        return if (currentState.currentStepIndex > 0) {
            _uiState.update { it.copy(currentStepIndex = it.currentStepIndex - 1) }
            true
        } else {
            false
        }
    }

    fun saveTriedPerfume(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val state = _uiState.value
                val perfume = state.perfume ?: throw IllegalStateException("No perfume selected")

                val triedPerfume = TriedPerfume(
                    id = perfume.key,
                    perfumeId = perfume.key,
                    rating = state.rating,
                    notes = state.impressions,
                    triedAt = existingTriedPerfume?.triedAt ?: Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    userPersonalities = emptyList(),
                    userPrice = state.singleAnswers["price"]?.value ?: "",
                    userSeasons = state.multiAnswers["season"]?.mapNotNull { it.value } ?: emptyList(),
                    userPresence = state.singleAnswers["presence"]?.value,
                    userDuration = state.singleAnswers["duration"]?.value,
                    userOccasions = state.multiAnswers["occasion"]?.mapNotNull { it.value }?.takeIf { it.isNotEmpty() },
                    userGender = state.singleAnswers["gender"]?.value,
                    perceivedNotes = state.perceivedNotes.toList().takeIf { it.isNotEmpty() },
                    addedTopNotes = state.addedTopNotes.takeIf { it.isNotEmpty() },
                    addedHeartNotes = state.addedHeartNotes.takeIf { it.isNotEmpty() },
                    addedBaseNotes = state.addedBaseNotes.takeIf { it.isNotEmpty() }
                )

                val result = if (existingTriedPerfume != null) {
                    AppLogger.info("EvaluationVM", "Updating tried perfume: ${triedPerfume.perfumeId}")
                    triedPerfumeService.updateTriedPerfume(triedPerfume)
                } else {
                    AppLogger.info("EvaluationVM", "Adding new tried perfume: ${triedPerfume.perfumeId}")
                    triedPerfumeService.addTriedPerfume(triedPerfume)
                }

                result.onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    onSuccess()
                }.onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }

            } catch (e: Exception) {
                AppLogger.error("EvaluationVM", "Error saving tried perfume", e)
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Custom notes management
    fun addTopNote(note: String) {
        _uiState.update { state ->
            if (state.addedTopNotes.size < 3 && !state.addedTopNotes.contains(note)) {
                state.copy(addedTopNotes = state.addedTopNotes + note)
            } else state
        }
    }

    fun removeTopNote(note: String) {
        _uiState.update { state ->
            state.copy(addedTopNotes = state.addedTopNotes - note)
        }
    }

    fun addHeartNote(note: String) {
        _uiState.update { state ->
            if (state.addedHeartNotes.size < 3 && !state.addedHeartNotes.contains(note)) {
                state.copy(addedHeartNotes = state.addedHeartNotes + note)
            } else state
        }
    }

    fun removeHeartNote(note: String) {
        _uiState.update { state ->
            state.copy(addedHeartNotes = state.addedHeartNotes - note)
        }
    }

    fun addBaseNote(note: String) {
        _uiState.update { state ->
            if (state.addedBaseNotes.size < 3 && !state.addedBaseNotes.contains(note)) {
                state.copy(addedBaseNotes = state.addedBaseNotes + note)
            } else state
        }
    }

    fun removeBaseNote(note: String) {
        _uiState.update { state ->
            state.copy(addedBaseNotes = state.addedBaseNotes - note)
        }
    }
}
