package com.richyss.app.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.richyss.app.core.LocalTranslations
import com.richyss.app.core.TimeFormat
import com.richyss.app.data.models.RpcProfile
import com.richyss.app.ui.theme.RichyssAccent
import com.richyss.app.ui.theme.RichyssDanger
import com.richyss.app.ui.theme.RichyssOnline
import com.richyss.app.ui.theme.RichyssWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun RichyssCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

enum class Tone { OK, WARN, ERROR, NEUTRAL }

@Composable
fun statusColor(tone: Tone): Color = when (tone) {
    Tone.OK -> RichyssOnline
    Tone.WARN -> RichyssWarning
    Tone.ERROR -> RichyssDanger
    Tone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun StatusPill(
    text: String,
    tone: Tone,
    modifier: Modifier = Modifier
) {
    val color = statusColor(tone)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Wordmark drawn with a gradient tile and the Richyss initial. */
@Composable
fun RichyssLogo(size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3).dp))
            .background(
                Brush.linearGradient(
                    listOf(RichyssAccent, Color(0xFF8B5CF6))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "R",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size * 0.52).sp
        )
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (supporting != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    hint: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RichyssLogo(size = 52)
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RichyssAccent,
                    contentColor = Color.White
                )
            ) { Text(actionLabel) }
        }
    }
}

/** Loads a bitmap referenced by a content:// or file:// URI chosen by the user. */
@Composable
fun rememberUriBitmap(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, uriString) {
        value = if (uriString.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) { decodeBitmap(context, uriString) }
        }
    }.value
}

private fun decodeBitmap(context: Context, uriString: String): ImageBitmap? = runCatching {
    context.contentResolver.openInputStream(Uri.parse(uriString))?.use { stream ->
        BitmapFactory.decodeStream(stream)?.asImageBitmap()
    }
}.getOrNull()

/** Ticks once per second so elapsed/remaining timers stay live in the preview. */
@Composable
fun rememberNowTicker(enabled: Boolean): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    androidx.compose.runtime.LaunchedEffect(enabled) {
        while (enabled) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    return now
}

/**
 * Visual reproduction of Discord's activity card.
 *
 * [publishedLabel] is always explicit about whether the payload is only a local
 * preview or has really been handed to the bridge.
 */
@Composable
fun DiscordActivityCard(
    profile: RpcProfile?,
    publishedLabel: String,
    publishedTone: Tone,
    modifier: Modifier = Modifier
) {
    val t = LocalTranslations.current
    val largeImage = rememberUriBitmap(profile?.largeImage)
    val smallImage = rememberUriBitmap(profile?.smallImage)
    val now = rememberNowTicker(profile?.timerEnabled == true)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "RICH PRESENCE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(text = publishedLabel, tone = publishedTone)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Top) {
                Box {
                    ImageSlot(
                        bitmap = largeImage,
                        label = profile?.largeText.orEmpty(),
                        size = 78
                    )
                    if (profile?.smallImage?.isNotBlank() == true) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(top = 56.dp, start = 56.dp)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp)
                        ) {
                            ImageSlot(bitmap = smallImage, label = profile.smallText, size = 22)
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = buildString {
                            append(activityVerb(profile))
                            val name = profile?.activityName?.takeIf { it.isNotBlank() }
                                ?: profile?.name.orEmpty()
                            if (name.isNotBlank()) append(" ").append(name)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = profile?.details?.takeIf { it.isNotBlank() } ?: t.previewNoDetails,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!profile?.state.isNullOrBlank()) {
                        Text(
                            text = profile?.state.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val timer = TimeFormat.timerLabel(
                        start = profile?.startTimestamp?.takeIf { profile.timerEnabled },
                        end = profile?.endTimestamp?.takeIf { profile.timerEnabled },
                        now = now,
                        elapsedSuffix = t.previewElapsed,
                        remainingSuffix = t.previewRemaining
                    )
                    if (timer != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = timer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val buttons = profile?.buttons?.filter { it.label.isNotBlank() }.orEmpty()
            if (buttons.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    buttons.forEach { button ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = button.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (button.url.isNotBlank()) {
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.ArrowOutward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun activityVerb(profile: RpcProfile?): String {
    val t = LocalTranslations.current
    return when (profile?.activity?.id ?: 0) {
        1 -> t.activityStreaming
        2 -> t.activityListening
        3 -> t.activityWatching
        5 -> t.activityCompeting
        else -> t.activityPlaying
    }
}

@Composable
private fun ImageSlot(bitmap: ImageBitmap?, label: String?, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size((size / 2.6).dp)
            )
        }
    }
}

/** Emoji badge used in profile rows. */
@Composable
fun ProfileIcon(icon: String, size: Int = 42) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon.ifBlank { RpcProfile.DEFAULT_ICON }, fontSize = (size * 0.46).sp)
    }
}

@Composable
fun KeyValueRow(label: String, value: String, valueTone: Tone = Tone.NEUTRAL) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = statusColor(valueTone),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
