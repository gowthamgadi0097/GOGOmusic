package com.example.data.model

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlaybackState(
    val currentSong: SongEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffle: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val queue: List<SongEntity> = emptyList(),
    val currentQueueIndex: Int = -1,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}
