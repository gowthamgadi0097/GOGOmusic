package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPlaylistsForUser(userId: String): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.orderIndex ASC
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getPlaylistSongCount(playlistId: Long): Flow<Int>
}
