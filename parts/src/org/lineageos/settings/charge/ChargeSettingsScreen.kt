/*
 * Copyright (C) 2025 The LineageOS Project
 *           (C) 2026 zylhdrXP
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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.lineageos.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val chargeUtils = remember { ChargeUtils(context) }
    
    var isBypassEnabled by remember { mutableStateOf(chargeUtils.isBypassChargeEnabled()) }
    val isSupported = remember { chargeUtils.isBypassChargeSupported() }
    var showWarningDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.charge_bypass_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Animation
            BypassChargingAnimation(
                modifier = Modifier.padding(vertical = 24.dp),
                isBypassing = isBypassEnabled
            )

            // Settings Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.charge_bypass_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isSupported) {
                                    stringResource(R.string.charge_bypass_summary)
                                } else {
                                    stringResource(R.string.charge_bypass_unavailable)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isBypassEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showWarningDialog = true
                                } else {
                                    isBypassEnabled = false
                                    chargeUtils.enableBypassCharge(false)
                                }
                            },
                            enabled = isSupported
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Informational text or Footer
            Text(
                text = stringResource(R.string.charge_bypass_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }

    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null) },
            title = { Text(stringResource(R.string.charge_bypass_title)) },
            text = { Text(stringResource(R.string.charge_bypass_warning)) },
            confirmButton = {
                Button(
                    onClick = {
                        isBypassEnabled = true
                        chargeUtils.enableBypassCharge(true)
                        showWarningDialog = false
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWarningDialog = false }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}
