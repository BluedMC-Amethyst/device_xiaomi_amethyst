/*
 * Copyright (C) 2026 zylhdrXP
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
 
package org.lineageos.settings.amethystparts

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import org.lineageos.settings.R
import org.lineageos.settings.corecontrol.CoreControlActivity
import org.lineageos.settings.charge.ChargeActivity
import org.lineageos.settings.hypercharge.HyperChargeSettingsActivity
import org.lineageos.settings.kernelmanager.KernelManagerActivity
import org.lineageos.settings.kprofiles.KprofilesSettingsActivity
import org.lineageos.settings.gpumanager.GpuManagerActivity
import org.lineageos.settings.saturation.SaturationActivity
import org.lineageos.settings.refreshrate.RefreshSettingsActivity
import org.lineageos.settings.speaker.ClearSpeakerActivity
import org.lineageos.settings.thermal.ThermalComposeActivity

data class AmethystFeature(
    val title: String,
    val summary: String,
    val iconRes: Int,
    val activityClass: Class<*>
)

val PremiumCardShape = RoundedCornerShape(28.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmethystDashboard(onBackPressed: () -> Unit) {
    val context = LocalContext.current

    val performanceFeatures = listOf(
        AmethystFeature("Core Control", "CPU core activation & onlining", R.drawable.ic_cpu, CoreControlActivity::class.java),
        AmethystFeature("Kernel Manager", "Governor & triple-cluster scaling", R.drawable.ic_kernel_manager, KernelManagerActivity::class.java),
        AmethystFeature("GPU Manager", "Graphics clock & power tuning", R.drawable.ic_gpu_manager, GpuManagerActivity::class.java),
        AmethystFeature("Thermal Engine", "Per-app thermal profile profiles", R.drawable.ic_thermal_settings, ThermalComposeActivity::class.java)
    )

    val utilityFeatures = listOf(
        AmethystFeature("Display Labs", "Screen color mode & saturation", R.drawable.ic_saturation_tile, SaturationActivity::class.java),
        AmethystFeature("Clear Speaker", "High-frequency speaker cleaning", R.drawable.ic_clear_speaker, ClearSpeakerActivity::class.java),
        AmethystFeature("Smooth Display", "Per-app display refresh rates", R.drawable.ic_refresh_default, RefreshSettingsActivity::class.java),
        AmethystFeature("Bypass Charge", "Direct power delivery without battery heat", R.drawable.ic_charge, ChargeActivity::class.java),
        AmethystFeature("HyperCharge", "Custom charging current & speed limit", R.drawable.ic_charge, HyperChargeSettingsActivity::class.java),
        AmethystFeature("KProfiles", "In-kernel automated performance profiles", R.drawable.ic_kprofiles, KprofilesSettingsActivity::class.java)
    )

    val darkTheme = isSystemInDarkTheme()
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val scrollState = rememberScrollState()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                val collapseThreshold = 120f
                val collapseTarget = (scrollState.value / collapseThreshold).coerceIn(0f, 1f)
                val collapseProgress by animateFloatAsState(
                    targetValue = collapseTarget,
                    animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
                    label = "header_collapse_progress"
                )
                CollapsingHeader(
                    collapseProgress = collapseProgress,
                    onBackPressed = onBackPressed
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                
                StaggeredAnimatedItem(index = 0, isVisible = isVisible) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        HeroBanner(scrollValue = scrollState.value)
                    }
                }

                // Performance Engine Section
                StaggeredAnimatedItem(index = 1, isVisible = isVisible) {
                    Text(
                        "PERFORMANCE ENGINE",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                StaggeredAnimatedItem(index = 2, isVisible = isVisible, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        performanceFeatures.forEach { feature ->
                            DashboardFeatureCard(feature, context)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // System Utilities Section
                StaggeredAnimatedItem(index = 3, isVisible = isVisible) {
                    Text(
                        "SYSTEM UTILITIES",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                StaggeredAnimatedItem(index = 4, isVisible = isVisible, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        utilityFeatures.forEach { feature ->
                            DashboardFeatureCard(feature, context)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun CollapsingHeader(
    collapseProgress: Float,
    onBackPressed: () -> Unit
) {
    val titleScale by animateFloatAsState(
        targetValue = 1f - (0.36f * collapseProgress),
        animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
        label = "header_title_scale"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = 1f - collapseProgress,
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
        label = "header_subtitle_alpha"
    )
    val headerHeight by animateDpAsState(
        targetValue = lerp(180.dp, 110.dp, collapseProgress), 
        animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
        label = "header_height"
    )
    val headerBottomCorner by animateDpAsState(
        targetValue = lerp(0.dp, 32.dp, collapseProgress),
        animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
        label = "header_bottom_corner"
    )
    val backButtonCorner by animateDpAsState(
        targetValue = lerp(12.dp, 24.dp, collapseProgress),
        animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
        label = "back_button_corner"
    )
    val backButtonBgColor by animateColorAsState(
        targetValue = androidx.compose.ui.graphics.lerp(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.primary,
            collapseProgress
        ),
        animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
        label = "back_button_bg_color"
    )
    val backButtonIconColor by animateColorAsState(
        targetValue = androidx.compose.ui.graphics.lerp(
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.onPrimary,
            collapseProgress
        ),
        animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
        label = "back_button_icon_color"
    )
    
    val titleXOffset = lerp(0.dp, 60.dp, collapseProgress)
    val titleYOffset = lerp(68.dp, 20.dp, collapseProgress) 
    
    val backButtonShape = if (collapseProgress >= 0.98f) {
        CircleShape
    } else {
        RoundedCornerShape(backButtonCorner)
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = lerp(0.dp, 4.dp, collapseProgress),
        shape = RoundedCornerShape(bottomStart = headerBottomCorner, bottomEnd = headerBottomCorner)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .statusBarsPadding()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 8.dp)
                    .background(
                        color = backButtonBgColor,
                        shape = backButtonShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = backButtonIconColor
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = titleXOffset, y = titleYOffset)
                    .graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                Text(
                    text = "AMETHYST PARTS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "SYSTEM IS YOURS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.graphicsLayer {
                        alpha = subtitleAlpha
                        translationY = -16f * collapseProgress
                    }
                )
            }
        }
    }
}

@Composable
fun HeroBanner(scrollValue: Int = 0) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_banner")
    
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    val iconOffset = (animationProgress - 0.5f) * 24f
    val parallaxOffset = scrollValue * 0.2f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = PremiumCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.65f)
                    .graphicsLayer {
                        translationY = parallaxOffset * 0.5f
                    }
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary, 
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "❯",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "root@amethyst:~#",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "XIAOMI AMETHYST",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 28.sp,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    "System performance optimized",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                painter = painterResource(id = R.drawable.ic_amethyst),
                contentDescription = "Amethyst Engine",
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp, y = iconOffset.dp)
                    .graphicsLayer {
                        translationY = parallaxOffset
                    }
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DashboardFeatureCard(
    feature: AmethystFeature,
    context: Context,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { context.startActivity(Intent(context, feature.activityClass)) },
        modifier = modifier.fillMaxWidth(),
        shape = PremiumCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeatureIcon(feature.iconRes)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = feature.summary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            ArrowBubble()
        }
    }
}

@Composable
private fun FeatureIcon(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ArrowBubble() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StaggeredAnimatedItem(
    index: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = index * 80, easing = FastOutSlowInEasing),
        label = "alpha_$index"
    )
    val translateY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(durationMillis = 500, delayMillis = index * 80, easing = FastOutSlowInEasing),
        label = "translateY_$index"
    )

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translateY.toPx()
        }
    ) {
        content()
    }
}
