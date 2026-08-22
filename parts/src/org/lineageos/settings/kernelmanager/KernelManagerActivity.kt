/*
 * Copyright (C) 2025 KamiKaonashi
 *           (C) 2026 zylhdrXP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package org.lineageos.settings.kernelmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.lineageos.settings.R
import org.lineageos.settings.kprofiles.KprofilesSettingsActivity

fun Modifier.bounceClick() = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}

class KernelManagerActivity : ComponentActivity() {
    private val viewModel: KernelManagerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val context = LocalContext.current
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            
            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KernelManagerScreen(
                        viewModel = viewModel,
                        onBackPressed = { finish() },
                        onNavigateToKProfiles = {
                            startActivity(Intent(this@KernelManagerActivity, KprofilesSettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KernelManagerScreen(
    viewModel: KernelManagerViewModel,
    onBackPressed: () -> Unit,
    onNavigateToKProfiles: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Kernel Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.applySettings()
                        Toast.makeText(context, R.string.settings_applied, Toast.LENGTH_SHORT).show()
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Apply") },
                    text = { Text("Apply Settings", fontWeight = FontWeight.SemiBold) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.bounceClick(),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 6.dp
                    )
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
        ) {
            item {
                AnimatedEntrance(visible = visible, index = 0) {
                    GovernorSelectionCard(
                        governors = state.availableGovernors,
                        currentGovernor = state.currentGovernor,
                        onGovernorSelected = viewModel::updateGovernor
                    )
                }
            }
            
            item {
                AnimatedEntrance(visible = visible, index = 1) {
                    FrequencyClusterCard(
                        title = "Efficiency Cluster (4x Cores)",
                        availableFreqs = state.effAvailableFreqs,
                        minFreq = state.effMinFreq,
                        maxFreq = state.effMaxFreq,
                        onMinFreqSelected = viewModel::updateEffMinFreq,
                        onMaxFreqSelected = viewModel::updateEffMaxFreq
                    )
                }
            }
            
            item {
                AnimatedEntrance(visible = visible, index = 2) {
                    FrequencyClusterCard(
                        title = "Performance Cluster (3x Cores)",
                        availableFreqs = state.perfAvailableFreqs,
                        minFreq = state.perfMinFreq,
                        maxFreq = state.perfMaxFreq,
                        onMinFreqSelected = viewModel::updatePerfMinFreq,
                        onMaxFreqSelected = viewModel::updatePerfMaxFreq
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 3) {
                    FrequencyClusterCard(
                        title = "Prime Cluster (1x Core)",
                        availableFreqs = state.primeAvailableFreqs,
                        minFreq = state.primeMinFreq,
                        maxFreq = state.primeMaxFreq,
                        onMinFreqSelected = viewModel::updatePrimeMinFreq,
                        onMaxFreqSelected = viewModel::updatePrimeMaxFreq
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 3) {
                    ActionCard(
                        title = "KProfiles",
                        subtitle = "Manage advanced KProfiles settings",
                        icon = Icons.Default.Settings,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onNavigateToKProfiles
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 4) {
                    ActionCard(
                        title = "Reset to Defaults",
                        subtitle = "Restore original kernel configurations",
                        icon = Icons.Default.Refresh,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = {
                            viewModel.resetSettings()
                            Toast.makeText(context, R.string.settings_reset, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 5) {
                    SwitchCard(
                        title = stringResource(R.string.apply_on_boot_title),
                        subtitle = stringResource(R.string.apply_on_boot_summary),
                        checked = state.applyOnBoot,
                        onCheckedChange = viewModel::setApplyOnBoot
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedEntrance(visible: Boolean, index: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { 50 + (index * 20) },
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
        ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = index * 50))
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovernorSelectionCard(
    governors: List<String>,
    currentGovernor: String,
    onGovernorSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val containerColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(300),
        label = "govColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Build, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "CPU Governor",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedContent(targetState = currentGovernor, label = "govAnim") { gov ->
                        Text(
                            text = "Active: $gov",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = currentGovernor,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Governor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = MaterialTheme.shapes.large
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    governors.forEach { gov ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = gov, 
                                    fontWeight = if (gov == currentGovernor) FontWeight.Bold else FontWeight.Normal,
                                    color = if (gov == currentGovernor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                onGovernorSelected(gov)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyClusterCard(
    title: String,
    availableFreqs: List<String>,
    minFreq: String,
    maxFreq: String,
    onMinFreqSelected: (String) -> Unit,
    onMaxFreqSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(stiffness = Spring.StiffnessLow)),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Build, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FrequencySelector(
                    modifier = Modifier.weight(1f),
                    label = "Min",
                    currentFreq = minFreq,
                    availableFreqs = availableFreqs,
                    onFreqSelected = onMinFreqSelected
                )
                
                FrequencySelector(
                    modifier = Modifier.weight(1f),
                    label = "Max",
                    currentFreq = maxFreq,
                    availableFreqs = availableFreqs,
                    onFreqSelected = onMaxFreqSelected
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencySelector(
    modifier: Modifier = Modifier,
    label: String,
    currentFreq: String,
    availableFreqs: List<String>,
    onFreqSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val displayFreq = try {
        "${currentFreq.toInt() / 1000} MHz"
    } catch (e: Exception) {
        if (currentFreq.isNotEmpty()) currentFreq else "Unknown"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.bounceClick()
    ) {
        OutlinedTextField(
            value = displayFreq,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, maxLines = 1) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary
            ),
            shape = MaterialTheme.shapes.large
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            availableFreqs.forEach { freq ->
                val freqDisplay = try {
                    "${freq.toInt() / 1000} MHz"
                } catch (e: Exception) {
                    freq
                }
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = freqDisplay,
                            fontWeight = if (freq == currentFreq) FontWeight.Bold else FontWeight.Normal,
                            color = if (freq == currentFreq) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    onClick = {
                        onFreqSelected(freq)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun SwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(durationMillis = 300),
        label = "switchBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 300),
        label = "switchContent"
    )

    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = MaterialTheme.shapes.extraLarge,
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
            .animateContentSize(spring(stiffness = Spring.StiffnessLow))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
