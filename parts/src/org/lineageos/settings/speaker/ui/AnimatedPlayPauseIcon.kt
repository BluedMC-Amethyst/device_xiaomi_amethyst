/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 *                         2026 zylhdrXP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.speaker.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun AnimatedPlayPauseIcon(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val fraction by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "play_pause_fraction"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isPlaying) 90f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "play_pause_rotation"
    )

    Canvas(modifier = modifier.graphicsLayer(rotationZ = rotation)) {
        val w = size.width
        val h = size.height
        
        fun lerp(start: Float, stop: Float, f: Float): Float {
            return start + (stop - start) * f
        }

        val l1x = lerp(0.35f, 0.2f, fraction) * w
        val l1y = lerp(0.2f, 0.6f, fraction) * h
        val l2x = lerp(0.56f, 0.8f, fraction) * w
        val l2y = lerp(0.32f, 0.6f, fraction) * h
        val l3x = lerp(0.56f, 0.8f, fraction) * w
        val l3y = lerp(0.68f, 0.75f, fraction) * h
        val l4x = lerp(0.35f, 0.2f, fraction) * w
        val l4y = lerp(0.8f, 0.75f, fraction) * h

        val leftPath = Path().apply {
            moveTo(l1x, l1y)
            lineTo(l2x, l2y)
            lineTo(l3x, l3y)
            lineTo(l4x, l4y)
            close()
        }
        
        val r1x = lerp(0.54f, 0.2f, fraction) * w
        val r1y = lerp(0.32f, 0.25f, fraction) * h
        val r2x = lerp(0.85f, 0.8f, fraction) * w
        val r2y = lerp(0.5f, 0.25f, fraction) * h
        val r3x = lerp(0.85f, 0.8f, fraction) * w
        val r3y = lerp(0.5f, 0.4f, fraction) * h
        val r4x = lerp(0.54f, 0.2f, fraction) * w
        val r4y = lerp(0.68f, 0.4f, fraction) * h

        val rightPath = Path().apply {
            moveTo(r1x, r1y)
            lineTo(r2x, r2y)
            lineTo(r3x, r3y)
            lineTo(r4x, r4y)
            close()
        }
        
        val mergedPath = Path().apply {
            op(leftPath, rightPath, PathOperation.Union)
        }
        
        val paint = Paint().apply {
            this.color = color
            this.isAntiAlias = true
            this.pathEffect = PathEffect.cornerPathEffect(w * 0.1f)
        }
        
        drawIntoCanvas { canvas ->
            canvas.drawPath(mergedPath, paint)
        }
    }
}
