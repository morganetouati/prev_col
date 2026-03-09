@echo off
setlocal enabledelayedexpansion
set ADB=C:\Users\morgane\AppData\Local\Android\Sdk\platform-tools\adb.exe
set OUTDIR=c:\Users\morgane\Desktop\prev_col\screenshots\tablet10
set PKG=com.regardsaumonde.app

mkdir "%OUTDIR%" 2>nul

echo [1/8] Checking device
%ADB% devices

echo [2/8] Going home
%ADB% shell input keyevent KEYCODE_HOME
ping -n 3 127.0.0.1 >nul

echo [3/8] Launching Privacy Activity
%ADB% shell am start -n "%PKG%/com.example.prevcol.PrivacyActivity"
ping -n 9 127.0.0.1 >nul

echo [4/8] Screenshot 1 - Privacy
%ADB% shell screencap -p /sdcard/s1.png
%ADB% pull /sdcard/s1.png "%OUTDIR%\01_privacy.png"

echo [5/8] Getting screen size and tapping accept
for /f "tokens=3" %%s in ('%ADB% shell wm size ^| findstr Physical') do set SZ=%%s
echo Screen size: !SZ!
for /f "tokens=1,2 delims=x" %%w in ("!SZ!") do (
    set /a TX=%%w/2
    set /a TY=%%x*3/4
    echo Tap at: !TX!, !TY!
)
%ADB% shell input tap !TX! !TY!
ping -n 4 127.0.0.1 >nul
%ADB% shell input tap !TX! !TY!
ping -n 6 127.0.0.1 >nul

echo [6/8] Screenshot 2 - Main
%ADB% shell screencap -p /sdcard/s2.png
%ADB% pull /sdcard/s2.png "%OUTDIR%\02_main.png"

echo [7/8] Scrolling and Screenshot 3 - Badges
%ADB% shell input swipe !TX! 1400 !TX! 400 500
ping -n 3 127.0.0.1 >nul
%ADB% shell screencap -p /sdcard/s3.png
%ADB% pull /sdcard/s3.png "%OUTDIR%\03_badges.png"

echo [8/8] Scrolling more and Screenshot 4 - Buttons
%ADB% shell input swipe !TX! 1400 !TX! 200 500
ping -n 3 127.0.0.1 >nul
%ADB% shell screencap -p /sdcard/s4.png
%ADB% pull /sdcard/s4.png "%OUTDIR%\04_buttons.png"

echo Cleanup
%ADB% shell rm /sdcard/s1.png /sdcard/s2.png /sdcard/s3.png /sdcard/s4.png 2>nul

echo === RESULTS ===
for %%f in ("%OUTDIR%\*.png") do echo %%~nxf - %%~zf bytes
echo === CAPTURE COMPLETE ===
