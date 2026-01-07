package com.jrlabs.baura.ui.screens.library

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Library UI State and related data classes
 */
class LibraryUiStateTest {

    // MARK: - LibraryUiState Tests

    @Test
    fun `default state has correct initial values`() {
        val state = LibraryUiState()

        assertThat(state.isLoadingTriedPerfumes).isTrue()
        assertThat(state.isLoadingWishlist).isTrue()
        assertThat(state.hasLoadedTriedPerfumes).isFalse()
        assertThat(state.hasLoadedWishlist).isFalse()
        assertThat(state.wishlistItems).isEmpty()
        assertThat(state.triedPerfumes).isEmpty()
        assertThat(state.error).isNull()
    }

    @Test
    fun `state copy works correctly`() {
        val initial = LibraryUiState()
        val updated = initial.copy(
            isLoadingTriedPerfumes = false,
            hasLoadedTriedPerfumes = true
        )

        assertThat(updated.isLoadingTriedPerfumes).isFalse()
        assertThat(updated.hasLoadedTriedPerfumes).isTrue()
        // Other values remain unchanged
        assertThat(updated.isLoadingWishlist).isTrue()
        assertThat(updated.hasLoadedWishlist).isFalse()
    }

    @Test
    fun `state with error`() {
        val state = LibraryUiState(error = "Network error")

        assertThat(state.error).isEqualTo("Network error")
    }

    // MARK: - EnrichedTriedPerfume Tests

    @Test
    fun `EnrichedTriedPerfume holds correct values`() {
        val perfume = EnrichedTriedPerfume(
            perfumeId = "sauvage",
            perfumeName = "Sauvage",
            perfumeBrand = "Dior",
            perfumeImageURL = "https://example.com/sauvage.jpg",
            family = "Woody",
            perfumeGender = "masculine",
            personalRating = 4.5,
            globalRating = 92.0
        )

        assertThat(perfume.perfumeId).isEqualTo("sauvage")
        assertThat(perfume.perfumeName).isEqualTo("Sauvage")
        assertThat(perfume.perfumeBrand).isEqualTo("Dior")
        assertThat(perfume.perfumeImageURL).isEqualTo("https://example.com/sauvage.jpg")
        assertThat(perfume.family).isEqualTo("Woody")
        assertThat(perfume.perfumeGender).isEqualTo("masculine")
        assertThat(perfume.personalRating).isEqualTo(4.5)
        assertThat(perfume.globalRating).isEqualTo(92.0)
    }

    @Test
    fun `EnrichedTriedPerfume with null optional fields`() {
        val perfume = EnrichedTriedPerfume(
            perfumeId = "test",
            perfumeName = "Test",
            perfumeBrand = "Brand",
            perfumeImageURL = null,
            family = null,
            perfumeGender = null,
            personalRating = 3.0,
            globalRating = null
        )

        assertThat(perfume.perfumeImageURL).isNull()
        assertThat(perfume.family).isNull()
        assertThat(perfume.perfumeGender).isNull()
        assertThat(perfume.globalRating).isNull()
    }

    @Test
    fun `EnrichedTriedPerfume equality`() {
        val perfume1 = EnrichedTriedPerfume(
            perfumeId = "sauvage",
            perfumeName = "Sauvage",
            perfumeBrand = "Dior",
            perfumeImageURL = null,
            family = "Woody",
            perfumeGender = "masculine",
            personalRating = 4.5,
            globalRating = 92.0
        )

        val perfume2 = EnrichedTriedPerfume(
            perfumeId = "sauvage",
            perfumeName = "Sauvage",
            perfumeBrand = "Dior",
            perfumeImageURL = null,
            family = "Woody",
            perfumeGender = "masculine",
            personalRating = 4.5,
            globalRating = 92.0
        )

        assertThat(perfume1).isEqualTo(perfume2)
        assertThat(perfume1.hashCode()).isEqualTo(perfume2.hashCode())
    }

    // MARK: - EnrichedWishlistItem Tests

    @Test
    fun `EnrichedWishlistItem holds correct values`() {
        val item = EnrichedWishlistItem(
            perfumeId = "aventus",
            perfumeName = "Aventus",
            perfumeBrand = "Creed",
            perfumeImageURL = "https://example.com/aventus.jpg",
            family = "Fruity",
            globalRating = 95.0
        )

        assertThat(item.perfumeId).isEqualTo("aventus")
        assertThat(item.perfumeName).isEqualTo("Aventus")
        assertThat(item.perfumeBrand).isEqualTo("Creed")
        assertThat(item.perfumeImageURL).isEqualTo("https://example.com/aventus.jpg")
        assertThat(item.family).isEqualTo("Fruity")
        assertThat(item.globalRating).isEqualTo(95.0)
    }

    @Test
    fun `EnrichedWishlistItem with null optional fields`() {
        val item = EnrichedWishlistItem(
            perfumeId = "test",
            perfumeName = "Test",
            perfumeBrand = "Brand",
            perfumeImageURL = null,
            family = null,
            globalRating = null
        )

        assertThat(item.perfumeImageURL).isNull()
        assertThat(item.family).isNull()
        assertThat(item.globalRating).isNull()
    }

    // MARK: - State Transition Tests

    @Test
    fun `state transitions from loading to loaded with data`() {
        val initial = LibraryUiState()

        val triedPerfumes = listOf(
            EnrichedTriedPerfume(
                perfumeId = "1",
                perfumeName = "Perfume 1",
                perfumeBrand = "Brand 1",
                perfumeImageURL = null,
                family = null,
                perfumeGender = null,
                personalRating = 4.0,
                globalRating = null
            )
        )

        val loaded = initial.copy(
            isLoadingTriedPerfumes = false,
            hasLoadedTriedPerfumes = true,
            triedPerfumes = triedPerfumes
        )

        assertThat(loaded.isLoadingTriedPerfumes).isFalse()
        assertThat(loaded.hasLoadedTriedPerfumes).isTrue()
        assertThat(loaded.triedPerfumes).hasSize(1)
        assertThat(loaded.triedPerfumes[0].perfumeName).isEqualTo("Perfume 1")
    }

    @Test
    fun `state transitions from loading to loaded empty`() {
        val initial = LibraryUiState()

        val loaded = initial.copy(
            isLoadingWishlist = false,
            hasLoadedWishlist = true,
            wishlistItems = emptyList()
        )

        assertThat(loaded.isLoadingWishlist).isFalse()
        assertThat(loaded.hasLoadedWishlist).isTrue()
        assertThat(loaded.wishlistItems).isEmpty()
    }

    @Test
    fun `can determine if truly empty after loading`() {
        // Still loading - not truly empty
        val loading = LibraryUiState(
            isLoadingTriedPerfumes = true,
            hasLoadedTriedPerfumes = false,
            triedPerfumes = emptyList()
        )
        val isTrulyEmptyWhileLoading = loading.hasLoadedTriedPerfumes && loading.triedPerfumes.isEmpty()
        assertThat(isTrulyEmptyWhileLoading).isFalse()

        // Loaded and empty - truly empty
        val loadedEmpty = LibraryUiState(
            isLoadingTriedPerfumes = false,
            hasLoadedTriedPerfumes = true,
            triedPerfumes = emptyList()
        )
        val isTrulyEmptyAfterLoading = loadedEmpty.hasLoadedTriedPerfumes && loadedEmpty.triedPerfumes.isEmpty()
        assertThat(isTrulyEmptyAfterLoading).isTrue()
    }
}
