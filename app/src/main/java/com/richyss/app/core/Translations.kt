package com.richyss.app.core

/**
 * In-app translation catalogue.
 *
 * Richyss ships its own catalogue instead of Android resources so the language
 * switch applies instantly, without recreating the activity, and so that a unit
 * test can prove both languages are complete (the interface makes that a compile
 * error rather than a silent fallback).
 */
interface Translations {
    val languageCode: String

    // Navigation
    val navHome: String
    val navProfiles: String
    val navPresets: String
    val navPatches: String
    val navSettings: String

    // Common
    val commonCancel: String
    val commonSave: String
    val commonDelete: String
    val commonEdit: String
    val commonDuplicate: String
    val commonClose: String
    val commonBack: String
    val commonRetry: String
    val commonRefresh: String
    val commonUnknown: String
    val commonShare: String
    val commonImport: String
    val commonExport: String
    val commonActive: String
    val commonInactive: String
    val commonCreate: String
    val commonCopied: String
    val commonNever: String
    val commonConfirm: String

    // Home
    val homeTagline: String
    val homeIntegration: String
    val homeProfilesCount: String
    val homeActiveProfile: String
    val homeNoActiveProfile: String
    val homeLastActivity: String
    val homeNoActivity: String
    val homeCreateRpc: String
    val homeRecentProfiles: String
    val homeSeeAll: String
    val homeLocalPreviewNote: String

    // Discord states
    val discordNotInstalled: String
    val discordInstalledNoBridge: String
    val bridgeActive: String
    val bridgeError: String
    val bridgeChecking: String

    // Profiles
    val profilesTitle: String
    val profilesEmptyTitle: String
    val profilesEmptyHint: String
    val profilesCreate: String
    val profilesImport: String
    val profilesImportFailed: String
    val profilesImported: String
    val profilesExported: String
    val profilesDeleteTitle: String
    val profilesDeleteMessage: String
    val profilesActivated: String
    val profilesDeactivated: String
    val profilesDuplicated: String
    val profilesActiveChip: String
    val profilesCountLabel: String

    // Editor
    val editorTitleNew: String
    val editorTitleEdit: String
    val editorGeneral: String
    val editorName: String
    val editorIcon: String
    val editorDescription: String
    val editorActivityType: String
    val editorActivityName: String
    val editorDetails: String
    val editorState: String
    val editorImages: String
    val editorLargeImage: String
    val editorSmallImage: String
    val editorPickImage: String
    val editorClearImage: String
    val editorImageHint: String
    val editorTime: String
    val editorTimerEnabled: String
    val editorStart: String
    val editorEnd: String
    val editorStartNow: String
    val editorEndInOneHour: String
    val editorEndInTwoHours: String
    val editorClearTimes: String
    val editorButtons: String
    val editorAddButton: String
    val editorButtonLabel: String
    val editorButtonUrl: String
    val editorRemoveButton: String
    val editorButtonsLimit: String
    val editorPreview: String
    val editorLocalOnly: String
    val editorSaved: String
    val editorFieldRequired: String
    val editorTooLong: String
    val editorUrlInvalid: String
    val editorUnsupported: String
    val editorUnsupportedBody: String
    val editorDeleteTitle: String
    val editorDeleteMessage: String

    // Activity types
    val activityPlaying: String
    val activityStreaming: String
    val activityListening: String
    val activityWatching: String
    val activityCompeting: String

    // Preview
    val previewLocalTitle: String
    val previewNotPublished: String
    val previewDelivered: String
    val previewNoDetails: String
    val previewElapsed: String
    val previewRemaining: String
    val previewButtons: String
    val previewPlaceholderImage: String

    // Presets
    val presetsTitle: String
    val presetsCategoryAll: String
    val presetsCategoryGaming: String
    val presetsCategoryMusic: String
    val presetsCategoryProgramming: String
    val presetsCategorySocial: String
    val presetsCategoryCustom: String
    val presetsFavoritesOnly: String
    val presetsEmptyTitle: String
    val presetsEmptyHint: String
    val presetsRefreshDone: String
    val presetsRefreshFailed: String
    val presetsCachedNotice: String
    val presetsImportToProfiles: String
    val presetsEditCopy: String
    val presetsShareJson: String
    val presetsFavorite: String
    val presetsUnfavorite: String
    val presetsAuthorLabel: String
    val presetsVersionLabel: String
    val presetsDetailTitle: String
    val presetsSourceRemote: String
    val presetsSourceBundled: String

    // Patches
    val patchesTitle: String
    val patchesDiscordState: String
    val patchesDiscordVersion: String
    val patchesBridgeCompat: String
    val patchesBridgeVersion: String
    val patchesProtocol: String
    val patchesInstalled: String
    val patchesNotInstalled: String
    val patchesCompatible: String
    val patchesIncompatible: String
    val patchesInstallGuide: String
    val patchesStep1: String
    val patchesStep2: String
    val patchesStep3: String
    val patchesStep4: String
    val patchesStep5: String
    val patchesOpenRepository: String
    val patchesOpenMorphe: String
    val patchesDiagnostics: String
    val patchesLastPayload: String
    val patchesNoPayload: String
    val patchesLimitations: String
    val patchesLimitationsBody: String
    val patchesRefresh: String
    val patchesDeliver: String
    val patchesDelivered: String
    val patchesDeliverFailed: String
    val patchesNoActiveProfile: String

    // Settings
    val settingsTitle: String
    val settingsLanguage: String
    val settingsLanguageSystem: String
    val settingsTheme: String
    val settingsThemeDark: String
    val settingsThemeLight: String
    val settingsThemeSystem: String
    val settingsStorage: String
    val settingsProfilesStored: String
    val settingsClearProfiles: String
    val settingsClearProfilesConfirm: String
    val settingsCleared: String
    val settingsGitHub: String
    val settingsPresetRepo: String
    val settingsPresetBranch: String
    val settingsSaveRepository: String
    val settingsRepositorySaved: String
    val settingsVersion: String
    val settingsUpdateCheck: String
    val settingsChecking: String
    val settingsUpToDate: String
    val settingsUpdateAvailable: String
    val settingsUpdateFailed: String
    val settingsAbout: String
    val settingsAboutBody: String
    val settingsOpenSource: String
}

object French : Translations {
    override val languageCode = "fr"

    override val navHome = "Accueil"
    override val navProfiles = "Profils"
    override val navPresets = "Presets"
    override val navPatches = "Patches"
    override val navSettings = "Réglages"

    override val commonCancel = "Annuler"
    override val commonSave = "Enregistrer"
    override val commonDelete = "Supprimer"
    override val commonEdit = "Modifier"
    override val commonDuplicate = "Dupliquer"
    override val commonClose = "Fermer"
    override val commonBack = "Retour"
    override val commonRetry = "Réessayer"
    override val commonRefresh = "Actualiser"
    override val commonUnknown = "Inconnu"
    override val commonShare = "Partager"
    override val commonImport = "Importer"
    override val commonExport = "Exporter"
    override val commonActive = "Actif"
    override val commonInactive = "Inactif"
    override val commonCreate = "Créer"
    override val commonCopied = "Copié"
    override val commonNever = "Jamais"
    override val commonConfirm = "Confirmer"

    override val homeTagline = "Custom Rich Presence pour Discord Android"
    override val homeIntegration = "Intégration Discord"
    override val homeProfilesCount = "Profils enregistrés"
    override val homeActiveProfile = "Profil actif"
    override val homeNoActiveProfile = "Aucun profil activé"
    override val homeLastActivity = "Dernière activité"
    override val homeNoActivity = "Aucune présence envoyée pour le moment"
    override val homeCreateRpc = "Créer un RPC"
    override val homeRecentProfiles = "Profils récents"
    override val homeSeeAll = "Tout voir"
    override val homeLocalPreviewNote = "L'aperçu est local : il n'est publié sur Discord qu'après un envoi réussi au bridge."

    override val discordNotInstalled = "Discord n'est pas installé"
    override val discordInstalledNoBridge = "Discord détecté, bridge Richyss absent"
    override val bridgeActive = "Bridge Richyss actif"
    override val bridgeError = "Bridge détecté mais en erreur"
    override val bridgeChecking = "Vérification…"

    override val profilesTitle = "Mes profils"
    override val profilesEmptyTitle = "Aucun profil"
    override val profilesEmptyHint = "Crée un profil ou importe un JSON pour commencer."
    override val profilesCreate = "Nouveau profil"
    override val profilesImport = "Importer un JSON"
    override val profilesImportFailed = "Import impossible : JSON invalide"
    override val profilesImported = "Profil importé"
    override val profilesExported = "Profil exporté"
    override val profilesDeleteTitle = "Supprimer ce profil ?"
    override val profilesDeleteMessage = "Cette action est définitive."
    override val profilesActivated = "Profil activé"
    override val profilesDeactivated = "Profil désactivé"
    override val profilesDuplicated = "Profil dupliqué"
    override val profilesActiveChip = "Actif"
    override val profilesCountLabel = "profils"

    override val editorTitleNew = "Créer un RPC"
    override val editorTitleEdit = "Modifier le RPC"
    override val editorGeneral = "Informations générales"
    override val editorName = "Nom du profil"
    override val editorIcon = "Icône"
    override val editorDescription = "Description"
    override val editorActivityType = "Type d'activité"
    override val editorActivityName = "Nom de l'activité"
    override val editorDetails = "Texte principal"
    override val editorState = "Texte secondaire"
    override val editorImages = "Images"
    override val editorLargeImage = "Grande image"
    override val editorSmallImage = "Petite image"
    override val editorPickImage = "Choisir une image"
    override val editorClearImage = "Retirer"
    override val editorImageHint = "Choisis une image du téléphone (elle est référencée par URI, jamais copiée hors de l'appareil)."
    override val editorTime = "Temps"
    override val editorTimerEnabled = "Afficher le chronomètre"
    override val editorStart = "Début"
    override val editorEnd = "Fin"
    override val editorStartNow = "Démarrer maintenant"
    override val editorEndInOneHour = "Fin dans 1 h"
    override val editorEndInTwoHours = "Fin dans 2 h"
    override val editorClearTimes = "Effacer les horaires"
    override val editorButtons = "Boutons"
    override val editorAddButton = "Ajouter un bouton"
    override val editorButtonLabel = "Texte du bouton"
    override val editorButtonUrl = "URL du bouton"
    override val editorRemoveButton = "Supprimer le bouton"
    override val editorButtonsLimit = "Le bridge accepte au maximum 2 boutons."
    override val editorPreview = "Aperçu"
    override val editorLocalOnly = "Aperçu local"
    override val editorSaved = "Profil enregistré"
    override val editorFieldRequired = "Ce champ est obligatoire"
    override val editorTooLong = "Texte trop long"
    override val editorUrlInvalid = "L'URL doit commencer par http:// ou https://"
    override val editorUnsupported = "Options non supportées"
    override val editorUnsupportedBody = "Le protocole du bridge 1.0 transporte le texte principal/secondaire, deux images, les horodatages et jusqu'à deux boutons. La reprise de session (party), les invitations et les boutons dynamiques ne sont pas pris en charge et ne sont pas affichés ici."
    override val editorDeleteTitle = "Supprimer ce bouton ?"
    override val editorDeleteMessage = "Le bouton sera retiré du profil."

    override val activityPlaying = "Joue à"
    override val activityStreaming = "Diffuse"
    override val activityListening = "Écoute"
    override val activityWatching = "Regarde"
    override val activityCompeting = "Participe à"

    override val previewLocalTitle = "Aperçu local"
    override val previewNotPublished = "Non publié sur Discord"
    override val previewDelivered = "Livré au bridge Discord"
    override val previewNoDetails = "Ajoute un texte principal"
    override val previewElapsed = "temps écoulé"
    override val previewRemaining = "restant"
    override val previewButtons = "Boutons"
    override val previewPlaceholderImage = "Image"

    override val presetsTitle = "Presets"
    override val presetsCategoryAll = "Tous"
    override val presetsCategoryGaming = "Gaming"
    override val presetsCategoryMusic = "Musique"
    override val presetsCategoryProgramming = "Programmation"
    override val presetsCategorySocial = "Social"
    override val presetsCategoryCustom = "Custom"
    override val presetsFavoritesOnly = "Favoris"
    override val presetsEmptyTitle = "Aucun preset"
    override val presetsEmptyHint = "Actualise la bibliothèque ou retire le filtre."
    override val presetsRefreshDone = "Bibliothèque à jour"
    override val presetsRefreshFailed = "Synchronisation impossible"
    override val presetsCachedNotice = "Presets chargés depuis le cache local (réseau indisponible)."
    override val presetsImportToProfiles = "Importer dans mes profils"
    override val presetsEditCopy = "Modifier une copie"
    override val presetsShareJson = "Partager en JSON"
    override val presetsFavorite = "Ajouter aux favoris"
    override val presetsUnfavorite = "Retirer des favoris"
    override val presetsAuthorLabel = "Auteur"
    override val presetsVersionLabel = "Version"
    override val presetsDetailTitle = "Détail du preset"
    override val presetsSourceRemote = "Dépôt GitHub"
    override val presetsSourceBundled = "Bibliothèque intégrée"

    override val patchesTitle = "Patch Manager"
    override val patchesDiscordState = "État de Discord"
    override val patchesDiscordVersion = "Version détectée"
    override val patchesBridgeCompat = "Compatibilité des patches"
    override val patchesBridgeVersion = "Version du bridge"
    override val patchesProtocol = "Protocole"
    override val patchesInstalled = "Installé"
    override val patchesNotInstalled = "Non détecté"
    override val patchesCompatible = "Compatible"
    override val patchesIncompatible = "Non compatible"
    override val patchesInstallGuide = "Ajouter le dépôt dans Morpher"
    override val patchesStep1 = "Installe Morphe Manager depuis le dépôt officiel."
    override val patchesStep2 = "Ouvre Réglages → Sources dans Morphe."
    override val patchesStep3 = "Ajoute https://github.com/ClmentCch/Richees"
    override val patchesStep4 = "Sélectionne Discord, active Richyss Bridge, patche et installe l'APK."
    override val patchesStep5 = "Reviens ici et actualise : le bridge doit être détecté."
    override val patchesOpenRepository = "Ouvrir le dépôt des patches"
    override val patchesOpenMorphe = "Ouvrir le projet Morphe"
    override val patchesDiagnostics = "Diagnostic brut du bridge"
    override val patchesLastPayload = "Dernier payload livré"
    override val patchesNoPayload = "Aucun payload stocké côté Discord"
    override val patchesLimitations = "Limites techniques"
    override val patchesLimitationsBody = "Le bridge transporte et conserve le payload localement. L'affichage dans la carte d'activité de Discord dépend d'un patch d'affichage qui n'est pas inclus dans la version 1.0.0, car il exige des fingerprints des classes obfusquées de Discord qui n'ont pas pu être développés ni vérifiés ici. Richyss n'affiche donc jamais \"publié sur Discord\" pour un simple stockage dans le bridge."
    override val patchesRefresh = "Revérifier"
    override val patchesDeliver = "Envoyer le profil actif au bridge"
    override val patchesDelivered = "Profil livré au bridge Discord"
    override val patchesDeliverFailed = "Livraison refusée par le bridge"
    override val patchesNoActiveProfile = "Active un profil avant d'envoyer."

    override val settingsTitle = "Réglages"
    override val settingsLanguage = "Langue"
    override val settingsLanguageSystem = "Système"
    override val settingsTheme = "Thème"
    override val settingsThemeDark = "Sombre"
    override val settingsThemeLight = "Clair"
    override val settingsThemeSystem = "Système"
    override val settingsStorage = "Stockage"
    override val settingsProfilesStored = "Profils stockés en base locale"
    override val settingsClearProfiles = "Effacer tous les profils"
    override val settingsClearProfilesConfirm = "Tous les profils seront supprimés de l'appareil."
    override val settingsCleared = "Profils supprimés"
    override val settingsGitHub = "Dépôt GitHub des presets"
    override val settingsPresetRepo = "Dépôt (owner/nom)"
    override val settingsPresetBranch = "Branche"
    override val settingsSaveRepository = "Enregistrer le dépôt"
    override val settingsRepositorySaved = "Dépôt enregistré"
    override val settingsVersion = "Version de Richyss"
    override val settingsUpdateCheck = "Vérifier les mises à jour"
    override val settingsChecking = "Vérification…"
    override val settingsUpToDate = "Richyss est à jour"
    override val settingsUpdateAvailable = "Nouvelle version disponible"
    override val settingsUpdateFailed = "Vérification impossible"
    override val settingsAbout = "À propos"
    override val settingsAboutBody = "Richyss 1.0.0 — application Android native (Kotlin, Compose, Room, DataStore). Licence GPL-3.0. Richyss ne demande jamais ton token Discord et n'utilise aucun self-bot."
    override val settingsOpenSource = "Code source"
}

object English : Translations {
    override val languageCode = "en"

    override val navHome = "Home"
    override val navProfiles = "Profiles"
    override val navPresets = "Presets"
    override val navPatches = "Patches"
    override val navSettings = "Settings"

    override val commonCancel = "Cancel"
    override val commonSave = "Save"
    override val commonDelete = "Delete"
    override val commonEdit = "Edit"
    override val commonDuplicate = "Duplicate"
    override val commonClose = "Close"
    override val commonBack = "Back"
    override val commonRetry = "Retry"
    override val commonRefresh = "Refresh"
    override val commonUnknown = "Unknown"
    override val commonShare = "Share"
    override val commonImport = "Import"
    override val commonExport = "Export"
    override val commonActive = "Active"
    override val commonInactive = "Inactive"
    override val commonCreate = "Create"
    override val commonCopied = "Copied"
    override val commonNever = "Never"
    override val commonConfirm = "Confirm"

    override val homeTagline = "Custom Rich Presence for Discord Android"
    override val homeIntegration = "Discord integration"
    override val homeProfilesCount = "Saved profiles"
    override val homeActiveProfile = "Active profile"
    override val homeNoActiveProfile = "No profile activated"
    override val homeLastActivity = "Last activity"
    override val homeNoActivity = "No presence delivered yet"
    override val homeCreateRpc = "Create RPC"
    override val homeRecentProfiles = "Recent profiles"
    override val homeSeeAll = "See all"
    override val homeLocalPreviewNote = "The preview is local: it is published to Discord only after a successful hand-off to the bridge."

    override val discordNotInstalled = "Discord is not installed"
    override val discordInstalledNoBridge = "Discord detected, Richyss bridge missing"
    override val bridgeActive = "Richyss bridge active"
    override val bridgeError = "Bridge detected but failing"
    override val bridgeChecking = "Checking…"

    override val profilesTitle = "My profiles"
    override val profilesEmptyTitle = "No profiles yet"
    override val profilesEmptyHint = "Create a profile or import a JSON file to start."
    override val profilesCreate = "New profile"
    override val profilesImport = "Import JSON"
    override val profilesImportFailed = "Import failed: invalid JSON"
    override val profilesImported = "Profile imported"
    override val profilesExported = "Profile exported"
    override val profilesDeleteTitle = "Delete this profile?"
    override val profilesDeleteMessage = "This cannot be undone."
    override val profilesActivated = "Profile activated"
    override val profilesDeactivated = "Profile deactivated"
    override val profilesDuplicated = "Profile duplicated"
    override val profilesActiveChip = "Active"
    override val profilesCountLabel = "profiles"

    override val editorTitleNew = "Create RPC"
    override val editorTitleEdit = "Edit RPC"
    override val editorGeneral = "General"
    override val editorName = "Profile name"
    override val editorIcon = "Icon"
    override val editorDescription = "Description"
    override val editorActivityType = "Activity type"
    override val editorActivityName = "Activity name"
    override val editorDetails = "Primary text"
    override val editorState = "Secondary text"
    override val editorImages = "Images"
    override val editorLargeImage = "Large image"
    override val editorSmallImage = "Small image"
    override val editorPickImage = "Pick an image"
    override val editorClearImage = "Remove"
    override val editorImageHint = "Pick an image from the phone (it is referenced by URI and never leaves the device)."
    override val editorTime = "Time"
    override val editorTimerEnabled = "Show elapsed timer"
    override val editorStart = "Start"
    override val editorEnd = "End"
    override val editorStartNow = "Start now"
    override val editorEndInOneHour = "End in 1 h"
    override val editorEndInTwoHours = "End in 2 h"
    override val editorClearTimes = "Clear times"
    override val editorButtons = "Buttons"
    override val editorAddButton = "Add a button"
    override val editorButtonLabel = "Button label"
    override val editorButtonUrl = "Button URL"
    override val editorRemoveButton = "Remove button"
    override val editorButtonsLimit = "The bridge accepts at most 2 buttons."
    override val editorPreview = "Preview"
    override val editorLocalOnly = "Local preview"
    override val editorSaved = "Profile saved"
    override val editorFieldRequired = "This field is required"
    override val editorTooLong = "Text is too long"
    override val editorUrlInvalid = "URL must start with http:// or https://"
    override val editorUnsupported = "Unsupported options"
    override val editorUnsupportedBody = "Bridge protocol 1.0 carries primary/secondary text, two images, timestamps and up to two buttons. Party, join requests and dynamic buttons are not supported and are not shown here."
    override val editorDeleteTitle = "Remove this button?"
    override val editorDeleteMessage = "The button will be removed from the profile."

    override val activityPlaying = "Playing"
    override val activityStreaming = "Streaming"
    override val activityListening = "Listening to"
    override val activityWatching = "Watching"
    override val activityCompeting = "Competing in"

    override val previewLocalTitle = "Local preview"
    override val previewNotPublished = "Not published to Discord"
    override val previewDelivered = "Delivered to the Discord bridge"
    override val previewNoDetails = "Add a primary text"
    override val previewElapsed = "elapsed"
    override val previewRemaining = "left"
    override val previewButtons = "Buttons"
    override val previewPlaceholderImage = "Image"

    override val presetsTitle = "Presets"
    override val presetsCategoryAll = "All"
    override val presetsCategoryGaming = "Gaming"
    override val presetsCategoryMusic = "Music"
    override val presetsCategoryProgramming = "Programming"
    override val presetsCategorySocial = "Social"
    override val presetsCategoryCustom = "Custom"
    override val presetsFavoritesOnly = "Favourites"
    override val presetsEmptyTitle = "No presets"
    override val presetsEmptyHint = "Refresh the library or clear the filter."
    override val presetsRefreshDone = "Library up to date"
    override val presetsRefreshFailed = "Could not sync"
    override val presetsCachedNotice = "Presets loaded from the local cache (network unavailable)."
    override val presetsImportToProfiles = "Import to my profiles"
    override val presetsEditCopy = "Edit a copy"
    override val presetsShareJson = "Share as JSON"
    override val presetsFavorite = "Add to favourites"
    override val presetsUnfavorite = "Remove from favourites"
    override val presetsAuthorLabel = "Author"
    override val presetsVersionLabel = "Version"
    override val presetsDetailTitle = "Preset details"
    override val presetsSourceRemote = "GitHub repository"
    override val presetsSourceBundled = "Bundled library"

    override val patchesTitle = "Patch Manager"
    override val patchesDiscordState = "Discord state"
    override val patchesDiscordVersion = "Detected version"
    override val patchesBridgeCompat = "Patch compatibility"
    override val patchesBridgeVersion = "Bridge version"
    override val patchesProtocol = "Protocol"
    override val patchesInstalled = "Installed"
    override val patchesNotInstalled = "Not detected"
    override val patchesCompatible = "Compatible"
    override val patchesIncompatible = "Not compatible"
    override val patchesInstallGuide = "Add the repository to Morpher"
    override val patchesStep1 = "Install Morphe Manager from the official repository."
    override val patchesStep2 = "Open Settings → Sources in Morphe."
    override val patchesStep3 = "Add https://github.com/ClmentCch/Richees"
    override val patchesStep4 = "Select Discord, enable Richyss Bridge, patch and install the APK."
    override val patchesStep5 = "Come back here and refresh: the bridge must be detected."
    override val patchesOpenRepository = "Open the patch repository"
    override val patchesOpenMorphe = "Open the Morphe project"
    override val patchesDiagnostics = "Raw bridge diagnostics"
    override val patchesLastPayload = "Last delivered payload"
    override val patchesNoPayload = "No payload stored on the Discord side"
    override val patchesLimitations = "Technical limits"
    override val patchesLimitationsBody = "The bridge transports and stores the payload locally. Rendering it in Discord's activity card needs a display patch that is not part of 1.0.0, because it requires fingerprints of Discord's obfuscated classes that could not be developed or verified here. Richyss therefore never reports \"published to Discord\" for a payload that merely reached the bridge."
    override val patchesRefresh = "Re-check"
    override val patchesDeliver = "Send active profile to the bridge"
    override val patchesDelivered = "Profile delivered to the Discord bridge"
    override val patchesDeliverFailed = "The bridge rejected the delivery"
    override val patchesNoActiveProfile = "Activate a profile before delivering."

    override val settingsTitle = "Settings"
    override val settingsLanguage = "Language"
    override val settingsLanguageSystem = "System"
    override val settingsTheme = "Theme"
    override val settingsThemeDark = "Dark"
    override val settingsThemeLight = "Light"
    override val settingsThemeSystem = "System"
    override val settingsStorage = "Storage"
    override val settingsProfilesStored = "Profiles stored locally"
    override val settingsClearProfiles = "Delete all profiles"
    override val settingsClearProfilesConfirm = "Every profile will be removed from this device."
    override val settingsCleared = "Profiles deleted"
    override val settingsGitHub = "Preset GitHub repository"
    override val settingsPresetRepo = "Repository (owner/name)"
    override val settingsPresetBranch = "Branch"
    override val settingsSaveRepository = "Save repository"
    override val settingsRepositorySaved = "Repository saved"
    override val settingsVersion = "Richyss version"
    override val settingsUpdateCheck = "Check for updates"
    override val settingsChecking = "Checking…"
    override val settingsUpToDate = "Richyss is up to date"
    override val settingsUpdateAvailable = "New version available"
    override val settingsUpdateFailed = "Check failed"
    override val settingsAbout = "About"
    override val settingsAboutBody = "Richyss 1.0.0 — native Android app (Kotlin, Compose, Room, DataStore). GPL-3.0. Richyss never asks for your Discord token and uses no self-bot."
    override val settingsOpenSource = "Source code"
}

object TranslationsCatalog {
    fun forLanguage(language: String?): Translations = when (language) {
        "fr" -> French
        "en" -> English
        else -> French
    }

    val all: List<Translations> = listOf(French, English)
}
