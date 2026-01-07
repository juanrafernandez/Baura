package com.jrlabs.baura.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Brand model
 */
class BrandTest {

    @Test
    fun `getLocalizedName returns name when no translations`() {
        val brand = Brand(
            id = "1",
            name = "Dior",
            key = "dior"
        )

        val result = brand.getLocalizedName("es")

        assertThat(result).isEqualTo("Dior")
    }

    @Test
    fun `getLocalizedName returns translated name when available`() {
        val brand = Brand(
            id = "1",
            name = "Dior",
            key = "dior",
            translations = mapOf(
                "es" to BrandTranslation(name = "Christian Dior"),
                "en" to BrandTranslation(name = "Dior")
            )
        )

        val result = brand.getLocalizedName("es")

        assertThat(result).isEqualTo("Christian Dior")
    }

    @Test
    fun `getLocalizedName falls back to name when language not available`() {
        val brand = Brand(
            id = "1",
            name = "Dior",
            key = "dior",
            translations = mapOf(
                "en" to BrandTranslation(name = "Dior English")
            )
        )

        val result = brand.getLocalizedName("fr")

        assertThat(result).isEqualTo("Dior")
    }

    @Test
    fun `getLocalizedDescription returns description when no translations`() {
        val brand = Brand(
            id = "1",
            name = "Dior",
            key = "dior",
            description = "French luxury brand"
        )

        val result = brand.getLocalizedDescription("es")

        assertThat(result).isEqualTo("French luxury brand")
    }

    @Test
    fun `getLocalizedDescription returns translated description when available`() {
        val brand = Brand(
            id = "1",
            name = "Dior",
            key = "dior",
            description = "French luxury brand",
            translations = mapOf(
                "es" to BrandTranslation(
                    name = "Christian Dior",
                    description = "Marca de lujo francesa"
                )
            )
        )

        val result = brand.getLocalizedDescription("es")

        assertThat(result).isEqualTo("Marca de lujo francesa")
    }

    @Test
    fun `getLocalizedDescription returns null when no description`() {
        val brand = Brand(
            id = "1",
            name = "Dior",
            key = "dior"
        )

        val result = brand.getLocalizedDescription("es")

        assertThat(result).isNull()
    }

    @Test
    fun `brand default values are correct`() {
        val brand = Brand()

        assertThat(brand.id).isEmpty()
        assertThat(brand.name).isEmpty()
        assertThat(brand.key).isEmpty()
        assertThat(brand.country).isNull()
        assertThat(brand.region).isEqualTo("european")
        assertThat(brand.description).isNull()
        assertThat(brand.imageURL).isNull()
        assertThat(brand.logoURL).isNull()
        assertThat(brand.marketSegment).isNull()
        assertThat(brand.perfumeCount).isEqualTo(0)
        assertThat(brand.perfumist).isEmpty()
        assertThat(brand.translations).isNull()
    }

    @Test
    fun `fromFirestore creates brand with basic data`() {
        val data = mapOf<String, Any?>(
            "key" to "chanel",
            "perfumeCount" to 150L,
            "country" to "France",
            "region" to "european",
            "translations" to mapOf(
                "es" to mapOf(
                    "name" to "Chanel",
                    "description" to "Casa de moda francesa"
                )
            )
        )

        val brand = Brand.fromFirestore("chanel-id", data, "es")

        assertThat(brand.id).isEqualTo("chanel-id")
        assertThat(brand.key).isEqualTo("chanel")
        assertThat(brand.name).isEqualTo("Chanel")
        assertThat(brand.description).isEqualTo("Casa de moda francesa")
        assertThat(brand.perfumeCount).isEqualTo(150)
        assertThat(brand.country).isEqualTo("France")
        assertThat(brand.region).isEqualTo("european")
    }

    @Test
    fun `fromFirestore falls back to Spanish when language not found`() {
        val data = mapOf<String, Any?>(
            "key" to "guerlain",
            "translations" to mapOf(
                "es" to mapOf(
                    "name" to "Guerlain ES",
                    "description" to "Descripción en español"
                )
            )
        )

        val brand = Brand.fromFirestore("guerlain-id", data, "fr")

        // Should fall back to Spanish
        assertThat(brand.name).isEqualTo("Guerlain ES")
        assertThat(brand.description).isEqualTo("Descripción en español")
    }

    @Test
    fun `fromFirestore uses key as name when no translations`() {
        val data = mapOf<String, Any?>(
            "key" to "hermès"
        )

        val brand = Brand.fromFirestore("hermes-id", data, "es")

        assertThat(brand.name).isEqualTo("hermès")
    }

    @Test
    fun `fromFirestore handles missing optional fields`() {
        val data = mapOf<String, Any?>(
            "key" to "test-brand"
        )

        val brand = Brand.fromFirestore("test-id", data)

        assertThat(brand.imageURL).isNull()
        assertThat(brand.logoURL).isNull()
        assertThat(brand.country).isNull()
        assertThat(brand.marketSegment).isNull()
        assertThat(brand.perfumeCount).isEqualTo(0)
        assertThat(brand.perfumist).isEmpty()
    }

    @Test
    fun `fromFirestore parses perfumist list correctly`() {
        val data = mapOf<String, Any?>(
            "key" to "dior",
            "perfumist" to listOf("François Demachy", "Francis Kurkdjian")
        )

        val brand = Brand.fromFirestore("dior-id", data)

        assertThat(brand.perfumist).containsExactly("François Demachy", "Francis Kurkdjian")
    }
}
