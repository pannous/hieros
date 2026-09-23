// osascript -l JavaScript probes/coretext_fallback.js 3F27C 3DDE5 ... : which font CoreText falls back to per code point (as Sublime Text does).
ObjC.import('CoreText');
ObjC.import('Foundation');
function run(argv) {
    const base = $.CTFontCreateWithName($('Menlo'), 14, null);
    return argv.map(hex => {
        const text = $(String.fromCodePoint(parseInt(hex, 16)));
        const font = $.CTFontCreateForString(base, text, $.CFRangeMake(0, text.length));
        return `U+${hex} -> ${ObjC.castRefToObject($.CTFontCopyPostScriptName(font)).js}`;
    }).join('\n');
}
