$ErrorActionPreference = "Stop"

$clipDir = Join-Path $PSScriptRoot "clips"
$finalDir = Join-Path $PSScriptRoot "final"
New-Item -ItemType Directory -Force -Path $finalDir | Out-Null

$ffmpeg = Get-Command ffmpeg -ErrorAction SilentlyContinue
if ($null -eq $ffmpeg) {
    throw "ffmpeg was not found on PATH. Install ffmpeg, or import demo/clips into a video editor and concatenate manually."
}

$clipOrder = @(
    "01_opening_people_phone.mp4",
    "02_problem_llm_too_much.mp4",
    "03_record_note_flow.mp4",
    "04_gemma_calculation.mp4",
    "05_borromean_words.mp4",
    "06_privacy_audio_export.mp4",
    "07_closing.mp4"
)

$existingClips = @()
foreach ($clip in $clipOrder) {
    $path = Join-Path $clipDir $clip
    if (Test-Path $path) {
        $existingClips += $path
    } else {
        Write-Warning "Missing clip: $clip"
    }
}

if ($existingClips.Count -lt 1) {
    throw "No clips found in $clipDir."
}

$listPath = Join-Path $finalDir "clips.txt"
$existingClips | ForEach-Object {
    "file '$($_.Replace('\', '/'))'"
} | Set-Content -Encoding UTF8 $listPath

$outputPath = Join-Path $finalDir "my_words_my_way_kaggle_demo.mp4"
ffmpeg -y -f concat -safe 0 -i $listPath -c copy $outputPath

Write-Host "Saved: $outputPath"

