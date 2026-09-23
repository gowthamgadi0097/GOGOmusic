package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)

@Composable
fun GoogleLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val strokeWidth = w * 0.22f
        val radius = (w - strokeWidth) / 2f
        val arcRect = Rect(cx - radius, cy - radius, cx + radius, cy + radius)

        // Red arc (top: roughly 195 to 345 degrees)
        drawArc(
            color = GoogleRed,
            startAngle = 195f,
            sweepAngle = 145f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Yellow arc (left: roughly 140 to 195 degrees)
        drawArc(
            color = GoogleYellow,
            startAngle = 140f,
            sweepAngle = 55f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Green arc (bottom: roughly 35 to 140 degrees)
        drawArc(
            color = GoogleGreen,
            startAngle = 35f,
            sweepAngle = 105f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Blue arc (right: roughly -20 to 35 degrees)
        drawArc(
            color = GoogleBlue,
            startAngle = -20f,
            sweepAngle = 55f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Blue center horizontal bar
        val barHeight = strokeWidth * 0.95f
        val barPath = Path().apply {
            moveTo(cx, cy - barHeight / 2f)
            lineTo(w, cy - barHeight / 2f)
            lineTo(w, cy + barHeight / 2f)
            lineTo(cx, cy + barHeight / 2f)
            close()
        }
        drawPath(path = barPath, color = GoogleBlue, style = Fill)
    }
}
