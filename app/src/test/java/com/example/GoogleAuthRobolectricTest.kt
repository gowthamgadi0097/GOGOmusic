package com.example

import com.example.data.SessionManager
import com.example.data.SoundVaultRepository
import com.example.data.dao.PlaylistDao
import com.example.data.dao.SongDao
import com.example.data.dao.UserDao
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.SongEntity
import com.example.data.model.UserEntity
import com.example.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GoogleAuthRobolectricTest {

    @Test
    fun `authenticateOrRegisterGoogleUser registers new user and creates default playlist`() = runTest {
        val userDao = FakeUserDao()
        val songDao = FakeSongDao()
        val playlistDao = FakePlaylistDao()
        val repository = SoundVaultRepository(userDao, songDao, playlistDao)

        val result = repository.authenticateOrRegisterGoogleUser(
            email = "test.artist@gmail.com",
            displayName = "Test Artist",
            googleSubId = "10987654321"
        )

        assertTrue(result.isSuccess)
        val user = result.getOrThrow()
        assertEquals("google_10987654321", user.userId)
        assertEquals("Test Artist", user.username)
        assertEquals("test.artist@gmail.com", user.email)
        assertEquals("google_oauth_verified", user.passwordHash)

        // Verify default playlist was created
        assertTrue(playlistDao.playlists.any { it.userId == user.userId })
    }

    @Test
    fun `authenticateOrRegisterGoogleUser signs in existing user if email matches`() = runTest {
        val userDao = FakeUserDao()
        val songDao = FakeSongDao()
        val playlistDao = FakePlaylistDao()
        val repository = SoundVaultRepository(userDao, songDao, playlistDao)

        val existingUser = UserEntity(
            userId = "existing_id_99",
            username = "OriginalGowtham",
            email = "gowthamgadi0097@gmail.com",
            passwordHash = "hash123"
        )
        userDao.insertUser(existingUser)

        val result = repository.authenticateOrRegisterGoogleUser(
            email = "gowthamgadi0097@gmail.com",
            displayName = "Gowtham Gadi"
        )

        assertTrue(result.isSuccess)
        val user = result.getOrThrow()
        assertEquals("existing_id_99", user.userId)
        assertEquals("OriginalGowtham", user.username)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `AuthViewModel loginWithGoogle sets session and reports success`() = runTest {
        val testDispatcher = kotlinx.coroutines.test.StandardTestDispatcher(testScheduler)
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        try {
            val context = RuntimeEnvironment.getApplication()
            val sessionManager = SessionManager(context)
            val userDao = FakeUserDao()
            val songDao = FakeSongDao()
            val playlistDao = FakePlaylistDao()
            val repository = SoundVaultRepository(userDao, songDao, playlistDao, testDispatcher)

            val viewModel = AuthViewModel(repository, sessionManager)
            viewModel.loginWithGoogle("gowthamgadi0097@gmail.com", "Gowtham Gadi")
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isSuccess)
            assertTrue(sessionManager.isLoggedIn())
            assertNotNull(sessionManager.currentUserId.value)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }
}
