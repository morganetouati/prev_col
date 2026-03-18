# 🚀 Play Store Release v1.3.2 – Ready to Upload

**Status:** ✅ READY  
**Date:** March 13, 2026  
**versionCode:** 12  
**versionName:** 1.3.2

---

## 📦 Download & Upload

### Release AAB (Signed)
```
C:\Users\morgane\Desktop\prev_col\app\build\outputs\bundle\release\app-release.aab
Size: ~24.7 MB
Signing: Release keystore (automatique)
Symbols: Full native debug symbols included ✅
R8: Minified & optimized ✅
```

**Upload this file to Play Console → Releases → Create new release**

---

## 🔑 Essential Information

### App Metadata (copy-paste ready)

| Field | Value |
|-------|-------|
| **App Name** | Regards au monde |
| **Short Description** | Privacy-first proximity awareness using on-device ML |
| **Full Description** | See `RELEASE_NOTES_v1.2.1.md` |
| **Changelog (v1.3.2)** | Camera sleep optimization + Onboarding intro slide + UI visibility fixes |
| **Category** | Utilities / Safety (your choice) |
| **Content Rating** | General (no mature content) |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 35 (Android 15) ✅ Play Store requirement |
| **Permissions** | CAMERA, VIBRATE, ACCELEROMETER, PROXIMITY (see folder: `docs/privacy/`) |

---

## 🔐 Privacy Page (REQUIRED)

### Current Status
- ✅ Privacy page created: `docs/privacy/index.html`
- ✅ Favicon added: `docs/privacy/favicon.svg`
- ⏳ **TODO:** Host publicly & provide URL to Play Console

### How to Publish Privacy Page

**Option A: GitHub Pages (FREE & EASY) – Recommended**
```powershell
# 1. Create a GitHub repo and push your project
git clone https://github.com/<username>/<your-repo>.git
cd <your-repo>
git add docs/privacy
git commit -m "Add privacy page"
git push origin main

# 2. Go to GitHub repo Settings → Pages
#    Source: main branch, /docs folder
#    ✅ Your privacy page will be at:
#    https://<username>.github.io/<repo>/privacy/

# 3. Copy this URL to Play Console
```

**Option B: Your Own Server**
```
Upload the contents of docs/privacy/ to your server
Example: https://yoursite.com/privacy/index.html
Copy this URL to Play Console
```

### Verify Privacy Page Works
- Open in browser: `https://<your-privacy-url>/index.html` (or just `/`)
- Must be HTTPS (not HTTP)
- Must be public (no login required)
- Must be reachable from anywhere

---

## 📝 Release Notes (for Play Store)

### English Version
```
Version 1.2.1 – Screen Lock Sleep + Improved Detection

✨ New: App now sleeps when your phone is locked (stops vibrating, saves battery).
🎯 Better: More accurate detection of people, less false alerts.
📱 Fixed: Now works smoothly on tablets (7", 10"+).

All processing happens on your device. Your privacy is protected.
```

### French Version (Français)
```
Version 1.2.1 – Mise en veille au verrouillage + Meilleure détection

✨ Nouveau : L'app se met en veille quand le téléphone est verrouillé (arrête les vibrations, économise la batterie).
🎯 Meilleur : Détection plus précise des personnes, moins de fausses alertes.
📱 Corrigé : Fonctionne maintenant parfaitement sur tablettes (7", 10"+).

Tous les traitements se font sur votre appareil. Votre vie privée est protégée.
```

---

## 📸 Optional: App Icon (Recommended)

Generate PNG icons from SVG:
```powershell
cd C:\Users\morgane\Desktop\prev_col
.\tools\generate_icons.ps1 -Source .\assets\icons\favicon.svg -OutDir .\assets\icons\output
```

Use the **512×512 PNG** as your app icon in Play Console.

---

## ✅ Final Checklist Before Upload

- [ ] Privacy page hosted & publicly accessible via HTTPS
- [ ] Privacy URL copied & ready to paste in Play Console
- [ ] AAB file verified: `app/build/outputs/bundle/release/app-release.aab` exists
- [ ] versionCode 6 is higher than previous upload (versionCode 5)
- [ ] Signing key & password ready (stored securely in `keystore.properties`)
- [ ] Release notes written (English + French)
- [ ] App icon prepared (512×512 PNG, optional but recommended)
- [ ] Screenshots captured (2+ per language, optional)
- [ ] Content rating questionnaire ready to fill
- [ ] Support email & contact info ready

---

## 🎯 Next Immediate Steps

1. **Publish privacy page to GitHub Pages** (or your server)
   - Follow instructions under "Privacy Page" section above
   - Get the public HTTPS URL

2. **Login to Play Console** → https://play.google.com/console

3. **Create new release**
   - Click: Releases → Create new release
   - Upload AAB: `app/build/outputs/bundle/release/app-release.aab`
   - Wait for analysis (~2-5 min)

4. **Fill in release details**
   - Add release notes (English + French)
   - Add privacy URL
   - Add app description & short description
   - Upload screenshots (if available)

5. **Review & Publish**
   - Click "Review release"
   - Click "Start rollout to Production" (or Staging first for testing)
   - Your app will go live in ~2-3 hours

---

## 📞 If Something Goes Wrong

| Error | Solution |
|-------|----------|
| versionCode already used | Increment versionCode in `app/build.gradle`, rebuild AAB |
| Signing error | Check `keystore.properties` path & passwords |
| Targeting error | Ensure `targetSdk 35` in `app/build.gradle` |
| Missing privacy policy | Publish `docs/privacy/` and provide HTTPS URL |
| No native symbols | Symbols already included in AAB ✅ |

---

**💚 Everything is ready. Go upload your app to the Play Store now!**

```
AAB path:  C:\Users\morgane\Desktop\prev_col\app\build\outputs\bundle\release\app-release.aab
versionCode: 6
versionName: 1.2.1
Status: ✅ READY
```
