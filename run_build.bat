@echo off
cd /d c:\Users\morgane\Desktop\prev_col
call gradlew.bat :app:bundleRelease --no-daemon > build_v10_output.txt 2>&1
echo EXIT_CODE=%ERRORLEVEL% >> build_v10_output.txt
