package com.jrlabs.baura.navigation

import android.content.Intent
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.jrlabs.baura.ui.navigation.DeepLinkHandler
import com.jrlabs.baura.ui.navigation.DeepLinkHandler.DeepLinkDestination
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for DeepLinkHandler
 * Uses Robolectric to provide Android framework classes (Uri)
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class DeepLinkHandlerTest {

    private lateinit var deepLinkHandler: DeepLinkHandler

    @Before
    fun setup() {
        deepLinkHandler = DeepLinkHandler()
    }

    // =====================
    // App scheme tests (baura://)
    // =====================

    @Test
    fun `parseUri with baura perfume deep link returns PerfumeDetail`() {
        val uri = Uri.parse("baura://perfume/abc123")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.PerfumeDetail::class.java)
        assertThat((result as DeepLinkDestination.PerfumeDetail).perfumeId).isEqualTo("abc123")
    }

    @Test
    fun `parseUri with baura profile deep link returns Profile`() {
        val uri = Uri.parse("baura://profile")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.Profile::class.java)
    }

    @Test
    fun `parseUri with baura explore deep link returns Explore`() {
        val uri = Uri.parse("baura://explore")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.Explore::class.java)
    }

    @Test
    fun `parseUri with baura library deep link returns Library`() {
        val uri = Uri.parse("baura://library")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.Library::class.java)
    }

    @Test
    fun `parseUri with baura settings deep link returns Settings`() {
        val uri = Uri.parse("baura://settings")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.Settings::class.java)
    }

    @Test
    fun `parseUri with baura wishlist deep link returns Wishlist`() {
        val uri = Uri.parse("baura://wishlist")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.Wishlist::class.java)
    }

    @Test
    fun `parseUri with baura tried deep link returns TriedPerfumes`() {
        val uri = Uri.parse("baura://tried")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.TriedPerfumes::class.java)
    }

    // =====================
    // Universal link tests (https://baura.app)
    // =====================

    @Test
    fun `parseUri with https perfume universal link returns PerfumeDetail`() {
        val uri = Uri.parse("https://baura.app/perfume/xyz789")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.PerfumeDetail::class.java)
        assertThat((result as DeepLinkDestination.PerfumeDetail).perfumeId).isEqualTo("xyz789")
    }

    @Test
    fun `parseUri with https www perfume universal link returns PerfumeDetail`() {
        val uri = Uri.parse("https://www.baura.app/perfume/test123")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.PerfumeDetail::class.java)
        assertThat((result as DeepLinkDestination.PerfumeDetail).perfumeId).isEqualTo("test123")
    }

    @Test
    fun `parseUri with https profile universal link returns Profile`() {
        val uri = Uri.parse("https://baura.app/profile")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.Profile::class.java)
    }

    // =====================
    // Edge cases
    // =====================

    @Test
    fun `parseUri with unknown path returns Unknown`() {
        val uri = Uri.parse("baura://unknown/path")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.Unknown::class.java)
    }

    @Test
    fun `parseUri with empty path returns Unknown`() {
        val uri = Uri.parse("baura://")

        val result = deepLinkHandler.parseUri(uri)

        assertThat(result).isInstanceOf(DeepLinkDestination.Unknown::class.java)
    }

    @Test
    fun `parseUri with perfume but no id returns Unknown`() {
        val uri = Uri.parse("baura://perfume")

        val result = deepLinkHandler.parseUri(uri)

        // Should return Unknown because perfumeId is missing
        assertThat(result).isInstanceOf(DeepLinkDestination.Unknown::class.java)
    }

    // =====================
    // Intent parsing tests
    // =====================

    @Test
    fun `parseIntent with null intent returns null`() {
        val result = deepLinkHandler.parseIntent(null)

        assertThat(result).isNull()
    }

    @Test
    fun `parseIntent with intent without data returns null`() {
        val intent = mockk<Intent>()
        every { intent.data } returns null

        val result = deepLinkHandler.parseIntent(intent)

        assertThat(result).isNull()
    }

    @Test
    fun `parseIntent with valid deep link returns destination`() {
        val intent = mockk<Intent>()
        every { intent.data } returns Uri.parse("baura://perfume/test123")

        val result = deepLinkHandler.parseIntent(intent)

        assertThat(result).isInstanceOf(DeepLinkDestination.PerfumeDetail::class.java)
        assertThat((result as DeepLinkDestination.PerfumeDetail).perfumeId).isEqualTo("test123")
    }

    // =====================
    // Deep link creation tests
    // =====================

    @Test
    fun `createPerfumeDeepLink creates correct URI`() {
        val uri = deepLinkHandler.createPerfumeDeepLink("perfume123")

        assertThat(uri.scheme).isEqualTo("https")
        assertThat(uri.host).isEqualTo("baura.app")
        assertThat(uri.pathSegments).containsExactly("perfume", "perfume123")
    }

    @Test
    fun `createAppDeepLink creates correct URI without id`() {
        val uri = deepLinkHandler.createAppDeepLink("profile")

        assertThat(uri.scheme).isEqualTo("baura")
        assertThat(uri.authority).isEqualTo("profile")
    }

    @Test
    fun `createAppDeepLink creates correct URI with id`() {
        val uri = deepLinkHandler.createAppDeepLink("perfume", "abc123")

        assertThat(uri.scheme).isEqualTo("baura")
        assertThat(uri.authority).isEqualTo("perfume")
        assertThat(uri.pathSegments).contains("abc123")
    }
}
