#!/usr/bin/env bash
set -euo pipefail

# Find the supplied CSTDe bundle beside the repository or this script.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../../.." && pwd)"
SETTING_DIR=""

if [ -d "$PROJECT_DIR/setting/cstd" ]; then
    SETTING_DIR="$PROJECT_DIR/setting"
elif [ -d "$PROJECT_DIR/cstd" ]; then
    SETTING_DIR="$PROJECT_DIR"
elif [ -d "$SCRIPT_DIR/setting/cstd" ]; then
    SETTING_DIR="$SCRIPT_DIR/setting"
elif [ -d "$SCRIPT_DIR/cstd" ]; then
    SETTING_DIR="$SCRIPT_DIR"
else
    echo 'CSTDe image bundle not found (expected setting/cstd or cstd).' >&2
    exit 1
fi

if [ -n "${ANDROID_HOME:-}" ]; then
    SDK_DIR="$ANDROID_HOME"
elif [ -n "${ANDROID_SDK_ROOT:-}" ]; then
    SDK_DIR="$ANDROID_SDK_ROOT"
else
    SDK_DIR="$HOME/Library/Android/sdk"
fi

TARGET_SYSIMG_DIR="$SDK_DIR/system-images/cstd"
TARGET_AVD_BASE="$HOME/.android/avd"
TARGET_AVD_DIR="$TARGET_AVD_BASE/CSTDe_API_34.avd"
TARGET_INI="$TARGET_AVD_BASE/CSTDe_API_34.ini"

mkdir -p "$TARGET_SYSIMG_DIR"
mkdir -p "$TARGET_AVD_BASE"

echo "Installing CSTDe system image from $SETTING_DIR."
if command -v rsync >/dev/null 2>&1; then
    rsync -av --exclude="*.lock" "$SETTING_DIR/cstd/" "$TARGET_SYSIMG_DIR/" >/dev/null
else
    cp -R "$SETTING_DIR/cstd/"* "$TARGET_SYSIMG_DIR/"
fi
echo 'Installing CSTDe_API_34 AVD.'
if [ -d "$SETTING_DIR/CSTDe_API_34.avd" ]; then
    if command -v rsync >/dev/null 2>&1; then
        rsync -av --exclude="*.lock" "$SETTING_DIR/CSTDe_API_34.avd/" "$TARGET_AVD_DIR/" >/dev/null
    else
        cp -R "$SETTING_DIR/CSTDe_API_34.avd" "$TARGET_AVD_BASE/"
    fi
fi

if [ -f "$SETTING_DIR/CSTDe_API_34.ini" ]; then
    cp -f "$SETTING_DIR/CSTDe_API_34.ini" "$TARGET_INI"
fi

# Point the imported AVD descriptor at its new directory.
if [ -f "$TARGET_INI" ]; then
    if sed --version 2>&1 | grep -q "GNU"; then
        sed -i "s|^path=.*|path=$TARGET_AVD_DIR|g" "$TARGET_INI"
    else
        sed -i '' "s|^path=.*|path=$TARGET_AVD_DIR|g" "$TARGET_INI"
    fi
fi

# Point the imported AVD at the installed system image.
TARGET_CONFIG="$TARGET_AVD_DIR/config.ini"
if [ -f "$TARGET_CONFIG" ]; then
    if sed --version 2>&1 | grep -q "GNU"; then
        sed -i 's|image.sysdir.1=.*|image.sysdir.1=system-images/cstd/x86_64/|g' "$TARGET_CONFIG"
    else
        sed -i '' 's|image.sysdir.1=.*|image.sysdir.1=system-images/cstd/x86_64/|g' "$TARGET_CONFIG"
    fi
fi

echo 'CSTDe image import finished. Start CSTDe_API_34 from Device Manager.'
