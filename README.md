# 👁️ Regards au Monde

**Application Android pour rester conscient de son environnement** — Alerte les utilisateurs de smartphone qui marchent en regardant leur écran lorsque des piétons, enfants ou animaux s'approchent.

---

## ✨ Fonctionnalités

### 🎯 Détection Intelligente
- **Détection ML Kit on-device** — Aucune donnée ne quitte votre téléphone
- **6 types d'objets** — Adultes, enfants, bébés, petits/moyens/grands chiens
- **Estimation de distance** corrigée par perspective (0.3m - 15m)
- **Mode simulation** automatique si caméra indisponible

### 📡 Radar HUD Animé
- **Overlay flottant** visible sur tout l'écran
- **Scan animé** style radar militaire (30 fps)
- **Traînées de mouvement** pour suivre les déplacements
- **Flèches directionnelles** ⬆️⬇️⬅️➡️ selon le sens de déplacement

### 🔔 Alertes Multimodales
- **Vibrations personnalisées** selon le type d'objet détecté
- **Notifications HeadsUp** avec couleur selon la distance
- **Son adaptatif** proportionnel à la proximité

### 🎮 Gamification
- **5 niveaux** : Débutant → Intermédiaire → Confirmé → Expert → Maître
- **12 badges** à débloquer (Premier Regard, Centurion, Éclair, etc.)
- **Streak journalier** avec récompenses

### ⚡ Optimisé pour la Batterie
- Traitement ML Kit toutes les 300ms (pas en continu)
- Détection de mouvement : s'active uniquement en marchant
- Caméra basse résolution (640×480)
- Mode veille automatique quand le téléphone est posé

### 🌍 Multilingue
- **7 langues** : 🇫🇷 Français, 🇬🇧 English, 🇪🇸 Español, 🇩🇪 Deutsch, 🇮🇹 Italiano, 🇮🇱 עברית, 🇸🇦 العربية
- Bouton de sélection de langue dans l'app (drapeau)
- Support RTL (droite à gauche) pour l'hébreu et l'arabe

### ♿ Accessibilité
- **TalkBack** : 100% compatible avec le lecteur d'écran Android
- **Non-voyants** : Alertes vocales + vibrations distinctives par type de danger
- **Sourds** : Radar visuel + vibrations + notifications (pas d'alerte uniquement sonore)
- **Daltoniens** : Textes + emojis + formes en complément des couleurs

---

## 📲 Installation

### Depuis le Play Store
> 🚀 Bientôt disponible sur Google Play Store

### Depuis l'APK (Sideload)
1. Téléchargez l'APK depuis la [page Releases](../../releases)
2. Activez "Sources inconnues" dans les paramètres Android
3. Installez l'APK
4. Ouvrez l'app et acceptez les permissions

---

## 🚀 Utilisation

### Premier lancement
1. **Acceptez la politique de confidentialité**
2. **Autorisez la superposition d'écran** (pour le radar HUD)
3. **Autorisez la caméra** (optionnel, pour détection réelle)

### Activer la surveillance
1. **Déroulez les Quick Settings** (balayez depuis le haut de l'écran)
2. **Appuyez sur la tuile "Regards au monde"** 👁️
3. Le radar apparaît quand vous marchez

### Comprendre les alertes

| Couleur | Distance | Signification |
|---------|----------|---------------|
| 🟢 Vert | > 2.5m | RAS |
| 🟠 Orange | 1.5m - 2.5m | Attention |
| 🔴 Rouge | < 1.5m | Danger proche |

---

## 🔒 Confidentialité

**Aucune image caméra n'est collectée ni transmise par l'app.**

- ✅ Traitement ML Kit 100% on-device
- ✅ Pas de compte utilisateur requis
- ✅ Pas d'analytics propriétaire côté application
- ℹ️ SDK publicitaire AdMob actif (consultez la section Data Safety Play Console)
- ✅ Consentement pub géré via UMP (RGPD/EEA) avant chargement des annonces
- ✅ Statistiques stockées uniquement en local (SharedPreferences)
- ✅ Caméra utilisée uniquement pour la détection en temps réel

---

## 🔧 Configuration Technique

| Paramètre | Valeur |
|-----------|--------|
| Min SDK | 23 (Android 6.0 Marshmallow) |
| Target SDK | 34 (Android 14) |
| Kotlin | 2.1.10 |
| CameraX | 1.4.0 |
| ML Kit Object Detection | 17.0.2 |
| Taille APK (release) | ~25 MB |

### Permissions requises

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

---

## 🛠️ Développement

### Prérequis
- Android Studio Hedgehog (2023.1.1) ou supérieur
- JDK 11+
- Gradle 8.12

### Build Debug
```powershell
cd prev_col
.\gradlew.bat assembleDebug
# APK → app\build\outputs\apk\debug\app-debug.apk
```

### Build Release (signé)
```powershell
.\gradlew.bat assembleRelease
# APK → app\build\outputs\apk\release\app-release.apk
```

### Build Bundle Play Store (AAB)
```powershell
.\gradlew.bat :app:verifyPlayReleaseConfig
.\gradlew.bat :app:bundleRelease
# AAB → app\build\outputs\bundle\release\app-release.aab
```

### Configuration AdMob (obligatoire pour release)
- Définissez `ADMOB_APP_ID` et `ADMOB_BANNER_ID` dans `gradle.properties`
- Les IDs de test Google (`ca-app-pub-3940256099942544/...`) bloquent désormais la release stricte

### Tests
```powershell
# Tests unitaires
.\gradlew.bat test

# Tests instrumentés (émulateur requis)
.\gradlew.bat connectedAndroidTest
```

---

## 📂 Structure du projet

```
app/src/main/
├── java/com/example/prevcol/
│   ├── MainActivity.kt           # Dashboard statistiques
│   ├── PrivacyActivity.kt        # Écran consentement RGPD
│   ├── DetectionService.kt       # Service foreground principal
│   ├── DetectionTileService.kt   # Quick Settings Tile
│   ├── CameraDetector.kt         # ML Kit + CameraX
│   ├── DemoDetectionSimulator.kt # Simulation (fallback)
│   ├── RadarOverlay.kt           # HUD radar animé
│   ├── GameStats.kt              # Gamification (points, badges)
│   └── StatsActivity.kt          # Statistiques détaillées
└── res/
    ├── layout/                   # XML layouts
    └── values/                   # Strings, styles, colors
```

---

## 📋 Changelog

### v1.1.0 (Février 2026)
- ✨ Radar HUD animé avec scan et traînées
- ✨ 6 types d'objets détectables
- ✨ Vibrations personnalisées par type
- ✨ 12 badges de gamification
- ✨ Streak journalier
- ⚡ Optimisations batterie (détection mouvement)
- 🔧 Support Android 14 (API 34)
- 🔧 Libs natives 16 KB alignées (Play Store)

### v1.0.0 (Janvier 2026)
- 🎉 Version initiale
- Détection ML Kit basique
- Notifications et vibrations

---

## 📜 Licence

Ce projet est sous licence MIT. Voir [LICENSE](LICENSE) pour plus de détails.

---

## 🤝 Contribuer

Les contributions sont les bienvenues ! 

1. Fork le projet
2. Créez une branche (`git checkout -b feature/amazing-feature`)
3. Commit (`git commit -m 'Add amazing feature'`)
4. Push (`git push origin feature/amazing-feature`)
5. Ouvrez une Pull Request

---

## 📧 Contact

**Projet** : Regards au Monde  
**Issue Tracker** : [GitHub Issues](../../issues)

---

<p align="center">
  Fait avec ❤️ pour la sécurité des piétons distraits
</p>
