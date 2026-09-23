package com.example

import com.example.data.dao.PlaylistDao
import com.example.data.dao.SongDao
import com.example.data.dao.UserDao
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.SongEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserDao : UserDao {
    val users = mutableMapOf<String, UserEntity>()

    override suspend fun insertUser(user: UserEntity) {
        users[user.userId] = user
    }

    override suspend fun getUserByUsername(username: String): UserEntity? {
        return users.values.firstOrNull { it.username.equals(username, ignoreCase = true) }
    }

    override suspend fun getUserByEmail(email: String): UserEntity? {
        return users.values.firstOrNull { it.email.equals(email, ignoreCase = true) }
    }

    override suspend fun getUserById(userId: String): UserEntity? {
        return users[userId]
    }

    override suspend fun getUserCount(): Int = users.size
}

class FakeSongDao : SongDao {
    override suspend fun insertSong(song: SongEntity): Long = 1L
    override suspend fun updateSong(song: SongEntity) {}
    override suspend fun deleteSong(song: SongEntity) {}
    override suspend fun deleteSongById(id: Long) {}
    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {}
    override suspend fun incrementPlayCount(id: Long, timestamp: Long) {}
    override fun getSongsForUser(userId: String): Flow<List<SongEntity>> = flowOf(emptyList())
    override fun getFavoritesForUser(userId: String): Flow<List<SongEntity>> = flowOf(emptyList())
    override fun searchSongs(userId: String, query: String): Flow<List<SongEntity>> = flowOf(emptyList())
    override suspend fun getSongById(id: Long): SongEntity? = null
    override fun getSongCount(userId: String): Flow<Int> = flowOf(0)
    override fun getTotalStorageBytes(userId: String): Flow<Long?> = flowOf(0L)
}

class FakePlaylistDao : PlaylistDao {
    val playlists = mutableListOf<PlaylistEntity>()
    override suspend fun insertPlaylist(playlist: PlaylistEntity): Long {
        playlists.add(playlist)
        return playlists.size.toLong()
    }
    override suspend fun deletePlaylist(playlist: PlaylistEntity) {
        playlists.remove(playlist)
    }
    override suspend fun deletePlaylistById(playlistId: Long) {
        playlists.removeAll { it.id == playlistId }
    }
    override fun getPlaylistsForUser(userId: String): Flow<List<PlaylistEntity>> = flowOf(playlists)
    override suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef) {}
    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {}
    override fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>> = flowOf(emptyList())
    override fun getPlaylistSongCount(playlistId: Long): Flow<Int> = flowOf(0)
}
