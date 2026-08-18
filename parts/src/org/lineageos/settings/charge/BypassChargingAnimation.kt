/*
 * Copyright (C) 2026 The zylhdrXP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.charge

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Battery0Bar
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BypassChargingAnimation(
    modifier: Modifier = Modifier,
    isBypassing: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BypassAnim")
    
    val stateTransition by animateFloatAsState(
        targetValue = if (isBypassing) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "StateTransition"
    )

    // Fast moving energy phase
    val energyPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "EnergyPhase"
    )

    // CPU Pulsing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    val boxHeight = 360.dp
    val cpuYRatio = 0.2f
    val batteryYRatio = 0.5f
    val chargerYRatio = 0.85f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(boxHeight),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            
            // Layout Coordinates
            val cpuY = height * cpuYRatio
            val batteryY = height * batteryYRatio
            val chargerY = height * chargerYRatio

            // Draw Paths around Battery
            val curveSpread = 90.dp.toPx()
            val curveHeight = 40.dp.toPx()
            
            val pathLeft = Path().apply {
                moveTo(centerX, chargerY)
                cubicTo(
                    centerX, chargerY - curveHeight,
                    centerX - curveSpread, batteryY + curveHeight,
                    centerX - curveSpread, batteryY
                )
                cubicTo(
                    centerX - curveSpread, batteryY - curveHeight,
                    centerX, cpuY + curveHeight,
                    centerX, cpuY
                )
            }

            val pathRight = Path().apply {
                moveTo(centerX, chargerY)
                cubicTo(
                    centerX, chargerY - curveHeight,
                    centerX + curveSpread, batteryY + curveHeight,
                    centerX + curveSpread, batteryY
                )
                cubicTo(
                    centerX + curveSpread, batteryY - curveHeight,
                    centerX, cpuY + curveHeight,
                    centerX, cpuY
                )
            }
            
            val directPath = Path().apply {
                moveTo(centerX, chargerY)
                lineTo(centerX, batteryY + 30.dp.toPx()) // Base to battery
            }

            // Draw base tracks
            drawPath(pathLeft, surfaceVariant, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            drawPath(pathRight, surfaceVariant, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            drawPath(directPath, surfaceVariant, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))

            // Draw flowing energy
            if (stateTransition > 0.01f) {
                // Outer Glow Layer
                val glowStyle = Stroke(
                    width = 14.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(60f, 80f), -energyPhase)
                )
                drawPath(pathLeft, primaryColor.copy(alpha = 0.25f * stateTransition), style = glowStyle)
                drawPath(pathRight, secondaryColor.copy(alpha = 0.25f * stateTransition), style = glowStyle)

                // Core Layer
                val coreStyle = Stroke(
                    width = 5.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 110f), -energyPhase * 1.5f)
                )
                drawPath(pathLeft, primaryColor.copy(alpha = stateTransition), style = coreStyle)
                drawPath(pathRight, secondaryColor.copy(alpha = stateTransition), style = coreStyle)
                
                // Fast particles
                val particleStyle = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 200f), -energyPhase * 2.5f)
                )
                drawPath(pathLeft, Color.White.copy(alpha = stateTransition * 0.9f), style = particleStyle)
                drawPath(pathRight, Color.White.copy(alpha = stateTransition * 0.9f), style = particleStyle)
            } 
            
            if (stateTransition < 0.99f) {
                // Normal charging flow to battery
                val chargingStyle = Stroke(
                    width = 6.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 40f), -energyPhase)
                )
                drawPath(directPath, primaryColor.copy(alpha = 1f - stateTransition), style = chargingStyle)
            }

            // Draw Source Port
            drawRoundRect(
                color = if (isBypassing) secondaryColor else primaryColor,
                topLeft = Offset(centerX - 16.dp.toPx(), chargerY),
                size = Size(32.dp.toPx(), 12.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }

        // Icons layered over Canvas
        
        // CPU Icon
        Icon(
            imageVector = Icons.Rounded.Memory,
            contentDescription = "CPU",
            modifier = Modifier
                .offset(y = boxHeight * cpuYRatio - 28.dp)
                .size(56.dp)
                .scale(1f + (pulseScale - 1f) * stateTransition),
            tint = if (isBypassing) primaryColor else surfaceVariant
        )

        // Battery Icon
        Icon(
            imageVector = Icons.Rounded.Battery0Bar,
            contentDescription = "Battery",
            modifier = Modifier
                .offset(y = boxHeight * batteryYRatio - 28.dp)
                .size(56.dp)
                .alpha(1f - (stateTransition * 0.5f)),
            tint = if (isBypassing) onSurface.copy(alpha = 0.5f) else primaryColor
        )
    }
}
