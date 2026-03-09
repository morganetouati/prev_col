@echo off
setlocal enabledelayedexpansion
set ADB=C:\Users\morgane\AppData\Local\Android\Sdk\platform-tools\adb.exe
set APK=c:\Users\morgane\Desktop\prev_col\app\build\outputs\apk\debug\app-debug.apk
set OUTDIR=c:\Users\morgane\Desktop\prev_col\screenshots\%1
set PKG=com.regardsaumonde.app

echo === Screenshot Capture for %1 ===
mkdir "%OUTDIR%" 2>nul

echo --- Waiting for device ---
%ADB% wait-for-device
echo Device connected

echo --- Wait for boot ---
:waitboot
timeout /t 5 /nobreak >nul
for /f %%b in ('%ADB% shell getprop sys.boot_completed 2^>nul') do set BOOT=%%b
if not "%BOOT%"=="1" (
    echo Still booting...
    goto waitboot
)
echo Boot complete!

echo --- Installing APK ---
%ADB% install -r -t "%APK%"
timeout /t 3 /nobreak >nul

echo --- Checking connection ---
%ADB% devices
timeout /t 2 /nobreak >nul

echo --- Going Home ---
%ADB% shell input keyevent KEYCODE_HOME
timeout /t 2 /nobreak >nul

echo --- Launching App via shell ---
%ADB% shell "am start -W -S com.regardsaumonde.app/.PrivacyActivity" 2>&1
timeout /t 8 /nobreak >nul

echo --- Check connection ---
%ADB% devices

echo --- Screenshot 1: Privacy ---
%ADB% exec-out screencap -p > "%OUTDIR%\01_privacy.png"
echo Saved 01_privacy.png

echo --- Get screen size ---
for /f "tokens=3" %%s in ('%ADB% shell wm size ^| findstr Physical') do set SIZE=%%s
echo Size: %SIZE%
for /f "tokens=1,2 delims=x" %%w in ("%SIZE%") do (
    set /a CX=%%w/2
    set /a CY=%%x*75/100
)
echo Tap position: %CX%, %CY%

echo --- Tap Accept ---
%ADB% shell input tap %CX% %CY%
timeout /t 5 /nobreak >nul

echo --- Screenshot 2: Main ---
%ADB% exec-out screencap -p > "%OUTDIR%\02_main.png"
echo Saved 02_main.png

echo --- Navigate to Stats ---
%ADB% shell "am start -n com.regardsaumonde.app/.StatsActivity" 2>&1
timeout /t 4 /nobreak >nul

echo --- Screenshot 3: Stats ---
%ADB% exec-out screencap -p > "%OUTDIR%\03_stats.png"
echo Saved 03_stats.png

echo --- Back to Main ---
%ADB% shell input keyevent KEYCODE_BACK
timeout /t 3 /nobreak >nul

echo --- Screenshot 4: Main Final ---
%ADB% exec-out screencap -p > "%OUTDIR%\04_main_final.png"
echo Saved 04_main_final.png

echo === Results ===
dir "%OUTDIR%\*.png"
echo === DONE ===
