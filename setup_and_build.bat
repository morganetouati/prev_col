@echo off
REM Script pour initialiser Gradle Wrapper et compiler l'app

echo Initialisation de Gradle Wrapper...
echo Téléchargement de Gradle 7.6...

REM Dossier où stocker le wrapper
set GRADLE_HOME=%USERPROFILE%\.gradle
set WRAPPER_DIR=%GRADLE_HOME%\wrapper\dists\gradle-7.6-bin

if not exist "%WRAPPER_DIR%" (
    echo Création du répertoire %WRAPPER_DIR%...
    mkdir "%WRAPPER_DIR%" >nul
)

REM Télécharger et extraire le wrapper si nécessaire
if not exist "%GRADLE_HOME%\wrapper\dists\gradle-7.6-bin\*.gradle-7.6" (
    echo Téléchargement de gradle-7.6-bin.zip...
    powershell -Command ^
        "$url = 'https://services.gradle.org/distributions/gradle-7.6-bin.zip'; ^
        $dest = '%GRADLE_HOME%\gradle-7.6-bin.zip'; ^
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
        Invoke-WebRequest -Uri $url -OutFile $dest; ^
        Add-Type -AssemblyName System.IO.Compression.FileSystem; ^
        [System.IO.Compression.ZipFile]::ExtractToDirectory($dest, '%GRADLE_HOME%\wrapper\dists\'); ^
        Remove-Item $dest"
    
    if %errorlevel% neq 0 (
        echo Erreur lors du téléchargement. Tentative alternative...
        REM Alternative: essayer avec curl
        cd /d %GRADLE_HOME%
        curl -o gradle-7.6-bin.zip https://services.gradle.org/distributions/gradle-7.6-bin.zip
        if %errorlevel% neq 0 (
            echo ERREUR: Impossible de télécharger Gradle
            echo Solution: Téléchargez manuellement depuis:
            echo https://services.gradle.org/distributions/gradle-7.6-bin.zip
            echo Et extrayez dans: %GRADLE_HOME%\wrapper\dists\
            pause
            exit /b 1
        )
    )
)

echo Gradle 7.6 téléchargé avec succès!
echo.
echo Compilation de l'app...
cd /d "%~dp0"
call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo SUCCESS! APK créé:
    echo app\build\outputs\apk\debug\app-debug.apk
) else (
    echo Erreur lors de la compilation
)

pause
