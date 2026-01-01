package com.jrlabs.baura.ui.screens.library.evaluation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrlabs.baura.R
import com.jrlabs.baura.ui.theme.*

/**
 * Note type for the tabs
 */
enum class NoteType(val displayName: String, val maxNotes: Int) {
    TOP("Salida", 3),
    HEART("Corazón", 3),
    BASE("Fondo", 3)
}

/**
 * Bottom sheet for adding custom notes that aren't in the perfume's list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNotesBottomSheet(
    onDismiss: () -> Unit,
    addedTopNotes: List<String>,
    addedHeartNotes: List<String>,
    addedBaseNotes: List<String>,
    onAddNote: (NoteType, String) -> Unit,
    onRemoveNote: (NoteType, String) -> Unit,
    availableNotes: List<String> = emptyList() // Could be populated from a notes database
) {
    var selectedTab by remember { mutableStateOf(NoteType.TOP) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val currentNotes = when (selectedTab) {
        NoteType.TOP -> addedTopNotes
        NoteType.HEART -> addedHeartNotes
        NoteType.BASE -> addedBaseNotes
    }

    // Filter available notes based on search query
    val filteredNotes = remember(searchQuery, availableNotes) {
        if (searchQuery.isBlank()) emptyList()
        else availableNotes.filter {
            it.contains(searchQuery, ignoreCase = true) &&
            !currentNotes.contains(it)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.size(width = 36.dp, height = 4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = AppColors.borderPrimary
                ) {}
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header with title and done button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(60.dp)) // Balance for the button

                Text(
                    text = "Añadir Notas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary
                )

                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.backgroundSecondary
                ) {
                    Text(
                        text = "Hecho",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoteType.entries.forEach { noteType ->
                    NoteTypeTab(
                        noteType = noteType,
                        isSelected = selectedTab == noteType,
                        onClick = { selectedTab = noteType },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Counter
            Text(
                text = "${currentNotes.size}/${selectedTab.maxNotes} notas de ${selectedTab.displayName.lowercase()}",
                fontSize = 14.sp,
                color = AppColors.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = {
                    Text(
                        text = "Buscar nota...",
                        color = AppColors.textTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = AppColors.textTertiary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = AppColors.brandAccent.copy(alpha = 0.5f),
                    focusedBorderColor = AppColors.brandAccent,
                    unfocusedContainerColor = AppColors.backgroundSecondary.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Add custom note if it's not empty and under limit
                        if (searchQuery.isNotBlank() && currentNotes.size < selectedTab.maxNotes) {
                            onAddNote(selectedTab, searchQuery.trim())
                            searchQuery = ""
                        }
                        focusManager.clearFocus()
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Added notes chips
            if (currentNotes.isNotEmpty()) {
                AddedNotesChips(
                    notes = currentNotes,
                    onRemove = { note -> onRemoveNote(selectedTab, note) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search results or empty state
            if (filteredNotes.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(filteredNotes) { note ->
                        SearchResultItem(
                            note = note,
                            onClick = {
                                if (currentNotes.size < selectedTab.maxNotes) {
                                    onAddNote(selectedTab, note)
                                    searchQuery = ""
                                }
                            }
                        )
                    }
                }
            } else if (currentNotes.isEmpty()) {
                // Empty state
                EmptyNotesState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NoteTypeTab(
    noteType: NoteType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (noteType) {
        NoteType.TOP -> R.drawable.ic_arrow_up
        NoteType.HEART -> R.drawable.ic_heart_outline
        NoteType.BASE -> R.drawable.ic_arrow_down
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) AppColors.brandAccent else Color.Transparent,
        border = if (!isSelected) BorderStroke(1.dp, AppColors.borderPrimary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color.White else AppColors.textSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = noteType.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else AppColors.textSecondary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddedNotesChips(
    notes: List<String>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        notes.forEach { note ->
            InputChip(
                selected = true,
                onClick = { onRemove(note) },
                label = {
                    Text(
                        text = note,
                        fontSize = 14.sp
                    )
                },
                trailingIcon = {
                    Text(
                        text = "×",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = AppColors.brandAccent.copy(alpha = 0.15f),
                    selectedLabelColor = AppColors.brandAccent,
                    selectedTrailingIconColor = AppColors.brandAccent
                ),
                border = null
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    note: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Text(
            text = note,
            fontSize = 16.sp,
            color = AppColors.textPrimary,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
    HorizontalDivider(color = AppColors.dividerPrimary)
}

@Composable
private fun EmptyNotesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Leaf icon placeholder
        Icon(
            painter = painterResource(id = R.drawable.ic_leaf),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = AppColors.textTertiary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aún no has añadido notas",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Usa el buscador para encontrar notas que hayas percibido y no estén en la lista del perfume",
            fontSize = 14.sp,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
