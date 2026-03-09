@echo off
setlocal enabledelayedexpansion
set ADB=C:\Users\morgane\AppData\Local\Android\Sdk\platform-tools\adb.exe
set OUTDIR=c:\Users\morgane\Desktop\prev_col\screenshots\%1
set PKG=com.regardsaumonde.app

echo === Capture Screenshots for %1 ===
mkdir "%OUTDIR%" 2>nul

rem Check device is ready
echo --- Device check ---
%ADB% devices -l
if errorlevel 1 goto :err

rem Go home first
echo --- Home ---
%ADB% shell input keyevent KEYCODE_HOME
timeout /t 2 /nobreak >nul

rem Launch app
echo --- Launching Privacy Activity ---
%ADB% shell am start %PKG%/.PrivacyActivity
timeout /t 8 /nobreak >nul

rem Screenshot 1: Privacy screen
echo --- Screenshot 1: Privacy ---
%ADB% shell screencap -p /sdcard/sc1.png
%ADB% pull /sdcard/sc1.png "%OUTDIR%\01_privacy.png"

rem Get screen dimensions for tap positions
for /f "tokens=3" %%s in ('%ADB% shell wm size ^| findstr Physical') do set SCRSIZE=%%s
echo Screen size: %SCRSIZE%
for /f "tokens=1,2 delims=x" %%w in ("%SCRSIZE%") do (
    set /a TAPX=%%w/2
    set /a TAPY=%%x*3/4
)
echo Will tap at %TAPX%, %TAPY%

rem Tap the accept/continue button
echo --- Tapping accept ---
%ADB% shell input tap %TAPX% %TAPY%
timeout /t 3 /nobreak >nul
%ADB% shell input tap %TAPX% %TAPY%
timeout /t 5 /nobreak >nul

rem Screenshot 2: Main screen
echo --- Screenshot 2: Main ---
%ADB% shell screencap -p /sdcard/sc2.png
%ADB% pull /sdcard/sc2.png "%OUTDIR%\02_main.png"

rem Launch Stats
echo --- Launching Stats ---
%ADB% shell am start -n %PKG%/.StatsActivity
timeout /t 5 /nobreak >nul

rem Screenshot 3: Stats
echo --- Screenshot 3: Stats ---
%ADB% shell screencap -p /sdcard/sc3.png
%ADB% pull /sdcard/sc3.png "%OUTDIR%\03_stats.png"

rem Go back
echo --- Back ---
%ADB% shell input keyevent KEYCODE_BACK
timeout /t 3 /nobreak >nul

rem Screenshot 4: Main after nav
echo --- Screenshot 4: Main after nav ---
%ADB% shell screencap -p /sdcard/sc4.png
%ADB% pull /sdcard/sc4.png "%OUTDIR%\04_main_final.png"

rem Cleanup temp files
%ADB% shell rm /sdcard/sc1.png /sdcard/sc2.png /sdcard/sc3.png /sdcard/sc4.png 2>nul

echo === Results ===
dir /b "%OUTDIR%\*.png"
echo === COMPLETE ===
goto :eof

:err
echo ERROR: No device available
exit /b 1
