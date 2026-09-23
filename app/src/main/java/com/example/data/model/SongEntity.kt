package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val filePath: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val albumArtPath: String? = null,
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null
)
