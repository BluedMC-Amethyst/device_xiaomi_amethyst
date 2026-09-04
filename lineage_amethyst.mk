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
PRODUCT_MODEL := 24115RA8EG

PRODUCT_SYSTEM_NAME := amethyst_global
PRODUCT_SYSTEM_DEVICE := amethyst

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="amethyst_global-user 16 BP2A.250605.031.A3 16OS3.1.260824.102142654.QCPEGL.S release-keys" \
    BuildFingerprint=Redmi/amethyst_global/amethyst:16/BP2A.250605.031.A3/16OS3.1.260824.102142654.QCPEGL.S:user/release-keys \
    DeviceName=$(PRODUCT_SYSTEM_DEVICE) \
    DeviceProduct=$(PRODUCT_SYSTEM_NAME)

PRODUCT_GMS_CLIENTID_BASE := android-xiaomi
