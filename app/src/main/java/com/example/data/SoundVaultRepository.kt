package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.dao.PlaylistDao
import com.example.data.dao.SongDao
import com.example.data.dao.UserDao
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.SongEntity
import com.example.data.model.UserEntity
import com.example.util.AudioImportHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class SoundVaultRepository(
    private val userDao: UserDao,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    // User / Auth operations
    suspend fun registerUser(username: String, email: String, password: String): Result<UserEntity> =
        withContext(ioDispatcher) {
            val trimmedUsername = username.trim()
            val trimmedEmail = email.trim()

            if (trimmedUsername.length < 3) {
                return@withContext Result.failure(Exception("Username must be at least 3 characters"))
            }
            if (password.length < 4) {
                return@withContext Result.failure(Exception("Password must be at least 4 characters"))
            }

            val existingUser = userDao.getUserByUsername(trimmedUsername)
            if (existingUser != null) {
                return@withContext Result.failure(Exception("Username already exists"))
            }

            val existingEmail = userDao.getUserByEmail(trimmedEmail)
            if (existingEmail != null && trimmedEmail.isNotBlank()) {
                return@withContext Result.failure(Exception("Email already registered"))
            }

            val userId = UUID.randomUUID().toString()
            val passwordHash = SessionManager.hashPassword(password)
            val newUser = UserEntity(
                userId = userId,
                username = trimmedUsername,
                email = trimmedEmail,
                passwordHash = passwordHash,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(newUser)

            // Auto-create default playlists
            playlistDao.insertPlaylist(
                PlaylistEntity(
                    userId = userId,
                    name = "Vault Top Picks",
                    description = "Personal vault favorites"
                )
            )

            Result.success(newUser)
        }

    suspend fun authenticateUser(identifier: String, password: String): Result<UserEntity> =
        withContext(ioDispatcher) {
            val trimmed = identifier.trim()
            val user = userDao.getUserByUsername(trimmed) ?: userDao.getUserByEmail(trimmed)
                ?: return@withContext Result.failure(Exception("Account not found"))

            val hash = SessionManager.hashPassword(password)
            if (user.passwordHash != hash) {
                return@withContext Result.failure(Exception("Incorrect password"))
            }

            Result.success(user)
        }

    suspend fun authenticateOrRegisterGoogleUser(
        email: String,
        displayName: String,
        googleSubId: String? = null
    ): Result<UserEntity> = withContext(ioDispatcher) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return@withContext Result.failure(Exception("Google account email is required"))
        }

        // Check if an existing account with this email exists
        val existingUser = userDao.getUserByEmail(trimmedEmail)
        if (existingUser != null) {
            return@withContext Result.success(existingUser)
        }

        val userId = if (!googleSubId.isNullOrBlank()) "google_$googleSubId" else "google_${UUID.randomUUID()}"
        val baseUsername = displayName.ifBlank { trimmedEmail.substringBefore("@") }

        // Ensure unique username
        var uniqueUsername = baseUsername
        var counter = 1
        while (userDao.getUserByUsername(uniqueUsername) != null) {
            uniqueUsername = "$baseUsername$counter"
            counter++
        }

        val newUser = UserEntity(
            userId = userId,
            username = uniqueUsername,
            email = trimmedEmail,
            passwordHash = "google_oauth_verified",
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)

        // Auto-create default playlists
        playlistDao.insertPlaylist(
            PlaylistEntity(
                userId = userId,
                name = "Google Vault Favorites",
                description = "Saved music for $uniqueUsername"
            )
        )

        Result.success(newUser)
    }

    suspend fun getUserById(userId: String): UserEntity? = withContext(ioDispatcher) {
        userDao.getUserById(userId)
    }

    // Song operations
    fun getSongs(userId: String): Flow<List<SongEntity>> = songDao.getSongsForUser(userId)

    fun getFavorites(userId: String): Flow<List<SongEntity>> = songDao.getFavoritesForUser(userId)

    fun searchSongs(userId: String, query: String): Flow<List<SongEntity>> =
        songDao.searchSongs(userId, query)

    fun getSongCount(userId: String): Flow<Int> = songDao.getSongCount(userId)

    fun getTotalStorageBytes(userId: String): Flow<Long?> = songDao.getTotalStorageBytes(userId)

    suspend fun importSongFromUri(context: Context, uri: Uri, userId: String): Result<SongEntity> =
        withContext(Dispatchers.IO) {
            val importResult = AudioImportHelper.importAudioFromUri(context, uri, userId)
            if (importResult.isSuccess) {
                val song = importResult.getOrThrow()
                val id = songDao.insertSong(song)
                Result.success(song.copy(id = id))
            } else {
                importResult
            }
        }

    suspend fun createDemoTrackIfNeeded(context: Context, userId: String): SongEntity? =
        withContext(Dispatchers.IO) {
            val demo = AudioImportHelper.createDemoSampleTrack(context, userId)
            if (demo != null) {
                val id = songDao.insertSong(demo)
                demo.copy(id = id)
            } else {
                null
            }
        }

    suspend fun toggleFavorite(songId: Long, currentIsFavorite: Boolean) = withContext(Dispatchers.IO) {
        songDao.setFavorite(songId, !currentIsFavorite)
    }

    suspend fun recordSongPlayed(songId: Long) = withContext(Dispatchers.IO) {
        songDao.incrementPlayCount(songId, System.currentTimeMillis())
    }

    suspend fun deleteSong(song: SongEntity) = withContext(Dispatchers.IO) {
        songDao.deleteSongById(song.id)
        AudioImportHelper.deleteSongFile(song.filePath, song.albumArtPath)
    }

    // Playlist operations
    fun getPlaylists(userId: String): Flow<List<PlaylistEntity>> =
        playlistDao.getPlaylistsForUser(userId)

    suspend fun createPlaylist(userId: String, name: String, description: String = ""): Long =
        withContext(Dispatchers.IO) {
            playlistDao.insertPlaylist(
                PlaylistEntity(
                    userId = userId,
                    name = name.trim(),
                    description = description.trim()
                )
            )
        }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(playlistId = playlistId, songId = songId)
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>> =
        playlistDao.getSongsInPlaylist(playlistId)
}
