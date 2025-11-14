import ctypes, unicodedata
from ctypes import c_void_p, c_uint32, c_bool, c_ulong

ct = ctypes.CDLL("/System/Library/Frameworks/CoreText.framework/CoreText")

CFStringCreateWithCharacters = ctypes.CFUNCTYPE(
    c_void_p, c_void_p, ctypes.POINTER(c_uint32), c_ulong
)(("CFStringCreateWithCharacters", ctypes.CDLL("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation")))

CTFontCreateForString = ct.CTFontCreateForString
CTFontCreateForString.restype = c_void_p
CTFontCreateForString.argtypes = [c_void_p, c_void_p, c_void_p]

CTFontGetGlyphCount = ct.CTFontGetGlyphCount
CTFontGetGlyphCount.restype = c_ulong
CTFontGetGlyphCount.argtypes = [c_void_p]

# test a character
def has_font_for(cp):
    # make a CFString from the char
    arr = (c_uint32 * 1)(cp)
    s = CFStringCreateWithCharacters(None, arr, 1)
    font = CTFontCreateForString(None, s, None)
    return bool(font)


for i in range(0x0, 0x10200):
    if has_font_for(i):
        print(hex(i), "→ has font")

for i in range(0xF0000, 0xF0100):
    if has_font_for(i):
        print(hex(i), "→ has font")
print("DONE")