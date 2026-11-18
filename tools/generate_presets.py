#!/usr/bin/env python3
"""Generate the Richyss preset library.

The same data produces two artefacts that must stay identical:

* ``presets/index.json`` + ``presets/<id>.json`` served from the ``apk`` branch,
  so the app can refresh its library without an APK update.
* ``app/src/main/assets/presets/library.json``, the offline fallback bundled in
  the APK.

Run ``python3 tools/generate_presets.py`` after editing ``PRESETS`` and commit
both outputs.
"""

import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent

PRESETS = [
    {
        "id": "gaming-ranked-session",
        "name": "Ranked session",
        "description": "Session classée avec compteur de temps et bouton vers ton serveur.",
        "category": "gaming",
        "icon": "🎯",
        "activity_name": "Ranked grind",
        "type": 0,
        "details": "Push vers le prochain rang",
        "state": "Queue en cours",
        "timer": True,
        "buttons": [{"label": "Serveur Discord", "url": "https://discord.gg/"}],
    },
    {
        "id": "gaming-coop-night",
        "name": "Coop night",
        "description": "Soirée coop avec tes amis, petit texte et chrono.",
        "category": "gaming",
        "icon": "🎮",
        "activity_name": "Coop night",
        "type": 0,
        "details": "Session coop",
        "state": "Avec les copains",
        "timer": True,
        "buttons": [],
    },
    {
        "id": "music-now-playing",
        "name": "Now playing",
        "description": "Montre ce que tu écoutes avec un style sobre façon Discord.",
        "category": "music",
        "icon": "🎧",
        "activity_name": "Now playing",
        "type": 2,
        "details": "Playlist du moment",
        "state": "Musique",
        "timer": False,
        "buttons": [],
    },
    {
        "id": "music-lofi-focus",
        "name": "Lo-fi focus",
        "description": "Session lo-fi pour travailler, avec chrono écoulé.",
        "category": "music",
        "icon": "🎵",
        "activity_name": "Lo-fi focus",
        "type": 2,
        "details": "Beats to focus to",
        "state": "Session de travail",
        "timer": True,
        "buttons": [],
    },
    {
        "id": "programming-deep-work",
        "name": "Deep work",
        "description": "Session de code concentrée avec chrono et lien vers ton dépôt.",
        "category": "programming",
        "icon": "💻",
        "activity_name": "Deep work",
        "type": 0,
        "details": "Écriture de code",
        "state": "Focus mode",
        "timer": True,
        "buttons": [{"label": "Voir le dépôt", "url": "https://github.com/"}],
    },
    {
        "id": "programming-debugging",
        "name": "Debugging",
        "description": "Quand tu chasses un bug depuis 3 heures.",
        "category": "programming",
        "icon": "🐛",
        "activity_name": "Debugging",
        "type": 0,
        "details": "Chasse au bug",
        "state": "Ça compile presque",
        "timer": True,
        "buttons": [],
    },
    {
        "id": "social-hangout",
        "name": "Hangout",
        "description": "Simple présence sociale, deux lignes de texte.",
        "category": "social",
        "icon": "💬",
        "activity_name": "Hangout",
        "type": 0,
        "details": "Discussion",
        "state": "Disponible",
        "timer": False,
        "buttons": [],
    },
    {
        "id": "social-chill-voice",
        "name": "Chill voice",
        "description": "Pour une session vocale détendue.",
        "category": "social",
        "icon": "🎙️",
        "activity_name": "Chill voice",
        "type": 2,
        "details": "Salon vocal",
        "state": "Entre deux conversations",
        "timer": False,
        "buttons": [],
    },
    {
        "id": "custom-ryss-mood",
        "name": "Richyss mood",
        "description": "Profil vitrine de Richyss avec ses deux boutons.",
        "category": "custom",
        "icon": "🟣",
        "activity_name": "Richyss",
        "type": 0,
        "details": "Custom Rich Presence",
        "state": "Mode personnalisé",
        "timer": True,
        "buttons": [
            {"label": "Richyss", "url": "https://github.com/ClmentCch/Richees"},
            {"label": "Patches", "url": "https://github.com/ClmentCch/Richees/tree/main"},
        ],
    },
    {
        "id": "custom-streaming-setup",
        "name": "Stream setup",
        "description": "Présence de diffusion avec horaire de fin.",
        "category": "custom",
        "icon": "📡",
        "activity_name": "Live",
        "type": 1,
        "details": "On est en direct",
        "state": "Streaming",
        "timer": True,
        "buttons": [{"label": "Chaîne", "url": "https://twitch.tv/"}],
    },
]


def document(preset):
    profile_id = f"preset-{preset['id']}"
    return {
        "schema": 1,
        "id": preset["id"],
        "name": preset["name"],
        "description": preset["description"],
        "category": preset["category"],
        "author": "Richyss",
        "version": "1.0.0",
        "profile": {
            "schema": 1,
            "id": profile_id,
            "name": preset["name"],
            "icon": preset["icon"],
            "description": preset["description"],
            "activityType": preset["type"],
            "activityName": preset["activity_name"],
            "details": preset["details"],
            "state": preset["state"],
            "largeImage": "",
            "smallImage": "",
            "largeText": "",
            "smallText": "",
            "timerEnabled": preset["timer"],
            "buttons": preset["buttons"],
            "updatedAt": 0,
        },
    }


def main():
    presets_dir = ROOT / "presets"
    presets_dir.mkdir(exist_ok=True)

    index_entries = []
    documents = []
    for preset in PRESETS:
        doc = document(preset)
        path = presets_dir / f"{preset['id']}.json"
        path.write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        index_entries.append(
            {
                "id": preset["id"],
                "name": preset["name"],
                "description": preset["description"],
                "category": preset["category"],
                "author": "Richyss",
                "version": "1.0.0",
                "path": f"presets/{preset['id']}.json",
            }
        )
        documents.append(doc)

    index = {"schema": 1, "version": "1.0.0", "presets": index_entries}
    (presets_dir / "index.json").write_text(
        json.dumps(index, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )

    assets_dir = ROOT / "app" / "src" / "main" / "assets" / "presets"
    assets_dir.mkdir(parents=True, exist_ok=True)
    library = {"fetchedAt": 0, "source": "bundled", "documents": documents}
    (assets_dir / "library.json").write_text(
        json.dumps(library, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )

    print(f"Generated {len(PRESETS)} presets in {presets_dir} and {assets_dir}")


if __name__ == "__main__":
    main()
