#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";
import vm from "node:vm";

const root = path.resolve(import.meta.dirname, "..");
const sourcePath = process.argv[2];
const outputPath = path.join(root, "Linear-A-texts-v2.md");
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

const { records: legacyRecords, glyphReadings } = loadPreferredReadings();
const inscriptions = loadExplorerData(sourcePath);
const output = [
  "# Linear-A-texts",
  "",
  "Complete GORILA-derived Linear A Explorer transcription. Numerals, fractions,",
  "separators, logograms, uncertainty markers, and logical token order are preserved.",
  "Source: https://github.com/mwenge/lineara.xyz/blob/master/LinearAInscriptions.js",
  "Source revision: 083cb0cdbcd908ec2a1e6399a656e01ff3549256",
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
