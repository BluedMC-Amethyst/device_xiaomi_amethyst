#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit_only.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit some common Lineage stuff.
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

# Inherit from amethyst device
$(call inherit-product, device/xiaomi/amethyst/device.mk)

PRODUCT_NAME := lineage_amethyst
PRODUCT_DEVICE := amethyst
PRODUCT_MANUFACTURER := Xiaomi
PRODUCT_BRAND := Redmi
PRODUCT_MODEL := Redmi Note 14 Pro+ 5G

PRODUCT_GMS_CLIENTID_BASE := android-xiaomi
