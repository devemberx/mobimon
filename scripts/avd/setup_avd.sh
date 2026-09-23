#!/bin/bash

# ========================================================
#   CSTDe AVD 및 CSTD System Image 설치 스크립트 (macOS / Linux)
# ========================================================

set -e

# 색상 정의
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}========================================================${NC}"
echo -e "${CYAN}  CSTDe AVD 및 CSTD System Image 설치 스크립트 (macOS)${NC}"
echo -e "${CYAN}========================================================${NC}"
echo ""

# 1. 소스 디렉터리 확인
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
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
    echo -e "${RED}[오류] setting 폴더 또는 cstd 폴더를 찾을 수 없습니다.${NC}"
    echo "스크립트 위치: $SCRIPT_DIR"
    exit 1
fi

echo -e "${GREEN}[1/5] 소스 폴더 확인 완료: $SETTING_DIR${NC}"
echo ""

# 2. 대상 디렉터리 확인 (macOS 기본 경로)
if [ -n "$ANDROID_HOME" ]; then
    SDK_DIR="$ANDROID_HOME"
elif [ -n "$ANDROID_SDK_ROOT" ]; then
    SDK_DIR="$ANDROID_SDK_ROOT"
else
    SDK_DIR="$HOME/Library/Android/sdk"
fi

TARGET_SYSIMG_DIR="$SDK_DIR/system-images/cstd"
TARGET_AVD_BASE="$HOME/.android/avd"
TARGET_AVD_DIR="$TARGET_AVD_BASE/CSTDe_API_34.avd"
TARGET_INI="$TARGET_AVD_BASE/CSTDe_API_34.ini"

echo -e "${GREEN}[2/5] 대상 경로 확인:${NC}"
echo "  - Android SDK 위치:       $SDK_DIR"
echo "  - System Image 대상 폴더: $TARGET_SYSIMG_DIR"
echo "  - AVD 대상 폴더:           $TARGET_AVD_BASE"
echo ""

# 3. 실행 중인 에뮬레이터 확인 및 종료
if pgrep -f "qemu-system" >/dev/null 2>&1 || pgrep -f "emulator" >/dev/null 2>&1; then
    echo -e "${YELLOW}[주의] 실행 중인 에뮬레이터가 감지되어 프로세스를 종료합니다...${NC}"
    pkill -f "qemu-system" 2>/dev/null || true
    pkill -f "emulator" 2>/dev/null || true
    sleep 2
fi

mkdir -p "$TARGET_SYSIMG_DIR"
mkdir -p "$TARGET_AVD_BASE"

# 4. cstd 시스템 이미지 복사
echo -e "${YELLOW}[3/5] cstd 시스템 이미지 복사 중... (용량이 커서 수 분 소요될 수 있습니다)${NC}"
if command -v rsync >/dev/null 2>&1; then
    rsync -av --exclude="*.lock" "$SETTING_DIR/cstd/" "$TARGET_SYSIMG_DIR/" >/dev/null
else
    cp -R "$SETTING_DIR/cstd/"* "$TARGET_SYSIMG_DIR/"
fi
echo -e "${GREEN}[성공] 시스템 이미지 복사 완료!${NC}"
echo ""

# 5. CSTDe_API_34 AVD 폴더 및 ini 복사
echo -e "${YELLOW}[4/5] CSTDe_API_34 AVD 폴더 및 설정 파일 복사 중...${NC}"
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

# 6. macOS에 맞게 경로 보정 (ini 파일의 path 및 config.ini의 슬래시 경로)
echo -e "${YELLOW}[5/5] macOS 경로 호환성 보정 중...${NC}"

# ini 파일의 path= 를 macOS 사용자 경로($TARGET_AVD_DIR)로 변경
if [ -f "$TARGET_INI" ]; then
    # sed -i 호환성 (macOS BSD sed vs GNU sed)
    if sed --version 2>&1 | grep -q "GNU"; then
        sed -i "s|^path=.*|path=$TARGET_AVD_DIR|g" "$TARGET_INI"
    else
        sed -i '' "s|^path=.*|path=$TARGET_AVD_DIR|g" "$TARGET_INI"
    fi
fi

# config.ini 내부의 윈도우식 역슬래시(\) 경로를 유닉스식 슬래시(/)로 변경
TARGET_CONFIG="$TARGET_AVD_DIR/config.ini"
if [ -f "$TARGET_CONFIG" ]; then
    if sed --version 2>&1 | grep -q "GNU"; then
        sed -i 's|image.sysdir.1=.*|image.sysdir.1=system-images/cstd/x86_64/|g' "$TARGET_CONFIG"
    else
        sed -i '' 's|image.sysdir.1=.*|image.sysdir.1=system-images/cstd/x86_64/|g' "$TARGET_CONFIG"
    fi
fi

echo -e "${GREEN}[성공] AVD 설정 파일 복사 및 경로 보정 완료!${NC}"
echo ""

# Apple Silicon (M1/M2/M3/M4) 확인 및 안내
ARCH=$(uname -m)
if [ "$ARCH" = "arm64" ]; then
    echo -e "${YELLOW}--------------------------------------------------------${NC}"
    echo -e "${YELLOW}[안내: Apple Silicon (M1/M2/M3/M4) Mac 사용자]${NC}"
    echo -e "현재 설치된 CSTDe 이미지는 ${CYAN}x86_64${NC} 아키텍처입니다."
    echo "Apple Silicon에서는 Rosetta 2 에뮬레이션을 통해 동작하므로,"
    echo "네이티브 ARM 기기보다 부팅 및 동작 속도가 다소 느릴 수 있습니다."
    echo -e "${YELLOW}--------------------------------------------------------${NC}"
    echo ""
fi

echo -e "${CYAN}========================================================${NC}"
echo -e "${CYAN}  [완료] CSTDe AVD 및 System Image 설치가 모두 완료되었습니다!${NC}"
echo -e "${CYAN}========================================================${NC}"
echo "이제 Android Studio의 Device Manager에서 CSTDe_API_34 에뮬레이터를 실행하실 수 있습니다."
echo ""
