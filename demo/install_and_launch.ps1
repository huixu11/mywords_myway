$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $repoRoot "android"
$apkPath = Join-Path $androidRoot "app\build\outputs\apk\debug\app-debug.apk"
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"

if (-not (Test-Path $adb)) {
    throw "adb.exe was not found at $adb. Install Android SDK Platform Tools or update this script with your adb path."
}

Push-Location $androidRoot
try {
    .\gradlew.bat :app:assembleDebug
} finally {
    Pop-Location
}

if (-not (Test-Path $apkPath)) {
    throw "APK was not created at $apkPath"
}

& $adb install -r $apkPath
& $adb shell monkey -p com.mywordsmyway 1

Write-Host "Installed and launched My Words, My Way."

