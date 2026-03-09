@echo off
setlocal
set ADB=C:\Users\morgane\AppData\Local\Android\Sdk\platform-tools\adb.exe
set APK=c:\Users\morgane\Desktop\prev_col\app\build\outputs\apk\debug\app-debug.apk
set OUTDIR=c:\Users\morgane\Desktop\prev_col\screenshots\%1
set PKG=com.regardsaumonde.app

echo === Screenshot Capture for %1 ===
mkdir "%OUTDIR%" 2>nul

echo --- ADB Devices ---
%ADB% devices -l

echo --- Installing APK ---
%ADB% install -r -t "%APK%"

echo --- Checking Package ---
%ADB% shell pm path %PKG%

echo --- Going Home ---
%ADB% shell input keyevent KEYCODE_HOME
timeout /t 2 /nobreak >nul

echo --- Launching App ---
%ADB% shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER %PKG%/.PrivacyActivity
timeout /t 5 /nobreak >nul

echo --- Screenshot 1: Privacy ---
%ADB% shell screencap -p /sdcard/s1.png
%ADB% pull /sdcard/s1.png "%OUTDIR%\01_privacy.png"

echo --- Tap Accept (center screen) ---
for /f "tokens=3 delims= " %%a in ('%ADB% shell wm size 2^>^&1 ^| findstr "Physical"') do set SIZE=%%a
echo Screen: %SIZE%
for /f "tokens=1,2 delims=x" %%w in ("%SIZE%") do (
    set /a TX=%%w/2
    set /a TY=%%h*3/4
    set SW=%%w
    set SH=%%h
)
rem Use fixed tap positions as fallback
set /a TX=%SW%/2
set /a TY=%SH%*75/100
echo Tapping at %TX%, %TY%
%ADB% shell input tap %TX% %TY%
timeout /t 5 /nobreak >nul

echo --- Screenshot 2: Main ---
%ADB% shell screencap -p /sdcard/s2.png
%ADB% pull /sdcard/s2.png "%OUTDIR%\02_main.png"

echo --- Launch Stats ---
%ADB% shell am start -n %PKG%/.StatsActivity
timeout /t 3 /nobreak >nul

echo --- Screenshot 3: Stats ---
%ADB% shell screencap -p /sdcard/s3.png
%ADB% pull /sdcard/s3.png "%OUTDIR%\03_stats.png"

echo --- Back to Main ---
%ADB% shell input keyevent KEYCODE_BACK
timeout /t 2 /nobreak >nul

echo --- Screenshot 4: Main Final ---
%ADB% shell screencap -p /sdcard/s4.png
%ADB% pull /sdcard/s4.png "%OUTDIR%\04_main_final.png"

echo --- Cleanup ---
%ADB% shell rm /sdcard/s1.png /sdcard/s2.png /sdcard/s3.png /sdcard/s4.png

echo === Done ===
dir "%OUTDIR%"
