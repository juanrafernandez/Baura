package com.jrlabs.baura.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrlabs.baura.data.model.TriedPerfume
import com.jrlabs.baura.data.model.WishlistItem
import com.jrlabs.baura.data.remote.TriedPerfumeService
import com.jrlabs.baura.data.remote.WishlistService
import com.jrlabs.baura.data.repository.PerfumeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enriched tried perfume with additional perfume details
 */
data class EnrichedTriedPerfume(
    val perfumeId: String,
    val perfumeName: String,
    val perfumeBrand: String,
    val perfumeImageURL: String?,
    val family: String?,
    val perfumeGender: String?,
    val personalRating: Double,
    val globalRating: Double?
)

/**
 * Enriched wishlist item with additional perfume details
 */
data class EnrichedWishlistItem(
    val perfumeId: String,
    val perfumeName: String,
    val perfumeBrand: String,
    val perfumeImageURL: String?,
    val family: String?,
    val globalRating: Double?
)

/**
 * LibraryViewModel - Manages library screen state
 * Enriches tried perfumes and wishlist items with perfume details (family, rating)
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val wishlistService: WishlistService,
    private val triedPerfumeService: TriedPerfumeService,
    private val perfumeRepository: PerfumeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load wishlist and enrich with perfume data
            wishlistService.getWishlistFlow().collect { items ->
                val enrichedItems = enrichWishlistItems(items)
                _uiState.update { it.copy(wishlistItems = enrichedItems, isLoading = false) }
            }
        }

        viewModelScope.launch {
            // Load tried perfumes and enrich with perfume data
            triedPerfumeService.getTriedPerfumesFlow().collect { items ->
                val enrichedItems = enrichTriedPerfumes(items)
                _uiState.update { it.copy(triedPerfumes = enrichedItems) }
            }
        }
    }

    /**
     * Enrich tried perfumes with family and global rating from perfume data
     * Sorted by personal rating (highest first), with 0-rated at the end
     */
    private suspend fun enrichTriedPerfumes(items: List<TriedPerfume>): List<EnrichedTriedPerfume> {
        if (items.isEmpty()) return emptyList()

        val perfumeIds = items.map { it.perfumeId }
        val perfumes = perfumeRepository.getPerfumesByIds(perfumeIds)
        val perfumeMap = perfumes.associateBy { it.id }

        return items.map { item ->
            val perfume = perfumeMap[item.perfumeId]
            EnrichedTriedPerfume(
                perfumeId = item.perfumeId,
                perfumeName = perfume?.name ?: item.perfumeName ?: item.perfumeId.replace("_", " "),
                perfumeBrand = perfume?.brandName?.ifEmpty { perfume.brand } ?: item.perfumeBrand ?: "",
                perfumeImageURL = perfume?.imageURL ?: item.perfumeImageURL,
                family = perfume?.family,
                perfumeGender = perfume?.gender,
                personalRating = item.rating,
                globalRating = perfume?.popularity
            )
        }.sortedByDescending { it.personalRating } // Sort by rating: highest first, 0-rated at the end
    }

    /**
     * Enrich wishlist items with family and global rating from perfume data
     */
    private suspend fun enrichWishlistItems(items: List<WishlistItem>): List<EnrichedWishlistItem> {
        if (items.isEmpty()) return emptyList()

        val perfumeIds = items.map { it.perfumeId }
        val perfumes = perfumeRepository.getPerfumesByIds(perfumeIds)
        val perfumeMap = perfumes.associateBy { it.id }

        return items.map { item ->
            val perfume = perfumeMap[item.perfumeId]
            EnrichedWishlistItem(
                perfumeId = item.perfumeId,
                perfumeName = perfume?.name ?: item.perfumeName ?: item.perfumeId.replace("_", " "),
                perfumeBrand = perfume?.brandName?.ifEmpty { perfume.brand } ?: item.perfumeBrand ?: "",
                perfumeImageURL = perfume?.imageURL ?: item.perfumeImageURL,
                family = perfume?.family,
                globalRating = perfume?.popularity
            )
        }
    }

    fun removeFromWishlist(perfumeId: String) {
        viewModelScope.launch {
            wishlistService.removeFromWishlist(perfumeId)
        }
    }

    fun removeFromTried(perfumeId: String) {
        viewModelScope.launch {
            triedPerfumeService.removeTriedPerfume(perfumeId)
        }
    }
}

data class LibraryUiState(
    val isLoading: Boolean = false,
    val wishlistItems: List<EnrichedWishlistItem> = emptyList(),
    val triedPerfumes: List<EnrichedTriedPerfume> = emptyList(),
    val error: String? = null
)
