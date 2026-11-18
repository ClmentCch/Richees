package com.richyss.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.richyss.app.BuildConfig
import com.richyss.app.core.AppContainer
import com.richyss.app.core.LocalTranslations
import com.richyss.app.data.prefs.AppSettings
import com.richyss.app.data.prefs.LanguageMode
import com.richyss.app.data.prefs.ThemeMode
import com.richyss.app.ui.components.KeyValueRow
import com.richyss.app.ui.components.RichyssCard
import com.richyss.app.ui.components.SectionTitle
import com.richyss.app.ui.theme.RichyssAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(container: AppContainer, settings: AppSettings) {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val t = LocalTranslations.current
    val context = LocalContext.current

    var owner by remember(settings.presetOwner) { mutableStateOf(settings.presetOwner) }
    var repo by remember(settings.presetRepo) { mutableStateOf(settings.presetRepo) }
    var branch by remember(settings.presetBranch) { mutableStateOf(settings.presetBranch) }
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(t.settingsTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SectionTitle(t.settingsLanguage) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = settings.language == LanguageMode.SYSTEM,
                        onClick = { viewModel.setLanguage(LanguageMode.SYSTEM) },
                        label = { Text(t.settingsLanguageSystem) }
                    )
                    FilterChip(
                        selected = settings.language == LanguageMode.FRENCH,
                        onClick = { viewModel.setLanguage(LanguageMode.FRENCH) },
                        label = { Text("Français") }
                    )
                    FilterChip(
                        selected = settings.language == LanguageMode.ENGLISH,
                        onClick = { viewModel.setLanguage(LanguageMode.ENGLISH) },
                        label = { Text("English") }
                    )
                }
            }

            item { SectionTitle(t.settingsTheme) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = settings.theme == ThemeMode.DARK,
                        onClick = { viewModel.setTheme(ThemeMode.DARK) },
                        label = { Text(t.settingsThemeDark) }
                    )
                    FilterChip(
                        selected = settings.theme == ThemeMode.LIGHT,
                        onClick = { viewModel.setTheme(ThemeMode.LIGHT) },
                        label = { Text(t.settingsThemeLight) }
                    )
                    FilterChip(
                        selected = settings.theme == ThemeMode.SYSTEM,
                        onClick = { viewModel.setTheme(ThemeMode.SYSTEM) },
                        label = { Text(t.settingsThemeSystem) }
                    )
                }
            }

            item { SectionTitle(t.settingsGitHub) }
            item {
                RichyssCard {
                    Column {
                        OutlinedTextField(
                            value = owner,
                            onValueChange = { owner = it },
                            label = { Text(t.settingsPresetRepo) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = branch,
                            onValueChange = { branch = it },
                            label = { Text(t.settingsPresetBranch) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "https://raw.githubusercontent.com/$owner/${repo.ifBlank { "-" }}/$branch/presets/index.json",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = repo,
                            onValueChange = { repo = it },
                            label = { Text(t.settingsPresetRepo) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val parts = repo.trim().split("/")
                                val targetOwner = if (parts.size == 2) parts[0] else owner
                                val targetRepo = if (parts.size == 2) parts[1] else repo
                                viewModel.saveRepository(targetOwner, targetRepo, branch)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RichyssAccent,
                                contentColor = Color.White
                            )
                        ) { Text(t.settingsSaveRepository) }
                    }
                }
            }

            item { SectionTitle(t.settingsStorage) }
            item {
                RichyssCard {
                    Column {
                        KeyValueRow(
                            label = t.settingsProfilesStored,
                            value = state.profileCount.toString()
                        )
                        OutlinedButton(
                            onClick = { confirmClear = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(t.settingsClearProfiles, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item { SectionTitle(t.settingsVersion) }
            item {
                RichyssCard {
                    Column {
                        KeyValueRow(label = t.settingsVersion, value = BuildConfig.VERSION_NAME)
                        if (state.updateAvailable != null) {
                            KeyValueRow(
                                label = t.settingsUpdateAvailable,
                                value = state.updateAvailable.orEmpty(),
                                valueTone = com.richyss.app.ui.components.Tone.OK
                            )
                        }
                        Button(
                            onClick = {
                                val url = state.updateUrl
                                if (url != null) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } else {
                                    viewModel.checkUpdates()
                                }
                            },
                            enabled = !state.checking,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RichyssAccent,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                when {
                                    state.checking -> t.settingsChecking
                                    state.updateAvailable != null -> t.settingsUpdateAvailable
                                    else -> t.settingsUpdateCheck
                                }
                            )
                        }
                    }
                }
            }

            item { SectionTitle(t.settingsAbout) }
            item {
                RichyssCard {
                    Column {
                        Text(
                            text = t.settingsAboutBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/ClmentCch/Richees/tree/apk")
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(t.settingsOpenSource) }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(t.settingsClearProfiles) },
            text = { Text(t.settingsClearProfilesConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearProfiles()
                    confirmClear = false
                }) { Text(t.commonConfirm, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(t.commonCancel) }
            }
        )
    }
}
