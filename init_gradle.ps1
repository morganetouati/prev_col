Write-Host "======================================"
Write-Host "Initialisation Gradle Wrapper"
Write-Host "======================================"
Write-Host ""

# Paths
$wrapperDir = $env:USERPROFILE + "\.gradle\wrapper"
$jarUrl = "https://repo.maven.apache.org/maven2/org/gradle/gradle-wrapper/7.6/gradle-wrapper-7.6.jar"
$jarPath = $wrapperDir + "\gradle-wrapper-7.6.jar"

# Create directory
if (-not (Test-Path $wrapperDir)) {
    Write-Host "Création du répertoire: $wrapperDir"
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

# Download JAR if it doesn't exist
if (-not (Test-Path $jarPath)) {
    Write-Host "Téléchargement du wrapper JAR..."
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    
    try {
        Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath -TimeoutSec 60
        Write-Host "✓ Téléchargement réussi!"
    } catch {
        Write-Host "✗ Erreur du téléchargement: $_"
        Write-Host "Tentative de téléchargement avec curl..."
        cmd /c curl -o "$jarPath" $jarUrl
        
        if (-not (Test-Path $jarPath)) {
            Write-Host "✗ ERREUR: Impossible de télécharger le wrapper"
            Write-Host ""
            Write-Host "Solution manuelle:"
            Write-Host "1. Téléchargez: $jarUrl"
            Write-Host "2. Sauvegardez dans: $jarPath"
            exit 1
        } else {
            Write-Host "✓ Téléchargement via curl réussi!"
        }
    }
} else {
    Write-Host "✓ JAR wrapper existe déjà"
}

Write-Host ""
Write-Host "======================================"
Write-Host "Compilation de l'application..."
Write-Host "======================================"
Write-Host ""

cd "C:\Users\morgane\Desktop\prev_col"
cmd /c gradlew.bat assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "======================================"
    Write-Host "✓ SUCCÈS! Compilation terminée!"
    Write-Host "======================================"
    Write-Host ""
    Write-Host "APK créé à:"
    Write-Host "app\build\outputs\apk\debug\app-debug.apk"
} else {
    Write-Host ""
    Write-Host "✗ Erreur lors de la compilation"
 }
