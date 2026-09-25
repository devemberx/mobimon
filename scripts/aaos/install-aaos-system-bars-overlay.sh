#!/usr/bin/env bash
set -euo pipefail

adb_bin=${ADB:-adb}
expected_avd=${AAOS_OVERLAY_AVD_NAME:?Set AAOS_OVERLAY_AVD_NAME to the disposable AVD name.}
script_dir=$(cd "$(dirname "$0")" && pwd)

case "$expected_avd" in
    mobimon_system_bars_*) ;;
    cstd_api_34)
        if [[ ${GITHUB_ACTIONS:-} != true ]]; then
            echo 'cstd_api_34 is reserved for the fresh GitHub Actions AVD.' >&2
            exit 1
        fi
        ;;
    *)
        echo 'Use a disposable AVD named mobimon_system_bars_*.' >&2
        exit 1
        ;;
esac

actual_avd=$("$adb_bin" emu avd name | tr -d '\r' | sed -n '1p')
if [[ "$actual_avd" != "$expected_avd" ]]; then
    echo "Refusing to modify AVD '$actual_avd'; expected '$expected_avd'." >&2
    exit 1
fi

if [[ $("$adb_bin" shell getprop ro.kernel.qemu | tr -d '\r') != 1 ]] ||
    [[ $("$adb_bin" shell getprop ro.build.version.sdk | tr -d '\r') != 34 ]] ||
    [[ $("$adb_bin" shell pm has-feature android.hardware.type.automotive | tr -d '\r') != true ]] ||
    ! "$adb_bin" shell wm size | tr -d '\r' | grep -Fxq 'Physical size: 2560x1440' ||
    ! "$adb_bin" shell wm density | tr -d '\r' | grep -Fxq 'Physical density: 160'; then
    echo 'Expected an AAOS API 34 emulator at 2560x1440 / 160 dpi.' >&2
    exit 1
fi

wait_for_boot() {
    "$adb_bin" wait-for-device
    for ((attempt = 0; attempt < 120; attempt++)); do
        if [[ $("$adb_bin" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r') == 1 ]]; then
            return
        fi
        sleep 2
    done
    echo 'AAOS emulator did not finish booting within four minutes.' >&2
    exit 1
}

root_adbd() {
    local attempt
    for ((attempt = 1; attempt <= 15; attempt++)); do
        if "$adb_bin" root; then
            "$adb_bin" wait-for-device
            return
        fi
        if ((attempt < 15)); then
            sleep 2
        fi
    done
    echo 'AAOS emulator did not accept adb root after 15 attempts.' >&2
    exit 1
}

apk=$(bash "$script_dir/build-aaos-system-bars-overlay.sh")
root_adbd
"$adb_bin" disable-verity
"$adb_bin" reboot
wait_for_boot

root_adbd
"$adb_bin" remount
"$adb_bin" push "$apk" /product/overlay/MobiMonSystemBars.apk
"$adb_bin" shell chmod 644 /product/overlay/MobiMonSystemBars.apk
"$adb_bin" shell restorecon /product/overlay/MobiMonSystemBars.apk
"$adb_bin" reboot
wait_for_boot

bash "$script_dir/check-aaos-system-bars.sh"
