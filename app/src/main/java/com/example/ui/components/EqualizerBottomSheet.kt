package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.equalizer.EqualizerBand
import com.example.player.equalizer.EqualizerPreset
import com.example.player.equalizer.EqualizerState
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VaultGold
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    equalizerState: EqualizerState,
    isPlaying: Boolean,
    onSetEnabled: (Boolean) -> Unit,
    onApplyPreset: (EqualizerPreset) -> Unit,
    onSetBandGain: (Int, Int) -> Unit,
    onSetBassBoost: (Int) -> Unit,
    onSetVirtualizer: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null,
        modifier = modifier
            .fillMaxSize()
            .testTag("equalizer_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.dp, CardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (equalizerState.isEnabled) NeonCyan else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AUDIO EQUALIZER",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = if (equalizerState.isEnabled) {
                                "DSP Active • ${equalizerState.currentPreset.displayName}"
                            } else {
                                "Bypassed (Flat Signal)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (equalizerState.isEnabled) NeonCyan else TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Master Enable / Bypass Switch
                    Switch(
                        checked = equalizerState.isEnabled,
                        onCheckedChange = onSetEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurfaceHighlight
                        ),
                        modifier = Modifier.testTag("equalizer_switch")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("close_equalizer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Equalizer",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Selector Row
            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("equalizer_presets_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(EqualizerPreset.entries) { preset ->
                    val isSelected = equalizerState.currentPreset == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (!equalizerState.isEnabled) {
                                onSetEnabled(true)
                            }
                            onApplyPreset(preset)
                        },
                        label = {
                            Text(
                                text = preset.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (preset) {
                                EqualizerPreset.BASS_BOOST -> NeonCyan.copy(alpha = 0.25f)
                                EqualizerPreset.JAZZ -> NeonViolet.copy(alpha = 0.25f)
                                EqualizerPreset.CLASSICAL -> VaultGold.copy(alpha = 0.25f)
                                else -> NeonViolet.copy(alpha = 0.2f)
                            },
                            selectedLabelColor = when (preset) {
                                EqualizerPreset.BASS_BOOST -> NeonCyan
                                EqualizerPreset.JAZZ -> NeonViolet
                                EqualizerPreset.CLASSICAL -> VaultGold
                                else -> TextPrimary
                            },
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) {
                                when (preset) {
                                    EqualizerPreset.BASS_BOOST -> NeonCyan
                                    EqualizerPreset.JAZZ -> NeonViolet
                                    EqualizerPreset.CLASSICAL -> VaultGold
                                    else -> NeonViolet
                                }
                            } else CardBorder
                        ),
                        modifier = Modifier.testTag("preset_${preset.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency Curve Canvas Visualization
            EqualizerCurveVisualizer(
                bands = equalizerState.bands,
                isEnabled = equalizerState.isEnabled,
                isPlaying = isPlaying,
                preset = equalizerState.currentPreset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5-Band Slider Adjustments
            Text(
                text = "FREQUENCY BANDS (-12 dB to +12 dB)",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    equalizerState.bands.forEach { band ->
                        BandControlRow(
                            band = band,
                            isEnabled = equalizerState.isEnabled,
                            onGainChanged = { gain ->
                                onSetBandGain(band.index, gain)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hardware Audio FX: Bass Boost & 3D Virtualizer Cards
            Text(
                text = "AUDIO ENHANCERS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bass Boost Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SpeakerGroup,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Bass Boost",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Deep sub-harmonic low-end punch",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = "${equalizerState.bassBoostStrength}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = equalizerState.bassBoostStrength.toFloat(),
                        onValueChange = { onSetBassBoost(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = equalizerState.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = DarkSurfaceElevated,
                            disabledThumbColor = TextMuted,
                            disabledActiveTrackColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bass_boost_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3D Virtualizer Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonViolet.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = NeonViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "3D Virtualizer",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Expands stereo acoustic soundstage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = "${equalizerState.virtualizerStrength}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonViolet
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = equalizerState.virtualizerStrength.toFloat(),
                        onValueChange = { onSetVirtualizer(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = equalizerState.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonViolet,
                            activeTrackColor = NeonViolet,
                            inactiveTrackColor = DarkSurfaceElevated,
                            disabledThumbColor = TextMuted,
                            disabledActiveTrackColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("virtualizer_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions: Reset & Done Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(CardBorder, CardBorder))
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reset_equalizer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset to Flat")
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("done_equalizer_button")
                ) {
                    Text(
                        text = "Apply & Done",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BandControlRow(
    band: EqualizerBand,
    isEnabled: Boolean,
    onGainChanged: (Int) -> Unit
) {
    val bandCategory = when (band.index) {
        0 -> "Sub Bass"
        1 -> "Bass"
        2 -> "Midrange"
        3 -> "Presence"
        4 -> "Air / Highs"
        else -> ""
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = band.label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• $bandCategory",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Text(
                text = if (band.gainDb > 0) "+${band.gainDb} dB" else "${band.gainDb} dB",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = when {
                    !isEnabled -> TextMuted
                    band.gainDb > 0 -> NeonCyan
                    band.gainDb < 0 -> NeonViolet
                    else -> TextSecondary
                }
            )
        }

        Slider(
            value = band.gainDb.toFloat(),
            onValueChange = { onGainChanged(it.toInt()) },
            valueRange = -12f..12f,
            steps = 23, // 1 dB increments between -12 and +12
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = if (band.gainDb > 0) NeonCyan else NeonViolet,
                activeTrackColor = if (band.gainDb > 0) NeonCyan else NeonViolet,
                inactiveTrackColor = DarkSurfaceElevated,
                disabledThumbColor = TextMuted,
                disabledActiveTrackColor = TextMuted
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("band_slider_${band.index}")
        )
    }
}

@Composable
private fun EqualizerCurveVisualizer(
    bands: List<EqualizerBand>,
    isEnabled: Boolean,
    isPlaying: Boolean,
    preset: EqualizerPreset,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_visualizer")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "eq_pulse"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Draw background grid lines: +12dB, 0dB, -12dB
        val gridColor = Color(0xFF242A3D)
        drawLine(
            color = gridColor,
            start = Offset(0f, height * 0.15f),
            end = Offset(width, height * 0.15f),
            strokeWidth = 1f
        )
        drawLine(
            color = gridColor.copy(alpha = 0.8f),
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 1.5f
        )
        drawLine(
            color = gridColor,
            start = Offset(0f, height * 0.85f),
            end = Offset(width, height * 0.85f),
            strokeWidth = 1f
        )

        if (bands.isEmpty()) return@Canvas

        val stepX = width / (bands.size + 1)
        val points = mutableListOf<Offset>()

        // Left edge point
        points.add(Offset(0f, midY))

        bands.forEachIndexed { index, band ->
            val x = stepX * (index + 1)
            // Gain mapped from -12dB..+12dB to height..0
            val normalizedGain = if (isEnabled) (band.gainDb / 12f).coerceIn(-1f, 1f) else 0f

            // Add dynamic subtle pulsation if music is playing
            val dynamicWave = if (isEnabled && isPlaying) {
                sin(pulseAnim + index * 1.2f) * 6f
            } else 0f

            val y = midY - (normalizedGain * (height * 0.38f)) + dynamicWave
            points.add(Offset(x, y))
        }

        // Right edge point
        points.add(Offset(width, midY))

        // Create smooth cubic Bezier curve path
        val curvePath = Path()
        curvePath.moveTo(points.first().x, points.first().y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val controlX1 = (p0.x + p1.x) / 2f
            val controlY1 = p0.y
            val controlX2 = (p0.x + p1.x) / 2f
            val controlY2 = p1.y
            curvePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
        }

        // Create fill path
        val fillPath = Path().apply {
            addPath(curvePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        val fillGradient = Brush.verticalGradient(
            colors = listOf(
                (if (isEnabled) NeonCyan.copy(alpha = 0.35f) else TextMuted.copy(alpha = 0.1f)),
                (if (isEnabled) NeonViolet.copy(alpha = 0.15f) else Color.Transparent),
                Color.Transparent
            ),
            startY = 0f,
            endY = height
        )

        // Draw fill gradient
        drawPath(fillPath, brush = fillGradient)

        // Draw smooth neon line
        val lineStroke = Brush.horizontalGradient(
            colors = listOf(
                if (isEnabled) NeonCyan else TextMuted,
                if (isEnabled) NeonViolet else TextMuted,
                if (isEnabled) NeonPink else TextMuted
            )
        )
        drawPath(
            curvePath,
            brush = lineStroke,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw control nodes
        bands.forEachIndexed { index, _ ->
            val p = points[index + 1]
            drawCircle(
                color = if (isEnabled) DarkBackground else Color.DarkGray,
                radius = 6.dp.toPx(),
                center = p
            )
            drawCircle(
                color = if (isEnabled) NeonCyan else TextMuted,
                radius = 4.dp.toPx(),
                center = p
            )
        }
    }
}
