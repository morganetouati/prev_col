# Play Store Policy - Overlay (SYSTEM_ALERT_WINDOW)

Ce document sert de dossier de justification pour la review Play Console.

## 1) Usage fonctionnel exact
- L'overlay est utilisé uniquement pour afficher un radar HUD de proximité.
- Le radar n'altère pas les autres applications et n'intercepte pas les entrées utilisateur.
- L'application reste fonctionnelle sans overlay (alertes vibration/son).

## 2) Consentement utilisateur
- Permission demandée explicitement, de manière optionnelle, après explication.
- Refus utilisateur respecté sans blocage de l'application.
- Aucun écran trompeur, aucune demande forcée répétitive.

## 3) Protections implémentées
- L'app n'utilise pas l'overlay pour capturer texte, clics ou credentials.
- Pas de navigation automatique ni d'action sans interaction utilisateur.
- Usage limité au contexte d'aide visuelle de sécurité piétonne.

## 4) Éléments à fournir en soumission
- Vidéo courte (30-60s) montrant: activation overlay, usage radar, désactivation.
- Description Play Store expliquant clairement pourquoi l'overlay est nécessaire.
- Capture d'écran de l'écran de consentement overlay.
- Mention explicite que la fonctionnalité reste disponible sans overlay.

## 5) Checklist avant soumission
- [ ] Texte in-app et fiche store alignés sur l'usage réel de l'overlay.
- [ ] Overlay désactivable à tout moment.
- [ ] Parcours sans overlay testé.
- [ ] Policy review interne validée.
