package com.jrlabs.baura.navigation

import com.google.common.truth.Truth.assertThat
import com.jrlabs.baura.ui.navigation.Screen
import org.junit.Test

/**
 * Unit tests for Screen navigation routes
 */
class ScreenRoutesTest {

    // =====================
    // Static route tests
    // =====================

    @Test
    fun `Splash route is correct`() {
        assertThat(Screen.Splash.route).isEqualTo("splash")
    }

    @Test
    fun `Onboarding route is correct`() {
        assertThat(Screen.Onboarding.route).isEqualTo("onboarding")
    }

    @Test
    fun `Login route is correct`() {
        assertThat(Screen.Login.route).isEqualTo("login")
    }

    @Test
    fun `Register route is correct`() {
        assertThat(Screen.Register.route).isEqualTo("register")
    }

    @Test
    fun `ForgotPassword route is correct`() {
        assertThat(Screen.ForgotPassword.route).isEqualTo("forgot_password")
    }

    @Test
    fun `Main route is correct`() {
        assertThat(Screen.Main.route).isEqualTo("main")
    }

    @Test
    fun `ProfileManagement route is correct`() {
        assertThat(Screen.ProfileManagement.route).isEqualTo("profile_management")
    }

    @Test
    fun `GiftProfileManagement route is correct`() {
        assertThat(Screen.GiftProfileManagement.route).isEqualTo("gift_profile_management")
    }

    @Test
    fun `EditProfile route is correct`() {
        assertThat(Screen.EditProfile.route).isEqualTo("edit_profile")
    }

    @Test
    fun `CacheStats route is correct`() {
        assertThat(Screen.CacheStats.route).isEqualTo("cache_stats")
    }

    @Test
    fun `AllTriedPerfumes route is correct`() {
        assertThat(Screen.AllTriedPerfumes.route).isEqualTo("all_tried_perfumes")
    }

    @Test
    fun `AllWishlist route is correct`() {
        assertThat(Screen.AllWishlist.route).isEqualTo("all_wishlist")
    }

    // =====================
    // Dynamic route tests
    // =====================

    @Test
    fun `PerfumeDetail route template is correct`() {
        assertThat(Screen.PerfumeDetail.route).isEqualTo("perfume_detail/{perfumeId}")
    }

    @Test
    fun `PerfumeDetail createRoute generates correct route`() {
        val route = Screen.PerfumeDetail.createRoute("abc123")

        assertThat(route).isEqualTo("perfume_detail/abc123")
    }

    @Test
    fun `PerfumeDetail createRoute with special characters`() {
        val route = Screen.PerfumeDetail.createRoute("perfume-id_123")

        assertThat(route).isEqualTo("perfume_detail/perfume-id_123")
    }

    @Test
    fun `PerfumeEvaluation route template is correct`() {
        assertThat(Screen.PerfumeEvaluation.route).isEqualTo("perfume_evaluation/{perfumeId}?isEditing={isEditing}")
    }

    @Test
    fun `PerfumeEvaluation createRoute with isEditing false`() {
        val route = Screen.PerfumeEvaluation.createRoute("xyz789", isEditing = false)

        assertThat(route).isEqualTo("perfume_evaluation/xyz789?isEditing=false")
    }

    @Test
    fun `PerfumeEvaluation createRoute with isEditing true`() {
        val route = Screen.PerfumeEvaluation.createRoute("xyz789", isEditing = true)

        assertThat(route).isEqualTo("perfume_evaluation/xyz789?isEditing=true")
    }

    @Test
    fun `PerfumeEvaluation createRoute default isEditing is false`() {
        val route = Screen.PerfumeEvaluation.createRoute("test123")

        assertThat(route).isEqualTo("perfume_evaluation/test123?isEditing=false")
    }

    // =====================
    // Route extraction tests
    // =====================

    @Test
    fun `routes are unique`() {
        val routes = listOf(
            Screen.Splash.route,
            Screen.Onboarding.route,
            Screen.Login.route,
            Screen.Register.route,
            Screen.ForgotPassword.route,
            Screen.Main.route,
            Screen.PerfumeDetail.route,
            Screen.GiftRecommendation.route,
            Screen.ProfileManagement.route,
            Screen.GiftProfileManagement.route,
            Screen.EditProfile.route,
            Screen.CacheStats.route,
            Screen.PerfumeEvaluation.route,
            Screen.AllTriedPerfumes.route,
            Screen.AllWishlist.route
        )

        assertThat(routes.distinct().size).isEqualTo(routes.size)
    }
}
