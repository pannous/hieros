#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";
import vm from "node:vm";

const root = path.resolve(import.meta.dirname, "..");
const sourcePath = process.argv[2];
const faithful = process.argv.includes("--faithful");
const outputPath = path.join(
  root,
  faithful ? "Linear-A-texts-v3.md" : "Linear-A-texts-v2.md",
);
const legacyPath = path.join(
  root,
  "texts/Linear_A_inscriptions_unicode_and_transcription.txt",
);

if (!sourcePath) {
  console.error("usage: build_linear_a_texts_v2.mjs LinearAInscriptions.js");
  process.exit(2);
}

function compactName(name) {
  return name.replace(/[ <>?()+]/g, "").toLowerCase();
}

function loadPreferredReadings() {
  const text = fs.readFileSync(legacyPath, "utf8");
  const blocks = text.trim().split(/\n\s*\n/);
  const records = new Map();
  const glyphReadings = new Map();

  for (const [recordIndex, block] of blocks.entries()) {
    const lines = block.split("\n");
    const unicode = lines.find((line) => line.startsWith("Unicode: "))?.slice(9);
    const reading = lines
      .find((line) => line.startsWith("Transcription: "))
      ?.slice(15);
    if (!unicode || !reading) continue;

    const glyphTokens = unicode.split(/\s+/);
    const readingTokens = reading.split(/\s+/);
    const record = {
      name: lines[0],
      unicode,
      reading,
      recordIndex,
      pairs:
        glyphTokens.length === readingTokens.length
          ? glyphTokens.map((glyph, index) => ({
              glyph,
              reading: readingTokens[index],
            }))
          : [],
    };
    records.set(compactName(lines[0]), record);

    if (glyphTokens.length !== readingTokens.length) continue;
    for (let i = 0; i < glyphTokens.length; i += 1) {
      if (!glyphTokens[i].includes("[")) {
        glyphReadings.set(glyphTokens[i], readingTokens[i]);
      }
    }
  }
  return { records, glyphReadings };
}

function loadExplorerData(filename) {
  const context = vm.createContext({ Map });
  vm.runInContext(fs.readFileSync(filename, "utf8"), context, {
    filename,
    timeout: 10_000,
  });
  if (!(context.inscriptions instanceof Map)) {
    throw new Error("LinearAInscriptions.js did not define an inscriptions Map");
  }
  return context.inscriptions;
}

function displayName(sourceName, legacyRecords) {
  const legacy = legacyRecords.get(compactName(sourceName));
  if (legacy) return legacy.name;
  return sourceName
    .replace(/^([A-Z?]+)(?=\d)/, "$1 ")
    .replace(/([a-z])(?=\d)/, "$1 ");
}

function cleanGlyph(token) {
  return token.replaceAll("𐝫", "");
}

function fallbackReading(token) {
  if (/^\d+$/.test(token) || /[⁄]/.test(token)) return token;
  if (token === "𐄁") return "·";
  if (token === "𐝫") return "?";
  if (/^[—–-]+$/.test(token)) return token;

  return token
    .split("-")
    .map((part) => {
      if (/^(?:A|AB)\d/i.test(part) || part.startsWith("*")) return part;
      return part.toLowerCase();
    })
    .join("-");
}

function preferredReading(glyph, fallback, glyphReadings) {
  const exact = glyphReadings.get(glyph);
  if (exact) return exact;
  const undamaged = glyph.replaceAll("𐝫", "");
  const preferred = glyphReadings.get(undamaged);
  if (!preferred) return fallbackReading(fallback);
  const prefix = glyph.startsWith("𐝫") ? "?-" : "";
  const suffix = glyph.endsWith("𐝫") ? "-?" : "";
  return `${prefix}${preferred}${suffix}`;
}

function legacyPairQueues(record) {
  const queues = new Map();
  for (const pair of record?.pairs ?? []) {
    const key = pair.glyph.replaceAll("[?]", "");
    const queue = queues.get(key) ?? [];
    queue.push(pair);
    queues.set(key, queue);
  }
  return queues;
}

function prettyReading(reading) {
  return reading
    .replaceAll("𐝫", "")
    .replaceAll("*118", "•dwo")
    .replaceAll("AB16", "•qa")
    .replaceAll("AB24", "•ne")
    .replaceAll("AB56", "•nü")
    .replaceAll("*56", "•nü")
    .replaceAll("*79", "•zu")
    .replaceAll("¹⁄₂", "½")
    .replaceAll("¹⁄₄", "¼")
    .replaceAll("³⁄₄", "¾");
}

function buildPhysicalDictionary(inscriptions, glyphReadings) {
  const readings = new Map();
  const add = (glyph, reading, weight = 1) => {
    glyph = glyph.replaceAll("𐝫", "");
    if (!glyph || glyph === "𐄁") return;
    const candidates = readings.get(glyph) ?? new Map();
    candidates.set(reading, (candidates.get(reading) ?? 0) + weight);
    readings.set(glyph, candidates);
  };

  for (const inscription of inscriptions.values()) {
    const glyphs = inscription.words.filter((token) => token !== "\n");
    const ascii = inscription.transliteratedWords.filter(
      (token) => token !== "\n",
    );
    if (glyphs.length !== ascii.length) continue;
    for (let index = 0; index < glyphs.length; index += 1) {
      add(glyphs[index], fallbackReading(ascii[index]));
    }
  }
  for (const [glyph, reading] of glyphReadings) add(glyph, reading, 1000);

  const dictionary = new Map();
  for (const [glyph, candidates] of readings) {
    const [reading] = [...candidates].sort((a, b) => b[1] - a[1])[0];
    dictionary.set(glyph, prettyReading(reading));
  }
  dictionary.set("𐘌", "•qa");
  dictionary.set("𐙈", "•dwo");
  dictionary.set("𐘗", "•ne");
  dictionary.set("𐘰", "•nü");
  dictionary.set("𐙂𐘴𐙎", "ku-ra-•ji");
  return dictionary;
}

function physicalTranscription(raw, dictionary) {
  if (!raw) return { unicode: "[no transcription]", reading: "[no transcription]" };
  const keys = [...dictionary.keys()].sort(
    (a, b) => [...b].length - [...a].length,
  );
  const unicodeRows = [];
  const readingRows = [];

  for (const row of raw.split("\n")) {
    if (!row) {
      unicodeRows.push("");
      readingRows.push("");
      continue;
    }
    const unicodeTokens = [];
    const readingTokens = [];
    let rest = row;
    while (rest) {
      const first = [...rest][0];
      if (first === "𐝫") {
        unicodeTokens.push(first);
        readingTokens.push(first);
        rest = rest.slice(first.length);
        continue;
      }
      if (first === "𐄁") {
        unicodeTokens.push(first);
        readingTokens.push("·");
        rest = rest.slice(first.length);
        continue;
      }
      const match = keys.find((key) => rest.startsWith(key));
      if (match) {
        unicodeTokens.push(match);
        readingTokens.push(dictionary.get(match));
        rest = rest.slice(match.length);
      } else {
        unicodeTokens.push(first);
        readingTokens.push(first);
        rest = rest.slice(first.length);
      }
    }
    unicodeRows.push(unicodeTokens.join(" "));
    readingRows.push(readingTokens.join(" "));
  }
  const joinRows = (rows) => rows.join(" | ").replaceAll(" |  | ", " || ");
  return { unicode: joinRows(unicodeRows), reading: joinRows(readingRows) };
}

const { records: legacyRecords, glyphReadings } = loadPreferredReadings();
const inscriptions = loadExplorerData(sourcePath);
const physicalDictionary = buildPhysicalDictionary(inscriptions, glyphReadings);
const output = [
  "# Linear-A-texts",
  "",
  "Complete GORILA-derived Linear A Explorer transcription. Numerals, fractions,",
  `separators, logograms, uncertainty markers, and ${faithful ? "physical line layout" : "logical token order"} are preserved.`,
  "Source: https://github.com/mwenge/lineara.xyz/blob/master/LinearAInscriptions.js",
  "Source revision: 083cb0cdbcd908ec2a1e6399a656e01ff3549256",
  ...(faithful
    ? [
        "Convention: 𐝫 = source lacuna/damage marker; | = physical line ending; || = blank physical line.",
      ]
    : []),
  "",
];

const orderedInscriptions = [...inscriptions].sort(([keyA, a], [keyB, b]) => {
  const legacyA = legacyRecords.get(compactName(a.name ?? keyA));
  const legacyB = legacyRecords.get(compactName(b.name ?? keyB));
  if (legacyA && legacyB) return legacyA.recordIndex - legacyB.recordIndex;
  if (legacyA) return -1;
  if (legacyB) return 1;
  return (a.name ?? keyA).localeCompare(b.name ?? keyB, "en", {
    numeric: true,
  });
});

for (const [key, inscription] of orderedInscriptions) {
  const glyphTokens = inscription.words.filter((token) => token !== "\n");
  let asciiTokens = inscription.transliteratedWords.filter(
    (token) => token !== "\n",
  );
  if (asciiTokens.length === 0 && glyphTokens.length > 0) {
    asciiTokens = [...glyphTokens];
  }
  if (glyphTokens.length !== asciiTokens.length) {
    throw new Error(
      `${key}: ${glyphTokens.length} glyph tokens but ${asciiTokens.length} readings`,
    );
  }

  const name = displayName(inscription.name ?? key, legacyRecords);
  if (faithful) {
    const physical = physicalTranscription(
      inscription.transcription,
      physicalDictionary,
    );
    output.push(
      `${name}   ${physical.unicode}  `,
      `${physical.reading}  `,
      "",
    );
    continue;
  }
  if (
    glyphTokens.length === 0 ||
    (glyphTokens.length === 1 && glyphTokens[0] === "")
  ) {
    output.push(`${name}   [no transcription]  `, `[no transcription]  `, "");
    continue;
  }
  const record = legacyRecords.get(compactName(inscription.name ?? key));
  const queues = legacyPairQueues(record);
  const unicodeOutput = [];
  const readingOutput = [];
  for (let index = 0; index < glyphTokens.length; index += 1) {
    const glyph = glyphTokens[index];
    if (glyph === "𐝫") continue;
    const cleaned = cleanGlyph(glyph);
    const queue = queues.get(cleaned);
    const legacyPair = queue?.shift();
    if (legacyPair) {
      unicodeOutput.push(legacyPair.glyph.replaceAll("[?]", "?"));
      readingOutput.push(legacyPair.reading);
    } else {
      unicodeOutput.push(cleaned);
      readingOutput.push(
        preferredReading(cleaned, asciiTokens[index], glyphReadings),
      );
    }
  }
  const unicode = unicodeOutput.join(" ");
  const reading = readingOutput.join(" ");

  output.push(`${name}   ${unicode}  `, `${reading}  `, "");
}

fs.writeFileSync(outputPath, `${output.join("\n").trimEnd()}\n`, "utf8");
console.log(`wrote ${inscriptions.size} inscriptions to ${outputPath}`);
