@echo off
setlocal

echo ===================================
echo PrevCol - Build and Test
echo ===================================
echo.

if not exist "gradlew.bat" (
    echo ERREUR: gradlew.bat non trouve
    echo Assurez-vous d'etre dans le dossier prev_col
    exit /b 1
)

echo Choisissez un mode:
echo [1] Build debug rapide
echo [2] Release Play Store stricte (lint + tests + bundle AAB)
echo.
set /p MODE="Votre choix (1/2): "

if "%MODE%"=="1" goto :debug
if "%MODE%"=="2" goto :release

echo Choix invalide.
exit /b 1

:debug
echo.
echo [1/3] Compilation debug...
call gradlew.bat :app:assembleDebug --console=plain
if %errorlevel% neq 0 (
    echo ERREUR: La compilation debug a echoue
    exit /b 1
)

echo [2/3] APK debug genere:
echo app\build\outputs\apk\debug\app-debug.apk
echo.

set /p INSTALL="[3/3] Installer sur un appareil? (o/n) "
if /i "%INSTALL%"=="o" (
    call gradlew.bat :app:installDebug --console=plain
    if %errorlevel% neq 0 (
        echo ERREUR: Installation debug echouee
        exit /b 1
    )
    echo Installation debug terminee.
) else (
    echo Installation annulee.
)

goto :done

:release
echo.
echo [1/5] Verification config Play release...
call gradlew.bat :app:verifyPlayReleaseConfig --console=plain
if %errorlevel% neq 0 (
    echo ERREUR: Configuration Play release invalide
    exit /b 1
)

echo [2/5] Lint release critique...
call gradlew.bat :app:lintVitalRelease --console=plain
if %errorlevel% neq 0 (
    echo ERREUR: lintVitalRelease a echoue
    exit /b 1
)

echo [3/5] Tests unitaires debug...
call gradlew.bat :app:testDebugUnitTest --console=plain
if %errorlevel% neq 0 (
    echo ERREUR: Les tests unitaires ont echoue
    exit /b 1
)

echo [4/5] Build APK release...
call gradlew.bat :app:assembleRelease --console=plain
if %errorlevel% neq 0 (
    echo ERREUR: assembleRelease a echoue
    exit /b 1
)

echo [5/5] Build Bundle Play (AAB)...
call gradlew.bat :app:bundleRelease --console=plain
if %errorlevel% neq 0 (
    echo ERREUR: bundleRelease a echoue
    exit /b 1
)

echo.
echo Artefacts release:
echo - APK: app\build\outputs\apk\release\app-release.apk
echo - AAB: app\build\outputs\bundle\release\app-release.aab

:done
echo.
echo ===================================
echo Termine!
echo ===================================
endlocal
pause
