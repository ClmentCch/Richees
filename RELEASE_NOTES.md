# Richyss 1.0.0

Première version publique de **Richyss**, l'application Android native de Custom Rich Presence pour
Discord, accompagnée du bundle de patches Morphe publié sur la branche `main`.

## 📦 Contenu de la release

- `Richyss-1.0.0.apk` — APK release signé (clé `CN=Richyss`, RSA 4096), minifié par R8, **1,6 Mo**.

## ✅ Fonctionnalités

- **Home** : statut d'intégration Discord réel (PackageManager + interrogation du bridge), nombre de
  profils, profil actif, dernière remise, accès rapide aux profils récents, bouton *Créer un RPC*.
- **Mes profils** : créer, modifier, supprimer, dupliquer, activer/désactiver, importer/exporter et
  partager en JSON (SAF), stockage Room.
- **Éditeur RPC** : nom, icône, description, type d'activité, nom d'activité, textes principal et
  secondaire, grande/petite image depuis le téléphone, textes au survol, chrono début/fin,
  jusqu'à 2 boutons libellé + URL. Validation alignée sur le bridge (4096 octets, http(s), 2 boutons).
- **Aperçu live** : carte d'activité Discord recréée, images, textes, chrono vivant, boutons, et
  étiquette explicite « aperçu local » / « livré au bridge ».
- **Presets** : bibliothèque GitHub actualisable sans mise à jour de l'APK, cache local, repli
  intégré, catégories Gaming / Music / Programming / Social / Custom, favoris, import, copie
  éditable, partage JSON.
- **Patch Manager** : version Discord détectée, compatibilité, version et protocole du bridge,
  diagnostic brut, guide d'installation Morphe, envoi du profil actif au bridge.
- **Réglages** : français / anglais instantané, thème sombre/clair/système, dépôt GitHub des presets,
  gestion du stockage, version, vérification des mises à jour GitHub, à propos.

## 🧪 Vérifications effectuées

- `./gradlew :app:testDebugUnitTest` → **42 tests JVM, 0 échec** (JSON, validation, presets, bridge,
  durées, versions, traductions).
- `:app:assembleDebug` et `:app:assembleRelease` → succès.
- APK vérifié avec `apksigner verify --print-certs` (signature `CN=Richyss`), `aapt2 dump badging`
  (`com.richyss.app`, versionName `1.0.0`, targetSdk 36) et inspection du contenu (assets presets
  présents).
- Bundle de patches : `./gradlew :patches:build` → `patches-1.0.0.mpp`, et
  `:extensions:richyss:test` → **12 tests, 0 échec**.

## ⚠️ Limitations

- L'APK **n'a pas été exécuté** sur un appareil ou un émulateur dans l'environnement de build :
  l'interface compile et est couverte par des tests unitaires, mais n'a pas été inspectée
  visuellement.
- **Discord n'affiche pas encore** le payload remis au bridge : le patch d'affichage nécessite des
  fingerprints des classes obfusquées de Discord, non développés ni vérifiés ici. Richyss affiche
  donc « livré au bridge », jamais « publié sur Discord ».
- Les patches Discord sont déclarés **expérimentaux pour toute version** ; aucune matrice de
  versions Discord n'a été validée.
- Aucun token Discord n'est demandé, stocké ou utilisé ; aucun self-bot.

## 📱 Compatibilité Android

- Minimum : **Android 8.0 (API 26)**
- Cible : **API 36**
- ABI : universel (aucune bibliothèque native)
- Permissions : `INTERNET`, `ACCESS_NETWORK_STATE`
