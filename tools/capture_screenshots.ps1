#!/usr/bin/env pwsh
# Script to install APK, launch app, and capture screenshots on running emulator

param(
    [string]$AvdName = "Tablet7_API35",
    [string]$OutDir = "tablet7"
)

$ErrorActionPreference = "Continue"
$adb = "C:\Users\morgane\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$projectDir = "c:\Users\morgane\Desktop\prev_col"
$apk = "$projectDir\app\build\outputs\apk\debug\app-debug.apk"
$screenshotDir = "$projectDir\screenshots\$OutDir"
$pkg = "com.regardsaumonde.app"

# Create output directory
New-Item -ItemType Directory -Force -Path $screenshotDir | Out-Null
Write-Host "=== Screenshot Capture for $AvdName ==="
Write-Host "Output: $screenshotDir"

# Check device
Write-Host "`n--- Checking ADB devices ---"
& $adb devices -l

# Install APK
Write-Host "`n--- Installing APK ---"
& $adb install -r $apk
Start-Sleep -Seconds 3

# Check package installed
Write-Host "`n--- Checking package ---"
$pkgList = & $adb shell pm list packages 2>&1
$found = $pkgList | Where-Object { $_ -match "regardsaumonde" }
if ($found) {
    Write-Host "Package found: $found"
} else {
    Write-Host "WARNING: Package not found in list! Trying install again..."
    & $adb install -t $apk
    Start-Sleep -Seconds 3
}

# Dismiss any system dialogs
Write-Host "`n--- Dismissing system dialogs ---"
& $adb shell input keyevent KEYCODE_HOME
Start-Sleep -Seconds 2

# Launch app via intent
Write-Host "`n--- Launching app ---"
& $adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n "$pkg/.PrivacyActivity" 2>&1
Start-Sleep -Seconds 5

# Screenshot 1: Privacy/Consent screen
Write-Host "`n--- Capturing Privacy screen ---"
& $adb shell screencap -p /sdcard/screen1.png
& $adb pull /sdcard/screen1.png "$screenshotDir\01_privacy_screen.png"
Write-Host "Saved: 01_privacy_screen.png"

# Accept privacy - click "Accepter et continuer" button
# Try tapping the center-bottom area where the accept button typically is
Write-Host "`n--- Accepting privacy (tap accept button) ---"
$screenSize = & $adb shell wm size 2>&1
Write-Host "Screen size: $screenSize"
# Extract screen dimensions
if ($screenSize -match "(\d+)x(\d+)") {
    $width = [int]$Matches[1]
    $height = [int]$Matches[2]
    # Tap center of screen, bottom third (where accept button should be)
    $tapX = [int]($width / 2)
    $tapY = [int]($height * 0.75)
    Write-Host "Tapping at ($tapX, $tapY)"
    & $adb shell input tap $tapX $tapY
}
Start-Sleep -Seconds 5

# Screenshot 2: Main screen (after accepting privacy)
Write-Host "`n--- Capturing Main screen ---"
& $adb shell screencap -p /sdcard/screen2.png
& $adb pull /sdcard/screen2.png "$screenshotDir\02_main_screen.png"
Write-Host "Saved: 02_main_screen.png"

# Try to navigate to Stats screen
Write-Host "`n--- Navigating to Stats ---"
& $adb shell am start -n "$pkg/.StatsActivity" 2>&1
Start-Sleep -Seconds 3

# Screenshot 3: Stats screen
Write-Host "`n--- Capturing Stats screen ---"
& $adb shell screencap -p /sdcard/screen3.png
& $adb pull /sdcard/screen3.png "$screenshotDir\03_stats_screen.png"
Write-Host "Saved: 03_stats_screen.png"

# Go back to main
& $adb shell input keyevent KEYCODE_BACK
Start-Sleep -Seconds 2

# Screenshot 4: Main screen after navigation
Write-Host "`n--- Capturing Main screen (final) ---"
& $adb shell screencap -p /sdcard/screen4.png
& $adb pull /sdcard/screen4.png "$screenshotDir\04_main_final.png"
Write-Host "Saved: 04_main_final.png"

# Cleanup device screenshots
& $adb shell rm /sdcard/screen1.png /sdcard/screen2.png /sdcard/screen3.png /sdcard/screen4.png 2>$null

Write-Host "`n=== Done! Screenshots saved to $screenshotDir ==="
Get-ChildItem $screenshotDir -Name
