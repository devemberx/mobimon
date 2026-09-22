[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  CSTDe AVD 및 CSTD System Image 설치 스크립트" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $baseDir) { $baseDir = (Get-Location).Path }

# 1. 소스 디렉터리 확인
$settingDir = $null
if (Test-Path (Join-Path $baseDir "setting\cstd")) {
    $settingDir = Join-Path $baseDir "setting"
} elseif (Test-Path (Join-Path $baseDir "cstd")) {
    $settingDir = $baseDir
} else {
    Write-Host "[오류] setting 폴더 또는 cstd 폴더를 찾을 수 없습니다." -ForegroundColor Red
    Write-Host "스크립트 위치: $baseDir"
    exit 1
}

Write-Host "[1/4] 소스 폴더 확인 완료: $settingDir" -ForegroundColor Green
Write-Host ""

# 2. 대상 디렉터리 확인
$sdkDir = $env:ANDROID_HOME
if (-not $sdkDir) { $sdkDir = $env:ANDROID_SDK_ROOT }
if (-not $sdkDir) { $sdkDir = Join-Path $env:LOCALAPPDATA "Android\Sdk" }

$targetSysImgDir = Join-Path $sdkDir "system-images\cstd"
$targetAvdBase = Join-Path $env:USERPROFILE ".android\avd"
$targetAvdDir = Join-Path $targetAvdBase "CSTDe_API_34.avd"

Write-Host "[2/4] 대상 경로 확인:" -ForegroundColor Green
Write-Host "  - Android SDK 위치:      $sdkDir"
Write-Host "  - System Image 대상 폴더: $targetSysImgDir"
Write-Host "  - AVD 대상 폴더:          $targetAvdBase"
Write-Host ""

# 3. 실행 중인 에뮬레이터 확인 및 종료
$runningQemu = Get-Process *qemu*, *emulator* -ErrorAction SilentlyContinue
if ($runningQemu) {
    Write-Host "[주의] 실행 중인 에뮬레이터가 감지되어 파일 잠금 방지를 위해 종료합니다..." -ForegroundColor Yellow
    Stop-Process -Name "qemu-system-x86_64", "emulator" -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

if (-not (Test-Path $targetSysImgDir)) {
    New-Item -ItemType Directory -Path $targetSysImgDir -Force | Out-Null
}
if (-not (Test-Path $targetAvdBase)) {
    New-Item -ItemType Directory -Path $targetAvdBase -Force | Out-Null
}

# 4. cstd 시스템 이미지 복사
Write-Host "[3/4] cstd 시스템 이미지 복사 중... (용량이 커서 수 분 소요될 수 있습니다)" -ForegroundColor Yellow
$cstdSrc = Join-Path $settingDir "cstd"
& robocopy $cstdSrc $targetSysImgDir /E /R:2 /W:2 /MT:8 /NP /XF *.lock | Out-Null
if ($LASTEXITCODE -ge 8) {
    Write-Host "[오류] 시스템 이미지 복사 중 에러가 발생했습니다." -ForegroundColor Red
    exit 1
}
Write-Host "[성공] 시스템 이미지 복사 완료!" -ForegroundColor Green
Write-Host ""

# 5. CSTDe_API_34 AVD 폴더 및 ini 복사
Write-Host "[4/4] CSTDe_API_34 AVD 폴더 및 ini 설정 복사 중..." -ForegroundColor Yellow
$avdSrc = Join-Path $settingDir "CSTDe_API_34.avd"
if (Test-Path $avdSrc) {
    & robocopy $avdSrc $targetAvdDir /E /R:2 /W:2 /MT:8 /NP /XF *.lock | Out-Null
    if ($LASTEXITCODE -ge 8) {
        Write-Host "[오류] AVD 폴더 복사 중 에러가 발생했습니다." -ForegroundColor Red
        exit 1
    }
}

$iniSrc = Join-Path $settingDir "CSTDe_API_34.ini"
$iniDest = Join-Path $targetAvdBase "CSTDe_API_34.ini"
if (Test-Path $iniSrc) {
    Copy-Item -Path $iniSrc -Destination $iniDest -Force
}

# ini 파일의 path= 값을 현재 사용자 PC의 실제 경로로 보정
if (Test-Path $iniDest) {
    $iniContent = Get-Content -Raw $iniDest
    $newPath = "path=" + $targetAvdDir
    ($iniContent -replace '(?m)^path=.*', $newPath) | Set-Content -Path $iniDest -NoNewline
}

Write-Host "[성공] AVD 폴더 및 ini 설정 복사 완료!" -ForegroundColor Green
Write-Host ""

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  [완료] CSTDe AVD 및 System Image 설치가 모두 완료되었습니다!" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "이제 Android Studio의 Device Manager에서 CSTDe_API_34 에뮬레이터를 바로 실행하실 수 있습니다."
Write-Host ""
