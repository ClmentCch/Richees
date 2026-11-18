package com.richyss.app.ui.profiles

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.richyss.app.data.models.RpcProfile
import com.richyss.app.ui.components.EmptyState
import com.richyss.app.ui.components.ProfileIcon
import com.richyss.app.ui.components.RichyssCard
import com.richyss.app.ui.components.StatusPill
import com.richyss.app.ui.components.Tone
import com.richyss.app.ui.theme.RichyssAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    container: AppContainer,
    onEditProfile: (String) -> Unit,
    onCreateProfile: () -> Unit
) {
    val viewModel: ProfilesViewModel = viewModel(factory = ProfilesViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val t = LocalTranslations.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingExport by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf<RpcProfile?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingExport
        if (uri != null && json != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
                container.messages.show(t.profilesExported)
            }
        }
        pendingExport = null
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()
                            ?.use { it.readText() }
                    }.getOrNull()
                }
                if (text == null) {
                    container.messages.show(t.profilesImportFailed)
                } else {
                    viewModel.importJson(text)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(t.profilesTitle) },
                actions = {
                    IconButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }) {
                        Icon(Icons.Filled.FileUpload, contentDescription = t.profilesImport)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateProfile,
                containerColor = RichyssAccent,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(t.profilesCreate) }
            )
        }
    ) { padding ->
        if (state.loaded && state.profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = t.profilesEmptyTitle,
                    hint = t.profilesEmptyHint,
                    actionLabel = t.profilesCreate,
                    onAction = onCreateProfile
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                item {
                    RichyssCard {
                        Text(
                            text = "${state.profiles.size} ${t.profilesCountLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(state.profiles, key = { it.id }) { profile ->
                    ProfileCard(
                        profile = profile,
                        active = profile.id == state.activeId,
                        onEdit = { onEditProfile(profile.id) },
                        onToggleActive = { active -> viewModel.setActive(profile.id, active) },
                        onDuplicate = { viewModel.duplicate(profile.id) },
                        onDelete = { deleting = profile },
                        onExport = {
                            viewModel.exportJson(profile) { json ->
                                pendingExport = json
                                exportLauncher.launch("${profile.name.ifBlank { "richyss" }}.json")
                            }
                        },
                        onShare = {
                            viewModel.exportJson(profile) { json ->
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(intent, t.commonShare))
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(84.dp)) }
            }
        }
    }

    val target = deleting
    if (target != null) {
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(t.profilesDeleteTitle) },
            text = { Text(t.profilesDeleteMessage) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(target.id)
                    deleting = null
                }) { Text(t.commonDelete, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text(t.commonCancel) }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: RpcProfile,
    active: Boolean,
    onEdit: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit
) {
    val t = LocalTranslations.current
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        onClick = onEdit,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileIcon(profile.icon)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = profile.activityName.ifBlank { profile.details },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (active) {
                    StatusPill(text = t.profilesActiveChip, tone = Tone.OK)
                    Spacer(Modifier.width(6.dp))
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(t.commonEdit) },
                            leadingIcon = { Icon(Icons.Filled.Edit, null) },
                            onClick = { menuOpen = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text(t.commonDuplicate) },
                            leadingIcon = { Icon(Icons.Filled.ContentCopy, null) },
                            onClick = { menuOpen = false; onDuplicate() }
                        )
                        DropdownMenuItem(
                            text = { Text(t.commonExport) },
                            leadingIcon = { Icon(Icons.Filled.Upload, null) },
                            onClick = { menuOpen = false; onExport() }
                        )
                        DropdownMenuItem(
                            text = { Text(t.commonShare) },
                            leadingIcon = { Icon(Icons.Filled.Share, null) },
                            onClick = { menuOpen = false; onShare() }
                        )
                        DropdownMenuItem(
                            text = { Text(t.commonDelete) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
            }
            if (profile.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (active) t.commonActive else t.commonInactive,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = active,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = RichyssAccent
                    )
                )
            }
        }
    }
}
