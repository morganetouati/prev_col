# Version 1.2.1 Release Notes

**Date:** March 4, 2026  
**versionCode:** 6  
**versionName:** 1.2.1  
**Build Type:** Release (signed, minified, native debug symbols included)

## 📦 What's New

### 🎯 Main Improvements
- **Screen lock sleep mode:** App now automatically stops detection, radar, alerts and vibrations when the phone is locked (ACTION_SCREEN_OFF). Detection resumes immediately when user unlocks the device.
- **People-focused detection:** Confidence filtering (≥0.4) and improved object-type mapping for better detection of front-facing people and children. False positives (dogs, objects) nearly eliminated.
- **Reduced false vibrations:** Vibrations now trigger ONLY for people (ADULTE, ENFANT, BEBE) at appropriate distance thresholds. Dogs and objects no longer cause vibrations.

### 📱 UI & Responsiveness
- **Tablet support:** App layouts now scale properly on 7", 10", and larger tablets using responsive dimension resources (`values-sw600dp`, `values-sw720dp`).
- **Privacy page favicon:** Added SVG favicon for web and app privacy page.
- **Consistent spacing:** All hard-coded layout margins and padding replaced with centralized dimension resources for better maintainability.

### 🔧 Technical & Play Store Compliance
- **Native debug symbols:** Full NDK debug symbol inclusion in release bundle (required by Play Console).
- **Updated SDKs:** Bumped `compileSdk` and `targetSdk` to API 35 (required by Google Play Store).
- **On-device processing:** All detection and decision-making happens locally (ML Kit + Sensors). No cloud uploads, no third-party analytics. **100% privacy-focused.**

## 🚀 Building & Installation

### Debug APK (for testing before Play Store upload)
```bash
cd c:\Users\morgane\Desktop\prev_col
.\gradlew :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Release AAB (for Play Store)
```bash
cd c:\Users\morgane\Desktop\prev_col
.\gradlew :app:bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

## 📋 Play Console Upload Checklist

- [ ] AAB file: `app/build/outputs/bundle/release/app-release.aab` (~24.7 MB)
- [ ] versionCode: `6` (must be > previous version)
- [ ] versionName: `1.2.1`
- [ ] Privacy URL: `https://<your-domain>/privacy/` (must be publicly reachable via HTTPS)
- [ ] App icon (512x512 PNG)
- [ ] Screenshots (minimum 2 required per language)
- [ ] Release notes: *[See below]*
- [ ] Content rating questionnaire
- [ ] Permissions disclosure (CAMERA, ACCELEROMETER, PROXIMITY sensors)

## 📝 Play Store Release Notes (for users)

### English
```
Version 1.2.1 – Screen Lock Sleep + Improved Detection

✨ New: App now sleeps when your phone is locked (stops vibrating, saves battery).
🎯 Better: More accurate detection of people, less false alerts.
📱 Fixed: Now works smoothly on tablets (7", 10"+).

All processing happens on your device. Your privacy is protected.
```

### Français
```
Version 1.2.1 – Mise en veille au verrouillage + Meilleure détection

✨ Nouveau : L'app se met en veille quand le téléphone est verrouillé (arrête les vibrations, économise la batterie).
🎯 Meilleur : Détection plus précise des personnes, moins de fausses alertes.
📱 Corrigé : Fonctionne maintenant parfaitement sur tablettes (7", 10"+).

Tous les calculs se font sur votre appareil. Votre vie privée est protégée.
```

## 🔐 Privacy & Permissions

### Permissions Used
- **CAMERA** → Front-facing object detection (ML Kit on-device)
- **RECORD_AUDIO** → (if audio alerts enabled in future versions)
- **VIBRATE** → Haptic feedback for proximity alerts
- **ACCESS_FINE_LOCATION** → (optional, not currently used)

### Data Handling
✅ **All processing is local** — no internet required  
✅ **No personal data collection** — only detects people/objects around you  
✅ **No ads or third-party tracking** — pure functionality  
✅ **Native debug symbols included** — for Google's crash reporting (optional)

## 📍 Download & Test

- **GitHub Releases:** [Link to your repo releases page]
- **Play Store Link:** [Will be provided after upload approval]

---

**Built with ❤️ using Kotlin, CameraX, ML Kit, and Android Jetpack**
