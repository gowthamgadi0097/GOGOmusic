package com.example.player.equalizer

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AudioEqualizerManager(
    private val context: Context
) {
    private val TAG = "AudioEqualizerManager"
    private val prefs: SharedPreferences = context.getSharedPreferences("soundvault_equalizer_prefs", Context.MODE_PRIVATE)

    private var activeSessionId: Int? = null
    private var hwEqualizer: Equalizer? = null
    private var hwBassBoost: BassBoost? = null
    private var hwVirtualizer: Virtualizer? = null

    private val _state = MutableStateFlow(loadInitialState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    private fun loadInitialState(): EqualizerState {
        val enabled = prefs.getBoolean("eq_enabled", true)
        val presetId = prefs.getString("eq_preset_id", EqualizerPreset.BASS_BOOST.id) ?: EqualizerPreset.BASS_BOOST.id
        val preset = EqualizerPreset.fromId(presetId)
        val bass = prefs.getInt("eq_bass_boost", preset.bassBoost)
        val virt = prefs.getInt("eq_virtualizer", preset.virtualizer)

        val bands = (0 until 5).map { index ->
            val defaultGain = preset.gains.getOrElse(index) { 0 }
            val savedGain = prefs.getInt("eq_band_$index", defaultGain)
            val (freq, label) = EqualizerState.FREQUENCY_SPECS[index]
            EqualizerBand(index, freq, label, savedGain)
        }

        return EqualizerState(
            isEnabled = enabled,
            currentPreset = preset,
            bands = bands,
            bassBoostStrength = bass,
            virtualizerStrength = virt,
            isSupportedOnDevice = true
        )
    }

    private fun persistState(state: EqualizerState) {
        prefs.edit().apply {
            putBoolean("eq_enabled", state.isEnabled)
            putString("eq_preset_id", state.currentPreset.id)
            putInt("eq_bass_boost", state.bassBoostStrength)
            putInt("eq_virtualizer", state.virtualizerStrength)
            state.bands.forEach { band ->
                putInt("eq_band_${band.index}", band.gainDb)
            }
            apply()
        }
    }

    @Synchronized
    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        if (activeSessionId == audioSessionId && hwEqualizer != null) return

        detachAudioSession()
        activeSessionId = audioSessionId

        try {
            hwEqualizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            hwBassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            hwVirtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }

            applyCurrentStateToHardware()
            _state.update { it.copy(isSupportedOnDevice = true) }
        } catch (e: Exception) {
            Log.w(TAG, "Hardware AudioEffects initialization skipped or not supported: ${e.message}")
            _state.update { it.copy(isSupportedOnDevice = false) }
        }
    }

    @Synchronized
    fun detachAudioSession() {
        try {
            hwEqualizer?.release()
        } catch (ignored: Exception) {}
        try {
            hwBassBoost?.release()
        } catch (ignored: Exception) {}
        try {
            hwVirtualizer?.release()
        } catch (ignored: Exception) {}

        hwEqualizer = null
        hwBassBoost = null
        hwVirtualizer = null
        activeSessionId = null
    }

    fun setEnabled(enabled: Boolean) {
        _state.update { it.copy(isEnabled = enabled) }
        persistState(_state.value)
        try {
            hwEqualizer?.enabled = enabled
            hwBassBoost?.enabled = enabled
            hwVirtualizer?.enabled = enabled
            if (enabled) {
                applyCurrentStateToHardware()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error toggling equalizer enabled: ${e.message}")
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        val updatedBands = EqualizerState.defaultBandsFor(preset)
        _state.update {
            it.copy(
                currentPreset = preset,
                bands = updatedBands,
                bassBoostStrength = preset.bassBoost,
                virtualizerStrength = preset.virtualizer
            )
        }
        persistState(_state.value)
        applyCurrentStateToHardware()
    }

    fun setBandGain(bandIndex: Int, gainDb: Int) {
        val clampedGain = gainDb.coerceIn(-12, 12)
        val currentBands = _state.value.bands.toMutableList()
        val existingIndex = currentBands.indexOfFirst { it.index == bandIndex }

        if (existingIndex >= 0) {
            currentBands[existingIndex] = currentBands[existingIndex].copy(gainDb = clampedGain)
        }

        // Determine if matches current preset gains or is custom
        val activePreset = _state.value.currentPreset
        val matchesPreset = activePreset != EqualizerPreset.CUSTOM &&
            currentBands.map { it.gainDb } == activePreset.gains

        val newPreset = if (matchesPreset) activePreset else EqualizerPreset.CUSTOM

        _state.update {
            it.copy(
                bands = currentBands,
                currentPreset = newPreset
            )
        }
        persistState(_state.value)
        applyBandGainToHardware(bandIndex, clampedGain)
    }

    fun setBassBoost(strengthPercent: Int) {
        val clamped = strengthPercent.coerceIn(0, 100)
        _state.update { it.copy(bassBoostStrength = clamped) }
        persistState(_state.value)
        try {
            val hwStrength = ((clamped / 100f) * 1000).toInt().toShort()
            hwBassBoost?.let {
                if (it.strengthSupported) {
                    it.setStrength(hwStrength)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying bass boost: ${e.message}")
        }
    }

    fun setVirtualizer(strengthPercent: Int) {
        val clamped = strengthPercent.coerceIn(0, 100)
        _state.update { it.copy(virtualizerStrength = clamped) }
        persistState(_state.value)
        try {
            val hwStrength = ((clamped / 100f) * 1000).toInt().toShort()
            hwVirtualizer?.let {
                if (it.strengthSupported) {
                    it.setStrength(hwStrength)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying virtualizer: ${e.message}")
        }
    }

    fun resetToFlat() {
        applyPreset(EqualizerPreset.FLAT)
    }

    private fun applyCurrentStateToHardware() {
        val currentState = _state.value
        if (!currentState.isEnabled) return

        try {
            hwEqualizer?.let { eq ->
                val numHwBands = eq.numberOfBands.toInt()
                val bandRange = eq.bandLevelRange // [min, max] in millibels e.g. -1500 to +1500
                val minMb = if (bandRange.size >= 2) bandRange[0].toInt() else -1200
                val maxMb = if (bandRange.size >= 2) bandRange[1].toInt() else 1200

                currentState.bands.forEachIndexed { i, band ->
                    if (i < numHwBands) {
                        // Map -12..+12 dB to millibels
                        val targetMb = (band.gainDb * 100).coerceIn(minMb, maxMb)
                        eq.setBandLevel(i.toShort(), targetMb.toShort())
                    }
                }
            }

            hwBassBoost?.let { bb ->
                if (bb.strengthSupported) {
                    val strength = ((currentState.bassBoostStrength / 100f) * 1000).toInt().toShort()
                    bb.setStrength(strength)
                }
            }

            hwVirtualizer?.let { virt ->
                if (virt.strengthSupported) {
                    val strength = ((currentState.virtualizerStrength / 100f) * 1000).toInt().toShort()
                    virt.setStrength(strength)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying equalizer state to hardware: ${e.message}")
        }
    }

    private fun applyBandGainToHardware(bandIndex: Int, gainDb: Int) {
        val currentState = _state.value
        if (!currentState.isEnabled) return

        try {
            hwEqualizer?.let { eq ->
                val numHwBands = eq.numberOfBands.toInt()
                if (bandIndex < numHwBands) {
                    val bandRange = eq.bandLevelRange
                    val minMb = if (bandRange.size >= 2) bandRange[0].toInt() else -1200
                    val maxMb = if (bandRange.size >= 2) bandRange[1].toInt() else 1200
                    val targetMb = (gainDb * 100).coerceIn(minMb, maxMb)
                    eq.setBandLevel(bandIndex.toShort(), targetMb.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error setting band gain to hardware: ${e.message}")
        }
    }

    fun release() {
        detachAudioSession()
    }
}
