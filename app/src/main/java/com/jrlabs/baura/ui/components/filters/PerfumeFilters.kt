package com.jrlabs.baura.ui.components.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrlabs.baura.ui.theme.AppColors

/**
 * Filter accordion types - shared between Explore and Library screens
 */
enum class FilterAccordion {
    GENDER, FAMILY, SEASON, PRESENCE, DURATION, PRICE, ORIGIN, SEGMENT, POPULARITY
}

/**
 * Sort order options
 */
enum class SortOrder {
    NONE, NAME_ASC, NAME_DESC, POPULARITY_ASC, POPULARITY_DESC, RATING_ASC, RATING_DESC
}

/**
 * Filter data with options and display names
 */
data class FilterDefinition(
    val key: String,
    val title: String,
    val accordion: FilterAccordion,
    val options: List<String>,
    val displayNames: Map<String, String>
)

/**
 * Standard filter definitions used across the app
 */
object FilterDefinitions {
    val gender = FilterDefinition(
        key = "Género",
        title = "Género",
        accordion = FilterAccordion.GENDER,
        options = listOf("male", "female", "unisex"),
        displayNames = mapOf(
            "male" to "Masculino",
            "female" to "Femenino",
            "unisex" to "Unisex"
        )
    )

    val family = FilterDefinition(
        key = "Familia Olfativa",
        title = "Familia Olfativa",
        accordion = FilterAccordion.FAMILY,
        options = listOf("woody", "oriental", "floral", "fresh", "citrus", "aromatic", "fougere", "chypre", "aquatic", "gourmand"),
        displayNames = mapOf(
            "woody" to "Amaderado",
            "oriental" to "Oriental",
            "floral" to "Floral",
            "fresh" to "Fresco",
            "citrus" to "Cítrico",
            "aromatic" to "Aromático",
            "fougere" to "Fougère",
            "chypre" to "Chipre",
            "aquatic" to "Acuático",
            "gourmand" to "Gourmand"
        )
    )

    val season = FilterDefinition(
        key = "Temporada",
        title = "Temporada Recomendada",
        accordion = FilterAccordion.SEASON,
        options = listOf("spring", "summer", "fall", "winter"),
        displayNames = mapOf(
            "spring" to "Primavera",
            "summer" to "Verano",
            "fall" to "Otoño",
            "winter" to "Invierno"
        )
    )

    val presence = FilterDefinition(
        key = "Proyección",
        title = "Proyección",
        accordion = FilterAccordion.PRESENCE,
        options = listOf("intimate", "moderate", "notable", "powerful", "enormous"),
        displayNames = mapOf(
            "intimate" to "Íntima",
            "moderate" to "Moderada",
            "notable" to "Notable",
            "powerful" to "Potente",
            "enormous" to "Enorme"
        )
    )

    val duration = FilterDefinition(
        key = "Duración",
        title = "Duración",
        accordion = FilterAccordion.DURATION,
        options = listOf("very_short", "short", "moderate", "long_lasting", "eternal"),
        displayNames = mapOf(
            "very_short" to "Muy corta",
            "short" to "Corta",
            "moderate" to "Moderada",
            "long_lasting" to "Duradera",
            "eternal" to "Eterna"
        )
    )

    val price = FilterDefinition(
        key = "Precio",
        title = "Precio",
        accordion = FilterAccordion.PRICE,
        options = listOf("cheap", "affordable", "mid_range", "premium", "luxury"),
        displayNames = mapOf(
            "cheap" to "Económico",
            "affordable" to "Accesible",
            "mid_range" to "Gama Media",
            "premium" to "Premium",
            "luxury" to "Lujo"
        )
    )

    val origin = FilterDefinition(
        key = "Origen",
        title = "Origen",
        accordion = FilterAccordion.ORIGIN,
        options = listOf("france", "italy", "usa", "spain", "uk", "germany", "middle_east", "other"),
        displayNames = mapOf(
            "france" to "Francia",
            "italy" to "Italia",
            "usa" to "Estados Unidos",
            "spain" to "España",
            "uk" to "Reino Unido",
            "germany" to "Alemania",
            "middle_east" to "Medio Oriente",
            "other" to "Otro"
        )
    )

    val segment = FilterDefinition(
        key = "Segmento",
        title = "Segmento de Mercado",
        accordion = FilterAccordion.SEGMENT,
        options = listOf("designer", "niche", "indie", "celebrity", "classic"),
        displayNames = mapOf(
            "designer" to "Diseñador",
            "niche" to "Nicho",
            "indie" to "Indie",
            "celebrity" to "Celebridad",
            "classic" to "Clásico"
        )
    )

    val popularity = FilterDefinition(
        key = "Popularidad",
        title = "Popularidad",
        accordion = FilterAccordion.POPULARITY,
        options = listOf("very_popular", "popular", "moderate", "niche", "rare"),
        displayNames = mapOf(
            "very_popular" to "Muy popular",
            "popular" to "Popular",
            "moderate" to "Moderada",
            "niche" to "De nicho",
            "rare" to "Rara"
        )
    )

    val all = listOf(gender, family, season, presence, duration, price, origin, segment, popularity)
}

/**
 * Filter Toggle Button - "Mostrar/Ocultar Filtros"
 */
@Composable
fun FilterToggleButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isExpanded) "Ocultar Filtros" else "Mostrar Filtros",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF007AFF)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color(0xFF007AFF),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Complete filter section with all accordions
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerfumeFilterSection(
    selectedFilters: Map<String, List<String>>,
    expandedAccordions: Set<FilterAccordion>,
    onToggleFilter: (String, String) -> Unit,
    onToggleAccordion: (FilterAccordion, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    filters: List<FilterDefinition> = FilterDefinitions.all
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        filters.forEach { filter ->
            FilterAccordionItem(
                title = filter.title,
                accordion = filter.accordion,
                options = filter.options,
                displayNames = filter.displayNames,
                selectedOptions = selectedFilters[filter.key] ?: emptyList(),
                isExpanded = filter.accordion in expandedAccordions,
                onToggleExpanded = { onToggleAccordion(filter.accordion, it) },
                onToggleOption = { onToggleFilter(filter.key, it) }
            )
        }
    }
}

/**
 * Single filter accordion item
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterAccordionItem(
    title: String,
    accordion: FilterAccordion,
    options: List<String>,
    displayNames: Map<String, String>,
    selectedOptions: List<String>,
    isExpanded: Boolean,
    onToggleExpanded: (Boolean) -> Unit,
    onToggleOption: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpanded(!isExpanded) }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.textPrimary
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = AppColors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option in selectedOptions
                    FilterChip(
                        text = displayNames[option] ?: option,
                        isSelected = isSelected,
                        onClick = { onToggleOption(option) }
                    )
                }
            }
        }
    }
}

/**
 * Filter chip component
 */
@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(17.dp))
            .background(
                if (isSelected) AppColors.brandAccent
                else Color.White.copy(alpha = 0.8f)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) Color.White else AppColors.textPrimary
        )
    }
}
