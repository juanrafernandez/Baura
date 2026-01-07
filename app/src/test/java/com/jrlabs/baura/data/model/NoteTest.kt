package com.jrlabs.baura.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Note model
 */
class NoteTest {

    @Test
    fun `getLocalizedName returns name when no translations`() {
        val note = Note(
            id = "1",
            name = "Rose",
            key = "rose"
        )

        val result = note.getLocalizedName("es")

        assertThat(result).isEqualTo("Rose")
    }

    @Test
    fun `getLocalizedName returns translated name when available`() {
        val note = Note(
            id = "1",
            name = "Rose",
            key = "rose",
            translations = mapOf(
                "es" to NoteTranslation(name = "Rosa"),
                "en" to NoteTranslation(name = "Rose")
            )
        )

        val result = note.getLocalizedName("es")

        assertThat(result).isEqualTo("Rosa")
    }

    @Test
    fun `getLocalizedName falls back to name when language not available`() {
        val note = Note(
            id = "1",
            name = "Rose",
            key = "rose",
            translations = mapOf(
                "en" to NoteTranslation(name = "Rose")
            )
        )

        val result = note.getLocalizedName("fr")

        assertThat(result).isEqualTo("Rose")
    }

    @Test
    fun `getLocalizedDescription returns description when no translations`() {
        val note = Note(
            id = "1",
            name = "Rose",
            key = "rose",
            description = "A floral note"
        )

        val result = note.getLocalizedDescription("es")

        assertThat(result).isEqualTo("A floral note")
    }

    @Test
    fun `getLocalizedDescription returns translated description when available`() {
        val note = Note(
            id = "1",
            name = "Rose",
            key = "rose",
            description = "A floral note",
            translations = mapOf(
                "es" to NoteTranslation(
                    name = "Rosa",
                    description = "Una nota floral"
                )
            )
        )

        val result = note.getLocalizedDescription("es")

        assertThat(result).isEqualTo("Una nota floral")
    }

    @Test
    fun `getLocalizedDescription returns null when no description`() {
        val note = Note(
            id = "1",
            name = "Rose",
            key = "rose"
        )

        val result = note.getLocalizedDescription("es")

        assertThat(result).isNull()
    }

    @Test
    fun `note default values are correct`() {
        val note = Note()

        assertThat(note.id).isEmpty()
        assertThat(note.name).isEmpty()
        assertThat(note.key).isEmpty()
        assertThat(note.category).isNull()
        assertThat(note.family).isNull()
        assertThat(note.description).isNull()
        assertThat(note.imageURL).isNull()
        assertThat(note.perfumeCount).isEqualTo(0)
        assertThat(note.translations).isNull()
    }
}
