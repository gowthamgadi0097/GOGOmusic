package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaylistEntity
import com.example.data.model.SongEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Playlist",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_playlist_name_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, description)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("create_playlist_confirm_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(20.dp))
    )
}

@Composable
fun AddToPlaylistDialog(
    song: SongEntity,
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (playlistId: Long) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add \"${song.title}\" to Playlist",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (playlists.isEmpty()) {
                    Text(
                        text = "No playlists found. Create one below!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(playlists) { playlist ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectPlaylist(playlist.id) },
                                color = DarkSurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(NeonViolet.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlaylistPlay,
                                            contentDescription = null,
                                            tint = NeonViolet,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCreateNewPlaylist,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create New Playlist", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(20.dp))
    )
}

@Composable
fun AddSongsToActivePlaylistDialog(
    playlist: PlaylistEntity,
    allSongs: List<SongEntity>,
    existingSongIds: Set<Long>,
    onDismiss: () -> Unit,
    onAddSong: (songId: Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSongs = remember(allSongs, searchQuery) {
        if (searchQuery.isBlank()) allSongs
        else allSongs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Add Songs to \"${playlist.name}\"",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Tap + to add tracks from your vault",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter songs...", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredSongs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (allSongs.isEmpty()) "No songs in your vault library yet." else "No songs matching \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(filteredSongs, key = { it.id }) { song ->
                            val isAlreadyAdded = existingSongIds.contains(song.id)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                color = DarkSurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NeonViolet.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlaylistPlay,
                                            contentDescription = null,
                                            tint = NeonViolet,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = song.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }

                                    if (isAlreadyAdded) {
                                        Surface(
                                            color = NeonCyan.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "Added",
                                                color = NeonCyan,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = { onAddSong(song.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp).testTag("add_song_to_playlist_btn_${song.id}")
                                        ) {
                                            Text("+ Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface,
        modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(20.dp))
    )
}
