# Interactive Neolithic / PPN Video Map Plan

## Goal

Build an interactive Middle East / Fertile Crescent map of Epipalaeolithic, Neolithic, and Pre-Pottery Neolithic sites. Each site should be clickable and should open or list relevant Ancient Architects videos.

## Source Strategy

1. Use the Wikimedia Commons SVG as the first reusable base map:
   - Page: https://commons.wikimedia.org/wiki/File:Fertile_crescent_Neolithic_B_circa_7500_BC.svg
   - File: https://upload.wikimedia.org/wikipedia/commons/7/7e/Fertile_crescent_Neolithic_B_circa_7500_BC.svg
   - License: CC BY-SA 4.0
   - Role: immediate visual base and seed labels.

2. Treat the denser Reddit / MapPorn image as a reference only unless permission is obtained:
   - https://www.reddit.com/r/MapPorn/comments/8g8a61/oc_prepottery_neolithic_sites_10000_7000_bce/
   - It likely matches the remembered "many black dots" map better, but its reuse license is unclear.

3. Use open structured data to expand the site list:
   - NERD: Near East Radiocarbon Dates, https://zenodo.org/records/7107507 and https://github.com/apalmisano82/NERD
   - License: CC BY 4.0
   - Role: coordinates, site names, chronological filtering.

4. Use PPND and Open Context as secondary references:
   - PPND: https://www.exoriente.org/associated_projects/ppnd.php
   - Open Context Kahramanmaras Survey: https://opencontext.org/projects/ca4f4719-f2a5-4119-99fa-b04573c8929a

## Execution Phases

1. MVP from Commons SVG
   - Download the SVG into `assets/`.
   - Extract visible site labels into `data/sites.json`.
   - Harvest Ancient Architects PPN playlist metadata into `data/videos.json`.
   - Auto-match videos to sites by title aliases.
   - Overlay clickable hit targets on the SVG.

2. Enriched site catalogue
   - Import NERD.
   - Filter to Near East sites in the target time span.
   - Normalize site names and alternate spellings.
   - Add true latitude/longitude.
   - Decide whether to keep SVG-space map mode, geographic map mode, or both.

3. Video linking
   - Match playlist videos by site aliases and region terms.
   - Add manual overrides for broad videos such as "Taş Tepeler" or "Desert Kites".
   - Store confidence: exact site, regional, thematic, uncertain.

4. Interface
   - Search/filter by site, period, region, and video coverage.
   - Click marker to open a side panel with site data and videos.
   - Add "open all videos for this site" and "copy site/video link".
   - Keep source attribution visible.

5. Verification
   - Validate JSON shape.
   - Check every YouTube URL is syntactically valid.
   - Render desktop/mobile screenshots.
   - Verify no marker text overlaps the core controls.

## Data Files

- `data/sites.json`: site records and SVG coordinates.
- `data/videos.json`: Ancient Architects playlist records.
- Future: `data/site_video_links.json` if matching becomes too large for inline `videoIds`.

## Notes

The current SVG coordinates are label coordinates, not geographic coordinates. They are good enough for the first overlay because the SVG itself is the visual coordinate system. NERD should provide the later geographic coordinate layer.
