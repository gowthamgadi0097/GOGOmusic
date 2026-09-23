package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PlaybackState
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun MiniPlayerBar(
    playbackState: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onExpandPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong = playbackState.currentSong ?: return

    // Vinyl spinning rotation animation when song is playing
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onExpandPlayer() }
            .testTag("mini_player_bar"),
        color = DarkSurfaceElevated,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thin top progress indicator
            LinearProgressIndicator(
                progress = { playbackState.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = NeonViolet,
                trackColor = DarkSurfaceElevated
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vinyl / Album art thumbnail with rotation
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .rotate(if (playbackState.isPlaying) rotationAngle else 0f)
                        .background(
                            Brush.sweepGradient(
                                listOf(Color(0xFF1E1B4B), NeonViolet, NeonCyan, Color(0xFF1E1B4B))
                            )
                        )
                        .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentSong.albumArtPath.isNullOrBlank() && File(currentSong.albumArtPath).exists()) {
                        AsyncImage(
                            model = File(currentSong.albumArtPath),
                            contentDescription = "Playing ${currentSong.title}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Center spindle hole
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.dp, Color.Black, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Song Title & Artist
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play / Pause button
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("mini_player_play_pause")
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonViolet, NeonCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Next Track button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("mini_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Song",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
