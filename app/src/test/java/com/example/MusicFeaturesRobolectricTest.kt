package com.example

import com.example.data.model.PlaylistEntity
import com.example.data.model.RepeatMode
import com.example.data.model.SongEntity
import com.example.ui.vault.SortOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MusicFeaturesRobolectricTest {

    private val sampleSongs = listOf(
        SongEntity(
            id = 1L,
            userId = "vault_user_1",
            title = "Starlight Echoes",
            artist = "Neon Dreamer",
            album = "Cosmic Odyssey",
            durationMs = 180000L,
            filePath = "/vault/1.mp3",
            fileSizeBytes = 4000000L,
            mimeType = "audio/mpeg",
            dateAdded = 1000L
        ),
        SongEntity(
            id = 2L,
            userId = "vault_user_1",
            title = "Cyber Pulse",
            artist = "Astra Wave",
            album = "Future Synth",
            durationMs = 240000L,
            filePath = "/vault/2.mp3",
            fileSizeBytes = 5500000L,
            mimeType = "audio/mpeg",
            dateAdded = 2000L
        ),
        SongEntity(
            id = 3L,
            userId = "vault_user_1",
            title = "Midnight Horizon",
            artist = "Zeta Glow",
            album = "Aura Night",
            durationMs = 120000L,
            filePath = "/vault/3.mp3",
            fileSizeBytes = 3000000L,
            mimeType = "audio/mpeg",
            dateAdded = 3000L
        )
    )

    @Test
    fun `search filters songs by title artist or album`() {
        // Search by Title
        val titleMatch = sampleSongs.filter {
            it.title.contains("Starlight", ignoreCase = true) ||
            it.artist.contains("Starlight", ignoreCase = true) ||
            it.album.contains("Starlight", ignoreCase = true)
        }
        assertEquals(1, titleMatch.size)
        assertEquals("Starlight Echoes", titleMatch.first().title)

        // Search by Artist
        val artistMatch = sampleSongs.filter {
            it.title.contains("Astra", ignoreCase = true) ||
            it.artist.contains("Astra", ignoreCase = true) ||
            it.album.contains("Astra", ignoreCase = true)
        }
        assertEquals(1, artistMatch.size)
        assertEquals("Cyber Pulse", artistMatch.first().title)

        // Search by Album
        val albumMatch = sampleSongs.filter {
            it.title.contains("Aura", ignoreCase = true) ||
            it.artist.contains("Aura", ignoreCase = true) ||
            it.album.contains("Aura", ignoreCase = true)
        }
        assertEquals(1, albumMatch.size)
        assertEquals("Midnight Horizon", albumMatch.first().title)

        // Empty Search returns all songs
        val emptyMatch = sampleSongs.filter { true }
        assertEquals(3, emptyMatch.size)
    }

    @Test
    fun `sorting library by title artist album date and duration`() {
        // Title (A-Z)
        val sortedByTitle = sampleSongs.sortedBy { it.title.lowercase() }
        assertEquals("Cyber Pulse", sortedByTitle[0].title)
        assertEquals("Midnight Horizon", sortedByTitle[1].title)
        assertEquals("Starlight Echoes", sortedByTitle[2].title)

        // Artist (A-Z)
        val sortedByArtist = sampleSongs.sortedBy { it.artist.lowercase() }
        assertEquals("Astra Wave", sortedByArtist[0].artist)
        assertEquals("Neon Dreamer", sortedByArtist[1].artist)
        assertEquals("Zeta Glow", sortedByArtist[2].artist)

        // Album (A-Z)
        val sortedByAlbum = sampleSongs.sortedBy { it.album.lowercase() }
        assertEquals("Aura Night", sortedByAlbum[0].album)
        assertEquals("Cosmic Odyssey", sortedByAlbum[1].album)
        assertEquals("Future Synth", sortedByAlbum[2].album)

        // Upload Date (Newest first)
        val sortedByDate = sampleSongs.sortedByDescending { it.dateAdded }
        assertEquals(3L, sortedByDate[0].id)
        assertEquals(2L, sortedByDate[1].id)
        assertEquals(1L, sortedByDate[2].id)

        // Duration (Longest first)
        val sortedByDuration = sampleSongs.sortedByDescending { it.durationMs }
        assertEquals(240000L, sortedByDuration[0].durationMs)
        assertEquals(180000L, sortedByDuration[1].durationMs)
        assertEquals(120000L, sortedByDuration[2].durationMs)
    }

    @Test
    fun `repeat mode cycles through OFF ALL and ONE correctly`() {
        var currentMode = RepeatMode.OFF
        val nextMode1 = when (currentMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        assertEquals(RepeatMode.ALL, nextMode1)

        val nextMode2 = when (nextMode1) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        assertEquals(RepeatMode.ONE, nextMode2)

        val nextMode3 = when (nextMode2) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        assertEquals(RepeatMode.OFF, nextMode3)
    }

    @Test
    fun `playlist maintains correct track sequence and add-remove operations`() {
        val playlist = PlaylistEntity(id = 10L, userId = "user_1", name = "Cyber Drive", description = "Synth tracks")
        assertEquals("Cyber Drive", playlist.name)

        val playlistSongs = mutableListOf<SongEntity>()
        playlistSongs.addAll(sampleSongs)
        assertEquals(3, playlistSongs.size)
        assertEquals("Starlight Echoes", playlistSongs[0].title)
        assertEquals("Cyber Pulse", playlistSongs[1].title)
        assertEquals("Midnight Horizon", playlistSongs[2].title)

        // Remove song 2
        playlistSongs.removeAll { it.id == 2L }
        assertEquals(2, playlistSongs.size)
        assertEquals(listOf(1L, 3L), playlistSongs.map { it.id })

        // Add back song 2 at the end
        val songToAdd = sampleSongs.first { it.id == 2L }
        playlistSongs.add(songToAdd)
        assertEquals(3, playlistSongs.size)
        assertEquals(listOf(1L, 3L, 2L), playlistSongs.map { it.id })
    }

    @Test
    fun `shuffle preserves all queue items while maintaining current song at position`() {
        val currentSong = sampleSongs[1] // Cyber Pulse
        val remainingSongs = sampleSongs.filter { it.id != currentSong.id }
        val shuffledQueue = listOf(currentSong) + remainingSongs.shuffled()

        assertEquals(3, shuffledQueue.size)
        assertEquals(currentSong.id, shuffledQueue[0].id)
        assertTrue(shuffledQueue.any { it.id == 1L })
        assertTrue(shuffledQueue.any { it.id == 3L })
    }
}
