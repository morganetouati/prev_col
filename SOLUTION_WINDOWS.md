# PrevCol sur Windows - Guide de Compilation

## ⚠️ Problème gradlew sur Windows

Le wrapper Gradle (`gradlew.bat`) peut avoir des problèmes d'initialisation sur Windows. Voici les solutions.

---

## ✅ SOLUTION 1: Android Studio (RECOMMANDÉE - Plus Simple)

### Avantages
- ✓ Installation complète et automatique
- ✓ Interface graphique pour tester l'app
- ✓ Meilleur support pour Windows  
- ✓ Déploiement direct sur téléphone
- ✓ Débogage intégré

### Procédure (5 min)

1. **Installez Android Studio**
   - Téléchargez: https://developer.android.com/studio
   - Exécutez l'installeur
   - Installez Java JDK 11+ (proposé pendant l'installation)

2. **Ouvrez le projet**
   ```
   File > Open > C:\Users\morgane\Desktop\prev_col
   ```

3. **Attendez la synchro**
   - Android Studio télécharge automatiquement Gradle, SDK, etc.
   - Cela peut prendre 5-10 minutes la première fois
   - Regardez les messages en bas : "Gradle Build Running..."

4. **Compilez l'APK**
   ```
   Build > Build Bundle(s)/APK(s) > Build APK(s)
   ```

5. **Testez**
   - Connectez un téléphone Android en USB
   - Cliquez "Run" (le bouton ▶️ vert)
   - Ou trouvez l'APK généré dans:
     ```
     app\build\outputs\apk\debug\app-debug.apk
     ```

---

## ✅ SOLUTION 2: Gradle Local (Alternative Ligne de Commande)

### Si vous voulez vraiment utiliser gradle en command-line

#### a) Installez Gradle
1. Téléchargez depuis: https://gradle.org/releases/
2. Prenez version **7.6** (compatible avec le projet)
3. Extrayez le ZIP dans `C:\gradle` (ou votre dossier préféré)

#### b) Ajoutez Gradle au PATH
1. Ouvrez les variables d'environnement:
   ```
   Windows + R > "sysdm.cpl" > Variables d'environnement
   ```
2. Variables système > Path > Éditer
3. Ajouter: `C:\gradle\gradle-7.6\bin`
4. Redémarrez PowerShell/CMD

#### c) Compilez
```bash
cd C:\Users\morgane\Desktop\prev_col
gradle assembleDebug
```

---

## ✅ SOLUTION 3: Docker (Avancé)

Si vous avez Docker installé:

```bash
docker run --rm -v C:\Users\morgane\Desktop\prev_col:/app gradle:7.6 gradle -p /app assembleDebug
```

---

## 🎯 Vérification du Résultat

Peu importe la solution, l'APK doit être généré ici:
```
c:\Users\morgane\Desktop\prev_col\app\build\outputs\apk\debug\app-debug.apk
```

---

## 📱 Installation sur un Téléphone

### Via Android Studio
1. Connectez votre téléphone en USB
2. Activez le débogage USB:
   - Paramètres > À propos du téléphone > Appuyez 7x sur "Numéro de build"
   - Développement > Débogage USB > Cocher
3. Cliquez le bouton ▶️ (Run) dans Android Studio

### Via ADB (Ligne de Commande)
```bash
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Manuellement
1. Transférez l'APK sur le téléphone
2. Ouvrez le fichier
3. Android demande la permission d'installer
4. Acceptez

---

## ❓ Dépannage

### "Java n'est pas trouvé"
```bash
java -version
```
- Si erreur → Installez JDK 11+ depuis https://adoptium.net/

### "Gradle n'est pas reconnu"
- Vérifiez que vous avez redémarré le terminal après modification du PATH
- Tapez `gradle -v` pour confirmer

### "Impossible de télécharger les dépendances"
- Vérifiez votre connexion Internet
- Essayez via proxy si derrière un pare-feu

### "Build échoue avec erreur de permission"
- Assurez-vous que le dossier est accessible
- Essayez de l'exécuter en tant qu'administrateur

---

## 🚀 Test Ultra-Rapide (Mode Démo)

Une fois installée, vous pouvez immédiatement tester le **Mode Démo** (aucune caméra requise):

1. Ouvrez l'app
2. Cliquez "Mode Démo"
3. Voyez la distance simulée
4. Testez vibrations et son

---

## 📚 Ressources

- Android Studio: https://developer.android.com/studio
- Java JDK: https://adoptium.net/
- Gradle: https://gradle.org/
- Android SDK Setup: https://developer.android.com/studio/install

---

**Recommandation**: Utilisez **Android Studio** pour votre première compilation. C'est plus facile et vous aurez accès à bien plus d'outils!
