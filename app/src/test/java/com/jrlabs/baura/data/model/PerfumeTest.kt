package com.jrlabs.baura.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Perfume model
 */
class PerfumeTest {

    @Test
    fun `getLocalizedName returns translated name when available`() {
        val perfume = Perfume(
            id = "sauvage",
            name = "Sauvage",
            brand = "dior",
            translations = mapOf(
                "es" to PerfumeTranslation(name = "Sauvage", description = "Fresco y salvaje"),
                "en" to PerfumeTranslation(name = "Sauvage", description = "Fresh and wild")
            )
        )

        assertThat(perfume.getLocalizedName("es")).isEqualTo("Sauvage")
        assertThat(perfume.getLocalizedName("en")).isEqualTo("Sauvage")
    }

    @Test
    fun `getLocalizedName falls back to name when no translations`() {
        val perfume = Perfume(
            id = "sauvage",
            name = "Sauvage",
            brand = "dior"
        )

        assertThat(perfume.getLocalizedName("es")).isEqualTo("Sauvage")
        assertThat(perfume.getLocalizedName("fr")).isEqualTo("Sauvage")
    }

    @Test
    fun `getLocalizedDescription returns translated description when available`() {
        val perfume = Perfume(
            id = "sauvage",
            name = "Sauvage",
            brand = "dior",
            translations = mapOf(
                "es" to PerfumeTranslation(name = "Sauvage", description = "Fresco y salvaje")
            )
        )

        assertThat(perfume.getLocalizedDescription("es")).isEqualTo("Fresco y salvaje")
    }

    @Test
    fun `getLocalizedDescription returns null when no translations`() {
        val perfume = Perfume(
            id = "sauvage",
            name = "Sauvage",
            brand = "dior"
        )

        assertThat(perfume.getLocalizedDescription("es")).isNull()
    }

    @Test
    fun `getAllNotes combines all note types`() {
        val perfume = Perfume(
            id = "test",
            name = "Test",
            brand = "test",
            topNotes = listOf("bergamot", "pepper"),
            heartNotes = listOf("lavender", "geranium"),
            baseNotes = listOf("cedar", "vetiver")
        )

        val allNotes = perfume.getAllNotes()

        assertThat(allNotes).hasSize(6)
        assertThat(allNotes).containsExactly("bergamot", "pepper", "lavender", "geranium", "cedar", "vetiver")
    }

    @Test
    fun `getAllNotes returns empty list when no notes`() {
        val perfume = Perfume(
            id = "test",
            name = "Test",
            brand = "test"
        )

        assertThat(perfume.getAllNotes()).isEmpty()
    }

    @Test
    fun `getAllNotes removes duplicates`() {
        val perfume = Perfume(
            id = "test",
            name = "Test",
            brand = "test",
            topNotes = listOf("bergamot", "rose"),
            heartNotes = listOf("rose", "jasmine"),
            baseNotes = listOf("cedar")
        )

        val allNotes = perfume.getAllNotes()

        assertThat(allNotes).hasSize(4)
        assertThat(allNotes).containsExactly("bergamot", "rose", "jasmine", "cedar")
    }

    @Test
    fun `getAllNotes handles null note lists`() {
        val perfume = Perfume(
            id = "test",
            name = "Test",
            brand = "test",
            topNotes = listOf("bergamot"),
            heartNotes = null,
            baseNotes = null
        )

        val allNotes = perfume.getAllNotes()

        assertThat(allNotes).hasSize(1)
        assertThat(allNotes).containsExactly("bergamot")
    }

    @Test
    fun `default values are correct`() {
        val perfume = Perfume()

        assertThat(perfume.id).isEmpty()
        assertThat(perfume.name).isEmpty()
        assertThat(perfume.brand).isEmpty()
        assertThat(perfume.family).isEmpty()
        assertThat(perfume.gender).isEqualTo("unisex")
        assertThat(perfume.topNotes).isNull()
        assertThat(perfume.heartNotes).isNull()
        assertThat(perfume.baseNotes).isNull()
        assertThat(perfume.popularity).isNull()
        assertThat(perfume.year).isNull()
        assertThat(perfume.imageURL).isNull()
    }

    @Test
    fun `perfume with all fields populated`() {
        val perfume = Perfume(
            id = "aventus",
            name = "Aventus",
            brand = "creed",
            brandName = "Creed",
            key = "aventus",
            family = "woody",
            subfamilies = listOf("fruity", "smoky"),
            topNotes = listOf("pineapple", "bergamot", "blackcurrant"),
            heartNotes = listOf("birch", "jasmine", "patchouli"),
            baseNotes = listOf("musk", "oakmoss", "ambergris"),
            presence = "powerful",
            duration = "long",
            recommendedSeason = listOf("spring", "fall"),
            occasion = listOf("office", "dates"),
            associatedPersonalities = listOf("confident", "sophisticated"),
            popularity = 95.0,
            year = 2010,
            imageURL = "https://example.com/aventus.jpg",
            gender = "masculine",
            price = "luxury",
            marketSegment = "niche",
            region = "european"
        )

        assertThat(perfume.id).isEqualTo("aventus")
        assertThat(perfume.brandName).isEqualTo("Creed")
        assertThat(perfume.subfamilies).hasSize(2)
        assertThat(perfume.topNotes).hasSize(3)
        assertThat(perfume.recommendedSeason).contains("spring")
        assertThat(perfume.popularity).isEqualTo(95.0)
        assertThat(perfume.year).isEqualTo(2010)
    }
}
