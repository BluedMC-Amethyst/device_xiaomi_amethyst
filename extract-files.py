#!/usr/bin/env -S PYTHONPATH=../../../tools/extract-utils python3
#
# SPDX-FileCopyrightText: 2024 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

from extract_utils.fixups_blob import (
    blob_fixup,
    blob_fixups_user_type,
)
from extract_utils.fixups_lib import (
    lib_fixup_remove,
    lib_fixups,
    lib_fixups_user_type,
)
from extract_utils.main import (
    ExtractUtils,
    ExtractUtilsModule,
)

namespace_imports = [
    'device/xiaomi/amethyst',
    'hardware/qcom-caf/sm8650',
    'hardware/qcom-caf/wlan',
    'hardware/xiaomi',
    'vendor/qcom/opensource/commonsys/display',
    'vendor/qcom/opensource/commonsys-intf/display',
    'vendor/qcom/opensource/dataservices',
    'vendor/qcom/opensource/display',
    'packages/apps/LunarisDolby',
]


def lib_fixup_vendor_suffix(lib: str, partition: str, *args, **kwargs):
    return f'{lib}_{partition}' if partition == 'vendor' else None


lib_fixups: lib_fixups_user_type = {
    **lib_fixups,
    (
    ): lib_fixup_remove,
    (
        'vendor.qti.diaghal@1.0',
        'vendor.qti.hardware.qccsyshal@1.0',
        'vendor.qti.hardware.qccsyshal@1.1',
        'vendor.qti.hardware.qccsyshal@1.2',
        'vendor.qti.hardware.camera.postproc@1.0',
        'vendor.qti.hardware.wifidisplaysession_aidl-V1-ndk',
        'vendor.qti.ImsRtpService-V1-ndk',
        'vendor.qti.imsrtpservice@3.0',
        'vendor.qti.imsrtpservice@3.1',
        'vendor.qti.qccvndhal_aidl-V1-ndk',
    ): lib_fixup_vendor_suffix,
}

blob_fixups: blob_fixups_user_type = {
    'system_ext/etc/init/qspa_system.rc': blob_fixup()
        .regex_replace(r'\$\{ro\.boot\.vendor\.qspa:-default\}', 'default'),

    'system_ext/etc/vintf/manifest/vendor.qti.qesdsys.service.xml': blob_fixup()
        .regex_replace(r'(?s)^.*?(?=<manifest)', ''),

    (
        'odm/etc/camera/enhance_motiontuning.xml',
        'odm/etc/camera/enhance_motiontuning_gl.xml',
        'odm/etc/camera/motiontuning.xml',
        'odm/etc/camera/motiontuning_gl.xml',
        'odm/etc/camera/night_motiontuning.xml',
    ): blob_fixup()
        .regex_replace('xml=version', 'xml version'),

    (
        'odm/bin/hw/android.hardware.security.keymint-service.strongbox-thales',
        'odm/lib64/libjc_keymint-thales.so',
    ): blob_fixup()
        .replace_needed(
            'android.hardware.security.keymint-V3-ndk.so',
            'android.hardware.security.keymint-V3-ndk_prebuilt.so',
        )
        .replace_needed(
            'libcppbor_external.so',
            'libcppbor_amethyst.so',
        ),

    (
        'odm/lib64/libTrueSight.so',
        'odm/lib64/libAncHumanVideoBokehV4.so',
        'odm/lib64/libwa_widelens_undistort.so',
        'odm/lib64/libanc_single_rt_bokeh.so',
        'odm/lib64/libalLDC.so',
        'vendor/lib64/libMiPhotoFilter.so',
        'vendor/lib64/libmorpho_ubwc.so',
    ): blob_fixup()
        .clear_symbol_version('AHardwareBuffer_allocate')
        .clear_symbol_version('AHardwareBuffer_describe')
        .clear_symbol_version('AHardwareBuffer_isSupported')
        .clear_symbol_version('AHardwareBuffer_lock')
        .clear_symbol_version('AHardwareBuffer_lockPlanes')
        .clear_symbol_version('AHardwareBuffer_release')
        .clear_symbol_version('AHardwareBuffer_unlock'),

    (
        'odm/bin/hw/vendor.xiaomi.sensor.citsensorservice.aidl',
    ): blob_fixup()
        .replace_needed(
            'android.hardware.graphics.composer3-V2-ndk.so',
            'android.hardware.graphics.composer3-V4-ndk.so',
        ),

    (
        'odm/lib64/camera/com.qti.actuator.amethyst_aac_s5khp3_gt9764ber_wide_ii_actuator.so',
        'odm/lib64/camera/com.qti.actuator.amethyst_ofilm_ovx8000_gt9764ber_wide_actuator.so',
        'odm/lib64/camera/com.qti.actuator.amethyst_ofilm_s5khp3_gt9764ber_wide_i_actuator.so',
        'odm/lib64/camera/com.qti.actuator.amethyst_sunny_s5kjn1_dw9800v_tele_actuator.so',
        'odm/lib64/camera/com.qti.actuator.amethyst_sunny_s5kjn1_dw9800v_tele_ii_actuator.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_aac_imx355_gt24p64e_ultra_cn_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_aac_imx355_gt24p64e_ultra_gl_i_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_aac_ov20b40_gt24p64e_front_gl_i_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_aac_ov02b10_p24c64e_macro_ii_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_aac_s5khp3_gt24p256h_wide_ii_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_ofilm_imx355_p24c64e_ultra_gl_ii_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_ofilm_ovx8000_gt24p128f_wide_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_ofilm_s5khp3_gt24p256h_wide_i_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_sunny_ov02b10_gt24p64e_macro_i_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_sunny_ov20b40_gt24p64e_front_gl_ii_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_sunny_s5kjn1_gt24p128e_tele_eeprom.so',
        'odm/lib64/camera/com.qti.eeprom.amethyst_sunny_s5kjn1_gt24p128e_tele_ii_eeprom.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_aac_imx355_ultra_cn.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_aac_imx355_ultra_gl_i.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_aac_ov02b10_macro_ii.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_aac_ov20b40_front_gl_i.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_aac_s5khp3_wide_ii.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_ofilm_imx355_ultra_gl_ii.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_ofilm_ovx8000_wide.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_ofilm_s5khp3_wide_i.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_sunny_ov02b10_macro_i.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_sunny_ov20b40_front_cn.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_sunny_ov20b40_front_gl_ii.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_sunny_s5kjn1_tele.so',
        'odm/lib64/camera/com.qti.sensor.amethyst_sunny_s5kjn1_tele_ii.so',
        'odm/lib64/camera/components/com.jigan.node.videobokeh.so',
        'odm/lib64/camera/components/com.mi.node.aiasd.so',
        'odm/lib64/camera/components/com.mi.node.rearvideo.so',
        'odm/lib64/camera/components/com.mi.node.videonight.so',
        'odm/lib64/camera/components/com.xiaomi.node.smooth_transition.so',
        'odm/lib64/camera/libchxlogicalcameratable.so',
        'vendor/lib64/camera/com.qti.eeprom.gt24p128c2csli_imx766.so',
        'vendor/lib64/camera/com.qti.eeprom.irs2381c_polar.so',
        'vendor/lib64/camera/com.qti.eeprom.truly_cmb433.so',
        'vendor/lib64/camera/components/com.mi.node.dlengine.so',
        'vendor/lib64/camera/components/com.mi.node.mawsaliency.so',
        'vendor/lib64/camera/components/com.mi.node.videobokeh.so',
        'vendor/lib64/camera/components/com.mi.node.videofilter.so',
        'vendor/lib64/camera/components/com.qti.hwcfg.bps.so',
        'vendor/lib64/camera/components/com.qti.hwcfg.ife.so',
        'vendor/lib64/camera/components/com.qti.hwcfg.ipe.so',
        'vendor/lib64/camera/components/com.qti.node.depth.so',
        'vendor/lib64/camera/components/com.qti.node.depthprovider.so',
        'vendor/lib64/camera/components/com.qti.node.dewarp.so',
        'vendor/lib64/camera/components/com.qti.node.eisv2.so',
        'vendor/lib64/camera/components/com.qti.node.eisv3.so',
        'vendor/lib64/camera/components/com.qti.node.evadepth.so',
        'vendor/lib64/camera/components/com.qti.node.gme.so',
        'vendor/lib64/camera/components/com.qti.node.gyrornn.so',
        'vendor/lib64/camera/components/com.qti.node.hdr10phist.so',
        'vendor/lib64/camera/components/com.qti.node.hdr10pgen.so',
        'vendor/lib64/camera/components/com.qti.node.itofpreprocess.so',
        'vendor/lib64/camera/components/com.qti.node.ml.so',
        'vendor/lib64/camera/components/com.qti.node.mlinference.so',
        'vendor/lib64/camera/components/com.qti.node.seg.so',
        'vendor/lib64/camera/components/com.qti.node.swec.so',
        'vendor/lib64/camera/components/com.qti.node.swregistration.so',
        'vendor/lib64/camera/components/com.qti.stats.cnndriver.so',
        'vendor/lib64/camera/components/libdepthmapwrapper_itof.so',
        'vendor/lib64/camera/components/libdepthmapwrapper_secure.so',
        'vendor/lib64/com.qualcomm.mcx.distortionmapper.so',
        'vendor/lib64/com.qualcomm.mcx.linearmapper.so',
        'vendor/lib64/com.qualcomm.mcx.nonlinearmapper.so',
        'vendor/lib64/com.qualcomm.mcx.policy.mfl.so',
        'vendor/lib64/com.qualcomm.qti.mcx.usecase.extension.so',
        'vendor/lib64/com.qti.camx.chiiqutils.so',
        'vendor/lib64/com.qti.chiusecaseselector.so',
        'vendor/lib64/com.qti.feature2.afbrckt.so',
        'vendor/lib64/com.qti.feature2.derivedoffline.so',
        'vendor/lib64/com.qti.feature2.demux.so',
        'vendor/lib64/com.qti.feature2.fusion.so',
        'vendor/lib64/com.qti.feature2.generic.so',
        'vendor/lib64/com.qti.feature2.gs.milos.so',
        'vendor/lib64/com.qti.feature2.hdr.so',
        'vendor/lib64/com.qti.feature2.mcreprocrt.so',
        'vendor/lib64/com.qti.feature2.memcpy.so',
        'vendor/lib64/com.qti.feature2.metadataserializer.so',
        'vendor/lib64/com.qti.feature2.mfsr.milos.so',
        'vendor/lib64/com.qti.feature2.mfsr.so',
        'vendor/lib64/com.qti.feature2.ml.so',
        'vendor/lib64/com.qti.feature2.mux.so',
        'vendor/lib64/com.qti.feature2.offlinestatsregeneration.so',
        'vendor/lib64/com.qti.feature2.qcfa.so',
        'vendor/lib64/com.qti.feature2.rawhdr.so',
        'vendor/lib64/com.qti.feature2.realtimeserializer.so',
        'vendor/lib64/com.qti.feature2.rt.so',
        'vendor/lib64/com.qti.feature2.rtmcx.so',
        'vendor/lib64/com.qti.feature2.serializer.so',
        'vendor/lib64/com.qti.feature2.statsregeneration.so',
        'vendor/lib64/com.qti.feature2.stub.so',
        'vendor/lib64/com.qti.feature2.swmf.so',
        'vendor/lib64/com.qti.qseeutils.so',
        'vendor/lib64/com.xiaomi.immunesystem.hook.camx.so',
        'vendor/lib64/com.xiaomi.immunesystem.hook.chi.so',
        'vendor/lib64/hw/camera.qcom.milos.so',
        'vendor/lib64/hw/camera.qcom.so',
        'vendor/lib64/hw/com.qti.chi.offline.so',
        'vendor/lib64/hw/com.qti.chi.override.so',
        'vendor/lib64/libcamerapostproc.so',
        'vendor/lib64/libcamxhwnodecontext.so',
        'vendor/lib64/libcamxifestriping.so',
        'vendor/lib64/libcamximageformatutils.so',
        'vendor/lib64/libcamxncsdatafactory.so',
        'vendor/lib64/libchifeature2.so',
        'vendor/lib64/libcom.xiaomi.mawutilsold.so',
        'vendor/lib64/libcom.xiaomi.offlinefeatureintf.so',
        'vendor/lib64/libcom.xiaomi.qimagebuffer.so',
        'vendor/lib64/libcommonchiutils.so',
        'vendor/lib64/libfastmessage.so',
        'vendor/lib64/libhme.so',
        'vendor/lib64/libipebpsstriping.so',
        'vendor/lib64/libipebpsstriping170.so',
        'vendor/lib64/libipebpsstriping480.so',
        'vendor/lib64/libisphwsetting.so',
        'vendor/lib64/libjpege.so',
        'vendor/lib64/libmmcamera_bestats.so',
        'vendor/lib64/libmmcamera_cac.so',
        'vendor/lib64/libmmcamera_lscv35.so',
        'vendor/lib64/libmmcamera_mfnr.so',
        'vendor/lib64/libmmcamera_pdpc.so',
        'vendor/lib64/libpostprocinfo.so',
        'vendor/lib64/vendor.qti.hardware.camera.aon-service-impl.so',
        'vendor/lib64/vendor.qti.hardware.camera.offlinecamera-service-impl.so',
        'vendor/lib64/vendor.qti.hardware.camera.postproc@1.0-service-impl.so',
    ): blob_fixup()
        .replace_needed(
            'android.hardware.graphics.allocator-V1-ndk.so',
            'android.hardware.graphics.allocator-V2-ndk.so',
        ),

    (
        'odm/lib64/camera/plugins/com.xiaomi.plugin.gainmap.so',
        'odm/lib64/camera/plugins/com.xiaomi.plugin.jpegrAggr.so',
    ): blob_fixup()
        .replace_needed(
            'libultrahdr.so',
            'libultrahdr_prebuilt.so',
        ),

    'vendor/etc/clstc_config_library.xml': blob_fixup()
        .regex_replace(r'<library>\s*<name>libdolbyclstc[\s\S]*?</library>', ''),

    'vendor/etc/sensors/hals.conf': blob_fixup()
        .regex_replace('.*vl53l8.*\n?', ''),

    'vendor/etc/vintf/manifest/c2_manifest_vendor.xml': blob_fixup()
        .regex_replace('.+default1.+\n', '')
        .regex_replace('.+dolby.+\n', ''),

    'vendor/lib64/libcameraopt.so': blob_fixup()
        .add_needed('libprocessgroup_shim.so'),

    'vendor/lib64/libcamera2ndk_vendor.so': blob_fixup()
        .replace_needed(
            'android.frameworks.cameraservice.device-V1-ndk.so',
            'android.frameworks.cameraservice.device-V3-ndk.so',
        )
        .replace_needed(
            'android.frameworks.cameraservice.service-V1-ndk.so',
            'android.frameworks.cameraservice.service-V3-ndk.so',
        ),

    'vendor/lib64/libmicamera_hal_core.so': blob_fixup()
        .add_needed('libui_shim.so')
        .replace_needed(
            'libtinyxml2.so',
            'libtinyxml2-v34.so'
    ),

    'vendor/lib64/libqcodec2_core.so': blob_fixup()
        .add_needed('libcodec2_shim.so')
        .replace_needed(
            'android.hardware.graphics.common-V5-ndk.so',
            'android.hardware.graphics.common-V6-ndk.so',
        ),

    'vendor/lib64/libultrahdr_prebuilt.so': blob_fixup()
        .replace_needed(
            'libjpegdecoder.so',
            'libjpegdecoder_prebuilt.so',
        )
        .replace_needed(
            'libjpegencoder.so',
            'libjpegencoder_prebuilt.so',
        ),

    'vendor/lib64/vendor.libdpmframework.so': blob_fixup()
        .add_needed('libbinder_shim.so')
        .add_needed('libhidlbase_shim.so'),

    (
        'vendor/etc/media_codecs_volcano_v0.xml',
    ): blob_fixup()
        .regex_replace('.*media_codecs_(google_audio|google_c2|google_telephony|google_video|vendor_audio).*\n', ''),

    (
        'odm/lib64/libaudioroute_ext.so',
        'vendor/lib64/libagm.so',
        'vendor/lib64/libar-pal.so',
        'vendor/lib64/libmcs.so',
        'vendor/lib64/libmikaraoke.so',
        'vendor/lib64/libtiantongpal.so',
    ): blob_fixup()
        .replace_needed(
            'libaudioroute.so',
            'libaudioroute-v34.so'
    ),

    (
        'vendor/bin/pnscr',
        'vendor/bin/pnscr-sst'
    ): blob_fixup()
        .add_needed('libbase_shim.so'),

    (
        'vendor/bin/qcc-vendor',
        'vendor/bin/qms',
        'vendor/bin/xtra-daemon',
        'vendor/lib64/libcne.so',
        'vendor/lib64/libqcc_sdk.so',
        'vendor/lib64/libqms_client.so',
        'vendor/lib64/libqms_xiaomi.so',
    ): blob_fixup()
        .add_needed('libbinder_shim.so'),

    'vendor/etc/init/vendor.xiaomi.hardware.vibratorfeature.service.rc': blob_fixup()
        .regex_replace('odm/bin', 'vendor/bin'),

    'vendor/bin/hw/vendor.xiaomi.hardware.vibratorfeature.service': blob_fixup()
        .replace_needed(
            'android.hardware.vibrator-V1-ndk_platform.so',
            'android.hardware.vibrator-V1-ndk_prebuilt.so'
    ),

    (
        'vendor/lib64/libVoiceSdk.so',
        'vendor/lib64/libcapiv2uvvendor.so',
        'vendor/lib64/liblistensoundmodel2vendor.so',
    ): blob_fixup()
        .replace_needed(
            'libtensorflowlite_c.so',
            'libtensorflowlite_c_vendor.so',
    ),

    'system_ext/lib64/vendor.qti.hardware.qccsyshal@1.2-halimpl.so': blob_fixup()
        .replace_needed(
            'libprotobuf-cpp-full.so',
            'libprotobuf-cpp-full-21.7.so'
    ),

    (
        'odm/bin/hw/vendor.xiaomi.sensor.citsensorservice.aidl',
        'odm/lib64/camera/plugins/com.xiaomi.plugin.anchor.so',
        'odm/lib64/hw/displayfeature.default.so',
        'vendor/bin/hw/vendor.qti.camera.provider-service_64',
        'vendor/bin/hw/vendor.qti.hardware.display.composer-service',
        'vendor/bin/poweropt-service',
        'vendor/lib64/libaodoptfeature.so',
        'vendor/lib64/libapengine.so',
        'vendor/lib64/libaudiocloudctrl.so',
        'vendor/lib64/libdpps.so',
        'vendor/lib64/liblearningmodule.so',
        'vendor/lib64/libmicamera_aidl_provider.so',
        'vendor/lib64/libpowercore.so',
        'vendor/lib64/libpsmoptfeature.so',
        'vendor/lib64/libsnapdragoncolor-manager.so',
        'vendor/lib64/libstandbyfeature.so',
        'vendor/lib64/libvideooptfeature.so',
    ): blob_fixup()
        .replace_needed(
            'libtinyxml2.so',
            'libtinyxml2-v34.so'
    ),

    (
        'vendor/lib64/com.qti.feature2.anchorsync.so',
        'vendor/lib64/libsimulation.so',
    ): blob_fixup()
        .replace_needed(
            'android.hardware.graphics.allocator-V1-ndk.so',
            'android.hardware.graphics.allocator-V2-ndk.so',
    )
        .replace_needed(
            'libtinyxml2.so',
            'libtinyxml2-v34.so'
    ),

}  # fmt: skip


module = ExtractUtilsModule(
    'amethyst',
    'xiaomi',
    blob_fixups=blob_fixups,
    lib_fixups=lib_fixups,
    namespace_imports=namespace_imports,
)

if __name__ == '__main__':
    utils = ExtractUtils.device(module)
    utils.run()
