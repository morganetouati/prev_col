@echo off
REM ===========================================
REM Script de génération du keystore de release
REM pour Regards au monde
REM ===========================================

echo.
echo ========================================
echo  Generation du Keystore de Release
echo  Regards au monde - Play Store
echo ========================================
echo.

REM Chemin du keystore
set KEYSTORE_PATH=keystore\release.keystore
set KEYSTORE_DIR=keystore

REM Créer le dossier keystore s'il n'existe pas
if not exist %KEYSTORE_DIR% mkdir %KEYSTORE_DIR%

REM Vérifier si le keystore existe déjà
if exist %KEYSTORE_PATH% (
    echo [!] ATTENTION: Le keystore existe deja!
    echo     %KEYSTORE_PATH%
    echo.
    echo     Supprimer le fichier existant pour en creer un nouveau?
    pause
    exit /b 1
)

echo [i] Le keystore sera cree dans: %KEYSTORE_PATH%
echo.
echo [!] IMPORTANT: Notez bien le mot de passe que vous allez creer!
echo     Si vous le perdez, vous ne pourrez plus mettre a jour l'app!
echo.
pause

REM Générer le keystore avec keytool
keytool -genkeypair ^
    -v ^
    -keystore %KEYSTORE_PATH% ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity 10000 ^
    -alias regardsaumonde ^
    -dname "CN=Regards au monde, OU=Mobile, O=Regards au monde, L=Paris, ST=IDF, C=FR"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [X] Erreur lors de la creation du keystore!
    exit /b 1
)

echo.
echo ========================================
echo  Keystore cree avec succes!
echo ========================================
echo.
echo Fichier: %KEYSTORE_PATH%
echo Alias: regardsaumonde
echo.
echo [!] CONSERVEZ PRECIEUSEMENT:
echo     - Le fichier keystore
echo     - Le mot de passe du keystore
echo     - Le mot de passe de la cle
echo.
echo [i] Prochaine etape: Configurez local.properties
echo     avec les informations du keystore.
echo.
pause
