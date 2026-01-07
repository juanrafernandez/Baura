package com.jrlabs.baura.data.model.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for enum classes
 */
class EnumsTest {

    // MARK: - Gender Tests

    @Test
    fun `Gender fromValue returns correct enum`() {
        assertThat(Gender.fromValue("masculine")).isEqualTo(Gender.MASCULINE)
        assertThat(Gender.fromValue("feminine")).isEqualTo(Gender.FEMININE)
        assertThat(Gender.fromValue("unisex")).isEqualTo(Gender.UNISEX)
    }

    @Test
    fun `Gender fromValue is case insensitive`() {
        assertThat(Gender.fromValue("MASCULINE")).isEqualTo(Gender.MASCULINE)
        assertThat(Gender.fromValue("Feminine")).isEqualTo(Gender.FEMININE)
        assertThat(Gender.fromValue("UNISEX")).isEqualTo(Gender.UNISEX)
    }

    @Test
    fun `Gender fromValue returns null for invalid value`() {
        assertThat(Gender.fromValue("invalid")).isNull()
        assertThat(Gender.fromValue("")).isNull()
    }

    @Test
    fun `Gender displayName returns correct language`() {
        assertThat(Gender.MASCULINE.displayName("es")).isEqualTo("Masculino")
        assertThat(Gender.MASCULINE.displayName("en")).isEqualTo("Masculine")
        assertThat(Gender.FEMININE.displayName("es")).isEqualTo("Femenino")
        assertThat(Gender.UNISEX.displayName("es")).isEqualTo("Unisex")
    }

    @Test
    fun `Gender displayName defaults to Spanish`() {
        assertThat(Gender.MASCULINE.displayName()).isEqualTo("Masculino")
    }

    @Test
    fun `Gender has correct values`() {
        assertThat(Gender.MASCULINE.value).isEqualTo("masculine")
        assertThat(Gender.FEMININE.value).isEqualTo("feminine")
        assertThat(Gender.UNISEX.value).isEqualTo("unisex")
    }

    // MARK: - Season Tests

    @Test
    fun `Season fromValue returns correct enum`() {
        assertThat(Season.fromValue("spring")).isEqualTo(Season.SPRING)
        assertThat(Season.fromValue("summer")).isEqualTo(Season.SUMMER)
        assertThat(Season.fromValue("fall")).isEqualTo(Season.FALL)
        assertThat(Season.fromValue("winter")).isEqualTo(Season.WINTER)
    }

    @Test
    fun `Season fromValue is case insensitive`() {
        assertThat(Season.fromValue("SPRING")).isEqualTo(Season.SPRING)
        assertThat(Season.fromValue("Summer")).isEqualTo(Season.SUMMER)
    }

    @Test
    fun `Season displayName returns correct language`() {
        assertThat(Season.SPRING.displayName("es")).isEqualTo("Primavera")
        assertThat(Season.SPRING.displayName("en")).isEqualTo("Spring")
        assertThat(Season.SUMMER.displayName("es")).isEqualTo("Verano")
        assertThat(Season.FALL.displayName("es")).isEqualTo("Otoño")
        assertThat(Season.WINTER.displayName("es")).isEqualTo("Invierno")
    }

    @Test
    fun `Season has all four seasons`() {
        assertThat(Season.entries).hasSize(4)
    }

    // MARK: - Duration Tests

    @Test
    fun `Duration fromValue returns correct enum`() {
        assertThat(Duration.fromValue("short")).isEqualTo(Duration.SHORT)
        assertThat(Duration.fromValue("moderate")).isEqualTo(Duration.MODERATE)
        assertThat(Duration.fromValue("long")).isEqualTo(Duration.LONG)
        assertThat(Duration.fromValue("veryLong")).isEqualTo(Duration.VERY_LONG)
    }

    @Test
    fun `Duration displayName returns correct language`() {
        assertThat(Duration.SHORT.displayName("es")).isEqualTo("Corta")
        assertThat(Duration.SHORT.displayName("en")).isEqualTo("Short")
        assertThat(Duration.MODERATE.displayName("es")).isEqualTo("Moderada")
        assertThat(Duration.LONG.displayName("es")).isEqualTo("Larga")
        assertThat(Duration.LONG.displayName("en")).isEqualTo("Long")
        assertThat(Duration.VERY_LONG.displayName("es")).isEqualTo("Muy Larga")
    }

    @Test
    fun `Duration has correct values`() {
        assertThat(Duration.entries).hasSize(4)
        assertThat(Duration.VERY_LONG.value).isEqualTo("veryLong")
    }

    // MARK: - Presence Tests

    @Test
    fun `Presence fromValue returns correct enum`() {
        assertThat(Presence.fromValue("intimate")).isEqualTo(Presence.INTIMATE)
        assertThat(Presence.fromValue("moderate")).isEqualTo(Presence.MODERATE)
        assertThat(Presence.fromValue("notable")).isEqualTo(Presence.NOTABLE)
        assertThat(Presence.fromValue("powerful")).isEqualTo(Presence.POWERFUL)
    }

    @Test
    fun `Presence displayName returns correct language`() {
        assertThat(Presence.INTIMATE.displayName("es")).isEqualTo("Íntimo")
        assertThat(Presence.INTIMATE.displayName("en")).isEqualTo("Intimate")
        assertThat(Presence.POWERFUL.displayName("es")).isEqualTo("Potente")
        assertThat(Presence.POWERFUL.displayName("en")).isEqualTo("Powerful")
    }

    @Test
    fun `Presence has correct number of entries`() {
        assertThat(Presence.entries).hasSize(4)
    }

    // MARK: - Price Tests

    @Test
    fun `Price fromValue returns correct enum`() {
        assertThat(Price.fromValue("ultraBudget")).isEqualTo(Price.ULTRA_BUDGET)
        assertThat(Price.fromValue("budget")).isEqualTo(Price.BUDGET)
        assertThat(Price.fromValue("accessible")).isEqualTo(Price.ACCESSIBLE)
        assertThat(Price.fromValue("premium")).isEqualTo(Price.PREMIUM)
        assertThat(Price.fromValue("luxury")).isEqualTo(Price.LUXURY)
    }

    @Test
    fun `Price displayName returns correct language`() {
        assertThat(Price.ULTRA_BUDGET.displayName("es")).isEqualTo("Ultra Económico")
        assertThat(Price.BUDGET.displayName("es")).isEqualTo("Económico")
        assertThat(Price.LUXURY.displayName("es")).isEqualTo("Lujo")
        assertThat(Price.LUXURY.displayName("en")).isEqualTo("Luxury")
    }

    @Test
    fun `Price has correct number of entries`() {
        assertThat(Price.entries).hasSize(5)
    }

    // MARK: - ProfileType Tests

    @Test
    fun `ProfileType fromValue returns correct enum`() {
        assertThat(ProfileType.fromValue("personal")).isEqualTo(ProfileType.PERSONAL)
        assertThat(ProfileType.fromValue("gift")).isEqualTo(ProfileType.GIFT)
    }

    @Test
    fun `ProfileType displayName returns correct language`() {
        assertThat(ProfileType.PERSONAL.displayName("es")).isEqualTo("Personal")
        assertThat(ProfileType.PERSONAL.displayName("en")).isEqualTo("Personal")
        assertThat(ProfileType.GIFT.displayName("es")).isEqualTo("Regalo")
        assertThat(ProfileType.GIFT.displayName("en")).isEqualTo("Gift")
    }

    @Test
    fun `ProfileType has correct number of entries`() {
        assertThat(ProfileType.entries).hasSize(2)
    }

    // MARK: - ExperienceLevel Tests

    @Test
    fun `ExperienceLevel fromValue returns correct enum`() {
        assertThat(ExperienceLevel.fromValue("beginner")).isEqualTo(ExperienceLevel.BEGINNER)
        assertThat(ExperienceLevel.fromValue("intermediate")).isEqualTo(ExperienceLevel.INTERMEDIATE)
        assertThat(ExperienceLevel.fromValue("advanced")).isEqualTo(ExperienceLevel.ADVANCED)
        assertThat(ExperienceLevel.fromValue("expert")).isEqualTo(ExperienceLevel.EXPERT)
    }

    @Test
    fun `ExperienceLevel displayName returns correct language`() {
        assertThat(ExperienceLevel.BEGINNER.displayName("es")).isEqualTo("Principiante")
        assertThat(ExperienceLevel.BEGINNER.displayName("en")).isEqualTo("Beginner")
        assertThat(ExperienceLevel.INTERMEDIATE.displayName("es")).isEqualTo("Intermedio")
        assertThat(ExperienceLevel.ADVANCED.displayName("es")).isEqualTo("Avanzado")
        assertThat(ExperienceLevel.EXPERT.displayName("es")).isEqualTo("Experto")
        assertThat(ExperienceLevel.EXPERT.displayName("en")).isEqualTo("Expert")
    }

    @Test
    fun `ExperienceLevel has correct number of entries`() {
        assertThat(ExperienceLevel.entries).hasSize(4)
    }

    // MARK: - Generic Enum Tests

    @Test
    fun `All enums return null for empty string`() {
        assertThat(Gender.fromValue("")).isNull()
        assertThat(Season.fromValue("")).isNull()
        assertThat(Duration.fromValue("")).isNull()
        assertThat(Presence.fromValue("")).isNull()
        assertThat(Price.fromValue("")).isNull()
        assertThat(ProfileType.fromValue("")).isNull()
        assertThat(ExperienceLevel.fromValue("")).isNull()
    }

    @Test
    fun `All enums return null for invalid values`() {
        assertThat(Gender.fromValue("unknown")).isNull()
        assertThat(Season.fromValue("autumn")).isNull() // Should be "fall"
        assertThat(Duration.fromValue("eternal")).isNull()
        assertThat(Presence.fromValue("strong")).isNull()
        assertThat(Price.fromValue("expensive")).isNull()
    }
}
