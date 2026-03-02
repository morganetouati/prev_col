# Play Store Assets & Compliance Checklist

## Listing Assets
- [ ] Icône app 512x512
- [ ] Feature graphic 1024x500
- [ ] Captures téléphone (min 2, max 8)
- [ ] Captures tablette (si support annoncé)
- [ ] Vidéo promo YouTube (optionnelle)

## Store Content
- [ ] Description courte (<= 80 chars)
- [ ] Description complète cohérente avec les permissions
- [ ] Catégorie et tags corrects
- [ ] Politique de confidentialité accessible publiquement

## Policy & Safety
- [ ] Formulaire Data Safety rempli (incluant SDK AdMob)
- [ ] Déclaration permissions sensibles vérifiée
- [ ] Justification overlay prête (voir PLAYSTORE_POLICY_OVERLAY.md)
- [ ] Contenu conforme familles/enfants si ciblage activé

## Quality Gates
- [ ] :app:verifyPlayReleaseConfig
- [ ] :app:lintVitalRelease
- [ ] :app:testDebugUnitTest
- [ ] :app:bundleRelease
- [ ] Signature release valide

## Release Ops
- [ ] VersionCode incrémenté
- [ ] Notes de version prêtes
- [ ] Rollout progressif (ex: 5% -> 20% -> 100%)
- [ ] Monitoring post-release (crash, ANR, avis)
