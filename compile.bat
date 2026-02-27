@echo off
REM Solution simple pour compiler l'APK
REM Utilise maven/gradle directement sans wrapper

cd /d C:\Users\morgane\Desktop\prev_col

REM Vérifier si java est disponible
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERREUR: Java n'est pas trouvé
    echo Installez le JDK 11+ depuis https://adoptium.net/
    pause
    exit /b 1
)

echo.
echo ========================================
echo PrevCol - Compilation directe
echo ========================================
echo.

REM Télécharger le wrapper maven
REM Alternative: utiliser maven directement si disponible

REM Pour Windows, la solution basique est d'utiliser Android Studio
echo.
echo OPTION 1: Utiliser Android Studio (RECOMMANDÉ)
echo ==================================================
echo 1. Ouvrez le projet dans Android Studio
echo    File ^> Open... ^> C:\Users\morgane\Desktop\prev_col
echo 2. Attendez la synchro Gradle
echo 3. Cliquez Build ^> Build Bundle(s) / APK(s) ^> Build APK(s)
echo.
echo OPTION 2: Ligne de commande avec Gradle local
echo ==================================================
echo Installez Gradle: https://gradle.org/releases/
echo Puis lancez: gradle assembleDebug
echo.
echo ========================================
echo Quelle option choisissez-vous ?
echo ========================================
echo.

pause
