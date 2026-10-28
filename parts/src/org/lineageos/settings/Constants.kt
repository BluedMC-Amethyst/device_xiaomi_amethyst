/*
 * Copyright (C) 2024 The LineageOS Project
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

package org.lineageos.settings;

object Constants {

    // Saturation
    const val KEY_SATURATION = "saturation"
    const val KEY_SATURATION_PREVIEW = "saturation_preview"

    // HyperCharge
    const val KEY_HYPERCHARGE_STATUS = "hypercharge_status"
    const val KEY_HYPERCHARGE_LIMIT = "hypercharge_limit"

    const val NODE_CONSTANT_CHARGE_CURRENT = "/sys/class/power_supply/battery/constant_charge_current"
    const val NODE_FASTCHG_MODE = "/sys/class/qcom-battery/fastchg_mode"

    const val CHARGE_LIMIT_120W = "22000000"
    const val CHARGE_LIMIT_90W = "16500000"
    const val CHARGE_LIMIT_67W = "12300000"
    const val CHARGE_LIMIT_50W = "9200000"
    const val CHARGE_LIMIT_33W = "6000000"
    const val CHARGE_LIMIT_18W = "3300000"
    const val CHARGE_LIMIT_10W = "1800000"

}
