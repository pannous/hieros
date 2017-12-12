// ResLite (formerly REScode) for hieroglyphic.

// Context.
function ResContext() {
    this.emSize = 35;
    this.padding = 1.0;
}
// Convert size in 1/1000 EM to number of pixels.
ResContext.prototype.milEmToPix =
function(d) {
    return Math.round(d * this.emSize / 1000.0);
}

// Top constructor.
function ResLite(dir, size, groups) {
    this.dir = dir;
    this.size = size;
    this.groups = groups;
}
// Constructor of groups.
function ResGroups() {
    this.advance = 1000;
    this.length = 1000;
    this.exprs = null;
    this.notes = null;
    this.shades = null;
    this.intershades = null;
    this.tl = null;
}
// Construction of expressions.
function ResGlyph() {
    this.fileNumber = 0;
    this.glyphIndex = 0;
    this.mirror = false;
    this.rotate = 0;
    this.color = "white";
    this.xscale = 1000;
    this.yscale = 1000;
    this.x = 500;
    this.y = 500;
    this.xMin = 0;
    this.yMin = 0;
    this.width = 1000;
    this.height = 1000;
    this.tl = null;
}
// Construction of pairs.
function ResPair() {
    this.list1 = null;
    this.list2 = null;
    this.tl = null;
}
// Construction of notes.
function ResNotes() {
    this.string = "";
    this.fileNumber = 0;
    this.color = "white";
    this.size = 1000;
    this.x = 500;
    this.y = 500;
    this.tl = null;
}
// Construction of shades.
function ResShades() {
    this.x = 0;
    this.y = 0;
    this.width = 0;
    this.height = 0;
    this.tl = null;
}

function ParseBuffer(string) {
    this.string = string;
    this.pos = 0;
    this.error = false;
}
ParseBuffer.prototype.isEmpty =
function() {
    return this.pos == this.string.length;
};
ParseBuffer.prototype.remainder =
function() {
    return this.string.substring(this.pos);
};
ParseBuffer.prototype.readToNonspace =
function() {
    while (!this.isEmpty() &&
	    this.string.charAt(this.pos).replace(/^\s+|\s+$/gm,'').length == 0)
	this.pos++;
};
ParseBuffer.prototype.readToSpace =
function() {
    while (!this.isEmpty() &&
	    this.string.charAt(this.pos).replace(/^\s+|\s+$/gm,'').length != 0)
	this.pos++;
};
ParseBuffer.prototype.readToEnd = 
function() {
    while (!this.isEmpty() &&
	    this.string.charAt(this.pos) != 'e')
	this.pos++;
    if (!this.isEmpty())
	this.pos++;
};
ParseBuffer.prototype.readChar =
function(c) {
    var oldPos = this.pos;
    this.readToSpace();
    if (this.pos == oldPos+1 && this.string.charAt(oldPos) == c) {
	this.readToNonspace();
	return true;
    } else {
	this.pos = oldPos;
	return false;
    }
};
ParseBuffer.prototype.readSingleChar =
function(c) {
    if (!this.isEmpty() && this.string.charAt(this.pos) == c) {
	this.pos++;
	return true;
    } else
	return false;
};
ParseBuffer.prototype.peekChar =
function(c) {
    return !this.isEmpty() && this.string.charAt(this.pos) == c;
}
ParseBuffer.prototype.readDirection =
function() {
    var dir = undefined;
    var oldPos = this.pos;
    this.readToSpace();
    if (this.pos != oldPos+3) {
	this.pos = oldPos;
	return dir;
    } else if (this.string.indexOf("hlr", oldPos) == oldPos)
	dir = "hlr";
    else if (this.string.indexOf("hrl", oldPos) == oldPos)
	dir = "hrl";
    else if (this.string.indexOf("vlr", oldPos) == oldPos)
	dir = "vlr";
    else if (this.string.indexOf("vrl", oldPos) == oldPos)
	dir = "vrl";
    else {
	this.pos = oldPos;
	return dir;
    }
    this.readToNonspace();
    return dir;
};
ParseBuffer.prototype.readNum =
function() {
    var i = undefined;
    var oldPos = this.pos;
    this.readToSpace();
    if (this.pos <= oldPos)
	return i;
    if (this.string.substring(oldPos, this.pos).replace(/^[0-9]*$/gm,'').length == 0)
	i = parseInt(this.string.substring(oldPos, this.pos));
    this.readToNonspace();
    return i;
};
ParseBuffer.prototype.readString =
function() {
    var end = this.readAcrossString();
    if (end >= this.pos + 3) {
	var sub = this.string.substring(this.pos, end);
	this.pos = end;
	this.readToNonspace();
	return sub;
    } else
	return undefined;
};
ParseBuffer.prototype.readAcrossString =
function() {
    var newPos = this.pos;
    if (this.string.charAt(newPos) == '\"') {
	newPos++;
	while (newPos < this.string.length) {
	    if (this.string.charAt(newPos) == '\"') {
		newPos++;
		if (newPos >= this.string.length ||
			this.string.charAt(newPos).replace(/^\s+|\s+$/gm,'').length == 0)
		    return newPos;
		else
		    return undefined;
	    } else if (this.string.charAt(newPos) == '\\') {
		newPos++;
		if (this.string.charAt(newPos) == '\"' ||
			this.string.charAt(newPos) == '\\')
		    newPos++;
		else
		    return undefined;
	    } else if (this.string.charAt(newPos) == ' ' ||
		    this.string.charAt(newPos).replace(/^\s+|\s+$/gm,'').length != 0)
		newPos++;
	    else
		return undefined;
	}
	return undefined;
    } else
	return undefined;
};

/////////////////////////////////////////////////////////////////////////////
// Parsing.

// Parse string into structure.
function parseResLite(str) {
    var resLite = new ResLite("hlr", 1000, null);
    var buffer = new ParseBuffer(str);
    buffer.readToNonspace();
    if (buffer.isEmpty())
	return;
    if (!readChunk(resLite, buffer))
	buffer.readToEnd();
    var remainder = buffer.remainder();
    if (remainder.replace(/^\s+|\s+$/gm,'').length != 0)
	alert("ResLite trailing symbols:" + remainder);
    return resLite;
}

// Try read header, then groups, then 'e'.
function readChunk(resLite, buffer) {
    var oldPos = buffer.pos;
    var newDir;
    var newSize;
    if (!buffer.readChar("$") ||
	    (newDir = buffer.readDirection()) == undefined ||
	    (newSize = buffer.readNum()) == undefined) {
	buffer.pos = oldPos;
	alert("ResLite ill-formed header");
	return false;
    }
    resLite.dir = newDir;
    resLite.size = newSize;
    resLite.groups = readGroups(buffer);
    if (!buffer.readSingleChar("e")) {
	alert("ResLite missing end");
	return false;
    } else 
	return true;
}

// Read zero or more groups. Return pointer to list, possibly null.
function readGroups(buffer) {
    var oldPos = buffer.pos;
    if (!buffer.readChar("g"))
	return null;
    var groups = new ResGroups();
    var advance;
    var length;
    if ((advance = buffer.readNum()) == undefined ||
	    (length = buffer.readNum()) == undefined) {
	buffer.pos = oldPos;
	alert("ResLite ill-formed group header");
	return groups;
    }
    groups.advance = advance;
    groups.length = length;
    groups.exprs = readExprs(buffer);
    groups.notes = readNotes(buffer);
    groups.shades = readShades(buffer);
    if (!buffer.readChar("i")) {
	buffer.pos = oldPos;
	alert("ResLite missing i in group");
	return groups;
    }
    groups.intershades = readShades(buffer);
    groups.tl = readGroups(buffer);
    return groups;
}

// Read zero or more expressions. Return pointer to list, possibly null.
function readExprs(buffer) {
    if (buffer.peekChar("c")) {
	var exprs = readGlyph(buffer);
	exprs.tl = readExprs(buffer);
	return exprs;
    } else if (buffer.peekChar("(")) {
	var exprs = readPair(buffer);
	exprs.tl = readExprs(buffer);
	return exprs;
    } else
	return null;
}

// Read glyph.
function readGlyph(buffer) {
    var oldPos = buffer.pos;
    var glyph = new ResGlyph();
    if (!buffer.readChar("c")) {
	alert("ResLite missing c in glyph");
	return glyph;
    }
    var fileNumber;
    var glyphIndex;
    var mirror;
    var rotate;
    var colorCode;
    var xscale;
    var yscale;
    var x;
    var y;
    var xMin;
    var yMin;
    var width;
    var height;
    if ((fileNumber = buffer.readNum()) == undefined ||
	    (glyphIndex = buffer.readNum()) == undefined ||
	    (mirror = buffer.readNum()) == undefined ||
	    (rotate = buffer.readNum()) == undefined ||
	    (colorCode = buffer.readNum()) == undefined ||
	    colorCode < 0 || colorCode >= 16 ||
	    (xscale = buffer.readNum()) == undefined ||
	    (yscale = buffer.readNum()) == undefined ||
	    (x = buffer.readNum()) == undefined ||
	    (y = buffer.readNum()) == undefined ||
	    (xMin = buffer.readNum()) == undefined ||
	    (yMin = buffer.readNum()) == undefined ||
	    (width = buffer.readNum()) == undefined ||
	    (height = buffer.readNum()) == undefined) {
	buffer.pos = oldPos;
	alert("ResLite ill-formed glyph");
	return glyph;
    }
    glyph.fileNumber = fileNumber;
    glyph.glyphIndex = glyphIndex;
    glyph.mirror = (mirror != 0);
    glyph.rotate = rotate;
    glyph.color = numToColor(colorCode);
    glyph.xscale = xscale;
    glyph.yscale = yscale;
    glyph.x = x;
    glyph.y = y;
    glyph.xMin = xMin;
    glyph.yMin = yMin;
    glyph.width = width;
    glyph.height = height;
    return glyph;
}

// Read pair.
function readPair(buffer) {
    var oldPos = buffer.pos;
    var pair = new ResPair();
    if (!buffer.readChar("(")) {
	alert("ResLite missing ( in pair");
	return pair;
    }
    pair.list1 = readExprs(buffer);
    if (!buffer.readChar("o")) {
	buffer.pos = oldPos;
	alert("ResLite missing o in pair");
	return pair;
    }
    pair.list2 = readExprs(buffer);
    if (!buffer.readChar(")")) {
	buffer.pos = oldPos;
	alert("ResLite missing ) in pair");
	return pair;
    }
    return pair;
}

// Read zero or more notes. Return pointer to list, possibly null.
function readNotes(buffer) {
    var oldPos = buffer.pos;
    if (!buffer.readChar("n"))
	return null;
    var notes = new ResNotes();
    var string;
    var fileNumber;
    var colorCode;
    var size;
    var x;
    var y;
    if ((string = buffer.readString()) == null ||
	    (fileNumber = buffer.readNum()) == undefined ||
	    (colorCode = buffer.readNum()) == undefined ||
	    colorCode < 0 || colorCode >= 16 ||
	    (size = buffer.readNum()) == undefined ||
	    (x = buffer.readNum()) == undefined ||
	    (y = buffer.readNum()) == undefined) {
	buffer.pos = oldPos;
	alert("ResLite ill-formed note");
	return notes;
    }
    notes.string = string;
    notes.fileNumber = fileNumber;
    notes.color = numToColor(colorCode);
    notes.size = size;
    notes.x = x;
    notes.y = y;
    notes.tl = readNotes(buffer);
    return notes;
}

// Read zero or more shades. Return pointer to list, possibly null.
function readShades(buffer) {
    var oldPos = buffer.pos;
    if (!buffer.readChar("s"))
	return null;
    var shades = new ResShades();
    var x;
    var y;
    var width;
    var height;
    if ((x = buffer.readNum()) == undefined ||
	    (y = buffer.readNum()) == undefined ||
	    (width = buffer.readNum()) == undefined ||
	    (height = buffer.readNum()) == undefined) {
        buffer.pos = oldPos;
        alert("ResLite ill-formed shade");
	return shades;
    }
    shades.x = x;
    shades.y = y;
    shades.width = width;
    shades.height = height;
    shades.tl = readShades(buffer);
    return shades;
}

// Convert color number to name.
function numToColor(num) {
    switch (num) {
	case 0: return "white";
	case 1: return "silver";
	case 2: return "gray";
	case 3: return "yellow";
	case 4: return "fuchsia";
	case 5: return "aqua";
	case 6: return "olive";
	case 7: return "purple";
	case 8: return "teal";
	case 9: return "red";
	case 10: return "lime";
	case 11: return "blue";
	case 12: return "maroon";
	case 13: return "green";
	case 14: return "navy";
	case 15: return "black";
	default: return "black";
    }
}

/////////////////////////////////////////////////////////////////////////////
// Formatting.

function ResDivision() {
    this.original = null; // complete ResLite before dividing
    this.resContext = null; // ResContext
    this.initialNumber = 0; // that fit within limit
    this.initialSize = 0; // size of initial prefix, in 1000 * EM
    this.pad = 0; // allowable padding inside prefix, in 1000 * EM
    this.remainder; // remaining ResLite that does not fit within limit.
}
ResDivision.prototype.getWidthMil = // expressed in 1000 * EM
function() {
    if (this.original.dir == "hlr" || this.original.dir == "hrl") 
	return this.initialSize + this.pad;
    else
	return this.original.size;
}
ResDivision.prototype.getHeightMil = // expressed in 1000 * EM
function() {
    if (this.original.dir == "vlr" || this.original.dir == "vrl") 
	return this.initialSize + this.pad;
    else
	return this.original.size;
}
ResDivision.prototype.getWidth = // expressed in pixels
function() {
    return this.resContext.milEmToPix(this.getWidthMil()); 
}
ResDivision.prototype.getHeight = // expressed in pixels
function() {
    return this.resContext.milEmToPix(this.getHeightMil()); 
}
ResDivision.prototype.render = 
function(hi) {
    var marginPt = 3;
    var marginMil = 1000.0 * marginPt / this.resContext.emSize;
    var ctx = hi.getContext("2d");
    hi.width = this.getWidth() + 2 * marginPt;
    hi.height = this.getHeight() + 2 * marginPt;
    renderDivisionGroups(this.original.groups, ctx, 
	this.getWidthMil(), this.getHeightMil(), this.original.dir,
	this.resContext, this.initialNumber, this.pad, marginMil);
}

// Process input until len limit is reached.
// len is in pt, convert to 1000 * EM. 
// len can be infinite (= no limit).
// Is padding allowed?
function makeDivision(resLite, len, resContext, paddingAllowed) {
    var div = new ResDivision();
    div.original = resLite;
    div.resContext = resContext;
    var lenMilEm;
    if (len == Number.MAX_VALUE)
	lenMilEm = Number.MAX_VALUE;
    else
	lenMilEm = Math.round(1000.0 * len / resContext.emSize);
    computeLengthLimit(div, lenMilEm);
    if (paddingAllowed && lenMilEm != Number.MAX_VALUE) {
	div.pad = lenMilEm - div.initialSize;
	if (div.pad > summedSepDiv(div) * resContext.padding)
	    div.pad = 0;
    }
    return div;
}

// Process until limit. Store remainder.
// Express distances in 1000 EM, to minimize rounding-off errors.
function computeLengthLimit(div, max) {
    var groups = div.original.groups;
    div.initialNumber = 0;
    div.initialSize = 0;
    var start = 0;
    var isFirst = true;
    while (groups != null) {
	var advance = (isFirst ? 0 : groups.advance);
	if (start + advance + groups.length <= max) {
	    start += advance;
	    div.initialNumber++;
	    div.initialSize = start + groups.length;
	    groups = groups.tl;
	    isFirst = false;
	} else
	    break;
    }
    div.remainder = new ResLite(div.original.dir, div.original.size, groups);
}

// Summed separation between groups, in 1000 * EM.
function summedSepDiv(div) {
    if (div.initialNumber <= 1 || div.original.groups == null)
	return 0;
    else
	return summedSepGroups(div.original.groups, initialNumber);
}
// Return sum of separations between groups, in first n groups.
function summedSepGroups(groups, n) {
    if (n > 1) {
	if (groups.tl != null) {
	    return groups.tl.advance - groups.length + summedSepGroups(groups.tl, n-1);
	} else
	    return 0;
    } else
	return 0;
}

/////////////////////////////////////////////////////////////
// Rendering.

function renderDivisionGroups(groups, ctx, wMil, hMil, dir, resContext, n, padding, margin) {
    // var wPix = resContext.milEmToPix(wMil);
    // var hPix = resContext.milEmToPix(hMil);
    var hor = dir == "hlr" || dir == "hlr";
    // var crackFill fill = new CrackFill(hor, wPix, hPix, resContext);
    var initRect = firstRect(dir, wMil, hMil, margin);
    renderGroups(groups, ctx, initRect, dir, resContext, n, padding, true);
    // renderShades(ctx, initRect, fill, dir, resContext, n, padding, true);
    // renderNotes(ctx, initRect, dir, resContext, n, padding, true);
}
function renderGroups(groups, ctx, rect, dir, resContext, n, padding, isFirst) {
    if (n <= 0 || groups == null) 
	return;
    var pad = (isFirst ? 0 : padding / n);
    padding -= pad;
    var newRect = updateRect(rect, groups.advance, groups.length, dir, isFirst, pad);
    renderExprs(groups.exprs, ctx, newRect, dir, false, resContext);
    renderGroups(groups.tl, ctx, newRect, dir, resContext, n-1, padding, false);
}
function renderExprs(exprs, ctx, rect, dir, isClipped, resContext) {
    if (exprs == null)
	;
    else if (exprs instanceof ResGlyph)
	renderGlyph(exprs, ctx, rect, dir, false, resContext);
    else 
	renderPair(exprs, ctx, rect, dir, false, resContext);
}
function renderGlyph(glyph, ctx, rect, dir, isClipped, resContext) {
    var size = resContext.milEmToPix(glyph.yscale);
    if (glyph.fileNumber == 1)
	var font = "" + size + "px NewGardiner";
    else if (glyph.fileNumber == 2)
	var font = "" + size + "px HieroglyphicAux";
    else {
	alert("ResLite unknown file number");
	return;
    }
    var str = String.fromCharCode(glyph.glyphIndex);
    ctx.save();
    ctx.font = font;
    ctx.fillStyle = glyph.color;
    ctx.textBaseline = "alphabetic";
    var gRect = glyphRect(str, size, font);
    if (glyph.rotate == 0 && !glyph.mirror && glyph.xscale == glyph.yscale) {
	ctx.fillText(str,
	    -gRect.x + resContext.milEmToPix(rect.x + glyph.x) - gRect.w/2, 
	    -gRect.y + resContext.milEmToPix(rect.y + glyph.y) + gRect.h/2);
    } else {
	var x = -gRect.x + resContext.milEmToPix(rect.x + glyph.x);
	var y = -gRect.y + resContext.milEmToPix(rect.y + glyph.y);
	var xDiff = -gRect.w/2;
	var yDiff = gRect.h/2;
	ctx.translate(x, y);
	ctx.rotate(glyph.rotate*Math.PI/180);
	if (glyph.mirror)
	    ctx.scale(-1.0 * glyph.xscale / glyph.yscale, 1);
	else
	    ctx.scale(1.0 * glyph.xscale / glyph.yscale, 1);
	ctx.fillText(str, xDiff, yDiff);
    }
    ctx.restore();
    renderExprs(glyph.tl, ctx, rect, dir, isClipped, resContext);
}
function renderPair(pair, ctx, rect, dir, isClipped, resContext) {
    renderExprs(pair.list1, ctx, rect, dir, isClipped, resContext);
    renderExprs(pair.list2, ctx, rect, dir, isClipped, resContext);
    renderExprs(pair.tl, ctx, rect, dir, isClipped, resContext);
}

function Rect(x, y, w, h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
}
function firstRect(dir, width, height, margin) {
    if (dir == "hlr" || dir == "hrl")
	width = 0;
    else
	height = 0;
    return new Rect(margin, margin, width, height);
}
function updateRect(rect, adv, len, dir, isFirst, pad) {
    var advance = (isFirst ? 0 : adv + pad);
    var x = rect.x;
    var y = rect.y;
    var width = rect.width;
    var height = rect.height;
    if (dir == "hlr" || dir == "hrl") {
	x += advance;
	width = len;
    } else {
	y += advance;
	height = len;
    }
    return new Rect(x, y, width, height);
}

// Compute rectangle of glyph.
function glyphRect(str, size, font) {
    var measCanvas = document.createElement("canvas");
    var ctx = measCanvas.getContext("2d");
    ctx.font = font;
    var width = Math.round(ctx.measureText(str).width);
    var height = Math.round(size);
    var lMargin = Math.floor(width / 5);
    var rMargin = Math.floor(width / 5);
    var tMargin = Math.floor(height / 5);
    var bMargin = Math.floor(height / 2);
    var w = width + lMargin + rMargin;
    var h = height + tMargin + bMargin;
    measCanvas.width = w;
    measCanvas.height = h;
    ctx = measCanvas.getContext("2d");
    ctx.font = font;
    ctx.textBaseline = "alphabetic";
    ctx.fillText(str, lMargin, height + tMargin);
    var data = ctx.getImageData(0, 0, w, h).data;
    var t = 0;
    for (var y = 0; y < h; y++) {
	var rowEmpty = true;
	for (var x = 0; x < w; x++) {
	    var col = data[y * w * 4 + x * 4 + 3] +
		data[y * w * 4 + x * 4 + 0] +
		data[y * w * 4 + x * 4 + 1] +
		data[y * w * 4 + x * 4 + 2];
	    if (col > 0) {
		rowEmpty = false;
		break;
	    }
	}
	if (rowEmpty) 
	    t++;
	else
	    break;
    }
    var b = 0;
    for (var y = h-1; y >= 0; y--) {
	var rowEmpty = true;
	for (var x = 0; x < w; x++) {
	    var col = data[y * w * 4 + x * 4 + 3] +
		data[y * w * 4 + x * 4 + 0] +
		data[y * w * 4 + x * 4 + 1] +
		data[y * w * 4 + x * 4 + 2];
	    if (col > 0) {
		rowEmpty = false;
		break;
	    }
	}
	if (rowEmpty) 
	    b++;
	else
	    break;
    }
    var l = 0;
    for (var x = 0; x < w; x++) {
        var colEmpty = true;
        for (var y = 0; y < h; y++) {
            var row = data[y * w * 4 + x * 4 + 3] +
                data[y * w * 4 + x * 4 + 0] +
                data[y * w * 4 + x * 4 + 1] +
                data[y * w * 4 + x * 4 + 2];
            if (row > 0) {
                colEmpty = false;
                break;
            }
        }
        if (colEmpty)
            l++;
        else
            break;
    }
    var r = 0;
    for (var x = w-1; x >= 0; x--) {
        var colEmpty = true;
        for (var y = 0; y < h; y++) {
            var row = data[y * w * 4 + x * 4 + 3] +
                data[y * w * 4 + x * 4 + 0] +
                data[y * w * 4 + x * 4 + 1] +
                data[y * w * 4 + x * 4 + 2];
            if (row > 0) {
                colEmpty = false;
                break;
            }
        }
        if (colEmpty)
            r++;
        else
            break;
    }
    return new Rect(l - lMargin, b - bMargin, w - l - r, h - t - b);
}

/////////////////////////////////////////////////////////////
// Font.

// Has font been loaded?
// We look at two signs of which the relative widths are know. Does this
// match?
// If not, the right font is not loaded.
function fontLoaded(font, s1, s2, ratio) {
    var measCanvas = document.createElement("canvas");
    var ctx = measCanvas.getContext("2d");
    ctx.font = font;
    var w1 = ctx.measureText(s1).width;
    var w2 = ctx.measureText(s2).width;
    if (Math.abs(ratio - (1.0 * w1 / w2)) > 0.1)
        return "expected " + ratio + " got " + (1.0 * w1 / w2);
    else
        return "";
}

function fontsLoaded() {
    var fontGard = "50px NewGardiner";
    var fontAux = "50px HieroglyphicAux";
    var result1 = "" + fontLoaded(fontGard, "\uE03E", "\uE03F", 0.34);
    var result2 = "" + fontLoaded(fontAux, "\u0023", "\u0028", 0.29);
    if (result1 == "" && result2 == "")
        return "";
    else
        return "newGardiner " +
            fontLoaded(fontGard, "\uE03E", "\uE03F", 0.34) +
            "HieroglyphicAux " +
            fontLoaded(fontAux, "\u0023", "\u0028", 0.29);
}
