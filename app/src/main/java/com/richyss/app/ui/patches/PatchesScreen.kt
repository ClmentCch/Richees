package com.richyss.app.ui.patches

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.richyss.app.core.AppContainer
import com.richyss.app.core.LocalTranslations
import com.richyss.app.core.TimeFormat
import com.richyss.app.rpc.DiscordBridge
import com.richyss.app.rpc.IntegrationState
import com.richyss.app.ui.components.KeyValueRow
import com.richyss.app.ui.components.RichyssCard
import com.richyss.app.ui.components.SectionTitle
import com.richyss.app.ui.components.StatusPill
import com.richyss.app.ui.components.Tone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatchesScreen(container: AppContainer) {
    val viewModel: PatchesViewModel = viewModel(factory = PatchesViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val t = LocalTranslations.current
    val context = LocalContext.current
    var showDiagnostics by remember { mutableStateOf(false) }

    val integration = state.integration

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(t.patchesTitle) },
                actions = {
                    TextButton(onClick = viewModel::refresh, enabled = !state.busy) {
                        Text(t.patchesRefresh)
                    }
                },
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
            item {
                RichyssCard {
                    Column {
                        Row {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = t.patchesDiscordState,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                val (label, tone) = when (integration) {
                                    null -> t.bridgeChecking to Tone.NEUTRAL
                                    IntegrationState.DiscordMissing -> t.discordNotInstalled to Tone.WARN
                                    is IntegrationState.PatchMissing ->
                                        t.discordInstalledNoBridge to Tone.WARN
                                    is IntegrationState.BridgeError -> t.bridgeError to Tone.ERROR
                                    is IntegrationState.BridgeActive -> t.bridgeActive to Tone.OK
                                }
                                StatusPill(text = label, tone = tone)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        KeyValueRow(
                            label = t.patchesDiscordVersion,
                            value = when (integration) {
                                null -> "—"
                                IntegrationState.DiscordMissing -> t.patchesNotInstalled
                                is IntegrationState.PatchMissing -> integration.discordVersion
                                is IntegrationState.BridgeError -> integration.discordVersion
                                is IntegrationState.BridgeActive -> integration.discordVersion
                            }
                        )
                        KeyValueRow(
                            label = t.patchesBridgeCompat,
                            value = when (integration) {
                                is IntegrationState.BridgeActive -> t.patchesCompatible
                                is IntegrationState.BridgeError -> t.patchesIncompatible
                                else -> t.patchesNotInstalled
                            },
                            valueTone = when (integration) {
                                is IntegrationState.BridgeActive -> Tone.OK
                                is IntegrationState.BridgeError -> Tone.ERROR
                                else -> Tone.WARN
                            }
                        )
                        KeyValueRow(
                            label = t.patchesBridgeVersion,
                            value = (integration as? IntegrationState.BridgeActive)?.bridgeVersion
                                ?: t.patchesNotInstalled
                        )
                        KeyValueRow(
                            label = t.patchesProtocol,
                            value = (integration as? IntegrationState.BridgeActive)?.protocol
                                ?: "—"
                        )
                        KeyValueRow(
                            label = t.patchesLastPayload,
                            value = (integration as? IntegrationState.BridgeActive)
                                ?.takeIf { it.hasPresence }
                                ?.let { TimeFormat.shortDateTime(it.updatedAt) }
                                ?: t.patchesNoPayload
                        )
                        if (integration is IntegrationState.BridgeError) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = integration.reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = viewModel::deliverActiveProfile,
                    enabled = !state.busy && state.activeProfile != null &&
                        integration is IntegrationState.BridgeActive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.richyss.app.ui.theme.RichyssAccent,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(t.patchesDeliver)
                }
            }

            if (state.activeProfile == null) {
                item {
                    Text(
                        text = t.patchesNoActiveProfile,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { SectionTitle(t.patchesInstallGuide) }
            item {
                RichyssCard {
                    Column {
                        listOf(
                            t.patchesStep1,
                            t.patchesStep2,
                            t.patchesStep3,
                            t.patchesStep4,
                            t.patchesStep5
                        ).forEachIndexed { index, step ->
                            Row(Modifier.padding(vertical = 3.dp)) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = com.richyss.app.ui.theme.RichyssAccent,
                                    modifier = Modifier.width(20.dp)
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/ClmentCch/Richees")
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(t.patchesOpenRepository) }
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/MorpheApp")
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(t.patchesOpenMorphe) }
                    }
                }
            }

            item { SectionTitle(t.patchesDiagnostics) }
            item {
                RichyssCard {
                    Column {
                        TextButton(onClick = { showDiagnostics = !showDiagnostics }) {
                            Text(
                                if (showDiagnostics) t.commonClose else t.patchesDiagnostics
                            )
                        }
                        if (showDiagnostics) {
                            Text(
                                text = state.diagnostics ?: "content://${DiscordBridge.BRIDGE_AUTHORITY}/status → null",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = t.patchesLastPayload,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = state.lastPayload ?: t.patchesNoPayload,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { SectionTitle(t.patchesLimitations) }
            item {
                RichyssCard {
                    Text(
                        text = t.patchesLimitationsBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
