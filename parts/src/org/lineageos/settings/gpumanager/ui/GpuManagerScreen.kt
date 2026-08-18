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

package org.lineageos.settings.gpumanager.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import org.lineageos.settings.gpumanager.GpuManagerViewModel

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
fun GpuManagerScreen(
    viewModel: GpuManagerViewModel,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.gpu_manager_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.resetSettings()
                        Toast.makeText(context, R.string.settings_reset, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
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
                    icon = { Icon(Icons.Default.Check, null) },
                    text = { Text(stringResource(R.string.apply_settings), fontWeight = FontWeight.SemiBold) },
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimatedEntrance(visible = visible, index = 0) {
                    GpuInfoCard(
                        model = uiState.gpuModel,
                        load = uiState.busyPercentage,
                        temp = uiState.temperature,
                        freq = uiState.currentFreq
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 1) {
                    CategoryHeader(stringResource(R.string.gpu_settings_category))
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 2) {
                    GpuDropdownPreference(
                        title = stringResource(R.string.gpu_governor_title),
                        selectedOption = uiState.currentGovernor,
                        options = uiState.availableGovernors,
                        onOptionSelected = { viewModel.setGovernor(it) },
                        icon = Icons.Default.Settings,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 3) {
                    GpuFrequenciesCard(
                        minFreq = uiState.currentMinFreq,
                        maxFreq = uiState.currentMaxFreq,
                        options = uiState.availableFrequencies,
                        onMinSelected = { viewModel.setMinFrequency(it) },
                        onMaxSelected = { viewModel.setMaxFrequency(it) }
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 4) {
                    CategoryHeader(stringResource(R.string.gpu_power_title))
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 5) {
                    GpuSwitchPreference(
                        title = stringResource(R.string.gpu_force_clk_on_title),
                        summary = stringResource(R.string.gpu_force_clk_on_summary),
                        checked = uiState.forceClkOn,
                        onCheckedChange = { viewModel.setForceClkOn(it) }
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 6) {
                    GpuSwitchPreference(
                        title = stringResource(R.string.gpu_force_bus_on_title),
                        summary = stringResource(R.string.gpu_force_bus_on_summary),
                        checked = uiState.forceBusOn,
                        onCheckedChange = { viewModel.setForceBusOn(it) }
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 7) {
                    GpuSwitchPreference(
                        title = stringResource(R.string.gpu_force_rail_on_title),
                        summary = stringResource(R.string.gpu_force_rail_on_summary),
                        checked = uiState.forceRailOn,
                        onCheckedChange = { viewModel.setForceRailOn(it) }
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 8) {
                    GpuSwitchPreference(
                        title = stringResource(R.string.gpu_force_no_nap_title),
                        summary = stringResource(R.string.gpu_force_no_nap_summary),
                        checked = uiState.forceNoNap,
                        onCheckedChange = { viewModel.setForceNoNap(it) }
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 9) {
                    GpuSwitchPreference(
                        title = stringResource(R.string.gpu_bus_split_title),
                        summary = stringResource(R.string.gpu_bus_split_summary),
                        checked = uiState.busSplit,
                        onCheckedChange = { viewModel.setBusSplit(it) }
                    )
                }
            }
            
            item {
                AnimatedEntrance(visible = visible, index = 10) {
                    CategoryHeader(stringResource(R.string.gpu_thermal_title))
                }
            }
            
            item {
                AnimatedEntrance(visible = visible, index = 11) {
                    InfoItem(
                        title = stringResource(R.string.gpu_thermal_pwrlevel_title),
                        value = "Level ${uiState.thermalPowerLevel}",
                        summary = stringResource(R.string.gpu_thermal_pwrlevel_summary)
                    )
                }
            }

            item {
                AnimatedEntrance(visible = visible, index = 12) {
                    GpuSwitchPreference(
                        title = stringResource(R.string.apply_on_boot_title),
                        summary = stringResource(R.string.apply_on_boot_summary),
                        checked = uiState.applyOnBoot,
                        onCheckedChange = { viewModel.setApplyOnBoot(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun GpuInfoCard(
    model: String,
    load: String,
    temp: String,
    freq: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Build, 
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = model,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(stringResource(R.string.gpu_busy_percentage_title), load)
                InfoColumn(stringResource(R.string.gpu_temp_title), "$temp°C")
                InfoColumn(
                    stringResource(R.string.gpu_current_freq_title), 
                    if (freq != "0" && freq.isNotEmpty()) "${try { freq.toLong() / 1000000 } catch(e:Exception) { freq }} MHz" else "Offline"
                )
            }
        }
    }
}

@Composable
fun InfoColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
        )
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            },
            label = "value_anim"
        ) { targetValue ->
            Text(
                text = targetValue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpuDropdownPreference(
    title: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    var expanded by remember { mutableStateOf(false) }
    
    val containerColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(300),
        label = "dropdownColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
            .animateContentSize(spring(stiffness = Spring.StiffnessLow)),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedContent(targetState = selectedOption, label = "optionAnim") { opt ->
                        Text(
                            text = "Active: $opt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = color
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Option") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = color
                    ),
                    shape = MaterialTheme.shapes.large
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = option,
                                    fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal,
                                    color = if (option == selectedOption) color else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                onOptionSelected(option)
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
fun GpuFrequenciesCard(
    minFreq: String,
    maxFreq: String,
    options: List<String>,
    onMinSelected: (String) -> Unit,
    onMaxSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(stiffness = Spring.StiffnessLow)),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.gpu_freq_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GpuFreqSelector(
                    modifier = Modifier.weight(1f),
                    label = "Min",
                    currentFreq = minFreq,
                    options = options,
                    onSelected = onMinSelected,
                    color = MaterialTheme.colorScheme.secondary
                )
                GpuFreqSelector(
                    modifier = Modifier.weight(1f),
                    label = "Max",
                    currentFreq = maxFreq,
                    options = options,
                    onSelected = onMaxSelected,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpuFreqSelector(
    modifier: Modifier = Modifier,
    label: String,
    currentFreq: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    color: Color
) {
    var expanded by remember { mutableStateOf(false) }
    
    val displayFreq = try {
        "${currentFreq.toLong() / 1000000} MHz"
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
                focusedBorderColor = color
            ),
            shape = MaterialTheme.shapes.large
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            options.forEach { freq ->
                val freqDisplay = try {
                    "${freq.toLong() / 1000000} MHz"
                } catch (e: Exception) {
                    freq
                }
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = freqDisplay,
                            fontWeight = if (freq == currentFreq) FontWeight.Bold else FontWeight.Normal,
                            color = if (freq == currentFreq) color else MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    onClick = {
                        onSelected(freq)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun GpuSwitchPreference(
    title: String,
    summary: String,
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
                    text = summary,
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
fun InfoItem(title: String, value: String, summary: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
