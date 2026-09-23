package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PlaybackState
import com.example.data.model.RepeatMode
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    playbackState: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onShowStorageInfo: () -> Unit,
    onOpenEqualizer: () -> Unit = {},
    currentEqualizerPresetName: String? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentSong = playbackState.currentSong ?: return

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    val displayProgress = if (isDraggingSlider) {
        sliderPosition
    } else {
        playbackState.progressFraction
    }

    val displayPositionMs = if (isDraggingSlider) {
        (sliderPosition * playbackState.durationMs).toLong()
    } else {
        playbackState.currentPositionMs
    }

    // Vinyl rotation
    val infiniteTransition = rememberInfiniteTransition(label = "now_playing_vinyl")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "rotation"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null,
        modifier = modifier
            .fillMaxSize()
            .testTag("now_playing_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("now_playing_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = NeonCyan
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenEqualizer,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("now_playing_equalizer_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Equalizer",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onShowStorageInfo,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("now_playing_storage_info")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Storage Info",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Vinyl Turntable Display
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .rotate(if (playbackState.isPlaying) rotationAngle else 0f)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF1E1C2B),
                                Color(0xFF12111A),
                                Color(0xFF0A090F)
                            )
                        )
                    )
                    .border(3.dp, Brush.sweepGradient(listOf(NeonViolet, NeonCyan, NeonPink, NeonViolet)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl grooves concentric circles
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                )

                // Center Record Label (Album Art or Icon)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonViolet, NeonCyan))
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentSong.albumArtPath.isNullOrBlank() && File(currentSong.albumArtPath).exists()) {
                        AsyncImage(
                            model = File(currentSong.albumArtPath),
                            contentDescription = currentSong.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Center Spindle Hole
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(DarkBackground)
                            .border(2.dp, Color.Black, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Song Info & Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSong.artist + if (currentSong.album.isNotBlank()) " • " + currentSong.album else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("now_playing_fav")
                ) {
                    Icon(
                        imageVector = if (currentSong.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentSong.isFavorite) NeonPink else TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seek Bar Slider
            Slider(
                value = displayProgress,
                onValueChange = {
                    isDraggingSlider = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    isDraggingSlider = false
                    val targetMs = (sliderPosition * playbackState.durationMs).toLong()
                    onSeekTo(targetMs)
                },
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonViolet,
                    inactiveTrackColor = DarkSurfaceElevated
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("now_playing_slider")
            )

            // Timestamps: Elapsed & Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = TimeUtils.formatDuration(displayPositionMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    text = TimeUtils.formatDuration(playbackState.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("now_playing_shuffle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.isShuffle) NeonCyan else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous button
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("now_playing_previous")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Song",
                        tint = TextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Center Play / Pause Large Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonViolet, NeonCyan))
                        )
                        .clickable { onTogglePlayPause() }
                        .testTag("now_playing_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("now_playing_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Song",
                        tint = TextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Repeat Mode button
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("now_playing_repeat")
                ) {
                    Icon(
                        imageVector = when (playbackState.repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat Mode",
                        tint = when (playbackState.repeatMode) {
                            RepeatMode.OFF -> TextMuted
                            else -> NeonViolet
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Informative Indicators for Active Shuffle and Repeat State
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (playbackState.isShuffle) "Shuffle: Random order" else "Shuffle: Sequential order",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (playbackState.isShuffle) NeonCyan else TextMuted
                )
                Text(
                    text = when (playbackState.repeatMode) {
                        RepeatMode.OFF -> "Repeat: Off"
                        RepeatMode.ALL -> "Repeat: Queue / Playlist"
                        RepeatMode.ONE -> "Repeat: Current Track"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (playbackState.repeatMode) {
                        RepeatMode.OFF -> TextMuted
                        else -> NeonViolet
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Playback Speed Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speed: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(end = 8.dp)
                )

                val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                speeds.forEach { speed ->
                    val isSelected = playbackState.playbackSpeed == speed
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetSpeed(speed) },
                        label = {
                            Text(
                                text = "${speed}x",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonViolet.copy(alpha = 0.3f),
                            selectedLabelColor = NeonViolet,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonViolet else CardBorder
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Equalizer Quick Access Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { onOpenEqualizer() }
                    .testTag("now_playing_equalizer_pill"),
                color = DarkSurface.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Equalizer",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Equalizer • ${currentEqualizerPresetName ?: "Preset"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Storage Details Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .clickable { onShowStorageInfo() },
                color = DarkSurface.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vault Stored • ${TimeUtils.formatFileSize(currentSong.fileSizeBytes)} • ${currentSong.mimeType.substringAfter("/")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
