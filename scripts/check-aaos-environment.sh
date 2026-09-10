#!/usr/bin/env bash
set -euo pipefail

report_dir=build/reports/aaos
mkdir -p "$report_dir"
adb shell getprop | tr -d '\r' > "$report_dir/properties.txt"
adb shell pm list features | tr -d '\r' > "$report_dir/features.txt"
adb shell wm size | tr -d '\r' > "$report_dir/display-size.txt"
adb shell wm density | tr -d '\r' > "$report_dir/display-density.txt"

require_line() {
    if ! grep -Fxq "$1" "$2"; then
        echo "Expected AAOS environment value missing: $1" >&2
        cat "$2" >&2
        exit 1
    fi
}

require_line '[ro.build.version.sdk]: [34]' "$report_dir/properties.txt"
require_line '[ro.product.cpu.abi]: [x86_64]' "$report_dir/properties.txt"
require_line 'feature:android.hardware.type.automotive' "$report_dir/features.txt"
require_line 'Physical size: 2560x1440' "$report_dir/display-size.txt"
require_line 'Physical density: 160' "$report_dir/display-density.txt"

echo 'Verified AAOS API 34 x86_64 at 2560x1440 / 160 dpi.'
