/***************************************************************************/
/*                                                                         */
/*  REScodeNotes.java                                                      */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// notes in REScode.

package nederhof.res;

import java.awt.*;
import java.util.*;

public class REScodeNotes {
    public String string;
    public int fileNumber;
    public Color16 color;
    public int size;
    public int x;
    public int y;
    public REScodeNotes tl;

    // Any problem during parsing?
    public boolean failed;

    // Only report error once for each glyph. Here record of reported errors.
    private static HashSet errors = new HashSet();

    public REScodeNotes() {
	string = "";
	fileNumber = 0;
	color = Color16.WHITE;
	size = 1000;
	x = 500;
	y = 500;
	tl = null;
	failed = false;
    }

    // Read zero or more notes. Return pointer to list,
    // possibly null.
    public static REScodeNotes read(ParseBuffer in) {
	int oldPos = in.pos;
	if (!in.readChar('n'))
	    return null;
	REScodeNotes notes = new REScodeNotes();
	String string = notes.string;
	int fileNumber = notes.fileNumber;
	int colorCode = 0;
	int size = notes.size;
	int x = notes.x;
	int y = notes.y;
	if ((string = in.readString()) == null ||
		(fileNumber = in.readInt()) == Integer.MAX_VALUE ||
		(colorCode = in.readInt()) == Integer.MAX_VALUE ||
		colorCode < 0 || colorCode >= Color16.SIZE ||
		(size = in.readInt()) == Integer.MAX_VALUE ||
		(x = in.readInt()) == Integer.MAX_VALUE ||
		(y = in.readInt()) == Integer.MAX_VALUE) {
	    in.pos = oldPos;
	    in.parseError("Ill-formed REScode note");
	    notes.failed = true;
	    return notes;
	}
	notes.string = string;
	notes.fileNumber = fileNumber;
	notes.color = new Color16(colorCode);
	notes.size = size;
	notes.x = x;
	notes.y = y;
	notes.tl = read(in);
	if (notes.tl != null && notes.tl.failed)
	    notes.failed = true;
	return notes;
    }

    // Convert to string.
    public String toString() {
	return "n " + string + " " + fileNumber + " " +
	    color.code() + " " +
	    size + " " + x + " " + y + " " +
	    (tl != null ? tl.toString() : "");
    }

    // Render.
    public void render(UniGraphics image, int x, int y, 
	    HieroRenderContext context) {
	OptionalGlyphs glyph = glyph(context);
	Rectangle glyphRect = glyphRect(x, y, context, glyph);
	image.renderStraight(glyph, glyphRect.x, glyphRect.y);
	if (tl != null)
	    tl.render(image, x, y, context);
    }

    // Get glyphs. Report error only once for each string.
    private OptionalGlyphs glyph(HieroRenderContext context) {
	Color color = context.effectColor(this.color).getColor();
	int size = (int) Math.round(this.size * context.fontSize() / 1000.0);
	OptionalGlyphs glyph;
	if (context.canDisplay(fileNumber, string)) {
	    Font font = context.getFont(fileNumber);
	    glyph = new Glyphs(string, font, color, size, context);
	} else {
	    if (!errors.contains(string)) {
		System.err.println("Cannot display string " + string +
			" of file " + fileNumber);
		errors.add(string);
	    }
	    glyph = new ErrorGlyphs(context.emSizePix()/2, context);
	}
	return glyph;
    }

    // Get rectangle covered by glyphs.
    private Rectangle glyphRect(int x, int y, HieroRenderContext context,
	                OptionalGlyphs glyph) {
	int width = glyph.dimension().width;
	int height = glyph.dimension().height;
	int xCorner = context.milEmToPix(this.x + x) - width/2;
	int yCorner = context.milEmToPix(this.y + y) - height/2;
	return new Rectangle(xCorner, yCorner, width, height);
    }
}
