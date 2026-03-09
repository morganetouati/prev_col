@echo off
setlocal enabledelayedexpansion
set ADB=C:\Users\morgane\AppData\Local\Android\Sdk\platform-tools\adb.exe
set APK=c:\Users\morgane\Desktop\prev_col\app\build\outputs\apk\debug\app-debug.apk
set OUTDIR=c:\Users\morgane\Desktop\prev_col\screenshots\tablet10
set PKG=com.regardsaumonde.app

mkdir "%OUTDIR%" 2>nul

echo [1/9] Checking device
%ADB% devices

echo [2/9] Installing APK
%ADB% install -r -t "%APK%"
ping -n 5 127.0.0.1 >nul

echo [3/9] Going home
%ADB% shell input keyevent KEYCODE_HOME
ping -n 3 127.0.0.1 >nul

echo [4/9] Launching Privacy Activity
%ADB% shell am start -n "%PKG%/com.example.prevcol.PrivacyActivity"
ping -n 9 127.0.0.1 >nul

echo [5/9] Getting screen size
%ADB% shell wm size
for /f "tokens=3" %%s in ('%ADB% shell wm size ^| findstr Physical') do set SZ=%%s
if "!SZ!"=="" for /f "tokens=3" %%s in ('%ADB% shell wm size ^| findstr Override') do set SZ=%%s
if "!SZ!"=="" set SZ=1280x800
echo Screen size: !SZ!
for /f "tokens=1,2 delims=x" %%w in ("!SZ!") do (
    set /a SW=%%w
    set /a SH=%%x
    set /a TX=%%w/2
    set /a TY=%%x*3/4
)
echo Width=!SW! Height=!SH! Tap=!TX!,!TY!

echo [6/9] Screenshot 1 - Privacy
%ADB% shell "screencap -p > /sdcard/Download/s1.png"
ping -n 2 127.0.0.1 >nul
%ADB% pull /sdcard/Download/s1.png "%OUTDIR%\01_privacy.png"

echo [7/9] Tap accept and Screenshot 2 - Main
%ADB% shell input tap !TX! !TY!
ping -n 4 127.0.0.1 >nul
%ADB% shell input tap !TX! !TY!
ping -n 6 127.0.0.1 >nul
%ADB% shell "screencap -p > /sdcard/Download/s2.png"
ping -n 2 127.0.0.1 >nul
%ADB% pull /sdcard/Download/s2.png "%OUTDIR%\02_main.png"

echo [8/9] Scroll down and Screenshot 3 - Badges
set /a SCROLL_FROM=!SH!*7/10
set /a SCROLL_TO=!SH!*2/10
echo Scrolling from !TX!,!SCROLL_FROM! to !TX!,!SCROLL_TO!
%ADB% shell input swipe !TX! !SCROLL_FROM! !TX! !SCROLL_TO! 500
ping -n 3 127.0.0.1 >nul
%ADB% shell "screencap -p > /sdcard/Download/s3.png"
ping -n 2 127.0.0.1 >nul
%ADB% pull /sdcard/Download/s3.png "%OUTDIR%\03_badges.png"

echo [9/9] Scroll more and Screenshot 4 - Buttons
%ADB% shell input swipe !TX! !SCROLL_FROM! !TX! !SCROLL_TO! 500
ping -n 3 127.0.0.1 >nul
%ADB% shell "screencap -p > /sdcard/Download/s4.png"
ping -n 2 127.0.0.1 >nul
%ADB% pull /sdcard/Download/s4.png "%OUTDIR%\04_buttons.png"

echo Cleanup device files
%ADB% shell rm /sdcard/Download/s1.png /sdcard/Download/s2.png /sdcard/Download/s3.png /sdcard/Download/s4.png 2>nul

echo === RESULTS ===
for %%f in ("%OUTDIR%\*.png") do echo %%~nxf - %%~zf bytes
echo === CAPTURE COMPLETE ===
