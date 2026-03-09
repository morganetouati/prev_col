@echo off
setlocal enabledelayedexpansion
set ADB=C:\Users\morgane\AppData\Local\Android\Sdk\platform-tools\adb.exe
set APK=c:\Users\morgane\Desktop\prev_col\app\build\outputs\apk\debug\app-debug.apk
set OUTDIR=c:\Users\morgane\Desktop\prev_col\screenshots\tablet10
set PKG=com.regardsaumonde.app

echo === Tablet 10" Screenshot Capture ===
mkdir "%OUTDIR%" 2>nul

echo --- Waiting for device ---
%ADB% wait-for-device
echo Device connected

echo --- Polling boot status ---
:waitboot
timeout /t 5 /nobreak >nul
for /f "usebackq" %%b in (`%ADB% shell getprop sys.boot_completed 2^>nul`) do set BOOT=%%b
if "!BOOT!"=="1" goto booted
echo Still booting...
goto waitboot
:booted
echo BOOT COMPLETE!
timeout /t 5 /nobreak >nul

echo --- Installing APK ---
%ADB% install -r -t "%APK%"
timeout /t 3 /nobreak >nul

echo --- Home ---
%ADB% shell input keyevent KEYCODE_HOME
timeout /t 2 /nobreak >nul

echo --- Launch Privacy Activity ---
%ADB% shell am start -n "%PKG%/com.example.prevcol.PrivacyActivity"
timeout /t 8 /nobreak >nul

echo --- Screenshot 1: Privacy ---
%ADB% shell screencap -p /sdcard/s1.png
%ADB% pull /sdcard/s1.png "%OUTDIR%\01_privacy.png"

echo --- Get screen dimensions ---
for /f "tokens=3" %%s in ('%ADB% shell wm size ^| findstr Physical') do set SZ=%%s
echo Screen: %SZ%
for /f "tokens=1,2 delims=x" %%w in ("%SZ%") do (
    set /a TX=%%w/2
    set /a TY=%%x*3/4
)

echo --- Tap accept (%TX%, %TY%) ---
%ADB% shell input tap %TX% %TY%
timeout /t 3 /nobreak >nul
%ADB% shell input tap %TX% %TY%
timeout /t 5 /nobreak >nul

echo --- Screenshot 2: Main screen ---
%ADB% shell screencap -p /sdcard/s2.png
%ADB% pull /sdcard/s2.png "%OUTDIR%\02_main.png"

echo --- Scroll down to badges ---
%ADB% shell input swipe %TX% 1400 %TX% 400 500
timeout /t 2 /nobreak >nul

echo --- Screenshot 3: Badges ---
%ADB% shell screencap -p /sdcard/s3.png
%ADB% pull /sdcard/s3.png "%OUTDIR%\03_badges.png"

echo --- Scroll more ---
%ADB% shell input swipe %TX% 1400 %TX% 200 500
timeout /t 2 /nobreak >nul

echo --- Screenshot 4: Buttons ---
%ADB% shell screencap -p /sdcard/s4.png
%ADB% pull /sdcard/s4.png "%OUTDIR%\04_buttons.png"

echo --- Cleanup device files ---
%ADB% shell rm /sdcard/s1.png /sdcard/s2.png /sdcard/s3.png /sdcard/s4.png 2>nul

echo === RESULTS ===
dir /b "%OUTDIR%\*.png"
echo === DONE ===
