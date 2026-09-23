package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SongEntity
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

@Composable
fun SongItemCard(
    song: SongEntity,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onSongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShowDetails: () -> Unit,
    onDeleteSong: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSongClick() }
            .testTag("song_item_${song.id}"),
        color = if (isCurrentSong) DarkSurfaceHighlight else DarkSurfaceElevated.copy(alpha = 0.7f),
        tonalElevation = if (isCurrentSong) 6.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art or Vinyl Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isCurrentSong) listOf(NeonViolet, NeonCyan) else listOf(DarkSurfaceHighlight, Color(0xFF202638))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!song.albumArtPath.isNullOrBlank() && File(song.albumArtPath).exists()) {
                    AsyncImage(
                        model = File(song.albumArtPath),
                        contentDescription = "Album art for ${song.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isCurrentSong) Color.White else NeonCyan.copy(alpha = 0.8f),
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Overlay play/equalizer state if active
                if (isCurrentSong) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPlaying) {
                            AnimatedWaveBars(tint = NeonCyan)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Paused",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Song Title, Artist & Duration
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isCurrentSong) NeonCyan else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Text(
                        text = TimeUtils.formatDuration(song.durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Text(
                        text = TimeUtils.formatFileSize(song.fileSizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Favorite button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("fav_button_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (song.isFavorite) NeonPink else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Options menu trigger
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("more_button_${song.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Song options",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkSurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Now", color = TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan)
                        },
                        onClick = {
                            showMenu = false
                            onSongClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = NeonViolet)
                        },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("File & Storage Info", color = TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary)
                        },
                        onClick = {
                            showMenu = false
                            onShowDetails()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete from Vault", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = {
                            showMenu = false
                            onDeleteSong()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedWaveBars(
    tint: Color = NeonCyan,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val bar1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(550, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "bar3"
    )

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar1)
                .clip(CircleShape)
                .background(tint)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar2)
                .clip(CircleShape)
                .background(tint)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar3)
                .clip(CircleShape)
                .background(tint)
        )
    }
}
