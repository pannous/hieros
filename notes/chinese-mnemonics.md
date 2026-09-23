# chinese-mnemonics.tsv

Source of truth: Apple Notes. Two curated tables both titled **#chinese mnemonics**
(they are separate notes, each duplicated across two Notes accounts — dedupe by body length):

| body chars | column order | status |
|---|---|---|
| 100358 | ★, Chinese, Pinyin, Hook, Meaning | was already fully in the TSV |
| 93782  | Chinese, Meaning, Hook, ★ (pinyin inline with the hanzi) | imported 2026-09-04 |

Everything else tagged `#chinese` is freeform study/etymology notes or song lyrics — no
star ratings, mixed with personal data (phone numbers, e-mail addresses). Hooks mined from
those were rated by hand; personal data must never reach the dict.

## Extraction recipe
`osascript` name-based queries are fast; `body contains "…"` over all notes is pathologically
slow (minutes, often killed). Copying `NoteStore.sqlite` is blocked by the permission classifier.
So: select notes by *name*, dump bodies to one file with a `=====NOTE=====` delimiter, then
strip HTML. Scripts live in `probes/notes_dump/` (`split_notes.py`, `parse_note.py`, `merge.py`).

`merge.py <target.tsv> <additions.tsv>` re-sorts the whole file by star rank descending,
stable within a band, header preserved.

## Rating scale
★★★ … ★★★★★ with ½ steps. ★★½ and below in the source notes are rejects
(`0`, `?`, `— reject` also appear) and were not imported.

## Known wart
霉 méi "mildew" appears twice (★★★★★ and ★★★★½) — inherited from the source note, left as is.

## Freq-table hooks (2026-09-23)
`dicts/chinese.freq.tsv` column 5 is the user's unfiltered hook brainstorm (includes ★0 ideas).
- 513 of those hooks rated ★4+ were imported (`probes/freq_hook_candidates.py`). ★3 and below stay out.
- A 2026-09-19 run (transplorer session) had ignored column 5 and invented hooks; it kept 9. Its rejections were right.
- Invented hooks for the 630 characters with no hook anywhere: only 19 cleared ★3½ (`probes/hook_proposals.tsv`).
- `probes/sync_hooks_to_freq.py --write` copies table hooks back into column 5 (skips ones already noted). Rerun after bulk additions.
