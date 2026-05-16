param(
    [Parameter(Mandatory = $true)]
    [string]$ClipName,

    [int]$Seconds = 30
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$clipDir = Join-Path $PSScriptRoot "clips"
New-Item -ItemType Directory -Force -Path $clipDir | Out-Null

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb.exe was not found at $adb. Install Android SDK Platform Tools or update this script with your adb path."
}

$devices = & $adb devices
if (($devices | Select-String "`tdevice").Count -lt 1) {
    throw "No connected Android device or emulator found. Start an emulator in Android Studio or connect a phone with USB debugging enabled."
}

$remotePath = "/sdcard/$ClipName.mp4"
$localPath = Join-Path $clipDir "$ClipName.mp4"

Write-Host "Launching My Words, My Way..."
& $adb shell monkey -p com.mywordsmyway 1 | Out-Null
Start-Sleep -Seconds 2

Write-Host "Recording $ClipName for $Seconds seconds..."
Write-Host "Use the phone/emulator now. Recording starts immediately."

& $adb shell screenrecord --time-limit $Seconds $remotePath

Write-Host "Pulling clip to $localPath..."
& $adb pull $remotePath $localPath | Out-Null
& $adb shell rm $remotePath | Out-Null

Write-Host "Saved: $localPath"

