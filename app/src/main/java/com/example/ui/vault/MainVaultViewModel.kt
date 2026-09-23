package com.example.ui.vault

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SessionManager
import com.example.data.SoundVaultRepository
import com.example.data.model.PlaybackState
import com.example.data.model.PlaylistEntity
import com.example.data.model.SongEntity
import com.example.data.model.UserEntity
import com.example.player.MusicPlayerController
import com.example.player.equalizer.EqualizerPreset
import com.example.player.equalizer.EqualizerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

enum class VaultTab {
    TRACKS,
    FAVORITES,
    PLAYLISTS,
    STORAGE_VAULT
}

enum class SortOption {
    DATE_DESC,
    TITLE_ASC,
    ARTIST_ASC,
    ALBUM_ASC,
    DURATION_DESC
}

data class MainVaultUiState(
    val currentUser: UserEntity? = null,
    val selectedTab: VaultTab = VaultTab.TRACKS,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.DATE_DESC,
    val isUploading: Boolean = false,
    val uploadStatusMessage: String? = null,
    val selectedSongForDetails: SongEntity? = null,
    val selectedSongForPlaylist: SongEntity? = null,
    val isNowPlayingExpanded: Boolean = false,
    val showCreatePlaylistDialog: Boolean = false,
    val showAddSongsToActivePlaylistDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false,
    val showEqualizerDialog: Boolean = false,
    val selectedPlaylist: PlaylistEntity? = null,
    val totalStorageBytes: Long = 0L
)

class MainVaultViewModel(
    private val repository: SoundVaultRepository,
    private val sessionManager: SessionManager,
    private val playerController: MusicPlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainVaultUiState())
    val uiState: StateFlow<MainVaultUiState> = _uiState.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playerController.playbackState
    val equalizerState: StateFlow<EqualizerState> = playerController.equalizerState

    val currentUserId: StateFlow<String?> = sessionManager.currentUserId

    // Reactive songs list depending on current user and search query
    val songs: StateFlow<List<SongEntity>> = combine(
        sessionManager.currentUserId,
        _uiState
    ) { userId, state ->
        Pair(userId, state)
    }.flatMapLatest { (userId, state) ->
        if (userId == null) flowOf(emptyList())
        else if (state.searchQuery.isBlank()) repository.getSongs(userId)
        else repository.searchSongs(userId, state.searchQuery)
    }.combine(_uiState) { list, state ->
        when (state.sortOption) {
            SortOption.DATE_DESC -> list.sortedByDescending { it.dateAdded }
            SortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            SortOption.ARTIST_ASC -> list.sortedBy { it.artist.lowercase() }
            SortOption.ALBUM_ASC -> list.sortedBy { it.album.lowercase() }
            SortOption.DURATION_DESC -> list.sortedByDescending { it.durationMs }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<SongEntity>> = sessionManager.currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList())
        else repository.getFavorites(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = sessionManager.currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList())
        else repository.getPlaylists(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val playlistSongs: StateFlow<List<SongEntity>> = _uiState
        .map { it.selectedPlaylist?.id }
        .distinctUntilChanged()
        .flatMapLatest { playlistId ->
            if (playlistId == null) flowOf(emptyList())
            else repository.getSongsInPlaylist(playlistId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStorageBytes: StateFlow<Long> = sessionManager.currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(0L)
        else repository.getTotalStorageBytes(userId)
    }.combine(flowOf(Unit)) { bytes, _ ->
        bytes ?: 0L
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId.value
            if (userId != null) {
                val user = repository.getUserById(userId)
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun onTabSelected(tab: VaultTab) {
        _uiState.update { it.copy(selectedTab = tab, selectedPlaylist = null) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSortOptionSelected(sortOption: SortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _uiState.update { it.copy(selectedPlaylist = playlist) }
    }

    fun showSongDetails(song: SongEntity?) {
        _uiState.update { it.copy(selectedSongForDetails = song) }
    }

    fun showAddToPlaylist(song: SongEntity?) {
        _uiState.update { it.copy(selectedSongForPlaylist = song) }
    }

    fun setShowCreatePlaylist(show: Boolean) {
        _uiState.update { it.copy(showCreatePlaylistDialog = show) }
    }

    fun setShowLogoutConfirm(show: Boolean) {
        _uiState.update { it.copy(showLogoutConfirmDialog = show) }
    }

    fun expandNowPlaying(expand: Boolean) {
        _uiState.update { it.copy(isNowPlayingExpanded = expand) }
    }

    // Directly imports audio files chosen from Android file explorer
    fun importAudioFiles(context: Context, uris: List<Uri>) {
        val userId = sessionManager.currentUserId.value ?: return
        if (uris.isEmpty()) return

        _uiState.update {
            it.copy(
                isUploading = true,
                uploadStatusMessage = "Storing ${uris.size} music file(s) into vault..."
            )
        }

        viewModelScope.launch {
            var successCount = 0
            var firstImportedSong: SongEntity? = null

            for (uri in uris) {
                val result = repository.importSongFromUri(context, uri, userId)
                if (result.isSuccess) {
                    val imported = result.getOrThrow()
                    if (firstImportedSong == null) {
                        firstImportedSong = imported
                    }
                    successCount++
                }
            }

            _uiState.update {
                it.copy(
                    isUploading = false,
                    uploadStatusMessage = "Successfully added $successCount song(s) to vault!"
                )
            }

            // Auto-play first imported song if nothing is playing
            if (firstImportedSong != null && playbackState.value.currentSong == null) {
                playerController.playSong(firstImportedSong, listOf(firstImportedSong))
            }
        }
    }

    fun createDemoTrack(context: Context) {
        val userId = sessionManager.currentUserId.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadStatusMessage = "Synthesizing demo acoustic prelude...") }
            val song = repository.createDemoTrackIfNeeded(context, userId)
            _uiState.update { it.copy(isUploading = false, uploadStatusMessage = "Demo track added to vault!") }
            if (song != null && playbackState.value.currentSong == null) {
                playerController.playSong(song, listOf(song))
            }
        }
    }

    fun playSong(song: SongEntity, queue: List<SongEntity> = emptyList()) {
        val targetQueue = if (queue.isNotEmpty()) queue else songs.value
        playerController.playSong(song, targetQueue)
        viewModelScope.launch {
            repository.recordSongPlayed(song.id)
        }
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun playNext() {
        playerController.playNext()
    }

    fun playPrevious() {
        playerController.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun toggleRepeat() {
        playerController.toggleRepeat()
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerController.setPlaybackSpeed(speed)
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun deleteSong(song: SongEntity) {
        viewModelScope.launch {
            if (playbackState.value.currentSong?.id == song.id) {
                playerController.pause()
            }
            repository.deleteSong(song)
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        val userId = sessionManager.currentUserId.value ?: return
        viewModelScope.launch {
            repository.createPlaylist(userId, name, description)
            _uiState.update { it.copy(showCreatePlaylistDialog = false) }
        }
    }

    fun setShowAddSongsToActivePlaylist(show: Boolean) {
        _uiState.update { it.copy(showAddSongsToActivePlaylistDialog = show) }
    }

    fun playPlaylist(playlist: PlaylistEntity, startSongIndex: Int = 0, shuffle: Boolean = false) {
        val currentPlaylistSongs = playlistSongs.value
        if (currentPlaylistSongs.isNotEmpty()) {
            val index = startSongIndex.coerceIn(0, currentPlaylistSongs.size - 1)
            val targetSong = currentPlaylistSongs[index]
            if (shuffle) {
                if (!playbackState.value.isShuffle) {
                    playerController.toggleShuffle()
                }
                playerController.playSong(targetSong, currentPlaylistSongs)
            } else {
                playerController.playSong(targetSong, currentPlaylistSongs, forceSequential = true)
            }
            viewModelScope.launch {
                repository.recordSongPlayed(targetSong.id)
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            _uiState.update { it.copy(selectedPlaylist = null) }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            _uiState.update { it.copy(selectedSongForPlaylist = null) }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun clearUploadStatus() {
        _uiState.update { it.copy(uploadStatusMessage = null) }
    }

    // Equalizer UI & Control
    fun openEqualizerDialog() {
        _uiState.update { it.copy(showEqualizerDialog = true) }
    }

    fun closeEqualizerDialog() {
        _uiState.update { it.copy(showEqualizerDialog = false) }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        playerController.setEqualizerEnabled(enabled)
    }

    fun applyEqualizerPreset(preset: EqualizerPreset) {
        playerController.applyEqualizerPreset(preset)
    }

    fun setEqualizerBandGain(bandIndex: Int, gainDb: Int) {
        playerController.setEqualizerBandGain(bandIndex, gainDb)
    }

    fun setBassBoost(strengthPercent: Int) {
        playerController.setBassBoost(strengthPercent)
    }

    fun setVirtualizer(strengthPercent: Int) {
        playerController.setVirtualizer(strengthPercent)
    }

    fun resetEqualizer() {
        playerController.resetEqualizer()
    }

    fun logout() {
        playerController.pause()
        sessionManager.logout()
        _uiState.update { it.copy(showLogoutConfirmDialog = false, currentUser = null) }
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
