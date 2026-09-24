#!/usr/bin/env bash
set -euo pipefail

sdk_dir=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}
if [[ -z "$sdk_dir" ]]; then
    echo 'Set ANDROID_HOME or ANDROID_SDK_ROOT to the Android SDK path.' >&2
    exit 1
fi

project_dir=$(cd "$(dirname "$0")/.." && pwd)
source_dir="$project_dir/.github/avd/system-bars-overlay"
output_dir="$project_dir/build/aaos-system-bars-overlay"
build_tools="$sdk_dir/build-tools/34.0.0"
mkdir -p "$output_dir"

"$build_tools/aapt2" compile --dir "$source_dir/res" -o "$output_dir/resources.zip"
"$build_tools/aapt2" link \
    -o "$output_dir/system-bars-unsigned.apk" \
    -I "$sdk_dir/platforms/android-34/android.jar" \
    --manifest "$source_dir/AndroidManifest.xml" \
    --min-sdk-version 34 \
    --target-sdk-version 34 \
    --no-resource-deduping \
    --no-resource-removal \
    "$output_dir/resources.zip"

keystore="$output_dir/debug.keystore"
if [[ ! -f "$keystore" ]]; then
    keytool -genkeypair \
        -keystore "$keystore" \
        -storepass android \
        -keypass android \
        -alias mobimon-aaos-debug \
        -dname 'CN=MobiMon AAOS Debug' \
        -keyalg RSA \
        -keysize 2048 \
        -validity 3650 \
        -noprompt
fi

apk="$output_dir/system-bars.apk"
"$build_tools/apksigner" sign \
    --ks "$keystore" \
    --ks-key-alias mobimon-aaos-debug \
    --ks-pass pass:android \
    --key-pass pass:android \
    --out "$apk" \
    "$output_dir/system-bars-unsigned.apk"
"$build_tools/apksigner" verify --min-sdk-version 34 "$apk"
echo "$apk"
