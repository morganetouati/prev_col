param(
    [Parameter(Mandatory = $true)][string]$AvdName,
    [Parameter(Mandatory = $true)][string]$OutDir,
    [int]$BootTimeoutSeconds = 900
)

$ErrorActionPreference = 'Stop'

$sdk = 'C:\Users\morgane\AppData\Local\Android\Sdk'
$adb = Join-Path $sdk 'platform-tools\adb.exe'
$emu = Join-Path $sdk 'emulator\emulator.exe'
$apk = 'C:\Users\morgane\Desktop\prev_col\app\build\outputs\apk\debug\app-debug.apk'

if (-not (Test-Path $adb)) { throw "adb not found: $adb" }
if (-not (Test-Path $emu)) { throw "emulator not found: $emu" }
if (-not (Test-Path $apk)) { throw "APK not found: $apk" }

New-Item -ItemType Directory -Path $OutDir -Force | Out-Null

# Start emulator detached (software mode fallback when hypervisor is unavailable)
$null = Start-Process -FilePath $emu -ArgumentList @('-avd', $AvdName, '-accel', 'off', '-no-snapshot', '-no-boot-anim', '-gpu', 'swiftshader_indirect') -PassThru
Start-Sleep -Seconds 8

$deadline = (Get-Date).AddSeconds($BootTimeoutSeconds)
$serial = $null
while ((Get-Date) -lt $deadline) {
    $serials = & $adb devices | Select-String 'emulator-\d+\s+device' | ForEach-Object { ($_ -split '\s+')[0] }
    if ($serials) {
        foreach ($candidate in $serials) {
            $candidateAvd = (& $adb -s $candidate shell getprop ro.boot.qemu.avd_name 2>$null).Trim()
            if ($candidateAvd -ne $AvdName) { continue }
            $boot = (& $adb -s $candidate shell getprop sys.boot_completed 2>$null).Trim()
            if ($boot -eq '1') {
                $serial = $candidate
                break
            }
        }
        if ($serial) { break }
    }
    Start-Sleep -Seconds 3
}

if (-not $serial) { throw "Emulator boot timeout for AVD: $AvdName" }

& $adb -s $serial install -r $apk | Out-Host

& $adb -s $serial shell am start -n com.regardsaumonde.app/.PrivacyActivity | Out-Host
Start-Sleep -Seconds 2
& $adb -s $serial shell screencap -p /sdcard/privacy.png | Out-Null
& $adb -s $serial pull /sdcard/privacy.png (Join-Path $OutDir 'privacy.png') | Out-Host

& $adb -s $serial shell am start -n com.regardsaumonde.app/.MainActivity | Out-Host
Start-Sleep -Seconds 3
& $adb -s $serial shell screencap -p /sdcard/main.png | Out-Null
& $adb -s $serial pull /sdcard/main.png (Join-Path $OutDir 'main.png') | Out-Host

& $adb -s $serial shell am start -n com.regardsaumonde.app/.StatsActivity | Out-Host
Start-Sleep -Seconds 2
& $adb -s $serial shell screencap -p /sdcard/stats.png | Out-Null
& $adb -s $serial pull /sdcard/stats.png (Join-Path $OutDir 'stats.png') | Out-Host

# Shutdown emulator
& $adb -s $serial emu kill | Out-Null

Write-Output "OK AVD=$AvdName SERIAL=$serial OUTDIR=$OutDir"