package com.example.player.equalizer

enum class EqualizerPreset(
    val id: String,
    val displayName: String,
    val gains: List<Int>, // 5 frequency bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz (-12 dB to +12 dB)
    val bassBoost: Int = 0, // 0 to 100%
    val virtualizer: Int = 0 // 0 to 100%
) {
    BASS_BOOST(
        id = "bass_boost",
        displayName = "Bass Boost",
        gains = listOf(9, 7, 2, 0, 0),
        bassBoost = 85,
        virtualizer = 20
    ),
    JAZZ(
        id = "jazz",
        displayName = "Jazz",
        gains = listOf(4, 2, -1, 3, 5),
        bassBoost = 30,
        virtualizer = 35
    ),
    CLASSICAL(
        id = "classical",
        displayName = "Classical",
        gains = listOf(5, 3, -2, 4, 4),
        bassBoost = 20,
        virtualizer = 45
    ),
    ROCK(
        id = "rock",
        displayName = "Rock",
        gains = listOf(6, 4, -2, 3, 6),
        bassBoost = 50,
        virtualizer = 30
    ),
    POP(
        id = "pop",
        displayName = "Pop",
        gains = listOf(-2, 2, 5, 3, -1),
        bassBoost = 35,
        virtualizer = 25
    ),
    ELECTRONIC(
        id = "electronic",
        displayName = "Electronic",
        gains = listOf(8, 6, 0, 2, 6),
        bassBoost = 75,
        virtualizer = 45
    ),
    VOCAL(
        id = "vocal",
        displayName = "Vocal Booster",
        gains = listOf(-3, 0, 6, 5, 1),
        bassBoost = 10,
        virtualizer = 15
    ),
    FLAT(
        id = "flat",
        displayName = "Flat",
        gains = listOf(0, 0, 0, 0, 0),
        bassBoost = 0,
        virtualizer = 0
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom",
        gains = listOf(0, 0, 0, 0, 0),
        bassBoost = 0,
        virtualizer = 0
    );

    companion object {
        fun fromId(id: String): EqualizerPreset {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BASS_BOOST
        }
    }
}

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val label: String,
    val gainDb: Int // Clamped between -12 and +12 dB
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val currentPreset: EqualizerPreset = EqualizerPreset.BASS_BOOST,
    val bands: List<EqualizerBand> = defaultBandsFor(EqualizerPreset.BASS_BOOST),
    val bassBoostStrength: Int = 85, // 0 to 100%
    val virtualizerStrength: Int = 20, // 0 to 100%
    val isSupportedOnDevice: Boolean = true
) {
    companion object {
        val FREQUENCY_SPECS = listOf(
            60 to "60 Hz",
            230 to "230 Hz",
            910 to "910 Hz",
            3600 to "3.6 kHz",
            14000 to "14 kHz"
        )

        fun defaultBandsFor(preset: EqualizerPreset): List<EqualizerBand> {
            return FREQUENCY_SPECS.mapIndexed { index, (freq, label) ->
                val gain = preset.gains.getOrElse(index) { 0 }
                EqualizerBand(
                    index = index,
                    centerFreqHz = freq,
                    label = label,
                    gainDb = gain
                )
            }
        }
    }
}
