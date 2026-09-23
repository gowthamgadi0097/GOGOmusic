package com.example.ui.components

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SongEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeUtils

@Composable
fun SongDetailsDialog(
    song: SongEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonViolet.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Vault Audio Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    icon = Icons.Default.MusicNote,
                    label = "Title / Artist",
                    value = "${song.title}\nby ${song.artist}"
                )
                DetailRow(
                    icon = Icons.Default.Folder,
                    label = "Stored App Location",
                    value = song.filePath,
                    isCode = true
                )
                DetailRow(
                    icon = Icons.Default.SdStorage,
                    label = "File Size & Format",
                    value = "${TimeUtils.formatFileSize(song.fileSizeBytes)} • ${song.mimeType}"
                )
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = "Duration",
                    value = TimeUtils.formatDuration(song.durationMs)
                )
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Date Imported",
                    value = TimeUtils.formatDate(song.dateAdded)
                )
                DetailRow(
                    icon = Icons.Default.PlayArrow,
                    label = "Play Count",
                    value = "${song.playCount} times played"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                modifier = Modifier.testTag("details_dialog_close")
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface,
        modifier = Modifier
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .testTag("song_details_dialog")
    )
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isCode: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Text(
                text = value,
                style = if (isCode) {
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = TextPrimary
            )
        }
    }
}
