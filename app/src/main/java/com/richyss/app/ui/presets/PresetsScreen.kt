package com.richyss.app.ui.presets

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.richyss.app.core.AppContainer
import com.richyss.app.core.LocalTranslations
import com.richyss.app.core.Translations
import com.richyss.app.data.models.PresetCategory
import com.richyss.app.data.models.PresetItem
import com.richyss.app.data.models.PresetSource
import com.richyss.app.data.models.ProfileJson
import com.richyss.app.ui.components.DiscordActivityCard
import com.richyss.app.ui.components.EmptyState
import com.richyss.app.ui.components.ProfileIcon
import com.richyss.app.ui.components.SectionTitle
import com.richyss.app.ui.components.StatusPill
import com.richyss.app.ui.components.Tone
import com.richyss.app.ui.theme.RichyssAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsScreen(
    container: AppContainer,
    onEditCopy: (String) -> Unit
) {
    val viewModel: PresetsViewModel = viewModel(factory = PresetsViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val t = LocalTranslations.current
    val context = LocalContext.current
    var selected by remember { mutableStateOf<PresetItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(t.presetsTitle) },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !state.refreshing) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = t.commonRefresh,
                            tint = if (state.refreshing) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(
                    text = if (state.source == PresetSource.REMOTE) {
                        t.presetsSourceRemote
                    } else {
                        t.presetsSourceBundled
                    },
                    tone = if (state.source == PresetSource.REMOTE) Tone.OK else Tone.NEUTRAL
                )
                Spacer(Modifier.width(8.dp))
                if (state.lastSyncAt > 0) {
                    Text(
                        text = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                            .format(Date(state.lastSyncAt)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = state.category == null && !state.favoritesOnly,
                        onClick = {
                            viewModel.selectCategory(null)
                            if (state.favoritesOnly) viewModel.toggleFavoritesOnly()
                        },
                        label = { Text(t.presetsCategoryAll) }
                    )
                }
                items(PresetCategory.entries.toList()) { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(categoryLabel(category, t)) }
                    )
                }
                item {
                    FilterChip(
                        selected = state.favoritesOnly,
                        onClick = viewModel::toggleFavoritesOnly,
                        label = { Text(t.presetsFavoritesOnly) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            when {
                !state.loaded -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = t.bridgeChecking,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                state.visible.isEmpty() -> {
                    EmptyState(
                        title = t.presetsEmptyTitle,
                        hint = t.presetsEmptyHint,
                        actionLabel = t.commonRefresh,
                        onAction = viewModel::refresh
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.error?.let { message ->
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${t.presetsRefreshFailed}: $message",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                        items(state.visible, key = { it.document.id }) { item ->
                            PresetRow(
                                item = item,
                                favorite = state.favorites.contains(item.document.id),
                                onOpen = { selected = item },
                                onToggleFavorite = { viewModel.toggleFavorite(item.document.id) }
                            )
                        }
                        item { Spacer(Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }

    val current = selected
    if (current != null) {
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SectionTitle(t.presetsDetailTitle)
                Spacer(Modifier.height(10.dp))
                DiscordActivityCard(
                    profile = current.document.profile,
                    publishedLabel = t.editorLocalOnly,
                    publishedTone = Tone.NEUTRAL
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = current.document.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = current.document.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${t.presetsAuthorLabel}: ${current.document.author} · " +
                        "${t.presetsVersionLabel}: ${current.document.version}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.importOnly(current.document)
                        selected = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RichyssAccent,
                        contentColor = Color.White
                    )
                ) { Text(t.presetsImportToProfiles) }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.importPreset(current.document) { id ->
                            selected = null
                            onEditCopy(id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(t.presetsEditCopy) }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val json = ProfileJson.encodePreset(current.document)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(intent, t.presetsShareJson))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(t.presetsShareJson) }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun PresetRow(
    item: PresetItem,
    favorite: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val t = LocalTranslations.current
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileIcon(item.document.profile.icon)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.document.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${categoryLabel(item.category, t)} · ${item.document.author}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (favorite) t.presetsUnfavorite else t.presetsFavorite,
                    tint = if (favorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

private fun categoryLabel(category: PresetCategory, t: Translations): String = when (category) {
    PresetCategory.GAMING -> t.presetsCategoryGaming
    PresetCategory.MUSIC -> t.presetsCategoryMusic
    PresetCategory.PROGRAMMING -> t.presetsCategoryProgramming
    PresetCategory.SOCIAL -> t.presetsCategorySocial
    PresetCategory.CUSTOM -> t.presetsCategoryCustom
}
