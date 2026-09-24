#!/usr/bin/env bash
set -euo pipefail

adb_bin=${ADB:-adb}
window_dump=$("$adb_bin" shell dumpsys window | tr -d '\r')

check_bar() {
    local name=$1
    local frame=$2
    if ! grep -Fq "type=$name frame=$frame" <<< "$window_dump"; then
        echo "Expected $name frame=$frame; observed:" >&2
        grep -E 'type=(statusBars|navigationBars) frame=' <<< "$window_dump" | sort -u >&2 || true
        exit 1
    fi
}

check_bar statusBars '[0,0][2560,96]'
check_bar navigationBars '[0,1280][2560,1440]'
echo 'Verified AAOS system bars: top 96px, bottom 160px.'
