package com.jrlabs.baura.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jrlabs.baura.ui.theme.*

/**
 * AddTriedPerfumeScreen - Search and select perfume to add as tried
 * Equivalent to AddPerfumeStep1View.swift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTriedPerfumeScreen(
    onDismiss: () -> Unit,
    onPerfumeSelected: (String) -> Unit,
    viewModel: AddTriedPerfumeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8D9C0),  // Warm golden/beige at top (same as Library/Home)
                        Color(0xFFF2E9DC),  // Lighter beige
                        Color(0xFFFFFFFF)   // White
                    ),
                    startY = 0f,
                    endY = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Edge-to-edge: pad for status bar
                .navigationBarsPadding() // Edge-to-edge: pad for nav bar
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Añadir Perfume",
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        color = AppColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = AppColors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte@baura.app"))
                            putExtra(Intent.EXTRA_SUBJECT, "Sugerencia de perfume")
                            putExtra(Intent.EXTRA_TEXT, "Hola,\n\nMe gustaría sugerir añadir el siguiente perfume a la aplicación:\n\nNombre: \nMarca: \n\nGracias.")
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Sugerir perfume",
                            tint = AppColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                onClear = viewModel::clearSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(vertical = 12.dp)
            )

            // Content
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isSearching -> {
                        // Searching indicator
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = AppColors.brandAccent)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Buscando...",
                                style = AppTypography.bodyMedium,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                    uiState.showEmptySearch -> {
                        // Empty search state
                        EmptySearchState()
                    }
                    uiState.showNoResults -> {
                        // No results state
                        NoResultsState()
                    }
                    uiState.hasResults -> {
                        // Results list
                        SearchResultsList(
                            results = uiState.searchResults,
                            onPerfumeClick = { perfumeId ->
                                onPerfumeSelected(perfumeId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Buscar perfume o marca...",
                color = AppColors.textTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AppColors.textSecondary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar",
                        tint = AppColors.textSecondary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppColors.backgroundSecondary,
            unfocusedContainerColor = AppColors.backgroundSecondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = AppColors.brandAccent
        )
    )
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Busca tu perfume",
            fontFamily = FontFamily.Serif,
            fontSize = 24.sp,
            color = AppColors.textPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Escribe el nombre del perfume o la marca en el buscador.",
            style = AppTypography.bodyMedium.copy(
                fontWeight = FontWeight.Light
            ),
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Por ejemplo: \"Sauvage\", \"Dior\", \"Acqua di Gio\"",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = 14.sp,
            color = AppColors.textSecondary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@Composable
private fun NoResultsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No encontramos resultados",
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            color = AppColors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Intenta con otro nombre o marca",
            style = AppTypography.bodyMedium.copy(
                fontWeight = FontWeight.Light
            ),
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@Composable
private fun SearchResultsList(
    results: List<PerfumeSearchResult>,
    onPerfumeClick: (String) -> Unit
) {
    Column {
        // Results count header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.backgroundSecondary
        ) {
            Text(
                text = "${results.size} resultado${if (results.size == 1) "" else "s"} encontrado${if (results.size == 1) "" else "s"}",
                style = AppTypography.labelSmall,
                color = AppColors.textSecondary,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal, vertical = 8.dp)
            )
        }

        // Results list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = results,
                key = { it.perfumeId }
            ) { result ->
                PerfumeSearchResultRow(
                    result = result,
                    onClick = {
                        if (!result.isAlreadyTried) {
                            onPerfumeClick(result.perfumeId)
                        }
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = AppColors.dividerPrimary
                )
            }
        }
    }
}

@Composable
private fun PerfumeSearchResultRow(
    result: PerfumeSearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (result.isAlreadyTried) AppColors.feedbackSuccess.copy(alpha = 0.08f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Perfume image
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            if (result.imageUrl != null) {
                AsyncImage(
                    model = result.imageUrl,
                    contentDescription = result.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.backgroundTertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.name.take(1).uppercase(),
                        style = AppTypography.titleMedium,
                        color = AppColors.textTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Perfume info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name,
                style = AppTypography.titleSmall,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.brand,
                style = AppTypography.bodySmall,
                color = AppColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Status indicator
        if (result.isAlreadyTried) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = AppColors.feedbackSuccess
                )
                Text(
                    text = "Añadido",
                    style = AppTypography.labelSmall,
                    color = AppColors.feedbackSuccess
                )
            }
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppColors.textSecondary
            )
        }
    }
}
