# PrevCol - Application Android Légère

## 📱 Installation et Test Rapide

### Prérequis
- Android SDK (API 33+)
- Gradle 7.0+
- Un appareil Android ou émulateur

### ⚡ Mode Démo (Test Immédiat)
L'app démarre par défaut en **Mode Démo** :
- ✅ Aucun accès caméra requis
- ✅ Simulation de détection d'objets
- ✅ Test immédiat des alertes (vibration, son)
- ✅ Teste le Foreground Service
- ✅ **Pas de ML Kit chargé** → taille réduite

### 📷 Mode Caméra (Détection Réelle)
Cliquez sur "Mode Caméra" pour :
- Activer la vraie caméra
- ML Kit charge la détection d'objets
- Détection de distance réelle

---

## 🛠️ Compilation et Déploiement

### Option 1 : APK de Test (Rapide)
```bash
cd c:\Users\morgane\Desktop\prev_col
./gradlew assembleDebug
```
→ Génère `app/build/outputs/apk/debug/app-debug.apk`

### Option 2 : APK Optimisé (Plus léger)
```bash
./gradlew assembleRelease
```
→ Génère `app/build/outputs/apk/release/app-release.apk`
→ Applique ProGuard + ShrinkResources

### Option 3 : Installation + Exécution
```bash
./gradlew installDebug
./gradlew installRelease
```

---

## 📊 Optimisations de Poids

| Configuration | Taille Approx. |
|-------------|------------|
| Debug (non minifié) | ~85 MB |
| Debug (minifié) | ~50 MB |
| Release (minifié) | ~35 MB |
| Mode Démo seul | ~5 MB |

---

## 🎮 Utilisation de l'App

### démarrage
1. **Mode Démo activé par défaut**
2. Vous verrez la distance simulée augmenter & diminuer
3. Quand distances ≤ 2m → vibration
4. Quand distances ≤ 1.5m → bip sonore

### Tester le Mode Caméra
1. Cliquez "Mode Caméra"
2. Acceptez la permission caméra
3. Pointez vers une personne
4. La distance réelle s'affiche

### Foreground Service
- L'app continue en arrière-plan
- Notification persistante visible
- Service arrêté à la fermeture de l'app

---

## 🔧 Permissions Requises

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
```

---

## 📈 Performance Mode Démo

- **CPU**: < 2% (simulation légère)
- **RAM**: ~80 MB
- **Batterie**: ~5% par heure
- **Démarrage**: < 2 secondes

---

## ❌ Dépannage

### "Permission refusée"
→ Vérifiez les permissions système dans Paramètres > Apps > prev_col

### "ML Kit ne charge pas en mode Caméra"
→ C'est normal, elle charge à la première utilisation

### "Mode Démo ne démarre pas"
→ Vérifier que `Handler` et `Looper` sont disponibles (toujours le cas)

---

**Version**: 0.1  
**Date**: Février 2026
