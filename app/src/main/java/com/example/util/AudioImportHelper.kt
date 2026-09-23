package com.example.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.model.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object AudioImportHelper {

    private const val TAG = "AudioImportHelper"

    suspend fun importAudioFromUri(
        context: Context,
        uri: Uri,
        userId: String
    ): Result<SongEntity> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            // 1. Get original file name and size from content provider
            var displayName = "uploaded_track_${System.currentTimeMillis()}.mp3"
            var reportedSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            displayName = name
                        }
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        reportedSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            // Clean filename
            val safeName = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val musicDir = File(context.filesDir, "vault_music/$userId").apply { mkdirs() }
            val destFile = File(musicDir, "${System.currentTimeMillis()}_$safeName")

            // 2. Stream into internal storage
            var copiedBytes = 0L
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        copiedBytes += read
                    }
                    outputStream.flush()
                }
            } ?: return@withContext Result.failure(Exception("Unable to open input stream for audio file"))

            val actualSize = if (reportedSize > 0) reportedSize else copiedBytes

            // 3. Extract metadata using MediaMetadataRetriever
            val retriever = MediaMetadataRetriever()
            var title = displayName.substringBeforeLast(".")
            var artist = "Unknown Artist"
            var album = "Vault Audio"
            var durationMs = 0L
            var mimeType = contentResolver.getType(uri) ?: "audio/mpeg"
            var albumArtPath: String? = null

            try {
                retriever.setDataSource(destFile.absolutePath)

                val metaTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                if (!metaTitle.isNullOrBlank()) {
                    title = metaTitle.trim()
                }

                val metaArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                if (!metaArtist.isNullOrBlank()) {
                    artist = metaArtist.trim()
                }

                val metaAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                if (!metaAlbum.isNullOrBlank()) {
                    album = metaAlbum.trim()
                }

                val metaDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (!metaDuration.isNullOrBlank()) {
                    durationMs = metaDuration.toLongOrNull() ?: 0L
                }

                val metaMime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                if (!metaMime.isNullOrBlank()) {
                    mimeType = metaMime
                }

                // Extract embedded album artwork if available
                val pictureBytes = retriever.embeddedPicture
                if (pictureBytes != null && pictureBytes.isNotEmpty()) {
                    val artDir = File(context.filesDir, "vault_art/$userId").apply { mkdirs() }
                    val artFile = File(artDir, "art_${destFile.nameWithoutExtension}.jpg")
                    FileOutputStream(artFile).use { it.write(pictureBytes) }
                    albumArtPath = artFile.absolutePath
                }
            } catch (e: Exception) {
                Log.w(TAG, "Metadata extraction fallback used: ${e.message}")
            } finally {
                try {
                    retriever.release()
                } catch (ignored: Exception) {}
            }

            // Fallback duration calculation if 0 (estimate ~128kbps if mp3/audio)
            if (durationMs <= 0 && actualSize > 0) {
                durationMs = ((actualSize * 8L) / 128L) // rough estimate
            }

            val song = SongEntity(
                userId = userId,
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                filePath = destFile.absolutePath,
                fileSizeBytes = actualSize,
                mimeType = mimeType,
                dateAdded = System.currentTimeMillis(),
                isFavorite = false,
                albumArtPath = albumArtPath
            )

            Result.success(song)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing audio: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Synthesizes a real, melodious 16-bit PCM WAV track directly into app storage.
     * Provides an out-of-the-box acoustic demo track for immediate playback.
     */
    suspend fun createDemoSampleTrack(
        context: Context,
        userId: String,
        title: String = "Vault Ambient Prelude",
        artist: String = "SoundVault Audio",
        album: String = "Internal Storage Demo"
    ): SongEntity? = withContext(Dispatchers.IO) {
        try {
            val musicDir = File(context.filesDir, "vault_music/$userId").apply { mkdirs() }
            val sampleFile = File(musicDir, "demo_ambient_prelude.wav")

            if (!sampleFile.exists() || sampleFile.length() < 1000) {
                generateMelodicWav(sampleFile, durationSeconds = 12)
            }

            val durationMs = 12_000L
            val size = sampleFile.length()

            SongEntity(
                userId = userId,
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                filePath = sampleFile.absolutePath,
                fileSizeBytes = size,
                mimeType = "audio/wav",
                dateAdded = System.currentTimeMillis(),
                isFavorite = true,
                albumArtPath = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create demo audio: ${e.message}")
            null
        }
    }

    private fun generateMelodicWav(outputFile: File, durationSeconds: Int) {
        val sampleRate = 44100
        val numSamples = sampleRate * durationSeconds
        val bytesPerSample = 2 // 16-bit
        val channels = 2 // Stereo
        val totalAudioBytes = numSamples * bytesPerSample * channels

        FileOutputStream(outputFile).use { fos ->
            // Write 44-byte WAV header
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(36 + totalAudioBytes)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16) // Subchunk1Size (16 for PCM)
            header.putShort(1) // AudioFormat (1 for PCM)
            header.putShort(channels.toShort())
            header.putInt(sampleRate)
            header.putInt(sampleRate * channels * bytesPerSample) // ByteRate
            header.putShort((channels * bytesPerSample).toShort()) // BlockAlign
            header.putShort(16) // BitsPerSample
            header.put("data".toByteArray())
            header.putInt(totalAudioBytes)
            fos.write(header.array())

            // Notes frequencies for arpeggio: C4, E4, G4, B4, C5, G4, E4
            val notes = doubleArrayOf(261.63, 329.63, 392.00, 493.88, 523.25, 392.00, 329.63, 293.66)
            val noteDurationSamples = sampleRate / 2 // 0.5s per note

            val buffer = ByteBuffer.allocate(8192).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val noteIdx = (i / noteDurationSamples) % notes.size
                val freq = notes[noteIdx]
                val noteLocalTime = (i % noteDurationSamples).toDouble() / sampleRate

                // Envelope: quick attack (0.01s), exponential decay
                val envelope = exp(-noteLocalTime * 3.2)

                // Bell / Rhodes chime harmonic sum
                val wave1 = sin(2.0 * PI * freq * t)
                val wave2 = 0.4 * sin(2.0 * PI * freq * 2.0 * t)
                val wave3 = 0.2 * sin(2.0 * PI * freq * 3.0 * t)
                val shimmer = 0.15 * sin(2.0 * PI * (freq * 4.01) * t)

                // Stereo panning based on note
                val panLeft = 0.6 + 0.3 * sin(2.0 * PI * 0.4 * t)
                val panRight = 1.2 - panLeft

                val sampleFloatLeft = (wave1 + wave2 + wave3 + shimmer) * envelope * panLeft * 0.45
                val sampleFloatRight = (wave1 + wave2 + wave3 + shimmer) * envelope * panRight * 0.45

                val sampleShortLeft = (sampleFloatLeft.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                val sampleShortRight = (sampleFloatRight.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()

                if (!buffer.hasRemaining()) {
                    fos.write(buffer.array(), 0, buffer.position())
                    buffer.clear()
                }
                buffer.putShort(sampleShortLeft)

                if (!buffer.hasRemaining()) {
                    fos.write(buffer.array(), 0, buffer.position())
                    buffer.clear()
                }
                buffer.putShort(sampleShortRight)
            }

            if (buffer.position() > 0) {
                fos.write(buffer.array(), 0, buffer.position())
            }
            fos.flush()
        }
    }

    fun deleteSongFile(filePath: String, albumArtPath: String?) {
        try {
            val audioFile = File(filePath)
            if (audioFile.exists()) {
                audioFile.delete()
            }
            if (!albumArtPath.isNullOrBlank()) {
                val artFile = File(albumArtPath)
                if (artFile.exists()) {
                    artFile.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${e.message}")
        }
    }
}
