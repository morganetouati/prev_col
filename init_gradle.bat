@echo off
setlocal enabledelayedexpansion

REM Initialiser Gradle Wrapper

set GRADLE_HOME=%USERPROFILE%\.gradle
set DISTS_DIR=!GRADLE_HOME!\wrapper\dists

echo Création des répertoires...
if not exist "!GRADLE_HOME!" mkdir "!GRADLE_HOME!"
if not exist "!DISTS_DIR!" mkdir "!DISTS_DIR!"

cd /d "!GRADLE_HOME!"

echo.
echo ========================================
echo Téléchargement de Gradle 7.6...
echo ========================================

REM Utiliser bitsadmin pour télécharger
bitsadmin /transfer GradleDownload /download /priority foreground ^
  "https://services.gradle.org/distributions/gradle-7.6-bin.zip" ^
  "!GRADLE_HOME!\gradle-7.6-bin.zip"

if %ERRORLEVEL% equ 0 (
    echo Téléchargement réussi!
    echo.
    echo Extraction en cours...
    
    REM Extraire avec PowerShell
    powershell -NoProfile -Command ^
      "Add-Type -AssemblyName System.IO.Compression.FileSystem; ^
       [System.IO.Compression.ZipFile]::ExtractToDirectory( ^
       '!GRADLE_HOME!\gradle-7.6-bin.zip', '!DISTS_DIR!\'); ^
       Write-Host 'Extraction terminée'"
    
    if %ERRORLEVEL% equ 0 (
        echo.
        echo Nettoyage...
        del /q "!GRADLE_HOME!\gradle-7.6-bin.zip"
        
        echo.
        echo ========================================
        echo SUCCESS! Gradle est configuré.
        echo ========================================
        echo.
        echo Compilation de l'application...
        echo.
        
        cd /d C:\Users\morgane\Desktop\prev_col
        cmd /c gradlew.bat assembleDebug
        
        if %ERRORLEVEL% equ 0 (
            echo.
            echo ========================================
            echo COMPILATION RÉUSSIE!
            echo ========================================
            echo APK créé: app\build\outputs\apk\debug\app-debug.apk
        ) else (
            echo Erreur lors de la compilation
        )
    ) else (
        echo Erreur lors de l'extraction
    )
) else (
    echo Erreur du téléchargement
    echo Essayez de télécharger manuellement:
    echo https://services.gradle.org/distributions/gradle-7.6-bin.zip
    echo Et extrayez dans: !GRADLE_HOME!\wrapper\dists\
)

endlocal
pause
