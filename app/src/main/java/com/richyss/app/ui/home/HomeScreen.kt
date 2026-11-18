package com.richyss.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.richyss.app.core.AppContainer
import com.richyss.app.core.LocalTranslations
import com.richyss.app.core.TimeFormat
import com.richyss.app.data.models.RpcProfile
import com.richyss.app.rpc.IntegrationState
import com.richyss.app.ui.components.DiscordActivityCard
import com.richyss.app.ui.components.ProfileIcon
import com.richyss.app.ui.components.RichyssCard
import com.richyss.app.ui.components.RichyssLogo
import com.richyss.app.ui.components.SectionTitle
import com.richyss.app.ui.components.StatTile
import com.richyss.app.ui.components.StatusPill
import com.richyss.app.ui.components.Tone
import com.richyss.app.ui.theme.RichyssAccent

@Composable
fun HomeScreen(
    container: AppContainer,
    onOpenProfiles: () -> Unit,
    onOpenPatches: () -> Unit,
    onCreateRpc: () -> Unit,
    onEditProfile: (String) -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val t = LocalTranslations.current

    val integration = state.integration
    val (statusLabel, statusTone) = when (integration) {
        null -> t.bridgeChecking to Tone.NEUTRAL
        IntegrationState.DiscordMissing -> t.discordNotInstalled to Tone.WARN
        is IntegrationState.PatchMissing -> t.discordInstalledNoBridge to Tone.WARN
        is IntegrationState.BridgeError -> t.bridgeError to Tone.ERROR
        is IntegrationState.BridgeActive -> t.bridgeActive to Tone.OK
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RichyssLogo(size = 46)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Richyss",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = t.homeTagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = viewModel::refresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = t.commonRefresh,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Surface(
                onClick = onOpenPatches,
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = t.homeIntegration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        StatusPill(text = statusLabel, tone = statusTone)
                    }
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    label = t.homeProfilesCount,
                    value = state.profileCount.toString(),
                    supporting = t.profilesCountLabel,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = t.homeActiveProfile,
                    value = state.activeProfile?.icon ?: "—",
                    supporting = state.activeProfile?.name ?: t.homeNoActiveProfile,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            RichyssCard {
                Column {
                    Text(
                        text = t.homeLastActivity,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    if (state.lastDeliveredAt > 0) {
                        Text(
                            text = state.lastDeliveredName ?: t.commonUnknown,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = TimeFormat.shortDateTime(state.lastDeliveredAt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = t.homeNoActivity,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            DiscordActivityCard(
                profile = state.activeProfile ?: state.recentProfiles.firstOrNull(),
                publishedLabel = t.previewNotPublished,
                publishedTone = Tone.WARN
            )
        }

        item {
            Text(
                text = t.homeLocalPreviewNote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Button(
                onClick = onCreateRpc,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RichyssAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = t.homeCreateRpc, style = MaterialTheme.typography.titleMedium)
            }
        }

        item {
            SectionTitle(
                text = t.homeRecentProfiles,
                trailing = {
                    Surface(
                        onClick = onOpenProfiles,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = t.homeSeeAll,
                            style = MaterialTheme.typography.labelLarge,
                            color = RichyssAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            )
        }

        if (state.recentProfiles.isEmpty()) {
            item {
                RichyssCard {
                    Text(
                        text = t.profilesEmptyHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(state.recentProfiles, key = { it.id }) { profile ->
                HomeProfileRow(
                    profile = profile,
                    active = profile.id == state.activeProfile?.id,
                    onClick = { onEditProfile(profile.id) }
                )
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun HomeProfileRow(profile: RpcProfile, active: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                StatusPill(text = LocalTranslations.current.commonActive, tone = Tone.OK)
            }
        }
    }
}
