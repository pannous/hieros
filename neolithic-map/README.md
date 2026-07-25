# Neolithic Sites / Ancient Architects Map

Static interactive prototype for mapping Epipalaeolithic, Neolithic, and Pre-Pottery Neolithic sites in the Fertile Crescent and linking them to Ancient Architects videos.

## Run

From the repository root:

```sh
python3 -m http.server 8765
```

Then open:

```text
http://localhost:8765/neolithic-map/
```

## Current Data

- Base map: Wikimedia Commons `Fertile crescent Neolithic B circa 7500 BC.svg`, CC BY-SA 4.0.
- Sites: extracted from the SVG text labels as SVG-space coordinates.
- Videos: `Pre-Pottery Neolithic | Fertile Crescent` and `Göbekli Tepe` Ancient Architects playlists, harvested with `yt-dlp` and deduplicated by YouTube id.

## Next Data Pass

Replace approximate SVG placements with sourced coordinates for video-only sites, especially:

```text
Karahan Tepe, Sefer Tepe, Çakmaktepe, Gürcü Tepe, Harbetsuvan Tepesi,
Boncuklu Tarla, Kurt Tepesi, Kahin Tepe, Ohalo II, Tell Qaramel,
WF16, Hilazon Tachtit, Sayburç, Mendiktepe, Çemka Höyük, Taşlı Tepe,
Sırçalıtepe, Gre Fılla Höyük, Direkli Cave, En Esur.
```

NERD is the preferred open coordinate source for a larger site layer:

```text
https://zenodo.org/records/7107507
https://github.com/apalmisano82/NERD
```
