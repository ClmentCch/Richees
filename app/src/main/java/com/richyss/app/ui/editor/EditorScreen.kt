package com.richyss.app.ui.editor

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.richyss.app.core.AppContainer
import com.richyss.app.core.LocalTranslations
import com.richyss.app.core.Translations
import com.richyss.app.data.models.ActivityType
import com.richyss.app.data.models.FieldIssue
import com.richyss.app.data.models.ProfileButton
import com.richyss.app.data.models.ProfileLimits
import com.richyss.app.data.models.RpcProfile
import com.richyss.app.ui.components.DiscordActivityCard
import com.richyss.app.ui.components.RichyssCard
import com.richyss.app.ui.components.SectionTitle
import com.richyss.app.ui.components.Tone
import com.richyss.app.ui.theme.RichyssAccent
import com.richyss.app.ui.theme.RichyssDanger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    container: AppContainer,
    profileId: String?,
    onFinished: () -> Unit
) {
    val viewModel: EditorViewModel = viewModel(
        key = "editor-${profileId ?: "new"}",
        factory = EditorViewModel.factory(container, profileId)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val t = LocalTranslations.current
    val context = LocalContext.current

    val draft = state.draft

    val largeImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.update { it.copy(largeImage = uri.toString()) }
        }
    }
    val smallImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.update { it.copy(smallImage = uri.toString()) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) t.editorTitleNew else t.editorTitleEdit) },
                navigationIcon = {
                    IconButton(onClick = onFinished) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = t.commonBack)
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { viewModel.delete(onFinished) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = t.commonDelete,
                                tint = RichyssDanger
                            )
                        }
                    }
                    TextButton(onClick = { viewModel.save(onFinished) }) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = RichyssAccent)
                        Spacer(Modifier.width(4.dp))
                        Text(t.commonSave, color = RichyssAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (draft == null) {
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DiscordActivityCard(
                    profile = draft,
                    publishedLabel = t.editorLocalOnly,
                    publishedTone = Tone.NEUTRAL
                )
            }

            if (state.issues.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = RichyssDanger.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            state.issues.take(4).forEach { issue ->
                                Text(
                                    text = "• ${issueText(issue, t)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RichyssDanger
                                )
                            }
                        }
                    }
                }
            }

            item { SectionTitle(t.editorGeneral) }

            item {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { value -> viewModel.update { it.copy(name = value) } },
                    label = { Text(t.editorName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draft.icon,
                        onValueChange = { value -> viewModel.update { it.copy(icon = value) } },
                        label = { Text(t.editorIcon) },
                        singleLine = true,
                        modifier = Modifier.width(110.dp)
                    )
                    OutlinedTextField(
                        value = draft.description,
                        onValueChange = { value -> viewModel.update { it.copy(description = value) } },
                        label = { Text(t.editorDescription) },
                        maxLines = 3,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = t.editorActivityType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ActivityType.entries.take(3).forEach { type ->
                            FilterChip(
                                selected = draft.activityType == type.id,
                                onClick = { viewModel.update { it.copy(activityType = type.id) } },
                                label = { Text(activityLabel(type, t), style = MaterialTheme.typography.bodyMedium) }
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ActivityType.entries.drop(3).forEach { type ->
                            FilterChip(
                                selected = draft.activityType == type.id,
                                onClick = { viewModel.update { it.copy(activityType = type.id) } },
                                label = { Text(activityLabel(type, t), style = MaterialTheme.typography.bodyMedium) }
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = draft.activityName,
                    onValueChange = { value -> viewModel.update { it.copy(activityName = value) } },
                    label = { Text(t.editorActivityName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = draft.details,
                    onValueChange = { value -> viewModel.update { it.copy(details = value) } },
                    label = { Text(t.editorDetails) },
                    supportingText = { Text("${draft.details.length}/${ProfileLimits.DETAILS}") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = draft.state,
                    onValueChange = { value -> viewModel.update { it.copy(state = value) } },
                    label = { Text(t.editorState) },
                    supportingText = { Text("${draft.state.length}/${ProfileLimits.STATE}") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { SectionTitle(t.editorImages) }
            item {
                ImageField(
                    label = t.editorLargeImage,
                    uri = draft.largeImage,
                    pickLabel = t.editorPickImage,
                    clearLabel = t.editorClearImage,
                    onPick = { largeImageLauncher.launch(arrayOf("image/*")) },
                    onClear = { viewModel.update { it.copy(largeImage = "") } }
                )
            }
            item {
                ImageField(
                    label = t.editorSmallImage,
                    uri = draft.smallImage,
                    pickLabel = t.editorPickImage,
                    clearLabel = t.editorClearImage,
                    onPick = { smallImageLauncher.launch(arrayOf("image/*")) },
                    onClear = { viewModel.update { it.copy(smallImage = "") } }
                )
            }
            item {
                Text(
                    text = t.editorImageHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draft.largeText,
                        onValueChange = { value -> viewModel.update { it.copy(largeText = value) } },
                        label = { Text("Hover — ${t.editorLargeImage}") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = draft.smallText,
                        onValueChange = { value -> viewModel.update { it.copy(smallText = value) } },
                        label = { Text("Hover — ${t.editorSmallImage}") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { SectionTitle(t.editorTime) }
            item {
                RichyssCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = t.editorTimerEnabled,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = draft.timerEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.update { profile ->
                                        profile.copy(
                                            timerEnabled = enabled,
                                            startTimestamp = if (enabled) {
                                                profile.startTimestamp ?: System.currentTimeMillis()
                                            } else {
                                                profile.startTimestamp
                                            }
                                        )
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = RichyssAccent
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${t.editorStart}: ${formatEpoch(draft.startTimestamp)}   —   " +
                                "${t.editorEnd}: ${formatEpoch(draft.endTimestamp)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(
                                onClick = {
                                    viewModel.update {
                                        it.copy(
                                            timerEnabled = true,
                                            startTimestamp = System.currentTimeMillis()
                                        )
                                    }
                                },
                                label = { Text(t.editorStartNow, style = MaterialTheme.typography.bodyMedium) }
                            )
                            AssistChip(
                                onClick = {
                                    viewModel.update {
                                        it.copy(
                                            timerEnabled = true,
                                            endTimestamp = System.currentTimeMillis() + 3_600_000
                                        )
                                    }
                                },
                                label = { Text(t.editorEndInOneHour, style = MaterialTheme.typography.bodyMedium) }
                            )
                            AssistChip(
                                onClick = {
                                    viewModel.update {
                                        it.copy(
                                            timerEnabled = true,
                                            endTimestamp = System.currentTimeMillis() + 7_200_000
                                        )
                                    }
                                },
                                label = { Text(t.editorEndInTwoHours, style = MaterialTheme.typography.bodyMedium) }
                            )
                        }
                        TextButton(onClick = {
                            viewModel.update {
                                it.copy(
                                    timerEnabled = false,
                                    startTimestamp = null,
                                    endTimestamp = null
                                )
                            }
                        }) { Text(t.editorClearTimes) }
                    }
                }
            }

            item { SectionTitle(t.editorButtons) }
            draft.buttons.forEachIndexed { index, button ->
                item(key = "button-$index") {
                    ButtonEditor(
                        index = index,
                        button = button,
                        onLabelChange = { value ->
                            viewModel.update { profile ->
                                profile.copy(
                                    buttons = profile.buttons.mapIndexed { i, current ->
                                        if (i == index) current.copy(label = value) else current
                                    }
                                )
                            }
                        },
                        onUrlChange = { value ->
                            viewModel.update { profile ->
                                profile.copy(
                                    buttons = profile.buttons.mapIndexed { i, current ->
                                        if (i == index) current.copy(url = value) else current
                                    }
                                )
                            }
                        },
                        onRemove = {
                            viewModel.update { profile ->
                                profile.copy(
                                    buttons = profile.buttons.filterIndexed { i, _ -> i != index }
                                )
                            }
                        }
                    )
                }
            }
            item {
                Column {
                    if (draft.buttons.size < ProfileLimits.MAX_BUTTONS) {
                        OutlinedButton(
                            onClick = {
                                viewModel.update {
                                    it.copy(buttons = it.buttons + ProfileButton())
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(t.editorAddButton)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = t.editorButtonsLimit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { SectionTitle(t.editorUnsupported) }
            item {
                RichyssCard {
                    Row {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = t.editorUnsupportedBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun ImageField(
    label: String,
    uri: String,
    pickLabel: String,
    clearLabel: String,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    RichyssCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (uri.isBlank()) "—" else uri.takeLast(38),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            TextButton(onClick = onPick) { Text(pickLabel) }
            if (uri.isNotBlank()) {
                TextButton(onClick = onClear) {
                    Text(clearLabel, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ButtonEditor(
    index: Int,
    button: ProfileButton,
    onLabelChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val t = LocalTranslations.current
    RichyssCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = RichyssAccent,
                    modifier = Modifier.width(22.dp)
                )
                OutlinedTextField(
                    value = button.label,
                    onValueChange = onLabelChange,
                    label = { Text(t.editorButtonLabel) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = t.editorRemoveButton,
                        tint = RichyssDanger
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = button.url,
                onValueChange = onUrlChange,
                label = { Text(t.editorButtonUrl) },
                singleLine = true,
                isError = button.url.isNotBlank() &&
                    !(button.url.startsWith("http://") || button.url.startsWith("https://")),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun activityLabel(type: ActivityType, t: Translations): String = when (type) {
    ActivityType.PLAYING -> t.activityPlaying
    ActivityType.STREAMING -> t.activityStreaming
    ActivityType.LISTENING -> t.activityListening
    ActivityType.WATCHING -> t.activityWatching
    ActivityType.COMPETING -> t.activityCompeting
}

private fun formatEpoch(value: Long?): String =
    if (value == null || value <= 0) "—" else com.richyss.app.core.TimeFormat.shortDateTime(value)

internal fun issueText(issue: FieldIssue, t: Translations): String = when (issue.messageKey) {
    "required" -> "${issue.field}: ${t.editorFieldRequired}"
    "too_long" -> "${issue.field}: ${t.editorTooLong}"
    "invalid_url" -> "${issue.field}: ${t.editorUrlInvalid}"
    "too_many" -> "${issue.field}: ${t.editorButtonsLimit}"
    "too_large" -> "${issue.field}: ${t.editorTooLong}"
    "no_times" -> "${issue.field}: ${t.editorStart} / ${t.editorEnd}"
    "before_start" -> "${issue.field}: ${t.editorStart} > ${t.editorEnd}"
    else -> "${issue.field}: ${issue.messageKey}"
}
