// osascript -l JavaScript probes/coretext_file_charset.js FONTFILE HEX... : does CoreText's character set for this font file (not the installed one) contain each code point?
ObjC.import('CoreText');
ObjC.import('Foundation');
function run(argv) {
    const url = $.NSURL.fileURLWithPath($(argv[0]));
    const descriptors = ObjC.castRefToObject($.CTFontManagerCreateFontDescriptorsFromURL(url));
    const font = $.CTFontCreateWithFontDescriptor(descriptors.objectAtIndex(0), 14, null);
    const charset = $.CTFontCopyCharacterSet(font);
    return argv.slice(1).map(hex => `U+${hex} ${$.CFCharacterSetIsLongCharacterMember(charset, parseInt(hex, 16))}`).join('  ');
}
