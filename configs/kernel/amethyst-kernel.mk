#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#
# Kernel selection for amethyst, included by both device.mk and BoardConfig.mk.
#
# AMETHYST_KERNEL picks the kernel:
#   source   - build kernel/xiaomi/sm7635 and every vendor module in
#              kernel/xiaomi/sm7635-modules (the LineageOS kernel layout);
#   prebuilt - use the stock kernel, modules and system_dlkm from
#              device/xiaomi/amethyst-kernel.
# By default it is "source" when the kernel and its compiler are present, and
# "prebuilt" otherwise. Set AMETHYST_KERNEL (environment or command line) to
# force either. TARGET_BUILD_KERNEL_FROM_SOURCE=true|false is still honoured.
#

AMETHYST_KERNEL_SOURCE_DIR := kernel/xiaomi/sm7635
AMETHYST_KERNEL_PREBUILT_DIR := device/xiaomi/amethyst-kernel
AMETHYST_KERNEL_EXT_MODULE_ROOT := kernel/xiaomi/sm7635-modules
AMETHYST_DT_DIR := kernel/xiaomi/sm7635-devicetrees

# 6.1.68 and Xiaomi's modules need the clang they were written for (newer
# clangs reject mm/shmem.c and WALT). It is not part of LineageOS, so the
# source kernel is only picked automatically when it is installed.
AMETHYST_KERNEL_CLANG_PATH := prebuilts/clang/kernel/linux-x86/clang-r487747c

ifeq ($(AMETHYST_KERNEL),)
  ifeq ($(TARGET_BUILD_KERNEL_FROM_SOURCE),true)
    AMETHYST_KERNEL := source
  else ifeq ($(TARGET_BUILD_KERNEL_FROM_SOURCE),false)
    AMETHYST_KERNEL := prebuilt
  else ifneq ($(and $(wildcard $(AMETHYST_KERNEL_SOURCE_DIR)/scripts/amethyst-ext-modules.txt), \
                    $(wildcard $(AMETHYST_KERNEL_CLANG_PATH)/bin/clang-17)),)
    AMETHYST_KERNEL := source
  else
    AMETHYST_KERNEL := prebuilt
  endif
endif

ifeq ($(AMETHYST_KERNEL),source)
  ifeq ($(wildcard $(AMETHYST_KERNEL_SOURCE_DIR)/scripts/amethyst-ext-modules.txt),)
    $(error AMETHYST_KERNEL=source, but $(AMETHYST_KERNEL_SOURCE_DIR) is not synced)
  endif
  ifeq ($(wildcard $(AMETHYST_KERNEL_CLANG_PATH)/bin/clang-17),)
    $(error AMETHYST_KERNEL=source, but clang r487747c is not installed in $(AMETHYST_KERNEL_CLANG_PATH))
  endif

  # External module projects: the kernel's list, minus projects that are not
  # synced. When any are missing, let the load lists skip their modules
  # instead of failing the build.
  AMETHYST_KERNEL_EXT_MODULES_ALL := $(strip $(shell grep -Ev '^[[:space:]]*(\#|$$)' \
      $(AMETHYST_KERNEL_SOURCE_DIR)/scripts/amethyst-ext-modules.txt))
  AMETHYST_KERNEL_EXT_MODULES := $(strip $(foreach m,$(AMETHYST_KERNEL_EXT_MODULES_ALL), \
      $(if $(wildcard $(AMETHYST_KERNEL_EXT_MODULE_ROOT)/$(m)/.),$(m))))
  AMETHYST_KERNEL_EXT_MODULES_MISSING := $(filter-out $(AMETHYST_KERNEL_EXT_MODULES), \
      $(AMETHYST_KERNEL_EXT_MODULES_ALL))
else ifeq ($(AMETHYST_KERNEL),prebuilt)
  ifeq ($(wildcard $(AMETHYST_KERNEL_PREBUILT_DIR)/images/kernel),)
    $(error AMETHYST_KERNEL=prebuilt, but $(AMETHYST_KERNEL_PREBUILT_DIR) is not synced)
  endif
  # The stock system_dlkm release directory, e.g. 6.1.138-android14-11-g...
  AMETHYST_GKI_VERSION := $(notdir $(patsubst %/,%,$(firstword \
      $(wildcard $(AMETHYST_KERNEL_PREBUILT_DIR)/modules/system_dlkm/*/))))
else
  $(error AMETHYST_KERNEL must be "source" or "prebuilt", not "$(AMETHYST_KERNEL)")
endif

# AMETHYST_DT picks the base DTBs (vendor_boot) and dtbo.img:
#   source   - built from kernel/xiaomi/sm7635-devicetrees with its
#              amethyst-dt.mk (content identical to stock, see its README);
#   prebuilt - the stock images in device/xiaomi/amethyst-kernel/images.
# By default it is "source" with the source kernel when that repository is
# synced, and "prebuilt" otherwise. The prebuilt kernel always uses the stock
# images.
AMETHYST_DT_MK := $(AMETHYST_DT_DIR)/amethyst-dt.mk
ifeq ($(AMETHYST_DT),)
  ifeq ($(AMETHYST_KERNEL)$(if $(wildcard $(AMETHYST_DT_MK)),y),sourcey)
    AMETHYST_DT := source
  else
    AMETHYST_DT := prebuilt
  endif
endif
ifeq ($(AMETHYST_DT),source)
  ifneq ($(AMETHYST_KERNEL),source)
    $(error AMETHYST_DT=source needs AMETHYST_KERNEL=source)
  endif
  ifeq ($(wildcard $(AMETHYST_DT_MK)),)
    $(error AMETHYST_DT=source, but $(AMETHYST_DT_MK) is missing; sync android_kernel_xiaomi_sm7635-devicetrees)
  endif
else ifneq ($(AMETHYST_DT),prebuilt)
  $(error AMETHYST_DT must be "source" or "prebuilt", not "$(AMETHYST_DT)")
endif
