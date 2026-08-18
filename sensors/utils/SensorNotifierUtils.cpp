/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "SensorNotifierUtils"

#include "SensorNotifierUtils.h"

#include <android-base/logging.h>
#include <fcntl.h>
#include <unistd.h>

bool readBool(int fd) {
    char c;
    int rc;
    rc = lseek(fd, 0, SEEK_SET);
    if (rc) {
        LOG(ERROR) << "failed to seek fd, err: " << rc;
        return false;
    }
    rc = read(fd, &c, sizeof(char));
    if (rc != 1) {
        LOG(ERROR) << "failed to read bool from fd, err: " << rc;
        return false;
    }
    return c != '0';
}

disp_event_resp* parseDispEvent(int fd) {
    char buf[1024];
    memset(buf, 0, sizeof(buf));
    ssize_t n = read(fd, buf, sizeof(buf));
    if (n < static_cast<ssize_t>(sizeof(disp_event))) {
        return nullptr;
    }

    disp_event_resp* resp = reinterpret_cast<disp_event_resp*>(buf);
    if (resp->base.length < sizeof(disp_event) || resp->base.length > sizeof(buf)) {
        return nullptr;
    }

    struct disp_event_resp* response =
            reinterpret_cast<struct disp_event_resp*>(malloc(resp->base.length));
    if (response == nullptr) {
        LOG(ERROR) << "failed to allocate memory for disp_event_resp";
        return nullptr;
    }

    memcpy(response, buf, resp->base.length);
    return response;
}
