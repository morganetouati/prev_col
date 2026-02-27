# 📘 Guide d'Installation et Configuration — Regards au Monde

Ce guide vous accompagne dans l'installation et la configuration de l'application "Regards au Monde" sur votre appareil Android.

---

## 📋 Table des matières

1. [Prérequis](#-prérequis)
2. [Installation](#-installation)
3. [Premier lancement](#-premier-lancement)
4. [Configuration des permissions](#-configuration-des-permissions)
5. [Changer la langue](#-changer-la-langue)
6. [Accessibilité](#-accessibilité)
7. [Utilisation quotidienne](#-utilisation-quotidienne)
8. [Personnalisation](#-personnalisation)
9. [Dépannage](#-dépannage)
10. [FAQ](#-faq)

---

## 📱 Prérequis

### Configuration minimale
- **Android** : Version 6.0 (Marshmallow) ou supérieure
- **Espace disque** : 50 MB disponibles
- **RAM** : 2 GB minimum recommandé

### Recommandé pour une expérience optimale
- **Android** : Version 10.0 ou supérieure
- **Processeur** : Snapdragon 600+ ou équivalent
- **Caméra** : Arrière, au moins 5 MP

---

## 📥 Installation

### Méthode 1 : Google Play Store (Recommandé)
1. Ouvrez le **Play Store** sur votre appareil
2. Recherchez "**Regards au Monde**"
3. Appuyez sur **Installer**
4. Attendez la fin du téléchargement

### Méthode 2 : Installation manuelle (APK)

> ⚠️ Cette méthode nécessite d'autoriser les sources inconnues.

1. **Téléchargez l'APK** depuis le site officiel ou GitHub
2. **Activez les sources inconnues** :
   - Paramètres → Sécurité → Sources inconnues → Activer
   - Ou : Paramètres → Applications → Menu → Accès spécial → Installation d'applis inconnues
3. **Ouvrez le fichier APK** téléchargé
4. **Appuyez sur Installer**
5. Une fois installé, **désactivez les sources inconnues** (recommandé)

---

## 🚀 Premier lancement

### Étape 1 : Écran de confidentialité

Au premier lancement, un écran de confidentialité s'affiche :

```
┌─────────────────────────────────────┐
│     🔒 Votre vie privée compte      │
│                                     │
│  • Traitement 100% local            │
│  • Aucune donnée transmise          │
│  • Pas de compte requis             │
│                                     │
│  [ Comprendre et continuer ]        │
└─────────────────────────────────────┘
```

**Action**: Appuyez sur "Comprendre et continuer" après avoir lu les informations.

### Étape 2 : Permissions

L'application demande deux permissions essentielles :

#### 1. Superposition d'écran (Obligatoire pour le radar)
- **Pourquoi** : Afficher le radar HUD par-dessus les autres applications
- **Comment autoriser** :
  1. Appuyez sur "Autoriser superposition"
  2. Dans les paramètres système, trouvez "Regards au monde"
  3. Activez "Autoriser l'affichage par-dessus d'autres applications"
  4. Revenez à l'application

#### 2. Caméra (Optionnel mais recommandé)
- **Pourquoi** : Détecter les personnes/animaux en temps réel
- **Sans caméra** : L'app fonctionne en mode simulation (pour tests)
- **Comment autoriser** : Appuyez simplement sur "Autoriser" dans la popup

---

## ⚙️ Configuration des permissions

### Vérifier les permissions accordées

**Chemin** : Paramètres → Applications → Regards au monde → Autorisations

| Permission | Usage | Obligatoire |
|------------|-------|-------------|
| Caméra | Détection temps réel | Non* |
| Superposition | Affichage radar HUD | Oui |

*Sans caméra, l'app utilise le mode simulation.

### Révoquer des permissions

Vous pouvez à tout moment désactiver une permission :
1. Paramètres → Applications → Regards au monde
2. Autorisations
3. Désactivez la permission souhaitée

---

## 🌍 Changer la langue

L'application est disponible en **7 langues** :

| Drapeau | Langue |
|---------|--------|
| 🇫🇷 | Français (par défaut) |
| 🇬🇧 | English |
| 🇪🇸 | Español |
| 🇩🇪 | Deutsch |
| 🇮🇹 | Italiano |
| 🇮🇱 | עברית (Hébreu) |
| 🇸🇦 | العربية (Arabe) |

### Comment changer la langue

1. Ouvrez l'application
2. Appuyez sur le **bouton drapeau** 🇫🇷 en haut à droite
3. Sélectionnez votre langue préférée
4. L'application redémarre automatiquement dans la nouvelle langue

> 💡 **Note** : L'hébreu et l'arabe s'affichent de droite à gauche (RTL).

---

## ♿ Accessibilité

L'application est conçue pour être accessible à tous.

### Pour les personnes non-voyantes (TalkBack)

L'application est **100% compatible avec TalkBack**, le lecteur d'écran d'Android.

**Comment utiliser l'app avec TalkBack :**

1. **Navigation** : Tous les éléments sont décrits vocalement
2. **Alertes vocales** : Les alertes de proximité déclenchent :
   - Une **annonce TalkBack** décrivant le type d'objet et la distance
   - Une **vibration distinctive** selon le niveau de danger
   - Une **notification sonore** pour les alertes critiques

3. **Vibrations par type de danger** :
   - 🟠 Alerte douce : 2 vibrations courtes
   - 🔴 Danger : 3 vibrations longues + son
   - ⚡ Approche rapide : vibration continue 500ms

> 💡 **Conseil** : Utilisez des écouteurs Bluetooth pour entendre les alertes vocales sans déranger autour de vous.

### Pour les personnes sourdes et malentendantes

L'application n'utilise **aucune alerte exclusivement sonore**. Tout est doublé par :

1. **Vibrations distinctives** selon le niveau d'alerte
2. **Radar visuel HUD** avec :
   - Points colorés (rouge, orange, jaune) selon la proximité
   - Flèches de direction montrant le mouvement
   - Animation de scan visible

3. **Notifications visuelles** dans la barre de notification

### Pour les personnes daltoniennes

L'interface utilise un **système multi-modal** :

1. **Couleurs + formes** : Les alertes ne sont jamais uniquement codées par couleur
2. **Emojis distinctifs** : 🔴🟠🟡 accompagnent les indicateurs de couleur
3. **Textes explicites** : "DANGER", "ALERTE", "RAS" sont toujours écrits
4. **Contraste élevé** : Interface lisible avec fort contraste

### Résumé des fonctionnalités d'accessibilité

| Handicap | Solution |
|----------|----------|
| Non-voyant | TalkBack + vibrations + sons |
| Sourd | Vibrations + radar visuel + notifications |
| Daltonien | Textes + emojis + formes + contraste |
| Mobilité réduite | Quick Settings facilement accessibles |

---

## 📲 Utilisation quotidienne

### Activer la surveillance

**Méthode rapide (Quick Settings)** :
1. Balayez vers le bas depuis le haut de l'écran (2 fois)
2. Trouvez la tuile **👁️ Regards au monde**
3. Appuyez dessus pour activer/désactiver

**Première utilisation des Quick Settings** :
1. Balayez vers le bas
2. Appuyez sur l'icône ✏️ (modifier)
3. Faites glisser "Regards au monde" dans les tuiles actives
4. Validez

### Indicateurs visuels

#### Notification de service
Quand la surveillance est active, une notification apparaît :
- 📋 **Téléphone posé** — En veille (économie batterie)
- 🚶 **En marche - RAS** — Surveillance active, rien détecté
- 👀 **Détection!** — Quelqu'un/quelque chose détecté

#### Radar HUD
Le radar s'affiche automatiquement quand :
1. Vous marchez (détecté via accéléromètre)
2. Un objet est détecté à proximité

**Légende du radar** :
- 🟢 Centre : Vous
- 🔴 Point rouge : Objet très proche (< 1.5m)
- 🟠 Point orange : Objet proche (1.5-2.5m)
- 🟡 Point jaune : Objet à distance (> 2.5m)In
- ⬆️⬇️⬅️➡️ Flèches : Direction de déplacement

### Alertes

| Type | Déclencheur | Alerte |
|------|-------------|--------|
| 🟠 Alerte | Distance < 2.5m | Vibration douce |
| 🔴 Danger | Distance < 1.5m | Vibration + son + notification |
| ⚡ Rapide | Approche soudaine | Vibration longue |

### Arrêter la surveillance

1. **Quick Settings** : Appuyez sur la tuile 👁️
2. **Notification** : Appuyez sur "Arrêter"
3. **Force stop** : Paramètres → Applications → Regards au monde → Forcer l'arrêt

---

## 🎨 Personnalisation

### Statistiques et progression

Ouvrez l'application pour voir :
- **Points accumulés** et niveau actuel
- **Badges débloqués** (12 au total)
- **Streak journalier** (jours consécutifs d'utilisation)

### Système de niveaux

| Niveau | Points requis | Label |
|--------|---------------|-------|
| 1 | 0 | 🌱 Débutant |
| 2 | 50 | 🚶 Intermédiaire |
| 3 | 200 | 👁️ Confirmé |
| 4 | 500 | ⚡ Expert |
| 5 | 1000 | 🏆 Maître |

### Liste des badges

| Badge | Condition |
|-------|-----------|
| 🥉 Premier Regard | 1ère alerte évitée |
| 🥈 Zone Dangereuse | 20 alertes danger |
| 🏅 Centurion | 100 alertes danger |
| 🥇 Gardien de la Rue | 100 points |
| 💎 Expert | 500 points |
| 👁️ Maître Vigilant | 1000 points |
| ⚡ Speedster | 1ère approche rapide |
| 🌩️ Éclair | 10 approches rapides |
| 🔥 En feu | 5 jours consécutifs |
| 💪 Iron Will | 30 jours consécutifs |
| 👶 Protecteur | 10 enfants/bébés détectés |
| 🐾 Ami des bêtes | 10 animaux détectés |

---

## 🔧 Dépannage

### Le radar ne s'affiche pas

**Cause probable** : Permission de superposition non accordée.

**Solution** :
1. Paramètres → Applications → Regards au monde
2. Autorisations → Affichage par-dessus d'autres apps
3. Activez la permission

### La détection ne fonctionne pas

**Vérifications** :
1. ✅ La surveillance est-elle activée ? (tuile Quick Settings)
2. ✅ Êtes-vous en train de marcher ? (le radar s'active au mouvement)
3. ✅ La caméra est-elle autorisée ?

**Mode simulation** : Si vous n'avez pas accordé la caméra, l'app fonctionne en mode simulation (détections aléatoires pour tester).

### Consommation batterie élevée

L'app est optimisée, mais si la batterie se vide rapidement :

1. **Vérifiez votre niveau d'optimisation** :
   - Paramètres → Batterie → Optimisation batterie
   - Assurez-vous que "Regards au monde" est en mode "Optimisé"

2. **Le radar consomme uniquement quand vous marchez**
   - Si vous êtes stationnaire, l'app est en veille

### L'app crash au démarrage

**Solutions** :
1. Redémarrez votre téléphone
2. Videz le cache : Paramètres → Applications → Regards au monde → Stockage → Vider le cache
3. Réinstallez l'application

### Vibrations trop fortes/faibles

Les vibrations sont prédéfinies par type d'objet. Vous pouvez :
1. Ajuster le volume de vibration de votre téléphone
2. Activer le mode "Ne pas déranger" pour désactiver les vibrations

---

## ❓ FAQ

### Q: L'app accède-t-elle à mes photos ou vidéos ?
**R**: Non. L'app utilise uniquement le flux caméra en temps réel. Aucune image n'est enregistrée ni transmise.

### Q: Mes données sont-elles envoyées sur un serveur ?
**R**: Non. Tout le traitement ML Kit est effectué localement sur votre appareil. Vos statistiques de jeu sont également stockées uniquement sur votre téléphone.

### Q: Puis-je utiliser l'app sans accorder la caméra ?
**R**: Oui. L'app fonctionne en mode simulation, utile pour tester les alertes et le système de gamification.

### Q: Comment désinstaller complètement l'app ?
**R**: Paramètres → Applications → Regards au monde → Désinstaller. Cela supprime également toutes les données locales (statistiques, badges).

### Q: L'app fonctionne-t-elle en arrière-plan ?
**R**: Oui, grâce au "Foreground Service". Une notification persistante vous informe que la surveillance est active.

### Q: Pourquoi le radar n'apparaît-il pas quand je suis assis ?
**R**: Pour économiser la batterie, le radar ne s'affiche que lorsque vous êtes en mouvement (marche détectée par l'accéléromètre).

### Q: Les badges sont-ils synchronisés entre appareils ?
**R**: Non. Les statistiques sont stockées localement. Si vous changez de téléphone, vous repartez de zéro.

---

## 📞 Support

Si vous rencontrez un problème non résolu par ce guide :

1. **Consultez les Issues GitHub** : [github.com/votre-repo/issues](../../issues)
2. **Créez une nouvelle Issue** avec :
   - Modèle de téléphone
   - Version Android
   - Description du problème
   - Captures d'écran si possible

---

**Version du guide** : 1.1.0  
**Dernière mise à jour** : Février 2026
