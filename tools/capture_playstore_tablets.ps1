<#
.SYNOPSIS
    Capture Play Store screenshots for 7" and 10" tablets.
.DESCRIPTION
    Boots each tablet AVD, installs the APK, navigates through the app,
    and captures screenshots suitable for Play Store listing.
#>
param(
    [string]$Avd7  = 'Tablet_7_API35',
    [string]$Avd10 = 'Tablet10_API35',
    [int]$BootTimeoutSeconds = 300
)

$ErrorActionPreference = 'Stop'

$sdk  = "$env:LOCALAPPDATA\Android\Sdk"
$adb  = Join-Path $sdk 'platform-tools\adb.exe'
$emu  = Join-Path $sdk 'emulator\emulator.exe'
$apk  = 'c:\Users\morgane\Desktop\prev_col\app\build\outputs\apk\debug\app-debug.apk'
$base = 'c:\Users\morgane\Desktop\prev_col\screenshots'
$pkg  = 'com.regardsaumonde.app'

foreach ($p in @($adb, $emu, $apk)) {
    if (-not (Test-Path $p)) { throw "Not found: $p" }
}

function Wait-EmulatorBoot {
    param([string]$AvdName, [int]$Timeout)
    $deadline = (Get-Date).AddSeconds($Timeout)
    $serial = $null
    while ((Get-Date) -lt $deadline) {
        $lines = & $adb devices 2>$null | Where-Object { $_ -match 'emulator-\d+\s+device' }
        foreach ($line in $lines) {
            $candidate = ($line -split '\s+')[0]
            $candidateAvd = (& $adb -s $candidate shell getprop ro.boot.qemu.avd_name 2>$null).Trim()
            if ($candidateAvd -eq $AvdName) {
                $boot = (& $adb -s $candidate shell getprop sys.boot_completed 2>$null).Trim()
                if ($boot -eq '1') {
                    $serial = $candidate
                    break
                }
            }
        }
        if ($serial) { break }
        Write-Host "  Waiting for $AvdName boot..." -ForegroundColor DarkGray
        Start-Sleep -Seconds 5
    }
    if (-not $serial) { throw "Emulator boot timeout for AVD: $AvdName" }
    return $serial
}

function Get-ScreenSize {
    param([string]$Serial)
    $size = (& $adb -s $Serial shell wm size 2>$null | Select-String 'Physical').ToString()
    if ($size -match '(\d+)x(\d+)') {
        return @{ Width = [int]$Matches[1]; Height = [int]$Matches[2] }
    }
    throw "Cannot determine screen size"
}

function Capture-TabletScreenshots {
    param(
        [string]$AvdName,
        [string]$OutDir
    )

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  Capturing: $AvdName -> $OutDir" -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan

    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null

    # Kill any lingering emulator
    & $adb devices 2>$null | Where-Object { $_ -match 'emulator-\d+' } | ForEach-Object {
        $s = ($_ -split '\s+')[0]
        $avd = (& $adb -s $s shell getprop ro.boot.qemu.avd_name 2>$null).Trim()
        if ($avd -eq $AvdName) {
            Write-Host "  Killing existing $AvdName ($s)..."
            & $adb -s $s emu kill 2>$null
            Start-Sleep -Seconds 5
        }
    }

    # Start emulator
    Write-Host "  Starting emulator $AvdName..."
    $null = Start-Process -FilePath $emu -ArgumentList @(
        '-avd', $AvdName,
        '-no-snapshot',
        '-no-boot-anim',
        '-gpu', 'swiftshader_indirect'
    ) -PassThru
    Start-Sleep -Seconds 10

    # Wait for boot
    Write-Host "  Waiting for boot..."
    $serial = Wait-EmulatorBoot -AvdName $AvdName -Timeout $BootTimeoutSeconds
    Write-Host "  Booted: $serial" -ForegroundColor Green

    # Extra wait for launcher to settle
    Start-Sleep -Seconds 10

    # Get screen dimensions
    $screen = Get-ScreenSize -Serial $serial
    $w = $screen.Width
    $h = $screen.Height
    $cx = [int]($w / 2)
    Write-Host "  Screen: ${w}x${h}" -ForegroundColor Yellow

    # Install APK
    Write-Host "  Installing APK..."
    & $adb -s $serial install -r $apk 2>&1 | Out-Host
    Start-Sleep -Seconds 3

    # Go to home
    & $adb -s $serial shell input keyevent KEYCODE_HOME 2>$null
    Start-Sleep -Seconds 2

    # --- Screenshot 1: Privacy ---
    Write-Host "  [1/4] Launching PrivacyActivity..."
    & $adb -s $serial shell am start -n "$pkg/com.example.prevcol.PrivacyActivity" 2>&1 | Out-Host
    Start-Sleep -Seconds 5
    & $adb -s $serial shell screencap -p /sdcard/s1.png 2>$null
    & $adb -s $serial pull /sdcard/s1.png (Join-Path $OutDir '01_privacy.png') 2>&1 | Out-Host
    Write-Host "  [1/4] Privacy screenshot OK" -ForegroundColor Green

    # --- Accept privacy (tap bottom area) ---
    $tapY = [int]($h * 3 / 4)
    Write-Host "  Tapping accept at ($cx, $tapY)..."
    & $adb -s $serial shell input tap $cx $tapY 2>$null
    Start-Sleep -Seconds 3
    & $adb -s $serial shell input tap $cx $tapY 2>$null
    Start-Sleep -Seconds 5

    # --- Screenshot 2: Main ---
    Write-Host "  [2/4] Main screen..."
    & $adb -s $serial shell screencap -p /sdcard/s2.png 2>$null
    & $adb -s $serial pull /sdcard/s2.png (Join-Path $OutDir '02_main.png') 2>&1 | Out-Host
    Write-Host "  [2/4] Main screenshot OK" -ForegroundColor Green

    # --- Screenshot 3: Scroll down - Badges ---
    Write-Host "  [3/4] Scrolling to badges..."
    $swipeFrom = [int]($h * 0.75)
    $swipeTo   = [int]($h * 0.25)
    & $adb -s $serial shell input swipe $cx $swipeFrom $cx $swipeTo 500 2>$null
    Start-Sleep -Seconds 3
    & $adb -s $serial shell screencap -p /sdcard/s3.png 2>$null
    & $adb -s $serial pull /sdcard/s3.png (Join-Path $OutDir '03_main_badges.png') 2>&1 | Out-Host
    Write-Host "  [3/4] Badges screenshot OK" -ForegroundColor Green

    # --- Screenshot 4: Scroll more - Buttons ---
    Write-Host "  [4/4] Scrolling to buttons..."
    & $adb -s $serial shell input swipe $cx $swipeFrom $cx $swipeTo 500 2>$null
    Start-Sleep -Seconds 3
    & $adb -s $serial shell screencap -p /sdcard/s4.png 2>$null
    & $adb -s $serial pull /sdcard/s4.png (Join-Path $OutDir '04_main_buttons.png') 2>&1 | Out-Host
    Write-Host "  [4/4] Buttons screenshot OK" -ForegroundColor Green

    # Cleanup temp files on device
    & $adb -s $serial shell rm /sdcard/s1.png /sdcard/s2.png /sdcard/s3.png /sdcard/s4.png 2>$null

    # Shutdown emulator
    Write-Host "  Shutting down $AvdName..."
    & $adb -s $serial emu kill 2>$null
    Start-Sleep -Seconds 5

    # Report
    Write-Host "`n  Results for $AvdName :" -ForegroundColor Cyan
    Get-ChildItem $OutDir -Filter '*.png' | ForEach-Object {
        $szKB = [math]::Round($_.Length / 1KB, 1)
        Write-Host "    $($_.Name) - ${szKB} KB"
    }
    Write-Host ""
}

# ==========================================
# Main execution
# ==========================================
Write-Host "Play Store Tablet Screenshot Capture" -ForegroundColor Cyan
Write-Host "====================================`n"

# Capture 7-inch tablet
Capture-TabletScreenshots -AvdName $Avd7 -OutDir (Join-Path $base 'tablet7')

# Capture 10-inch tablet
Capture-TabletScreenshots -AvdName $Avd10 -OutDir (Join-Path $base 'tablet10')

Write-Host "`n=====================================" -ForegroundColor Green
Write-Host "  ALL CAPTURES COMPLETE" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host "  tablet7:  $(Join-Path $base 'tablet7')"
Write-Host "  tablet10: $(Join-Path $base 'tablet10')"

# Verify image dimensions
Write-Host "`nImage dimensions:" -ForegroundColor Yellow
foreach ($dir in @('tablet7', 'tablet10')) {
    $dirPath = Join-Path $base $dir
    Write-Host "  $dir :"
    Get-ChildItem $dirPath -Filter '*.png' | ForEach-Object {
        $szKB = [math]::Round($_.Length / 1KB, 1)
        Write-Host "    $($_.Name) - ${szKB} KB"
    }
}
