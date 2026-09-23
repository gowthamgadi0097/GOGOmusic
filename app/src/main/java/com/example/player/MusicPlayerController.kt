package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import com.example.data.model.PlaybackState
import com.example.data.model.RepeatMode
import com.example.data.model.SongEntity
import com.example.player.equalizer.AudioEqualizerManager
import com.example.player.equalizer.EqualizerPreset
import com.example.player.equalizer.EqualizerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class MusicPlayerController(
    private val context: Context,
    private val onSongFinished: ((SongEntity) -> Unit)? = null
) {
    private val TAG = "MusicPlayerController"
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    val equalizerManager = AudioEqualizerManager(context)
    val equalizerState: StateFlow<EqualizerState> = equalizerManager.state

    private var mediaPlayer: MediaPlayer? = null
    private var originalQueue: List<SongEntity> = emptyList()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private fun getOrCreatePlayer(): MediaPlayer {
        return mediaPlayer ?: MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener {
                handleSongCompletion()
            }
            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                _playbackState.update { it.copy(isPlaying = false, errorMessage = "Playback error ($what, $extra)") }
                true
            }
            mediaPlayer = this
        }
    }

    fun playSong(song: SongEntity, newQueue: List<SongEntity> = emptyList(), forceSequential: Boolean = false) {
        val baseQueue = if (newQueue.isNotEmpty()) {
            originalQueue = newQueue
            newQueue
        } else if (originalQueue.isNotEmpty()) {
            originalQueue
        } else {
            val cur = _playbackState.value.queue
            if (cur.contains(song)) cur else listOf(song)
        }

        val queue = if (_playbackState.value.isShuffle && !forceSequential && baseQueue.size > 1) {
            // Keep selected song at index 0, randomize remaining songs in queue
            listOf(song) + baseQueue.filter { it.id != song.id }.shuffled()
        } else {
            baseQueue
        }

        val index = queue.indexOfFirst { it.id == song.id }.let { if (it == -1) 0 else it }
        playSongInternal(song, queue, index)
    }

    private fun playSongInternal(song: SongEntity, queue: List<SongEntity>, index: Int) {
        try {
            val file = File(song.filePath)
            if (!file.exists()) {
                _playbackState.update {
                    it.copy(errorMessage = "File not found: ${song.title}")
                }
                return
            }

            val player = getOrCreatePlayer()
            player.reset()
            player.setDataSource(file.absolutePath)
            player.prepare()

            // Apply playback speed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val params = PlaybackParams().apply {
                        speed = _playbackState.value.playbackSpeed
                    }
                    player.playbackParams = params
                } catch (e: Exception) {
                    Log.w(TAG, "Could not set playback speed: ${e.message}")
                }
            }

            player.start()
            try {
                equalizerManager.attachAudioSession(player.audioSessionId)
            } catch (e: Exception) {
                Log.w(TAG, "AudioFX attach failed: ${e.message}")
            }

            val realDuration = if (player.duration > 0) player.duration.toLong() else song.durationMs

            _playbackState.update {
                it.copy(
                    currentSong = song,
                    isPlaying = true,
                    currentPositionMs = 0L,
                    durationMs = realDuration,
                    queue = queue,
                    currentQueueIndex = index,
                    errorMessage = null
                )
            }

            startProgressTracker()
            onSongFinished?.invoke(song)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing song: ${e.message}", e)
            _playbackState.update {
                it.copy(isPlaying = false, errorMessage = "Failed to play audio: ${e.localizedMessage}")
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        val currentSong = _playbackState.value.currentSong ?: return

        if (player.isPlaying) {
            player.pause()
            _playbackState.update { it.copy(isPlaying = false) }
            stopProgressTracker()
        } else {
            player.start()
            _playbackState.update { it.copy(isPlaying = true) }
            startProgressTracker()
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.update { it.copy(isPlaying = false) }
                stopProgressTracker()
            }
        }
    }

    fun resume() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying && _playbackState.value.currentSong != null) {
                player.start()
                _playbackState.update { it.copy(isPlaying = true) }
                startProgressTracker()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            try {
                val clamped = positionMs.coerceIn(0L, _playbackState.value.durationMs)
                player.seekTo(clamped.toInt())
                _playbackState.update { it.copy(currentPositionMs = clamped) }
            } catch (e: Exception) {
                Log.w(TAG, "Error seeking: ${e.message}")
            }
        }
    }

    fun playNext() {
        val state = _playbackState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        val nextIndex = when {
            state.currentQueueIndex + 1 < queue.size -> {
                state.currentQueueIndex + 1
            }
            state.repeatMode == RepeatMode.ALL -> {
                // Repeat entire queue / playlist
                0
            }
            else -> {
                // End of queue in RepeatMode.OFF
                mediaPlayer?.pause()
                _playbackState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
                stopProgressTracker()
                return
            }
        }

        val nextSong = queue[nextIndex]
        playSongInternal(nextSong, queue, nextIndex)
    }

    fun playPrevious() {
        val state = _playbackState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        // If played more than 3 seconds, restart current track
        if (state.currentPositionMs > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = when {
            state.currentQueueIndex - 1 >= 0 -> {
                state.currentQueueIndex - 1
            }
            state.repeatMode == RepeatMode.ALL -> {
                queue.size - 1
            }
            else -> {
                0
            }
        }

        val prevSong = queue[prevIndex]
        playSongInternal(prevSong, queue, prevIndex)
    }

    fun toggleRepeat() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.update { it.copy(repeatMode = nextMode) }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _playbackState.update { it.copy(repeatMode = mode) }
    }

    fun toggleShuffle() {
        val state = _playbackState.value
        val newShuffle = !state.isShuffle
        val currentSong = state.currentSong
        val queue = state.queue

        if (newShuffle && queue.size > 1 && currentSong != null) {
            // Randomize order of remaining songs in queue
            val others = queue.filter { it.id != currentSong.id }.shuffled()
            val randomizedQueue = listOf(currentSong) + others
            _playbackState.update {
                it.copy(
                    isShuffle = true,
                    queue = randomizedQueue,
                    currentQueueIndex = 0
                )
            }
        } else if (!newShuffle && originalQueue.isNotEmpty() && currentSong != null) {
            // Restore original sequence
            val origIdx = originalQueue.indexOfFirst { it.id == currentSong.id }.coerceAtLeast(0)
            _playbackState.update {
                it.copy(
                    isShuffle = false,
                    queue = originalQueue,
                    currentQueueIndex = origIdx
                )
            }
        } else {
            _playbackState.update { it.copy(isShuffle = newShuffle) }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackState.update { it.copy(playbackSpeed = speed) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { player ->
                    val params = player.playbackParams.apply {
                        this.speed = speed
                    }
                    player.playbackParams = params
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error updating playback params speed: ${e.message}")
            }
        }
    }

    private fun handleSongCompletion() {
        val state = _playbackState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                // Repeat the current song
                seekTo(0L)
                mediaPlayer?.start()
                _playbackState.update { it.copy(isPlaying = true) }
                startProgressTracker()
            }
            RepeatMode.ALL -> {
                // Repeat entire queue / playlist (loops to index 0 after last song)
                playNext()
            }
            RepeatMode.OFF -> {
                // In OFF mode, play next if available, or stop at end of queue
                if (state.currentQueueIndex + 1 < state.queue.size) {
                    playNext()
                } else {
                    mediaPlayer?.pause()
                    _playbackState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
                    stopProgressTracker()
                }
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            val pos = player.currentPosition.toLong()
                            val dur = if (player.duration > 0) player.duration.toLong() else _playbackState.value.durationMs
                            _playbackState.update {
                                it.copy(currentPositionMs = pos, durationMs = dur)
                            }
                        }
                    } catch (e: Exception) {
                        // ignore state errors during async transitions
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        try {
            equalizerManager.release()
        } catch (ignored: Exception) {}
        try {
            mediaPlayer?.release()
        } catch (ignored: Exception) {}
        mediaPlayer = null
    }

    // Equalizer Delegations
    fun setEqualizerEnabled(enabled: Boolean) = equalizerManager.setEnabled(enabled)
    fun applyEqualizerPreset(preset: EqualizerPreset) = equalizerManager.applyPreset(preset)
    fun setEqualizerBandGain(bandIndex: Int, gainDb: Int) = equalizerManager.setBandGain(bandIndex, gainDb)
    fun setBassBoost(strengthPercent: Int) = equalizerManager.setBassBoost(strengthPercent)
    fun setVirtualizer(strengthPercent: Int) = equalizerManager.setVirtualizer(strengthPercent)
    fun resetEqualizer() = equalizerManager.resetToFlat()
}
