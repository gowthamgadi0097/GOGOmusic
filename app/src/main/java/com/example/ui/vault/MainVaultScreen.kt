package com.example.ui.vault

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.ui.components.GoogleLogoIcon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlaylistEntity
import com.example.data.model.SongEntity
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.AddSongsToActivePlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.EqualizerBottomSheet
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.NowPlayingSheet
import com.example.ui.components.SongDetailsDialog
import com.example.ui.components.SongItemCard
import com.example.ui.components.UploadPromptCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainVaultScreen(
    viewModel: MainVaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()

    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val playlistSongs by viewModel.playlistSongs.collectAsStateWithLifecycle()
    val totalStorageBytes by viewModel.totalStorageBytes.collectAsStateWithLifecycle()

    var isSearchActive by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Direct File Explorer Picker Launcher
    // Directly opens Android system document / file manager to select audio files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importAudioFiles(context, uris)
        }
    }

    val openFileExplorer = {
        filePickerLauncher.launch(
            arrayOf(
                "audio/*",
                "audio/mpeg",
                "audio/mp4",
                "audio/x-wav",
                "audio/ogg",
                "audio/flac",
                "audio/aac"
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search songs, artists, albums...", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("search_text_field")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(NeonViolet, NeonCyan))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SoundVault",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Secure Music Storage",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Search toggle
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) {
                                viewModel.onSearchQueryChange("")
                            }
                        },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "Close Search" else "Search",
                            tint = TextPrimary
                        )
                    }

                    // Audio Equalizer DSP in App Bar
                    IconButton(
                        onClick = { viewModel.openEqualizerDialog() },
                        modifier = Modifier.testTag("appbar_equalizer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Equalizer",
                            tint = if (equalizerState.isEnabled) NeonCyan else TextMuted
                        )
                    }

                    // Direct Upload Action in App Bar
                    IconButton(
                        onClick = openFileExplorer,
                        modifier = Modifier.testTag("appbar_upload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Upload Music",
                            tint = NeonCyan
                        )
                    }

                    // Logout Button in App Bar for quick security
                    IconButton(
                        onClick = { viewModel.setShowLogoutConfirm(true) },
                        modifier = Modifier.testTag("appbar_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Log Out",
                            tint = NeonPink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = openFileExplorer,
                containerColor = NeonViolet,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                },
                text = {
                    Text(
                        text = "Upload Music",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = if (playbackState.currentSong != null) 64.dp else 0.dp)
                    .testTag("main_upload_fab")
            )
        },
        bottomBar = {
            Column {
                // Persistent Mini Player above bottom bar
                if (playbackState.currentSong != null) {
                    MiniPlayerBar(
                        playbackState = playbackState,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNext() },
                        onExpandPlayer = { viewModel.expandNowPlaying(true) }
                    )
                }

                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == VaultTab.TRACKS,
                        onClick = { viewModel.onTabSelected(VaultTab.TRACKS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Tracks"
                            )
                        },
                        label = { Text("Songs", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceHighlight
                        ),
                        modifier = Modifier.testTag("tab_songs")
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == VaultTab.FAVORITES,
                        onClick = { viewModel.onTabSelected(VaultTab.FAVORITES) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorites"
                            )
                        },
                        label = { Text("Favorites", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonPink,
                            selectedTextColor = NeonPink,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceHighlight
                        ),
                        modifier = Modifier.testTag("tab_favorites")
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == VaultTab.PLAYLISTS,
                        onClick = { viewModel.onTabSelected(VaultTab.PLAYLISTS) },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "Playlists"
                            )
                        },
                        label = { Text("Playlists", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonViolet,
                            selectedTextColor = NeonViolet,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceHighlight
                        ),
                        modifier = Modifier.testTag("tab_playlists")
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == VaultTab.STORAGE_VAULT,
                        onClick = { viewModel.onTabSelected(VaultTab.STORAGE_VAULT) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Vault Storage"
                            )
                        },
                        label = { Text("Vault", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceHighlight
                        ),
                        modifier = Modifier.testTag("tab_storage_vault")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Status Toast/Notification
                AnimatedVisibility(
                    visible = uiState.uploadStatusMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.uploadStatusMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearUploadStatus() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Main Content Based on Selected Tab
                when (uiState.selectedTab) {
                    VaultTab.TRACKS -> {
                        SongsTabContent(
                            songs = songs,
                            currentSong = playbackState.currentSong,
                            isPlaying = playbackState.isPlaying,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                            sortOption = uiState.sortOption,
                            onSortOptionChange = { viewModel.onSortOptionSelected(it) },
                            onUploadClick = openFileExplorer,
                            onLoadDemoTrack = { viewModel.createDemoTrack(context) },
                            onSongClick = { song -> viewModel.playSong(song, songs) },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onAddToPlaylist = { song -> viewModel.showAddToPlaylist(song) },
                            onShowDetails = { song -> viewModel.showSongDetails(song) },
                            onDeleteSong = { song -> viewModel.deleteSong(song) }
                        )
                    }
                    VaultTab.FAVORITES -> {
                        FavoritesTabContent(
                            favorites = favorites,
                            currentSong = playbackState.currentSong,
                            isPlaying = playbackState.isPlaying,
                            onUploadClick = openFileExplorer,
                            onSongClick = { song -> viewModel.playSong(song, favorites) },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onAddToPlaylist = { song -> viewModel.showAddToPlaylist(song) },
                            onShowDetails = { song -> viewModel.showSongDetails(song) },
                            onDeleteSong = { song -> viewModel.deleteSong(song) }
                        )
                    }
                    VaultTab.PLAYLISTS -> {
                        PlaylistsTabContent(
                            playlists = playlists,
                            selectedPlaylist = uiState.selectedPlaylist,
                            playlistSongs = playlistSongs,
                            currentSong = playbackState.currentSong,
                            isPlaying = playbackState.isPlaying,
                            onCreatePlaylistClick = { viewModel.setShowCreatePlaylist(true) },
                            onSelectPlaylist = { playlist -> viewModel.selectPlaylist(playlist) },
                            onBackFromPlaylist = { viewModel.selectPlaylist(null) },
                            onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                            onPlayPlaylist = { playlist, startSongIndex, shuffle ->
                                viewModel.playPlaylist(playlist, startSongIndex, shuffle)
                            },
                            onRemoveSongFromPlaylist = { playlistId, songId ->
                                viewModel.removeSongFromPlaylist(playlistId, songId)
                            },
                            onOpenAddSongsDialog = { viewModel.setShowAddSongsToActivePlaylist(true) },
                            onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onShowDetails = { song -> viewModel.showSongDetails(song) },
                            onDeleteSong = { song -> viewModel.deleteSong(song) }
                        )
                    }
                    VaultTab.STORAGE_VAULT -> {
                        StorageVaultTabContent(
                            user = uiState.currentUser,
                            songCount = songs.size,
                            totalBytes = totalStorageBytes,
                            onUploadClick = openFileExplorer,
                            onLoadDemoTrack = { viewModel.createDemoTrack(context) },
                            onLogoutClick = { viewModel.setShowLogoutConfirm(true) }
                        )
                    }
                }
            }
        }
    }

    // Full Expanded Now Playing Sheet
    if (uiState.isNowPlayingExpanded && playbackState.currentSong != null) {
        NowPlayingSheet(
            playbackState = playbackState,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.playNext() },
            onPrevious = { viewModel.playPrevious() },
            onSeekTo = { pos -> viewModel.seekTo(pos) },
            onToggleRepeat = { viewModel.toggleRepeat() },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onSetSpeed = { speed -> viewModel.setPlaybackSpeed(speed) },
            onToggleFavorite = { playbackState.currentSong?.let { viewModel.toggleFavorite(it) } },
            onDismiss = { viewModel.expandNowPlaying(false) },
            onShowStorageInfo = {
                viewModel.showSongDetails(playbackState.currentSong)
            },
            onOpenEqualizer = { viewModel.openEqualizerDialog() },
            currentEqualizerPresetName = equalizerState.currentPreset.displayName
        )
    }

    // Audio Equalizer Bottom Sheet
    if (uiState.showEqualizerDialog) {
        EqualizerBottomSheet(
            equalizerState = equalizerState,
            isPlaying = playbackState.isPlaying,
            onSetEnabled = { enabled -> viewModel.setEqualizerEnabled(enabled) },
            onApplyPreset = { preset -> viewModel.applyEqualizerPreset(preset) },
            onSetBandGain = { index, gain -> viewModel.setEqualizerBandGain(index, gain) },
            onSetBassBoost = { strength -> viewModel.setBassBoost(strength) },
            onSetVirtualizer = { strength -> viewModel.setVirtualizer(strength) },
            onReset = { viewModel.resetEqualizer() },
            onDismiss = { viewModel.closeEqualizerDialog() }
        )
    }

    // Song Storage Details Dialog
    uiState.selectedSongForDetails?.let { song ->
        SongDetailsDialog(
            song = song,
            onDismiss = { viewModel.showSongDetails(null) }
        )
    }

    // Add To Playlist Dialog
    uiState.selectedSongForPlaylist?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { viewModel.showAddToPlaylist(null) },
            onSelectPlaylist = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, song.id)
            },
            onCreateNewPlaylist = {
                viewModel.showAddToPlaylist(null)
                viewModel.setShowCreatePlaylist(true)
            }
        )
    }

    // Create New Playlist Dialog
    if (uiState.showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { viewModel.setShowCreatePlaylist(false) },
            onCreate = { name, desc ->
                viewModel.createPlaylist(name, desc)
            }
        )
    }

    // Add Songs to Active Playlist Dialog
    if (uiState.showAddSongsToActivePlaylistDialog && uiState.selectedPlaylist != null) {
        AddSongsToActivePlaylistDialog(
            playlist = uiState.selectedPlaylist!!,
            allSongs = songs,
            existingSongIds = playlistSongs.map { it.id }.toSet(),
            onDismiss = { viewModel.setShowAddSongsToActivePlaylist(false) },
            onAddSong = { songId ->
                viewModel.addSongToPlaylist(uiState.selectedPlaylist!!.id, songId)
            }
        )
    }

    // Logout Confirmation Dialog
    if (uiState.showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowLogoutConfirm(false) },
            title = {
                Text(
                    text = "Lock Vault & Log Out?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Your uploaded audio files remain securely encrypted in your on-device vault. You will need your login credentials to unlock them again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.setShowLogoutConfirm(false) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(20.dp))
        )
    }
}

@Composable
private fun SongsTabContent(
    songs: List<SongEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    onUploadClick: () -> Unit,
    onLoadDemoTrack: () -> Unit,
    onSongClick: (SongEntity) -> Unit,
    onToggleFavorite: (SongEntity) -> Unit,
    onAddToPlaylist: (SongEntity) -> Unit,
    onShowDetails: (SongEntity) -> Unit,
    onDeleteSong: (SongEntity) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Direct Hero Upload Prompt Card
        item {
            UploadPromptCard(onUploadClick = onUploadClick)
        }

        // Search Bar for Title, Artist, or Album
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search title, artist, or album...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar")
            )
        }

        // Horizontal Quick Sort Chips: Title, Artist, Album, Upload Date, Duration
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = sortOption == SortOption.DATE_DESC,
                    onClick = { onSortOptionChange(SortOption.DATE_DESC) },
                    label = { Text("Upload Date", fontSize = 12.sp) },
                    leadingIcon = if (sortOption == SortOption.DATE_DESC) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonViolet.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        selectedLeadingIconColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("sort_date")
                )
                FilterChip(
                    selected = sortOption == SortOption.TITLE_ASC,
                    onClick = { onSortOptionChange(SortOption.TITLE_ASC) },
                    label = { Text("Title", fontSize = 12.sp) },
                    leadingIcon = if (sortOption == SortOption.TITLE_ASC) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonViolet.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        selectedLeadingIconColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("sort_title")
                )
                FilterChip(
                    selected = sortOption == SortOption.ARTIST_ASC,
                    onClick = { onSortOptionChange(SortOption.ARTIST_ASC) },
                    label = { Text("Artist", fontSize = 12.sp) },
                    leadingIcon = if (sortOption == SortOption.ARTIST_ASC) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonViolet.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        selectedLeadingIconColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("sort_artist")
                )
                FilterChip(
                    selected = sortOption == SortOption.ALBUM_ASC,
                    onClick = { onSortOptionChange(SortOption.ALBUM_ASC) },
                    label = { Text("Album", fontSize = 12.sp) },
                    leadingIcon = if (sortOption == SortOption.ALBUM_ASC) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonViolet.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        selectedLeadingIconColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("sort_album")
                )
                FilterChip(
                    selected = sortOption == SortOption.DURATION_DESC,
                    onClick = { onSortOptionChange(SortOption.DURATION_DESC) },
                    label = { Text("Duration", fontSize = 12.sp) },
                    leadingIcon = if (sortOption == SortOption.DURATION_DESC) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonViolet.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        selectedLeadingIconColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("sort_duration")
                )
            }
        }

        // Header bar with count and sort
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "All Vault Tracks (${songs.size})" else "Search Results (${songs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Box {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("sort_dropdown_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (sortOption) {
                                SortOption.DATE_DESC -> "Recent"
                                SortOption.TITLE_ASC -> "Title"
                                SortOption.ARTIST_ASC -> "Artist"
                                SortOption.ALBUM_ASC -> "Album"
                                SortOption.DURATION_DESC -> "Duration"
                            },
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Recently Added", color = TextPrimary) },
                            onClick = {
                                onSortOptionChange(SortOption.DATE_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Title (A-Z)", color = TextPrimary) },
                            onClick = {
                                onSortOptionChange(SortOption.TITLE_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Artist Name", color = TextPrimary) },
                            onClick = {
                                onSortOptionChange(SortOption.ARTIST_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Album (A-Z)", color = TextPrimary) },
                            onClick = {
                                onSortOptionChange(SortOption.ALBUM_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Longest Duration", color = TextPrimary) },
                            onClick = {
                                onSortOptionChange(SortOption.DURATION_DESC)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Empty state if no songs
        if (songs.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                    color = DarkSurfaceElevated.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NeonViolet.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = NeonViolet,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Audio Files Stored Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Directly open your device file explorer to store songs into SoundVault, or sample our acoustic prelude.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onUploadClick,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open File Explorer to Upload", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onLoadDemoTrack,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text("Try Acoustic Sample Track")
                        }
                    }
                }
            }
        } else {
            items(songs, key = { it.id }) { song ->
                SongItemCard(
                    song = song,
                    isCurrentSong = currentSong?.id == song.id,
                    isPlaying = isPlaying && currentSong?.id == song.id,
                    onSongClick = { onSongClick(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) },
                    onShowDetails = { onShowDetails(song) },
                    onDeleteSong = { onDeleteSong(song) }
                )
            }
        }

        // Bottom spacer for FAB & mini player
        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun FavoritesTabContent(
    favorites: List<SongEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    onUploadClick: () -> Unit,
    onSongClick: (SongEntity) -> Unit,
    onToggleFavorite: (SongEntity) -> Unit,
    onAddToPlaylist: (SongEntity) -> Unit,
    onShowDetails: (SongEntity) -> Unit,
    onDeleteSong: (SongEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Favorite Vault Tracks (${favorites.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        if (favorites.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = DarkSurfaceElevated.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = NeonPink.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Starred Songs Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the heart icon on any track in your library to keep quick access here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(favorites, key = { it.id }) { song ->
                SongItemCard(
                    song = song,
                    isCurrentSong = currentSong?.id == song.id,
                    isPlaying = isPlaying && currentSong?.id == song.id,
                    onSongClick = { onSongClick(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) },
                    onShowDetails = { onShowDetails(song) },
                    onDeleteSong = { onDeleteSong(song) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<PlaylistEntity>,
    selectedPlaylist: PlaylistEntity?,
    playlistSongs: List<SongEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    onCreatePlaylistClick: () -> Unit,
    onSelectPlaylist: (PlaylistEntity) -> Unit,
    onBackFromPlaylist: () -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlayPlaylist: (PlaylistEntity, startSongIndex: Int, shuffle: Boolean) -> Unit,
    onRemoveSongFromPlaylist: (playlistId: Long, songId: Long) -> Unit,
    onOpenAddSongsDialog: () -> Unit,
    onSongClick: (SongEntity, List<SongEntity>) -> Unit,
    onToggleFavorite: (SongEntity) -> Unit,
    onShowDetails: (SongEntity) -> Unit,
    onDeleteSong: (SongEntity) -> Unit
) {
    if (selectedPlaylist != null) {
        // Detailed playlist view
        val totalMs = remember(playlistSongs) { playlistSongs.sumOf { it.durationMs } }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Navigation & Actions
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBackFromPlaylist,
                        modifier = Modifier.testTag("playlist_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Playlists",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedPlaylist.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        if (selectedPlaylist.description.isNotBlank()) {
                            Text(
                                text = selectedPlaylist.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = { onDeletePlaylist(selectedPlaylist.id) },
                        modifier = Modifier.testTag("playlist_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Playlist",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Playlist Banner Card & Actions
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                    color = DarkSurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(listOf(NeonViolet, NeonCyan))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "${playlistSongs.size} Tracks in Sequence",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Total length: ${TimeUtils.formatDuration(totalMs)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Controls: Play All in Sequence, Shuffle, Add Songs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onPlayPlaylist(selectedPlaylist, 0, false) },
                                enabled = playlistSongs.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("playlist_play_sequence_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onPlayPlaylist(selectedPlaylist, 0, true) },
                                enabled = playlistSongs.isNotEmpty(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("playlist_shuffle_button")
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Shuffle", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = onOpenAddSongsDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("playlist_add_songs_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Tracks", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Playlist Tracks List
            if (playlistSongs.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                        color = DarkSurfaceElevated.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No songs in this playlist yet",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Add songs to play this playlist in sequence or randomized shuffle mode.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onOpenAddSongsDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("playlist_empty_add_songs_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Songs Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(playlistSongs.size, key = { index -> playlistSongs[index].id }) { index ->
                    val song = playlistSongs[index]
                    val isCurrent = currentSong?.id == song.id

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) NeonCyan else CardBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onPlayPlaylist(selectedPlaylist, index, false) }
                            .testTag("playlist_song_item_${song.id}"),
                        color = if (isCurrent) DarkSurfaceHighlight else DarkSurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Track Sequence Number / Playing Indicator
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrent) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceHighlight),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrent && isPlaying) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Playing",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) NeonCyan else TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Song Metadata
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isCurrent) NeonCyan else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${song.artist} • ${song.album}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = TimeUtils.formatDuration(song.durationMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            // Quick Remove from Playlist button
                            IconButton(
                                onClick = { onRemoveSongFromPlaylist(selectedPlaylist.id, song.id) },
                                modifier = Modifier.testTag("playlist_remove_song_${song.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RemoveCircleOutline,
                                    contentDescription = "Remove from Playlist",
                                    tint = NeonPink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Playlists (${playlists.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Button(
                        onClick = onCreatePlaylistClick,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("create_playlist_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(playlists, key = { it.id }) { playlist ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .clickable { onSelectPlaylist(playlist) }
                        .testTag("playlist_card_${playlist.id}"),
                    color = DarkSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(NeonViolet, Color(0xFF6366F1)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            if (playlist.description.isNotBlank()) {
                                Text(
                                    text = playlist.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Direct Quick Play button
                        IconButton(
                            onClick = { onPlayPlaylist(playlist, 0, false) },
                            modifier = Modifier.testTag("playlist_quick_play_${playlist.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Playlist in Sequence",
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeletePlaylist(playlist.id) },
                            modifier = Modifier.testTag("playlist_delete_item_${playlist.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
private fun StorageVaultTabContent(
    user: com.example.data.model.UserEntity?,
    songCount: Int,
    totalBytes: Long,
    onUploadClick: () -> Unit,
    onLoadDemoTrack: () -> Unit,
    onLogoutClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                color = DarkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(NeonViolet, NeonCyan))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.username?.take(1) ?: "V").uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.username ?: "Authenticated User",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = user?.email ?: "vault.account@soundvault.local",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            if (user?.userId?.startsWith("google_") == true || user?.passwordHash == "google_oauth_verified") {
                                GoogleLogoIcon(size = 14.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google Account Linked",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF60A5FA)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Vault Protected Sandbox",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.testTag("vault_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Log Out",
                            tint = NeonPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Storage Statistics Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                color = DarkSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Storage & Security Metrics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Stored Audio Files",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = "$songCount Tracks",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = NeonCyan
                            )
                        }

                        Column {
                            Text(
                                text = "Internal Storage Used",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = TimeUtils.formatFileSize(totalBytes),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = NeonViolet
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "• All files are stored directly in Android application internal sandbox\n• No files are uploaded to external clouds or third-party servers\n• Passwords are encrypted with salted SHA-256 hashes",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Quick Vault Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onUploadClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Audio via File Explorer", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onLoadDemoTrack,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synthesize Demo Acoustic Track")
                }

                Button(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceElevated,
                        contentColor = NeonPink
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out & Lock Vault", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}
