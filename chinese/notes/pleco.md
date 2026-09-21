# Pleco vocabulary export

Pleco syncs its weekly flashcard backups to iCloud Drive, so nothing has to be
pulled off the iPhone:

    ~/Library/Mobile Documents/TY59B5X35J~com~pleco~chinesesystem/Documents/Flashcard Backups/*.pqb

`.pqb` files are plain SQLite. Tables: `pleco_flash_cards` (hw = simplified,
althw = traditional, pron = numbered pinyin, `@` separates syllables/characters),
`pleco_flash_categories` + `pleco_flash_categoryassigns`, and the score/profile
tables used for SRS state.

`defn` is **empty** — Pleco keeps definitions in the dictionaries and cards only
reference them via dictid/dictentry, so a backup alone yields headword + reading.
Glosses have to come from a separate CC-CEDICT-style source.

Run `probes/export_pleco.py` to regenerate `pleco_vocabulary.tsv` (unions all five
weekly backups, ~693 unique entries, 2017-06 … 2026-05). Categories seen: `searches`
(auto-added lookups) and `Numbers`; ~300 entries are uncategorized.
