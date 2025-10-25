#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

DEVICE_PATH := device/xiaomi/amethyst

# Architecture
TARGET_ARCH := arm64
TARGET_ARCH_VARIANT := armv8-a-branchprot
TARGET_CPU_ABI := arm64-v8a
TARGET_CPU_VARIANT := generic
TARGET_CPU_VARIANT_RUNTIME := kryo300

# Bootloader
TARGET_BOOTLOADER_BOARD_NAME := amethyst
TARGET_NO_BOOTLOADER := true

# Platform
BOARD_USES_QCOM_HARDWARE := true
TARGET_BOARD_PLATFORM := volcano

# Inherit the proprietary files
include vendor/xiaomi/amethyst/BoardConfigVendor.mk
