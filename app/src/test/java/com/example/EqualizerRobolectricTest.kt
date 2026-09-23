package com.example

import com.example.data.SessionManager
import com.example.data.SoundVaultRepository
import com.example.player.MusicPlayerController
import com.example.player.equalizer.AudioEqualizerManager
import com.example.player.equalizer.EqualizerPreset
import com.example.ui.vault.MainVaultViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EqualizerRobolectricTest {

    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `preset options Bass Boost, Jazz, Classical, Flat exist and have valid values`() {
        val bassBoost = EqualizerPreset.BASS_BOOST
        assertEquals("Bass Boost", bassBoost.displayName)
        assertTrue(bassBoost.gains[0] > bassBoost.gains[4]) // Heavy low-end
        assertTrue(bassBoost.bassBoost >= 80)

        val jazz = EqualizerPreset.JAZZ
        assertEquals("Jazz", jazz.displayName)
        assertEquals(5, jazz.gains.size)
        assertTrue(jazz.virtualizer > 0)

        val classical = EqualizerPreset.CLASSICAL
        assertEquals("Classical", classical.displayName)
        assertEquals(5, classical.gains.size)
        assertTrue(classical.virtualizer >= 40)

        val flat = EqualizerPreset.FLAT
        assertEquals("Flat", flat.displayName)
        assertTrue(flat.gains.all { it == 0 })
        assertEquals(0, flat.bassBoost)
        assertEquals(0, flat.virtualizer)
    }

    @Test
    fun `AudioEqualizerManager preset switching updates state and persists`() {
        val context = RuntimeEnvironment.getApplication()
        val manager = AudioEqualizerManager(context)

        // Apply Jazz
        manager.applyPreset(EqualizerPreset.JAZZ)
        var state = manager.state.value
        assertEquals(EqualizerPreset.JAZZ, state.currentPreset)
        assertEquals(EqualizerPreset.JAZZ.gains[0], state.bands[0].gainDb)
        assertEquals(EqualizerPreset.JAZZ.gains[4], state.bands[4].gainDb)
        assertEquals(EqualizerPreset.JAZZ.bassBoost, state.bassBoostStrength)

        // Apply Classical
        manager.applyPreset(EqualizerPreset.CLASSICAL)
        state = manager.state.value
        assertEquals(EqualizerPreset.CLASSICAL, state.currentPreset)
        assertEquals(EqualizerPreset.CLASSICAL.gains[0], state.bands[0].gainDb)
        assertEquals(EqualizerPreset.CLASSICAL.virtualizer, state.virtualizerStrength)

        // Apply Bass Boost
        manager.applyPreset(EqualizerPreset.BASS_BOOST)
        state = manager.state.value
        assertEquals(EqualizerPreset.BASS_BOOST, state.currentPreset)
        assertEquals(EqualizerPreset.BASS_BOOST.bassBoost, state.bassBoostStrength)

        // Verify persistence by creating a fresh manager instance
        val newManager = AudioEqualizerManager(context)
        assertEquals(EqualizerPreset.BASS_BOOST, newManager.state.value.currentPreset)
    }

    @Test
    fun `modifying individual band gain switches preset to Custom`() {
        val context = RuntimeEnvironment.getApplication()
        val manager = AudioEqualizerManager(context)

        manager.applyPreset(EqualizerPreset.FLAT)
        assertEquals(EqualizerPreset.FLAT, manager.state.value.currentPreset)

        // Boost 60 Hz band to +10 dB
        manager.setBandGain(bandIndex = 0, gainDb = 10)
        val state = manager.state.value
        assertEquals(EqualizerPreset.CUSTOM, state.currentPreset)
        assertEquals(10, state.bands[0].gainDb)
    }

    @Test
    fun `bass boost and virtualizer can be adjusted within bounds`() {
        val context = RuntimeEnvironment.getApplication()
        val manager = AudioEqualizerManager(context)

        manager.setBassBoost(95)
        assertEquals(95, manager.state.value.bassBoostStrength)

        // Clamping test
        manager.setBassBoost(150)
        assertEquals(100, manager.state.value.bassBoostStrength)

        manager.setVirtualizer(60)
        assertEquals(60, manager.state.value.virtualizerStrength)

        manager.setVirtualizer(-10)
        assertEquals(0, manager.state.value.virtualizerStrength)
    }

    @Test
    fun `reset to flat zeroes all bands and boosts`() {
        val context = RuntimeEnvironment.getApplication()
        val manager = AudioEqualizerManager(context)

        manager.applyPreset(EqualizerPreset.BASS_BOOST)
        manager.setBassBoost(80)
        manager.setVirtualizer(50)

        manager.resetToFlat()
        val state = manager.state.value
        assertEquals(EqualizerPreset.FLAT, state.currentPreset)
        assertTrue(state.bands.all { it.gainDb == 0 })
        assertEquals(0, state.bassBoostStrength)
        assertEquals(0, state.virtualizerStrength)
    }

    @Test
    fun `toggling enabled bypasses equalizer correctly`() {
        val context = RuntimeEnvironment.getApplication()
        val manager = AudioEqualizerManager(context)

        manager.setEnabled(false)
        assertFalse(manager.state.value.isEnabled)

        manager.setEnabled(true)
        assertTrue(manager.state.value.isEnabled)
    }

    @Test
    fun `MainVaultViewModel controls equalizer dialog and presets`() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val sessionManager = SessionManager(context)
        val repository = SoundVaultRepository(FakeUserDao(), FakeSongDao(), FakePlaylistDao(), testDispatcher)
        val playerController = MusicPlayerController(context)
        val viewModel = MainVaultViewModel(repository, sessionManager, playerController)

        // Test dialog visibility toggle
        assertFalse(viewModel.uiState.value.showEqualizerDialog)
        viewModel.openEqualizerDialog()
        assertTrue(viewModel.uiState.value.showEqualizerDialog)
        viewModel.closeEqualizerDialog()
        assertFalse(viewModel.uiState.value.showEqualizerDialog)

        // Test preset selection via ViewModel
        viewModel.applyEqualizerPreset(EqualizerPreset.JAZZ)
        assertEquals(EqualizerPreset.JAZZ, viewModel.equalizerState.value.currentPreset)

        viewModel.applyEqualizerPreset(EqualizerPreset.CLASSICAL)
        assertEquals(EqualizerPreset.CLASSICAL, viewModel.equalizerState.value.currentPreset)

        viewModel.setBassBoost(75)
        assertEquals(75, viewModel.equalizerState.value.bassBoostStrength)

        viewModel.resetEqualizer()
        assertEquals(EqualizerPreset.FLAT, viewModel.equalizerState.value.currentPreset)

        playerController.release()
    }
}
