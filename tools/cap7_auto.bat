@echo off
setlocal enabledelayedexpansion
set ADB=C:\Users\morgane\AppData\Local\Android\Sdk\platform-tools\adb.exe
set SERIAL=emulator-5554
set OUTDIR=c:\Users\morgane\Desktop\prev_col\screenshots\tablet7
set PKG=com.regardsaumonde.app

mkdir "%OUTDIR%" 2>nul

echo ===== TABLET 7 SCREENSHOT CAPTURE =====
echo [INFO] Device: %SERIAL%

echo [1] Going home
%ADB% -s %SERIAL% shell input keyevent KEYCODE_HOME
ping -n 3 127.0.0.1 >nul

echo [2] Launching PrivacyActivity
%ADB% -s %SERIAL% shell am start -n "%PKG%/com.example.prevcol.PrivacyActivity"
ping -n 8 127.0.0.1 >nul

echo [3] Selecting French
%ADB% -s %SERIAL% shell input tap 600 709
ping -n 6 127.0.0.1 >nul

echo [4] Screenshot: Privacy
%ADB% -s %SERIAL% shell screencap -p /sdcard/s1.png
%ADB% -s %SERIAL% pull /sdcard/s1.png "%OUTDIR%\01_privacy.png"
echo [OK] 01_privacy.png captured

echo [5] Scrolling down to button
%ADB% -s %SERIAL% shell input swipe 600 1600 600 400 500
ping -n 4 127.0.0.1 >nul

echo [6] Tapping COMPRENDRE ET CONTINUER (600,1578)
%ADB% -s %SERIAL% shell input tap 600 1578
ping -n 8 127.0.0.1 >nul

echo [7] Checking current activity
%ADB% -s %SERIAL% shell "dumpsys window windows | grep mCurrentFocus"

echo [8] If still on Privacy, try launching MainActivity directly
%ADB% -s %SERIAL% shell am start -n "%PKG%/com.example.prevcol.MainActivity"
ping -n 10 127.0.0.1 >nul

echo [9] Dismiss any system dialogs
%ADB% -s %SERIAL% shell input keyevent KEYCODE_BACK
ping -n 3 127.0.0.1 >nul

echo [10] Screenshot: Main
%ADB% -s %SERIAL% shell screencap -p /sdcard/s2.png
%ADB% -s %SERIAL% pull /sdcard/s2.png "%OUTDIR%\02_main.png"
echo [OK] 02_main.png captured

echo [11] Getting screen size
for /f "tokens=3" %%s in ('%ADB% -s %SERIAL% shell wm size ^| findstr Physical') do set SZ=%%s
echo Screen size: !SZ!
for /f "tokens=1,2 delims=x" %%w in ("!SZ!") do (
    set /a CX=%%w/2
    set /a SF=%%x*75/100
    set /a ST=%%x*25/100
)

echo [12] Scrolling to badges
%ADB% -s %SERIAL% shell input swipe !CX! !SF! !CX! !ST! 500
ping -n 4 127.0.0.1 >nul

echo [13] Screenshot: Badges
%ADB% -s %SERIAL% shell screencap -p /sdcard/s3.png
%ADB% -s %SERIAL% pull /sdcard/s3.png "%OUTDIR%\03_main_badges.png"
echo [OK] 03_main_badges.png captured

echo [14] Scrolling more to buttons
%ADB% -s %SERIAL% shell input swipe !CX! !SF! !CX! !ST! 500
ping -n 4 127.0.0.1 >nul

echo [15] Screenshot: Buttons
%ADB% -s %SERIAL% shell screencap -p /sdcard/s4.png
%ADB% -s %SERIAL% pull /sdcard/s4.png "%OUTDIR%\04_main_buttons.png"
echo [OK] 04_main_buttons.png captured

echo [16] Cleanup device files
%ADB% -s %SERIAL% shell rm /sdcard/s1.png /sdcard/s2.png /sdcard/s3.png /sdcard/s4.png 2>nul

echo ===== RESULTS =====
for %%f in ("%OUTDIR%\*.png") do echo %%~nxf - %%~zf bytes
echo ===== TABLET 7 COMPLETE =====
