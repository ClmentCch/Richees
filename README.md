# Richyss

**Custom Rich Presence pour Discord Android** — application Android native en Kotlin/Jetpack Compose.

Richyss crée, gère et prévisualise des profils de Rich Presence, et les remet à un client Discord
patché via le **Richyss Bridge** publié sur la branche [`main`](https://github.com/ClmentCch/Richees/tree/main).

> **Téléchargement** : [release Richyss 1.0.0](https://github.com/ClmentCch/Richees/releases/tag/richyss-apk-v1.0.0) →
> `Richyss-1.0.0.apk`

---

## ✨ Fonctionnalités

| Section | Ce qui fonctionne réellement |
| --- | --- |
| **Home** | Dashboard alimenté par Room et par une vraie interrogation de `PackageManager` + du `ContentProvider` du bridge. Statut « Discord absent / patché ou non / bridge actif », nombre de profils, profil actif, dernière remise de payload. |
| **Mes profils** | Créer, modifier, supprimer, dupliquer, activer/désactiver, importer et exporter en JSON (SAF), partager le JSON. Stockage Room. |
| **Créer un RPC** | Éditeur complet : nom, icône, description, type d'activité (Joue / Diffuse / Écoute / Regarde / Participe), nom d'activité, texte principal et secondaire, grande et petite image choisies depuis le téléphone, textes au survol, chrono début/fin, jusqu'à 2 boutons libellé + URL. Validation identique à celle du bridge. |
| **Aperçu** | Reproduction de la carte d'activité Discord, mise à jour à chaque frappe, avec images, textes, chrono vivant et boutons. Toujours étiquetée « aperçu local » ou « livré au bridge ». |
| **Presets** | Bibliothèque récupérée depuis GitHub (`presets/index.json` sur cette branche), cache local, repli sur la bibliothèque intégrée, filtres par catégorie, favoris, import, édition d'une copie, partage JSON. |
| **Settings** | Langue FR/EN (catalogue intégré, changement instantané), thème sombre/clair/système, dépôt GitHub des presets, nombre de profils, suppression totale, version, vérification des mises à jour via l'API GitHub, à propos. |
| **Patch Manager** | État Discord, version détectée, compatibilité, version du bridge, protocole, diagnostic brut, guide d'ajout du dépôt dans Morphe, envoi du profil actif au bridge. |

Aucun bouton décoratif : chaque action écrit en base, ouvre un sélecteur de fichiers, lance une
requête réseau réelle, ou interroge réellement le bridge.

---

## 📱 Installation

1. Télécharge `Richyss-1.0.0.apk` depuis la [release](https://github.com/ClmentCch/Richees/releases/tag/richyss-apk-v1.0.0).
2. Autorise l'installation depuis cette source si Android le demande.
3. Ouvre Richyss.

- **Android minimum** : 8.0 (API 26). **Cible** : API 36. **Compilé** : SDK 36.
- **Signature** : clé de release RSA 4096 (`CN=Richyss`), distincte de la clé debug.
- **Permissions** : `INTERNET` et `ACCESS_NETWORK_STATE` uniquement. Aucun accès aux contacts, au
  stockage global, ni aux médias : les images sont choisies via le sélecteur système et conservées
  par URI persistante.

---

## 🔗 Configuration Discord

Richyss ne demande **jamais** de token, n'utilise **aucun self-bot**, et ne publie rien à la place de
ton compte.

### Avec les patches Morphe (branche `main`)

1. Installe [Morphe Manager](https://github.com/MorpheApp).
2. Ajoute la source `https://github.com/ClmentCch/Richees` dans **Réglages → Sources**.
3. Patche Discord (`com.discord`) en activant **Richyss Bridge**, puis installe l'APK patché.
4. Dans Richyss → **Patch Manager**, actualise : le bridge doit être détecté avec sa version.
5. Active un profil, puis **Envoyer le profil actif au bridge**.

Le bridge expose `content://com.discord.richyss.bridge` et n'accepte que le paquet Richyss.

### Ce que le bridge fait — et ne fait pas

Le payload est validé, stocké dans le répertoire privé de Discord, et relu pour vérifier la remise.
**Discord n'affiche pas encore** ce payload dans sa carte d'activité : cela demande un patch
d'affichage qui n'est pas inclus dans la 1.0.0, car il nécessite des fingerprints des classes
obfusquées de Discord qui n'ont pas pu être développés ni vérifiés dans l'environnement de build.
Richyss affiche donc « livré au bridge », jamais « publié sur Discord ».

### Voie officielle

Discord propose depuis le **Social SDK 1.10** une publication de Rich Presence sur Android via un
transport RPC local non authentifié, à condition d'avoir une application Discord et le SDK distribué
par le portail développeur. Richyss documente cette voie ; elle n'est pas simulée dans l'app.

---

## 🎛️ Créer un profil

1. **Mes profils → Nouveau profil**.
2. Renseigne le nom, l'icône (emoji), le type et le nom d'activité.
3. Ajoute un texte principal et un texte secondaire (limites : 128 caractères chacun).
4. Choisis une grande et une petite image (sélecteur système, URI persistante).
5. Active le chrono et choisis « Démarrer maintenant » et/ou « Fin dans 1 h / 2 h ».
6. Ajoute jusqu'à 2 boutons (libellé ≤ 32 caractères, URL http(s) ≤ 512).
7. **Enregistrer** : l'aperçu se met à jour pendant la saisie.

Les limites du bridge (4 096 octets, 2 boutons, URL http(s), fin ≥ début) sont vérifiées avant
l'enregistrement.

## 📥 Importer des presets

La bibliothèque est récupérée dynamiquement :
`https://raw.githubusercontent.com/ClmentCch/Richees/apk/presets/index.json`.

- **Actualiser** recharge l'index et chaque fichier de preset ; les entrées invalides (chemin hors
  `presets/`, `..`, doublons, JSON cassé) sont ignorées sans casser la bibliothèque.
- Sans réseau, le dernier téléchargement réussi est utilisé, puis la bibliothèque intégrée à l'APK.
- Le dépôt et la branche sont modifiables dans **Réglages**.
- **Importer dans mes profils** crée une copie locale ; **Modifier une copie** l'ouvre dans l'éditeur ;
  **Partager en JSON** envoie le preset via le partage Android.

`tools/generate_presets.py` régénère `presets/*.json`, `presets/index.json` et la copie embarquée
`app/src/main/assets/presets/library.json` à partir d'une seule source de vérité.

---

## 🏗️ Architecture

```
app/src/main/java/com/richyss/app/
├── MainActivity.kt            # navigation Compose, thème, snackbars
├── RichyssApplication.kt
├── core/                      # conteneur de dépendances, traductions, formatage
├── data/
│   ├── database/              # Room : entité, DAO, base
│   ├── models/                # RpcProfile, validation, codec JSON, presets
│   ├── prefs/                 # DataStore (langue, thème, dépôt, favoris, remises)
│   └── repository/            # ProfileRepository, PresetRepository
├── network/                   # HttpClient, GitHubClient, UpdateChecker
├── rpc/                       # DiscordBridge (ContentProvider), état d'intégration, livraison
└── ui/
    ├── components/            # cartes, pastilles, carte d'activité Discord
    ├── editor/  home/  profiles/  presets/  patches/  settings/
    └── theme/                 # palette Richyss + Material 3
```

- **MVVM** : un `ViewModel` par écran, `StateFlow` + `collectAsStateWithLifecycle`.
- **Room** : profils (JSON + colonnes indexées), schéma exporté dans `app/schemas`.
- **DataStore Preferences** : préférences et favoris.
- **Aucune dépendance superflue** : pas de DI framework, pas de bibliothèque d'images, pas de client
  HTTP tiers.

### Identité visuelle

`#232428` (fond), `#2B2D31` (surface), `#313338` (cartes), `#5865F2` (accent), `#F2F3F5` /
`#B5BAC1` (textes), `#23A55A` (statut). Mode sombre par défaut, coins arrondis, navigation basse
inspirée de Discord, logo Richyss dessiné en vectoriel.

---

## 🧪 Tests et build

```bash
./gradlew :app:testDebugUnitTest   # 42 tests JVM
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease     # nécessite la clé de release
```

Les tests couvrent la sérialisation JSON (aller-retour, champs inconnus, schéma futur, JSON
invalide), la validation (champs requis, URL, boutons, timestamps, taille du payload), le parsing de
l'index de presets (traversée de chemin, doublons), le parsing du statut du bridge, le formatage des
durées, la comparaison de versions et la complétude des deux catalogues de traduction.

**Signature durable** : la clé de release n'est jamais commitée. Le workflow
`.github/workflows/release.yml` restaure le keystore depuis les secrets
`RICHYSS_KEYSTORE_BASE64`, `RICHYSS_KEYSTORE_PASSWORD`, `RICHYSS_KEY_ALIAS`, `RICHYSS_KEY_PASSWORD`
et signe l'APK. Sans ces secrets, le build release est signé avec la clé debug et le workflow émet un
avertissement explicite.

---

## ⚠️ Limitations connues

- **Affichage dans Discord** : non inclus (voir ci-dessus). Le bridge transporte et vérifie, il
  n'affiche pas.
- **Aucun test sur appareil** : l'application compile, les tests JVM passent et l'APK est vérifié
  (signature, manifeste), mais l'interface n'a pas été exécutée sur un téléphone ni un émulateur dans
  l'environnement de build. Les écrans n'ont pas été inspectés visuellement.
- **Compatibilité Discord** : les patches sont déclarés *expérimentaux pour toute version* ; aucune
  matrice de versions Discord n'a pu être validée.
- **Mises à jour** : la vérification compare le tag de la dernière release GitHub à la version
  installée ; elle n'installe rien automatiquement.

---

## 📄 Licence

GPL-3.0.
