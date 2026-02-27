@echo off
REM Script de test rapide pour PrevCol
REM Compile et installe l'app en mode debug

echo ===================================
echo PrevCol - Build Rapide
echo ===================================
echo.

REM Vérifier si gradlew existe
if not exist "gradlew.bat" (
    echo ERREUR: gradlew.bat non trouvé
    echo Assurez-vous d'être dans le dossier prev_col
    exit /b 1
)

REM Compiler en mode debug (plus rapide)
echo [1/3] Compilation en mode debug...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ERREUR: La compilation a échoué
    exit /b 1
)

echo.
echo [2/3] APK généré avec succès!
echo Localisation: app\build\outputs\apk\debug\app-debug.apk
echo.

REM Demander si l'utilisateur veut installer
set /p INSTALL="[3/3] Installer sur un appareil? (o/n) "
if /i "%INSTALL%"=="o" (
    echo Installation en cours...
    call gradlew.bat installDebug
    if %errorlevel% equ 0 (
        echo.
        echo SUCCESS: App installée! Mode Démo prêt à tester.
    ) else (
        echo ERREUR: L'installation a échoué
        echo Vérifiez que l'appareil Android est connecté
    )
) else (
    echo Installation annulée
)

echo.
echo ===================================
echo Terminé!
echo ===================================
pause
