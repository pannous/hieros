/***************************************************************************/
/*                                                                         */
/*  REScodeGlyph.java                                                      */
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

// glyph in REScode.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

public class REScodeGlyph extends REScodeExprs {
    public int fileNumber;
    public long glyphIndex;
    public boolean mirror;
    public int rotate;
    public Color16 color;
    public int xscale;
    public int yscale;
    public int x;
    public int y;
    public int xMin;
    public int yMin;
    public int width;
    public int height;

    // Only report error once for each glyph. Here record of reported errors.
    private static HashSet errors = new HashSet();

    public REScodeGlyph() {
	fileNumber = 0;
	glyphIndex = 0;
	mirror = false;
	rotate = 0;
	color = Color16.WHITE;
	xscale = 1000;
	yscale = 1000;
	x = 500;
	y = 500;
	xMin = 0;
	yMin = 0;
	width = 1000;
	height = 1000;
    }

    // Read glyph. Make it failed if there is no valid glyph.
    public static REScodeExprs read(ParseBuffer in) {
	int oldPos = in.pos;
	REScodeGlyph glyph = new REScodeGlyph();
	if (!in.readChar('c')) {
	    in.parseError("Missing c in REScode glyph");
	    glyph.failed = true;
	    return glyph;
	}
	int fileNumber = glyph.fileNumber;
	long glyphIndex = glyph.glyphIndex;
	int mirror = 0;
	int rotate = glyph.rotate;
	int colorCode = 0;
	int xscale = glyph.xscale;
	int yscale = glyph.yscale;
	int x = glyph.x;
	int y = glyph.y;
	int xMin = glyph.xMin;
	int yMin = glyph.yMin;
	int width = glyph.width;
	int height = glyph.height;
	if ((fileNumber = in.readInt()) == Integer.MAX_VALUE ||
		(glyphIndex = in.readLong()) == Long.MAX_VALUE ||
		(mirror = in.readInt()) == Integer.MAX_VALUE ||
		(rotate = in.readInt()) == Integer.MAX_VALUE ||
		(colorCode = in.readInt()) == Integer.MAX_VALUE ||
		colorCode < 0 || colorCode >= Color16.SIZE ||
		(xscale = in.readInt()) == Integer.MAX_VALUE ||
		(yscale = in.readInt()) == Integer.MAX_VALUE ||
		(x = in.readInt()) == Integer.MAX_VALUE ||
		(y = in.readInt()) == Integer.MAX_VALUE ||
		(xMin = in.readInt()) == Integer.MAX_VALUE ||
		(yMin = in.readInt()) == Integer.MAX_VALUE ||
		(width = in.readInt()) == Integer.MAX_VALUE ||
		(height = in.readInt()) == Integer.MAX_VALUE) {
	    in.pos = oldPos;
	    in.parseError("Ill-formed REScode glyph");
	    glyph.failed = true;
	    return glyph;
	}
	glyph.fileNumber = fileNumber;
	glyph.glyphIndex = glyphIndex;
	glyph.mirror = (mirror != 0);
	glyph.rotate = rotate;
	glyph.color = new Color16(colorCode);
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

    // Convert to string.
    public String toString() {
	return "c " +
	    fileNumber + " " + glyphIndex + " " +
	    (mirror ? 1 : 0) + " " +
	    rotate + " " +
	    color.code() + " " +
	    xscale + " " +
	    yscale + " " +
	    x + " " + y + " " + xMin + " " + yMin + " " +
	    width + " " + height + " " +
	    (tl != null ? tl.toString() : "");
    }

    // Render in clipped area. Do not use clipped area if area is full area.
    public void render(UniGraphics image, Rectangle rect, int dir,
	    Area clipped, boolean isClipped, HieroRenderContext context) {
	OptionalGlyphs glyph = glyph(context);
	Rectangle glyphRect = glyphRect(rect, context, glyph);
	if (isClipped || isPartialRect()) {
	    Area cutoutRect = new Area(cutoutRect(glyphRect));
	    cutoutRect.intersect(clipped);
	    image.render(glyph, glyphRect.x, glyphRect.y, cutoutRect);
	} else
	    image.render(glyph, glyphRect.x, glyphRect.y);
	if (color.isColored() && context.lineMode() != ResValues.NO_LINE) {
	    Rectangle lineRect = lineRect(rect, dir, context);
	    image.fillRect(context.lineColor(), lineRect);
	}
	if (tl != null)
	    tl.render(image, rect, dir, clipped, isClipped, context);
    }

    // Get glyph. Report error only once for each glyph.
    private OptionalGlyphs glyph(HieroRenderContext context) {
	Color color = context.effectColor(this.color).getColor();
	int size = (int) Math.round(xscale * context.fontSize() / 1000.0);
	OptionalGlyphs glyph;
	if (context.canDisplay(fileNumber, glyphIndex)) {
	    Font font = context.getFont(fileNumber);
	    float heightScaling = yscale / xscale;
	    char c = (char) glyphIndex;
	    glyph = new Glyphs(c, font, mirror, rotate, color, size,
		    1f, heightScaling,
		    context);
	} else {
	    Point2D p = new Point2D.Float(fileNumber, glyphIndex);
	    if (!errors.contains(p)) {
		System.err.println("Cannot display glyph " + glyphIndex +
			" of file " + fileNumber);
		errors.add(p);
	    }
	    glyph = new ErrorGlyphs(context.emSizePix(), context);
	}
	return glyph;
    }

    // Get rectangle covered by whole glyph.
    private Rectangle glyphRect(Rectangle rect, HieroRenderContext context,
	    OptionalGlyphs glyph) { 
	int width = glyph.dimension().width;
	int height = glyph.dimension().height;
	int xCorner = context.milEmToPix(x + rect.x) - width/2;
	int yCorner = context.milEmToPix(y + rect.y) - height/2;
	return new Rectangle(xCorner, yCorner, width, height);
    }

    // Get cutout rectangle.
    private Rectangle cutoutRect(Rectangle glyphRect) {
	int subX = glyphRect.x + (int) Math.round(xMin * glyphRect.width / 1000.0);
	int subY = glyphRect.y + (int) Math.round(yMin * glyphRect.height / 1000.0);
	int subWidth = (int) Math.round(this.width * glyphRect.width / 1000.0);
	int subHeight = (int) Math.round(this.height * glyphRect.height / 1000.0);
	return new Rectangle(subX, subY, subWidth, subHeight);
    }

    // Cutout rectangle does not cover full area.
    private boolean isPartialRect() {
	return xMin != 0 || yMin != 0 || width != 1000 || height != 1000;
    }

    // Rectangle formed by underline or overline.
    // We assume that the line code is not "no overline".
    private Rectangle lineRect(Rectangle rect, int dir, HieroRenderContext context) {
	OptionalGlyphs glyph = glyph(context);
	Rectangle glyphRect = glyphRect(rect, context, glyph);
	if (context.lineMode() == ResValues.UNDERLINE) {
	    if (HieroRenderContext.isH(dir)) {
		int height = context.milEmToPix(rect.height);
		return new Rectangle(glyphRect.x, height + context.lineDistPix(), 
			glyphRect.width, context.lineThicknessPix());
	    } else
		return new Rectangle(-context.lineDistPix() - context.lineThicknessPix(),
			glyphRect.y, context.lineThicknessPix(), glyphRect.height);
	} else { /* OVERLINE */
	    if (HieroRenderContext.isH(dir)) 
		return new Rectangle(glyphRect.x, 
			-context.lineDistPix() - context.lineThicknessPix(),
			glyphRect.width, context.lineThicknessPix());
	    else {
		int width = context.milEmToPix(rect.width);
		return new Rectangle(width + context.lineDistPix(),
			glyphRect.y, context.lineThicknessPix(), glyphRect.height);
	    }
	}
    }
}
