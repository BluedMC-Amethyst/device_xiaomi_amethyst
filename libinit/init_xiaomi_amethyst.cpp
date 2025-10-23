/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#include <vector>

#include <android-base/properties.h>
#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>

using android::base::GetProperty;

std::vector<std::string> ro_props_default_source_order = {
    "",
    "bootimage.",
    "odm.",
    "odm_dlkm.",
    "product.",
    "system.",
    "system_ext.",
    "vendor.",
    "vendor_dlkm.",
};

void property_override(char const prop[], char const value[], bool add = true)
{
    prop_info *pi;

    pi = (prop_info *) __system_property_find(prop);
    if (pi)
        __system_property_update(pi, value, strlen(value));
    else if (add)
        __system_property_add(prop, strlen(prop), value, strlen(value));
}

void set_ro_build_prop(const std::string &prop, const std::string &value) {
    for (const auto &source : ro_props_default_source_order) {
        auto prop_name = "ro." + source + "build." + prop;
        if (source == "")
            property_override(prop_name.c_str(), value.c_str());
        else
            property_override(prop_name.c_str(), value.c_str(), false);
    }
};

void set_ro_product_prop(const std::string &prop, const std::string &value) {
    for (const auto &source : ro_props_default_source_order) {
        auto prop_name = "ro.product." + source + prop;
        property_override(prop_name.c_str(), value.c_str(), false);
    }
};

void vendor_load_properties() {
    std::string region;
    std::string sku;
    region = GetProperty("ro.boot.hwc", "");
    sku = GetProperty("ro.boot.hardware.sku", "");

    std::string model;
    std::string brand;
    std::string device;
    std::string fingerprint;
    std::string description;
    std::string marketname;
    std::string mod_device;

    if (region == "IN") {
        // India — Redmi Note 14 Pro+ 5G
        device = "amethyst";
        mod_device = "amethyst_in";
        brand = "Redmi";
        description = "amethyst_in-user 16 BP2A.250605.031.A3 OS3.0.3.0.WOPINXM release-keys";
        fingerprint = "Redmi/amethyst_in/amethyst:16/BP2A.250605.031.A3/OS3.0.3.0.WOPINXM:user/release-keys";
        marketname = "Redmi Note 14 Pro+ 5G";
        model = "24115RA8EI";
    } else if (region == "GL") {
        // Global — Redmi Note 14 Pro+ 5G
        device = "amethyst";
        mod_device = "amethyst_global";
        brand = "Redmi";
        description = "amethyst_global-user 16 BP2A.250605.031.A3 OS3.0.4.0.WOPMIXM release-keys";
        fingerprint = "Redmi/amethyst_global/amethyst:16/BP2A.250605.031.A3/OS3.0.4.0.WOPMIXM:user/release-keys";
        marketname = "Redmi Note 14 Pro+ 5G";
        model = "24115RA8EG";
    } else if (region == "CN") {
        // China — Redmi Note 14 Pro+
        device = "amethyst";
        mod_device = "amethyst";
        brand = "Redmi";
        description = "amethyst_global-user 16 BP2A.250605.031.A3 OS3.0.4.0.WOPCNXM release-keys";
        fingerprint = "Redmi/amethyst_global/amethyst:16/BP2A.250605.031.A3/OS3.0.4.0.WOPCNXM:user/release-keys";
        marketname = "Redmi Note 14 Pro+";
        model = "24115RA8EC";
    }

    set_ro_build_prop("fingerprint", fingerprint);
    set_ro_product_prop("brand", brand);
    set_ro_product_prop("device", device);
    set_ro_product_prop("model", model);

    property_override("bluetooth.device.default_name", marketname.c_str());
    property_override("ro.build.description", description.c_str());
    property_override("ro.product.marketname", marketname.c_str());
    property_override("vendor.usb.product_string", marketname.c_str());
    if (mod_device != "") {
        property_override("ro.product.mod_device", mod_device.c_str());
    }
}
