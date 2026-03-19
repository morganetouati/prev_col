@echo off
setlocal enabledelayedexpansion
set ADB=C:\Users\morgane\AppData\Local\Android\Sdk\platform-tools\adb.exe
set SERIAL=emulator-5554
set OUTDIR=c:\Users\morgane\Desktop\prev_col\screenshots\tablet10
set PKG=com.regardsaumonde.app

mkdir "%OUTDIR%" 2>nul

echo ===== TABLET 10 SCREENSHOT CAPTURE =====
echo [INFO] Device: %SERIAL%

echo [1] Going home
%ADB% -s %SERIAL% shell input keyevent KEYCODE_HOME
ping -n 3 127.0.0.1 >nul

echo [2] Launching PrivacyActivity
%ADB% -s %SERIAL% shell am force-stop %PKG%
ping -n 2 127.0.0.1 >nul
%ADB% -s %SERIAL% shell am start -n "%PKG%/com.example.prevcol.PrivacyActivity"
ping -n 10 127.0.0.1 >nul

echo [3] Dumping UI to find language dialog
%ADB% -s %SERIAL% shell "uiautomator dump /sdcard/ui.xml 2>&1 && cat /sdcard/ui.xml" > "%OUTDIR%\ui_lang.xml" 2>&1

echo [4] Selecting French (tap center of Francais item at bounds [366,251][914,299])
%ADB% -s %SERIAL% shell input tap 640 275
ping -n 8 127.0.0.1 >nul

echo [5] Screenshot: Privacy
%ADB% -s %SERIAL% shell screencap -p /sdcard/s1.png
%ADB% -s %SERIAL% pull /sdcard/s1.png "%OUTDIR%\01_privacy.png"
echo [OK] 01_privacy.png captured

echo [6] Scrolling down
%ADB% -s %SERIAL% shell input swipe 640 700 640 200 500
ping -n 4 127.0.0.1 >nul

echo [7] Dumping UI for accept button
%ADB% -s %SERIAL% shell "uiautomator dump /sdcard/ui2.xml 2>&1 && cat /sdcard/ui2.xml" > "%OUTDIR%\ui_accept.xml" 2>&1

echo [8] Finding and tapping accept button
REM Screen is 1280x800, accept button should be near bottom after scroll
REM Tap at center-x=640, near bottom y=700
echo Tapping accept at 640, 700
%ADB% -s %SERIAL% shell input tap 640 700
ping -n 10 127.0.0.1 >nul

echo [9] Checking activity
%ADB% -s %SERIAL% shell "dumpsys window windows | grep mCurrentFocus"

echo [10] Dismiss any dialogs
%ADB% -s %SERIAL% shell input keyevent KEYCODE_BACK
ping -n 3 127.0.0.1 >nul

echo [11] Screenshot: Main
%ADB% -s %SERIAL% shell screencap -p /sdcard/s2.png
%ADB% -s %SERIAL% pull /sdcard/s2.png "%OUTDIR%\02_main.png"
echo [OK] 02_main.png captured

echo [12] Scrolling to badges
%ADB% -s %SERIAL% shell input swipe 640 650 640 150 500
ping -n 4 127.0.0.1 >nul

echo [13] Screenshot: Badges
%ADB% -s %SERIAL% shell screencap -p /sdcard/s3.png
%ADB% -s %SERIAL% pull /sdcard/s3.png "%OUTDIR%\03_badges.png"
echo [OK] 03_badges.png captured

echo [14] Scrolling more
%ADB% -s %SERIAL% shell input swipe 640 650 640 150 500
ping -n 4 127.0.0.1 >nul

echo [15] Screenshot: Buttons
%ADB% -s %SERIAL% shell screencap -p /sdcard/s4.png
%ADB% -s %SERIAL% pull /sdcard/s4.png "%OUTDIR%\04_buttons.png"
echo [OK] 04_buttons.png captured

echo [16] Cleanup device files
%ADB% -s %SERIAL% shell rm /sdcard/s1.png /sdcard/s2.png /sdcard/s3.png /sdcard/s4.png /sdcard/ui.xml /sdcard/ui2.xml 2>nul

echo ===== RESULTS =====
for %%f in ("%OUTDIR%\*.png") do echo %%~nxf - %%~zf bytes
echo ===== TABLET 10 COMPLETE =====
