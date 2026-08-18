/*
 * Copyright (C) 2024 The LineageOS Project
 * Copyright (C) 2026 LumineDroid
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "UdfpsHandler.amethyst"

#include "UdfpsHandler.h"

#include <aidl/android/hardware/biometrics/fingerprint/BnFingerprint.h>
#include <android-base/logging.h>
#include <android-base/unique_fd.h>
#include <display/drm/mi_disp.h>

#include <atomic>
#include <chrono>
#include <fstream>
#include <mutex>
#include <poll.h>
#include <sys/ioctl.h>
#include <thread>

#define TOUCH_DEV_PATH "/dev/xiaomi-touch"
#define TOUCH_MAGIC 0x54
#define CMD_DATA_BUF_SIZE 256

#define IOCTL_IDX_COMMON_DATA 0
#define IOCTL_IDX_SELECT_TOUCH 3

#define TOUCH_MODE_FOD_ENABLE 10
#define TOUCH_MODE_FOD_DOWNUP_CTL 1001
#define TOUCH_MODE_FOD_COORD 0x5403

#define FOD_COORD_X 61000
#define FOD_COORD_Y 243700

typedef struct {
  int8_t touch_id;
  uint8_t cmd;
  uint16_t mode;
  uint16_t data_len;
  int32_t data_buf[CMD_DATA_BUF_SIZE];
} touch_base;

#define TOUCH_IOC_SELECT_TOUCH_ID _IOW(TOUCH_MAGIC, IOCTL_IDX_SELECT_TOUCH, int)
#define TOUCH_IOC_COMMON_DATA                                                  \
  _IOW(TOUCH_MAGIC, IOCTL_IDX_COMMON_DATA, touch_base)

#define DISP_FEATURE_PATH "/dev/mi_display/disp_feature"
#define DISP_POWER_ON 1

#define COMMAND_NIT 10
#define PARAM_NIT_FOD 1
#define PARAM_NIT_NONE 0

#define COMMAND_FOD_PRESS_STATUS 1
#define PARAM_FOD_PRESSED 1
#define PARAM_FOD_RELEASED 0

#define FOD_PRESS_STATUS_PATH "/sys/class/touch/touch_dev/fod_press_status"

#define SHIELD_DURATION_SCREEN_ON_MS 400
#define SHIELD_DURATION_AUTH_END_MS 1000
#define HBM_SETTLE_DELAY_MS 28
#define CLEANUP_INITIAL_DELAY_MS 40
#define CLEANUP_PULSE_HOLD_MS 20

using ::aidl::android::hardware::biometrics::fingerprint::AcquiredInfo;

namespace {

static disp_base kDisplayPrimary = {.flag = 0, .disp_id = MI_DISP_PRIMARY};

static bool readBool(int fd) {
  char c;
  lseek(fd, 0, SEEK_SET);
  return (read(fd, &c, sizeof(c)) == 1) && (c != '0');
}

static disp_event_resp *parseDispEvent(int fd) {
  static char buf[1024];
  memset(buf, 0, sizeof(buf));
  ssize_t n = read(fd, buf, sizeof(buf));
  if (n < static_cast<ssize_t>(sizeof(disp_event)))
    return nullptr;
  return reinterpret_cast<disp_event_resp *>(buf);
}

} // namespace

class XiaomiAmethystUdfpsHandler : public UdfpsHandler {
public:
  void init(fingerprint_device_t *device) override {
    mDevice = device;
    mAuthActive = false;
    mScreenOn = true;
    mUnlockTransition = false;
    mFodModeActive = false;
    mLastHbmValue = -1;

    touch_fd_ = android::base::unique_fd(open(TOUCH_DEV_PATH, O_RDWR));
    disp_fd_ = android::base::unique_fd(open(DISP_FEATURE_PATH, O_RDWR));

    sendFodCoordinates();
    startFodPressThread();
    startDispEventThread();
  }

  void onFingerDown(uint32_t /*x*/, uint32_t /*y*/, float /*minor*/,
                    float /*major*/) override {
    if (mAuthSuccess)
      return;
    if (mUnlockTransition)
      return;

    mCleanupGeneration++;

    if (mAuthActive)
      return;

    mAuthActive = true;

    if (!mFodModeActive) {
      sendFodCoordinates();
      setFodStatus(true);
    }

    setFpStatus(1);
    setFingerDown(true);
    updateHbm(true);
    mDevice->extCmd(mDevice, COMMAND_FOD_PRESS_STATUS, PARAM_FOD_PRESSED);
  }

  void onFingerUp() override {
    if (!mAuthActive)
      return;

    mAuthActive = false;
    mLastHbmValue = -1;

    updateHbm(false);
    setFingerDown(false);
    mDevice->extCmd(mDevice, COMMAND_FOD_PRESS_STATUS, PARAM_FOD_RELEASED);
    forceDeepClean(/*isCancel=*/false);
  }

  void onAcquired(int32_t result, int32_t vendorCode) override {
    if (result == 0 || vendorCode == 23) {
      if (!mScreenOn) {
        activateShield(SHIELD_DURATION_AUTH_END_MS);
        onFingerUp();
      }
      return;
    }

    switch (static_cast<AcquiredInfo>(result)) {
    case AcquiredInfo::GOOD:
    case AcquiredInfo::PARTIAL:
    case AcquiredInfo::INSUFFICIENT:
    case AcquiredInfo::SENSOR_DIRTY:
    case AcquiredInfo::TOO_SLOW:
    case AcquiredInfo::TOO_FAST:
      onFingerUp();
      break;
    default:
      break;
    }
  }

  void cancel() override {
    mAuthActive = false;
    mLastHbmValue = -1;

    updateHbm(false);
    setFingerDown(false);
    setFpStatus(0);
    if (mScreenOn)
      setFodStatus(false);

    mDevice->extCmd(mDevice, COMMAND_FOD_PRESS_STATUS, PARAM_FOD_RELEASED);
    activateShield(SHIELD_DURATION_AUTH_END_MS);
    forceDeepClean(/*isCancel=*/true);
  }

  void onAuthenticationSucceeded() override {
    mAuthSuccess = true;
    onFingerUp();
    std::thread([this]() {
      std::this_thread::sleep_for(std::chrono::milliseconds(500));
      mAuthSuccess = false;
    }).detach();
  }

  void onAuthenticationFailed() override {
    onFingerUp();
  }

private:
  fingerprint_device_t *mDevice;
  android::base::unique_fd touch_fd_;
  android::base::unique_fd disp_fd_;

  std::atomic<bool> mAuthSuccess{false};

  std::atomic<bool> mAuthActive;
  std::atomic<bool> mScreenOn;
  std::atomic<bool> mUnlockTransition;
  std::atomic<bool> mFodModeActive{false};
  std::atomic<int> mCleanupGeneration{0};
  std::atomic<int> mShieldGeneration{0};

  int mLastHbmValue;
  std::mutex mHbmMutex;

  void startFodPressThread() {
    std::thread([this]() {
      int fd = open(FOD_PRESS_STATUS_PATH, O_RDONLY);
      if (fd < 0) {
        LOG(ERROR) << "Failed to open fod_press_status";
        return;
      }

      pollfd pfd = {.fd = fd, .events = POLLERR | POLLPRI};

      while (true) {
        if (poll(&pfd, 1, -1) < 0)
          continue;

        bool pressed = readBool(fd);

        if (!mScreenOn || !mAuthActive)
          continue;
        if (!pressed && !mUnlockTransition)
          onFingerUp();
      }
    }).detach();
  }

  void startDispEventThread() {
    std::thread([this]() {
      android::base::unique_fd fd(open(DISP_FEATURE_PATH, O_RDWR));

      disp_event_req fodEvt = {.base = kDisplayPrimary,
                               .type = MI_DISP_EVENT_FOD};
      ioctl(fd.get(), MI_DISP_IOCTL_REGISTER_EVENT, &fodEvt);

      disp_event_req pwrEvt = {.base = kDisplayPrimary,
                               .type = MI_DISP_EVENT_POWER};
      ioctl(fd.get(), MI_DISP_IOCTL_REGISTER_EVENT, &pwrEvt);

      pollfd pfd = {.fd = fd.get(), .events = POLLIN};

      while (true) {
        if (poll(&pfd, 1, -1) < 0)
          continue;

        disp_event_resp *resp = parseDispEvent(fd.get());
        if (!resp)
          continue;

        if (resp->base.type == MI_DISP_EVENT_FOD) {
          handleFodEvent(resp);
        } else if (resp->base.type == MI_DISP_EVENT_POWER) {
          handlePowerEvent(resp);
        }
      }
    }).detach();
  }

  void handleFodEvent(disp_event_resp *resp) {
    bool uiReady = resp->data[0] & LOCAL_HBM_UI_READY;
    mDevice->extCmd(mDevice, COMMAND_NIT,
                    uiReady ? PARAM_NIT_FOD : PARAM_NIT_NONE);
  }

  void handlePowerEvent(disp_event_resp *resp) {
    bool screenOn = (resp->data[0] == DISP_POWER_ON);
    bool wasScreenOn = mScreenOn.exchange(screenOn);

    if (screenOn) {
      if (!wasScreenOn)
        activateShield(SHIELD_DURATION_SCREEN_ON_MS);
      if (!mAuthActive) {
        setFodStatus(false);
        forceDeepClean(/*isCancel=*/true);
      }
    } else {
      sendFodCoordinates();
      setFodStatus(true);
    }
  }

  void sendFodCoordinates() {
    touch_base coord = {
        .mode = TOUCH_MODE_FOD_COORD,
        .data_len = 2,
        .data_buf = {FOD_COORD_X, FOD_COORD_Y},
    };
    ioctl(touch_fd_.get(), TOUCH_IOC_COMMON_DATA, &coord);
  }

  void setFodStatus(bool enable) {
    mFodModeActive = enable;

    ioctl(touch_fd_.get(), TOUCH_IOC_SELECT_TOUCH_ID, MI_DISP_PRIMARY);

    touch_base data = {
        .mode = TOUCH_MODE_FOD_ENABLE,
        .data_len = 1,
        .data_buf = {enable ? 1 : 0},
    };
    ioctl(touch_fd_.get(), TOUCH_IOC_COMMON_DATA, &data);
  }

  void setFingerDown(bool pressed) {
    ioctl(touch_fd_.get(), TOUCH_IOC_SELECT_TOUCH_ID, MI_DISP_PRIMARY);

    touch_base data = {
        .mode = TOUCH_MODE_FOD_DOWNUP_CTL,
        .data_len = 1,
        .data_buf = {pressed ? 1 : 0},
    };
    ioctl(touch_fd_.get(), TOUCH_IOC_COMMON_DATA, &data);
  }

  void setFpStatus(int status) {
    disp_feature_req req = {
        .base = kDisplayPrimary,
        .feature_id = DISP_FEATURE_FP_STATUS,
        .feature_val = static_cast<__s32>(status),
    };
    ioctl(disp_fd_.get(), MI_DISP_IOCTL_SET_FEATURE, &req);
  }

  void updateHbm(bool enable) {
    std::lock_guard<std::mutex> lock(mHbmMutex);

    int target = enable ? LHBM_TARGET_BRIGHTNESS_WHITE_1000NIT
                        : LHBM_TARGET_BRIGHTNESS_OFF_FINGER_UP;

    if (target != 0 && mLastHbmValue == target)
      return;

    disp_local_hbm_req req = {
        .base = kDisplayPrimary,
        .local_hbm_value = static_cast<uint32_t>(target),
    };
    ioctl(disp_fd_.get(), MI_DISP_IOCTL_SET_LOCAL_HBM, &req);
    mLastHbmValue = target;

    if (enable)
      std::this_thread::sleep_for(
          std::chrono::milliseconds(HBM_SETTLE_DELAY_MS));
  }

  void activateShield(int durationMs) {
    mUnlockTransition = true;
    int currentGen = ++mShieldGeneration;

    std::thread([this, currentGen, durationMs]() {
      std::this_thread::sleep_for(std::chrono::milliseconds(durationMs));
      if (mShieldGeneration == currentGen)
        mUnlockTransition = false;
    }).detach();
  }

  void forceDeepClean(bool isCancel) {
    int currentGen = ++mCleanupGeneration;

    std::thread([this, currentGen, isCancel]() {
      using namespace std::chrono_literals;

      auto aborted = [&]() {
        return mAuthActive || (mCleanupGeneration != currentGen);
      };

      std::this_thread::sleep_for(
          std::chrono::milliseconds(CLEANUP_INITIAL_DELAY_MS));
      if (aborted())
        return;

      setFpStatus(0);
      if (mScreenOn && isCancel)
        setFodStatus(false);

      static constexpr int kDelays[] = {40, 60, 100, 150, 250, 350, 500};

      for (int delay : kDelays) {
        std::this_thread::sleep_for(std::chrono::milliseconds(delay));
        if (aborted())
          return;

        setFpStatus(1);
        std::this_thread::sleep_for(
            std::chrono::milliseconds(CLEANUP_PULSE_HOLD_MS));

        if (aborted()) {
          setFpStatus(0);
          return;
        }

        mLastHbmValue = -1;
        updateHbm(false);
        std::this_thread::sleep_for(
            std::chrono::milliseconds(CLEANUP_PULSE_HOLD_MS));
        if (aborted())
          return;

        setFpStatus(0);
        if (mScreenOn && (isCancel || delay >= 350)) {
          setFodStatus(false);
        }
      }
    }).detach();
  }
};

static UdfpsHandler *create() { return new XiaomiAmethystUdfpsHandler(); }
static void destroy(UdfpsHandler *h) { delete h; }

extern "C" UdfpsHandlerFactory UDFPS_HANDLER_FACTORY = {
    .create = create,
    .destroy = destroy,
};
