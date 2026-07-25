import { readFile, writeFile } from 'node:fs/promises';

const BASE = 'https://sigla.phis.me/';
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

async function get(url, attempts = 4) {
  for (let i = 0; i < attempts; i++) {
    try {
      const response = await fetch(url);
      if (response.ok) return response.text();
      if (i === attempts - 1) throw new Error(`${response.status} ${url}`);
    } catch (error) {
      if (i === attempts - 1) throw error;
    }
    await sleep(250 * (i + 1));
  }
}

function plain(html = '') {
  return html.replace(/<sub>(.*?)<\/sub>/g, '_$1')
    .replace(/<[^>]+>/g, '')
    .replaceAll('&amp;', '&').replaceAll('&lt;', '<').replaceAll('&gt;', '>')
    .replaceAll('&quot;', '"').replaceAll('&#39;', "'").trim();
}

const unicodeData = await get('https://www.unicode.org/Public/17.0.0/ucd/UnicodeData.txt');
const glyphById = new Map();
for (const row of unicodeData.split('\n')) {
  const [hex, name] = row.split(';');
  const match = name?.match(/^LINEAR A SIGN (.+)$/);
  if (match) glyphById.set(match[1], String.fromCodePoint(parseInt(hex, 16)));
}

const normalizeId = id => id.replace(/^AB(\d+)(.*)$/i, (_, n, rest) => `AB${n.padStart(3, '0')}${rest}`).toUpperCase();
const browse = await get(`${BASE}browse.html`);
const names = [...browse.matchAll(/href="document\/([^"/]+)\/"/g)].map(m => plain(m[1]));
const documents = [...new Set(names)];

async function parseDocument(name) {
  const url = `${BASE}document/${encodeURIComponent(name)}/`;
  const html = await get(url);
  const kind = plain(html.match(/href="\.\.\/\.\.\/kind\/[^"/]+\/">(.*?)<\/a> found at/)?.[1]);
  const site = plain(html.match(/href="\.\.\/\.\.\/location\/[^"/]+\/">(.*?)<\/a>/)?.[1]);
  const period = plain(html.match(/Period: <a[^>]*>(.*?)<\/a>/)?.[1]);
  const corpusUrl = html.match(/<a href="(https?:\/\/[^" ]+)">Link to corpus<\/a>/)?.[1]?.replaceAll('&amp;', '&') ?? '';
  const count = html.match(/(\d+) signs \/ (\d+) words/)?.slice(1).map(Number) ?? [0, 0];
  const occurrences = [];
  for (const match of html.matchAll(/<span[^>]*id="occ-(\d+)"[^>]*>([\s\S]*?)(?=<span[^>]*id="occ-|<\/div><\/div><\/main>)/g)) {
    const n = Number(match[1]);
    const chunk = match[2];
    const reading = chunk.match(/reading-pattern:\(([^,)]+),\s*(true|false)\)/);
    const display = plain(chunk.match(/<span class="(?:sure|unsure)-reading">([\s\S]*?)<\/span>/)?.[1]);
    const role = plain(chunk.match(/<span class="role">(.*?)<\/span>/)?.[1]);
    occurrences.push({ n, id: reading?.[1] ?? '?', uncertain: reading?.[2] === 'true', display: display || '?', role: role || '?' });
  }
  occurrences.sort((a, b) => a.n - b.n);
  const rectClasses = new Map([...html.matchAll(/href="\.\.\/\.\.\/document\/[^"/]+\/index-(\d+)\.html"><rect[^>]*class="([^"]+)"/g)].map(m => [Number(m[1]), m[2]]));
  for (const o of occurrences) o.group = rectClasses.get(o.n)?.match(/\b(even|odd)\b/)?.[1] ?? '';
  const glyphTokens = [], readingTokens = [], idTokens = [], roleTokens = [];
  let previous;
  for (const o of occurrences) {
    const boundary = previous && ((o.group && previous.group && o.group !== previous.group) || o.role !== previous.role);
    if (boundary) { glyphTokens.push(' '); readingTokens.push(' '); idTokens.push(' '); roleTokens.push(' '); }
    const normalized = normalizeId(o.id);
    const glyph = glyphById.get(normalized) ?? `[${o.id}${o.uncertain ? '?' : ''}]`;
    glyphTokens.push(glyph); readingTokens.push(`${o.display}${o.uncertain ? '?' : ''}`); idTokens.push(`${o.id}${o.uncertain ? '?' : ''}`); roleTokens.push(o.role);
    previous = o;
  }
  return { name, kind, site, period, signs: count[0], words: count[1], unicode: glyphTokens.join(''), reading: readingTokens.join('-').replace(/- -/g, ' '), ids: idTokens.join('-').replace(/- -/g, ' '), roles: roleTokens.join(',').replace(/, ,/g, ' | '), source: url, corpusUrl, encoded: occurrences.filter(o => glyphById.has(normalizeId(o.id))).length, occurrences: occurrences.length };
}

const result = [];
for (let i = 0; i < documents.length; i += 5) {
  result.push(...await Promise.all(documents.slice(i, i + 5).map(parseDocument)));
  await sleep(100);
}

const header = `SigLA Linear A tablet texts rendered in Unicode\n================================================\n\nDATA TYPE AND COVERAGE\nActual tablet/document sign sequences derived from SigLA's scholarly sign-attestation\ndataset, not a character inventory and not a translation. This snapshot was generated\n2026-07-25 and contains ${result.length} SigLA documents. SigLA reports that all Linear A\ntablets listed in GORILA are present, plus selected other document types and newer finds.\nEach row preserves the SigLA document identifier, metadata, sign order, approximate word\ngrouping, Unicode glyph sequence, scholarly display readings, GORILA sign identifiers,\nroles, and source links.\n\nMETHOD AND LIMITATIONS\nThe Unicode sequence is a derivative rendering: each classified SigLA GORILA sign ID was\nmapped to its identically named Unicode 17.0 Linear A character. Uncertain readings retain\n"?" in the ID/reading fields. [SIGN-ID] in the Unicode field marks an unclassified,\nunreadable, or not-directly-encoded sign; it is deliberately not guessed. Spaces reflect\nSigLA's word/role grouping, but line layout, two-dimensional placement, numerals not tagged\nas sign attestations, erasures, damage, and palaeographic variants cannot be fully preserved\nin plain text. Consult each source URL and drawing for the authoritative context. Linear A\nis undeciphered; display readings are conventional approximate values, not translations.\n\nPROVENANCE, ATTRIBUTION, AND LICENSE\nDerived from SigLA: The Signs of Linear A, by Ester Salgarella and Simon Castellan.\nDataset snapshot source: https://sigla.phis.me/\nCoverage/change log: https://sigla.phis.me/about.html\nDataset documentation: https://sigla.phis.me/paper.html\nSigLA dataset and drawings license: Creative Commons Attribution-NonCommercial-ShareAlike\n4.0 International (CC BY-NC-SA 4.0):\nhttps://creativecommons.org/licenses/by-nc-sa/4.0/\nThis transformed Unicode transcription is distributed under the same CC BY-NC-SA 4.0\nlicense. Changes made: selected textual metadata/sign attestations, ordered occurrences,\nmapped GORILA IDs to Unicode 17.0 code points, and serialized as UTF-8 TSV. No drawings or\nGORILA page images are included.\nUnicode mapping source: https://www.unicode.org/Public/17.0.0/ucd/UnicodeData.txt\nUnicode data license: https://www.unicode.org/license.txt\nINSCRIBE viewer (3D models; no direct alignment asserted here):\nhttps://www.inscribercproject.com/3d_viewer_home.php\n\nFORMAT\nTab-separated UTF-8. One record per document. A "-" value means SigLA did not supply it.\n\ndocument\ttype\tsite\tperiod\tsigns_reported\twords_reported\toccurrences_extracted\tunicode_encoded\tunicode_sequence\tscholarly_reading\tgorila_sign_ids\troles\tsigla_url\tcorpus_url\n`;
const clean = value => String(value ?? '').replace(/[\t\r\n]+/g, ' ').trim() || '-';
const lines = result.map(d => [d.name,d.kind,d.site,d.period,d.signs,d.words,d.occurrences,d.encoded,d.unicode,d.reading,d.ids,d.roles,d.source,d.corpusUrl].map(clean).join('\t'));
await writeFile('texts/Linear_A_SigLA_tablet_texts_unicode.tsv', header + lines.join('\n') + '\n', 'utf8');
console.log(JSON.stringify({documents: result.length, occurrences: result.reduce((n,d)=>n+d.occurrences,0), encoded: result.reduce((n,d)=>n+d.encoded,0), zero: result.filter(d=>!d.occurrences).map(d=>d.name), unencoded: result.reduce((n,d)=>n+d.occurrences-d.encoded,0)}, null, 2));
