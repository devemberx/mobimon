$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $baseDir) { $baseDir = (Get-Location).Path }
$projectDir = (Resolve-Path (Join-Path $baseDir "../..")).Path

# Find the supplied CSTDe bundle beside the repository or this script.
$settingDir = $null
if (Test-Path (Join-Path $projectDir "setting\cstd")) {
    $settingDir = Join-Path $projectDir "setting"
} elseif (Test-Path (Join-Path $projectDir "cstd")) {
    $settingDir = $projectDir
} elseif (Test-Path (Join-Path $baseDir "setting\cstd")) {
    $settingDir = Join-Path $baseDir "setting"
} elseif (Test-Path (Join-Path $baseDir "cstd")) {
    $settingDir = $baseDir
} else {
    Write-Error "CSTDe image bundle not found (expected setting/cstd or cstd)."
    exit 1
}

$sdkDir = $env:ANDROID_HOME
if (-not $sdkDir) { $sdkDir = $env:ANDROID_SDK_ROOT }
if (-not $sdkDir) { $sdkDir = Join-Path $env:LOCALAPPDATA "Android\Sdk" }

$targetSysImgDir = Join-Path $sdkDir "system-images\cstd"
$targetAvdBase = Join-Path $env:USERPROFILE ".android\avd"
$targetAvdDir = Join-Path $targetAvdBase "CSTDe_API_34.avd"

if (-not (Test-Path $targetSysImgDir)) {
    New-Item -ItemType Directory -Path $targetSysImgDir -Force | Out-Null
}
if (-not (Test-Path $targetAvdBase)) {
    New-Item -ItemType Directory -Path $targetAvdBase -Force | Out-Null
}

Write-Host "Installing CSTDe system image from $settingDir."
$cstdSrc = Join-Path $settingDir "cstd"
& robocopy $cstdSrc $targetSysImgDir /E /R:2 /W:2 /MT:8 /NP /XF *.lock | Out-Null
if ($LASTEXITCODE -ge 8) {
    Write-Error "Failed to copy CSTDe system image from $cstdSrc."
    exit 1
}

Write-Host "Installing CSTDe_API_34 AVD."
$avdSrc = Join-Path $settingDir "CSTDe_API_34.avd"
if (Test-Path $avdSrc) {
    & robocopy $avdSrc $targetAvdDir /E /R:2 /W:2 /MT:8 /NP /XF *.lock | Out-Null
    if ($LASTEXITCODE -ge 8) {
        Write-Error "Failed to copy CSTDe_API_34 AVD from $avdSrc."
        exit 1
    }
}

$iniSrc = Join-Path $settingDir "CSTDe_API_34.ini"
$iniDest = Join-Path $targetAvdBase "CSTDe_API_34.ini"
if (Test-Path $iniSrc) {
    Copy-Item -Path $iniSrc -Destination $iniDest -Force
}

# Point the imported AVD descriptor at its new directory.
if (Test-Path $iniDest) {
    $iniContent = Get-Content -Raw $iniDest
    $newPath = "path=" + $targetAvdDir
    ($iniContent -replace '(?m)^path=.*', $newPath) | Set-Content -Path $iniDest -NoNewline
}

Write-Host "CSTDe image import finished. Start CSTDe_API_34 from Device Manager."
