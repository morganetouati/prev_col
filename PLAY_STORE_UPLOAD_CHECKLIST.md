# Play Store Upload Checklist – v1.2.1

**Date:** March 4, 2026  
**Status:** Ready to upload

---

## ✅ Artifact Ready

| Item | Path | Status |
|------|------|--------|
| **AAB (signed + minified)** | `app/build/outputs/bundle/release/app-release.aab` | ✅ Ready (~24.7 MB) |
| **versionCode** | See `app/build.gradle` line 27 | ✅ Set to 6 |
| **versionName** | See `app/build.gradle` line 28 | ✅ Set to 1.2.1 |
| **Signing** | Configured via `keystore.properties` | ✅ Release keystore ready |
| **Native Symbols** | Included (R8 + NDK debug symbols) | ✅ Full symbols |

---

## 📱 Before Uploading to Play Console

### Must Do:
1. **Publish privacy page** (required by Play Console)
   - Folder: `docs/privacy/` (contains `index.html` + `favicon.svg`)
   - Options to host:
     - **GitHub Pages** (free): See `docs/privacy/README_GH_PAGES.md`
     - **Your own server** (if available)
   - **Future Privacy URL format:** `https://<your-domain>/privacy/index.html` (or just `/privacy/`)
   - **Test:** Make sure it's publicly accessible via HTTPS before entering URL in Play Console

2. **Update Privacy URL in Play Console**
   - Play Store → App → Store settings → App privacy
   - Paste the public HTTPS URL from step 1
   - Save

### Recommended:
1. **App icon** (512×512 PNG)
   - Currently: using emoji (👁️) in layouts
   - Better: Create a proper raster icon from `assets/icons/favicon.svg`
   - Command (if Inkscape installed):
     ```powershell
     .\tools\generate_icons.ps1 -Source .\assets\icons\favicon.svg -OutDir .\assets\icons\output
     ```
   - Use the 512px PNG as app icon in Play Console

2. **Screenshots** (minimum 2 per language, max 8)
   - Recommended sizes: 1080×1920 (phone), 2560×1600 (tablet)
   - Suggested content:
     - Screenshot 1: Main radar view with detection
     - Screenshot 2: Stats/badges screen
     - Screenshot 3: Privacy info screen
   - Add captions in English + French

3. **Short description** (80 chars max)
   - Example: "Privacy-first proximity awareness using on-device ML detection"

4. **Full description** (4000 chars max)
   - Use content from `RELEASE_NOTES_v1.2.1.md`

5. **Release notes** (for this version)
   - Use pre-written notes from `RELEASE_NOTES_v1.2.1.md`

---

## 🚀 Upload Steps (in Google Play Console)

1. Go to: https://play.google.com/console → Your App → Releases
2. Click **"Create new release"** (Production or Staging first)
3. Upload AAB file: `app/build/outputs/bundle/release/app-release.aab`
4. Google Play will analyze dependencies, permissions, crashes
5. Wait for **"Passed internal testing"** ✅
6. Add release notes (English + French)
7. Review playable ads, content rating, etc.
8. Click **"Review release"** → **"Start rollout to Production"**
9. APKs are generated automatically from AAB for all devices

---

## 📋 Info to Have Ready

### App Details (if editing for first time)
- **App name:** Regards au monde
- **Short description:** Privacy-first proximity awareness with on-device ML
- **Category:** Utilities or Safety
- **Content rating:** Fill questionnaire (simple app, no sensitive content)
- **Target audience:** General (13+)

### Contact & Support
- **Support email:** [Your email or contact]
- **Website:** [If you have one, or privacy page URL]
- **Privacy policy:** `docs/privacy/index.html` (URL from step 1)

### Permissions to Disclose
- ✅ **CAMERA** – People detection (no photos stored)
- ✅ **VIBRATE** – Haptic alerts
- ✅ **ACCELEROMETER** – Motion detection
- ✅ **PROXIMITY** – Pocket detection
- ❌ **No Location** – Off by default
- ❌ **No Microphone** – Off by default
- ❌ **No Ads** – No third-party SDKs

---

## 🔗 Quick Links

- [Play Console](https://play.google.com/console)
- [Google Play Policies](https://play.google.com/about/developer-content-policy/)
- [App Bundle Documentation](https://developer.android.com/guide/app-bundle)
- [Privacy Policy Guidance](https://support.google.com/googleplay/android-developer/answer/10787469)

---

## 📞 Support

If you encounter upload errors:
- **versionCode already used:** Increment `versionCode` in `app/build.gradle`
- **Unsigned/Invalid signing:** Verify `keystore.properties` path and passwords
- **Missing privacy URL:** Must be HTTPS and publicly accessible
- **Targeting issue:** Ensure `targetSdk 35` in `app/build.gradle` (for 2026+ Play Store requirement)

---

**Build Date:** March 4, 2026  
**Ready to ship! 🚀**
