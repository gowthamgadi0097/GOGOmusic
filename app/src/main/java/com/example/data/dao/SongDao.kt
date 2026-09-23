package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs WHERE userId = :userId ORDER BY dateAdded DESC")
    fun getSongsForUser(userId: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE userId = :userId AND isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoritesForUser(userId: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    @Update
    suspend fun updateSong(song: SongEntity)

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: Long, timestamp: Long)

    @Query("""
        SELECT * FROM songs 
        WHERE userId = :userId 
          AND (title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%')
        ORDER BY dateAdded DESC
    """)
    fun searchSongs(userId: String, query: String): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM songs WHERE userId = :userId")
    fun getSongCount(userId: String): Flow<Int>

    @Query("SELECT SUM(fileSizeBytes) FROM songs WHERE userId = :userId")
    fun getTotalStorageBytes(userId: String): Flow<Long?>
}
