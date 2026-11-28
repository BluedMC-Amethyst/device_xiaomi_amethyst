#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#
# Kernel selection for amethyst, included by both device.mk and BoardConfig.mk.
#
# AMETHYST_KERNEL picks the kernel:
#   source   - build kernel/xiaomi/amethyst and every vendor module that has
#              source (see scripts/amethyst-los-setup.sh in that tree);
#   prebuilt - use the stock kernel, modules and system_dlkm from
#              device/xiaomi/amethyst-kernel.
# By default it is "source" when the kernel tree is synced and set up, and
# "prebuilt" otherwise. Set AMETHYST_KERNEL (environment or command line) to
# force either. TARGET_BUILD_KERNEL_FROM_SOURCE=true|false is still honoured.
#

AMETHYST_KERNEL_SOURCE_DIR := kernel/xiaomi/amethyst
AMETHYST_KERNEL_PREBUILT_DIR := device/xiaomi/amethyst-kernel
AMETHYST_KERNEL_EXT_MODULE_ROOT := kernel/xiaomi/sm7635-modules

ifeq ($(AMETHYST_KERNEL),)
  ifeq ($(TARGET_BUILD_KERNEL_FROM_SOURCE),true)
    AMETHYST_KERNEL := source
  else ifeq ($(TARGET_BUILD_KERNEL_FROM_SOURCE),false)
    AMETHYST_KERNEL := prebuilt
  else ifneq ($(wildcard $(AMETHYST_KERNEL_SOURCE_DIR)/scripts/amethyst-ext-modules.txt),)
    AMETHYST_KERNEL := source
  else
    AMETHYST_KERNEL := prebuilt
  endif
endif

ifeq ($(AMETHYST_KERNEL),source)
  ifeq ($(wildcard $(AMETHYST_KERNEL_SOURCE_DIR)/scripts/amethyst-ext-modules.txt),)
    $(error AMETHYST_KERNEL=source, but $(AMETHYST_KERNEL_SOURCE_DIR) is not set up; run its scripts/amethyst-los-setup.sh)
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
