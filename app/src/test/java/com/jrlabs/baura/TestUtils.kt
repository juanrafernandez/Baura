package com.jrlabs.baura

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Test utilities and helpers
 */

/**
 * JUnit Rule that sets the main dispatcher to a test dispatcher.
 * Use this rule in ViewModel tests that use viewModelScope.
 *
 * Usage:
 * ```
 * @get:Rule
 * val mainDispatcherRule = MainDispatcherRule()
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * Test data factory for creating test objects
 */
object TestDataFactory {

    fun createTestPerfumeData(
        id: String = "test-perfume-id",
        name: String = "Test Perfume",
        brand: String = "test-brand"
    ): Map<String, Any?> = mapOf(
        "key" to id,
        "brand" to brand,
        "translations" to mapOf(
            "es" to mapOf(
                "name" to name,
                "description" to "Test description"
            )
        ),
        "gender" to "unisex",
        "family" to "woody",
        "rating" to 4.5,
        "releaseYear" to 2023L
    )

    fun createTestBrandData(
        key: String = "test-brand",
        name: String = "Test Brand"
    ): Map<String, Any?> = mapOf(
        "key" to key,
        "perfumeCount" to 100L,
        "country" to "France",
        "region" to "european",
        "translations" to mapOf(
            "es" to mapOf(
                "name" to name,
                "description" to "Test brand description"
            )
        )
    )

    fun createTestFamilyData(
        key: String = "woody",
        name: String = "Amaderado"
    ): Map<String, Any?> = mapOf(
        "key" to key,
        "perfumeCount" to 500L,
        "translations" to mapOf(
            "es" to mapOf(
                "name" to name,
                "description" to "Familia amaderada"
            )
        )
    )

    fun createTestNoteData(
        key: String = "rose",
        name: String = "Rosa",
        category: String = "heart"
    ): Map<String, Any?> = mapOf(
        "key" to key,
        "category" to category,
        "family" to "floral",
        "perfumeCount" to 250L,
        "translations" to mapOf(
            "es" to mapOf(
                "name" to name,
                "description" to "Nota floral clásica"
            )
        )
    )
}

/**
 * Extension function for easier assertion messages
 */
infix fun <T> T.shouldBe(expected: T) {
    if (this != expected) {
        throw AssertionError("Expected $expected but was $this")
    }
}

infix fun <T> T.shouldNotBe(unexpected: T) {
    if (this == unexpected) {
        throw AssertionError("Expected value to not be $unexpected")
    }
}

infix fun <T> Collection<T>.shouldContain(element: T) {
    if (!this.contains(element)) {
        throw AssertionError("Expected collection to contain $element but it didn't")
    }
}

infix fun <T> Collection<T>.shouldHaveSize(expectedSize: Int) {
    if (this.size != expectedSize) {
        throw AssertionError("Expected collection size to be $expectedSize but was ${this.size}")
    }
}

fun <T> Collection<T>.shouldBeEmpty() {
    if (this.isNotEmpty()) {
        throw AssertionError("Expected collection to be empty but had ${this.size} elements")
    }
}

fun <T> Collection<T>.shouldNotBeEmpty() {
    if (this.isEmpty()) {
        throw AssertionError("Expected collection to not be empty")
    }
}

fun String.shouldStartWith(prefix: String) {
    if (!this.startsWith(prefix)) {
        throw AssertionError("Expected '$this' to start with '$prefix'")
    }
}

fun String.shouldEndWith(suffix: String) {
    if (!this.endsWith(suffix)) {
        throw AssertionError("Expected '$this' to end with '$suffix'")
    }
}

fun <T> T?.shouldBeNull() {
    if (this != null) {
        throw AssertionError("Expected null but was $this")
    }
}

fun <T> T?.shouldNotBeNull(): T {
    if (this == null) {
        throw AssertionError("Expected non-null value but was null")
    }
    return this
}
