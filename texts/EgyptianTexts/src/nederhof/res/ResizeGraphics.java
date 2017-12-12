/***************************************************************************/
/*                                                                         */
/*  ResizeGraphics.java                                                    */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Simulates graphics. Only determines the necessary boundary around text
// that is sufficient to contain all pixels.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import com.itextpdf.awt.PdfGraphics2D;

public class ResizeGraphics implements UniGraphics {

    // The rectangle so far.
    private Rectangle rect;

    // The initial rectangle, which indicates the virtual dimensions.
    public ResizeGraphics(Rectangle rect) {
	this.rect = new Rectangle(rect);
    }
    // Same, but from dimensions. 
    public ResizeGraphics(Dimension dim) {
	this(new Rectangle(dim));
    }
    // In addition, process margins determined by context and whether mirrored.
    public ResizeGraphics(Dimension dim, HieroRenderContext context,
	    boolean mirrored) {
	int leftMargin;
	int rightMargin;
	if (mirrored) {
	    leftMargin = context.leftMarginPix();
	    rightMargin = context.rightMarginPix();
	} else {
	    leftMargin = context.rightMarginPix();
	    rightMargin = context.leftMarginPix();
	}
	int topMargin = context.topMarginPix();
	int bottomMargin = context.bottomMarginPix();
	int totalWidth = Math.max(dim.width + leftMargin + rightMargin, 1);
	int totalHeight = Math.max(dim.height + topMargin + bottomMargin, 1);
	rect = new Rectangle(-leftMargin, -topMargin, totalWidth, totalHeight);
    }

    // Return bounding box around rendered material.
    public Rectangle getRect() {
	return rect;
    }

    // Enlarge rectangle, if glyphs would be printed outside it.
    public void render(OptionalGlyphs glyphs, int x, int y) {
	Dimension dim = glyphs.dimension();
	Rectangle glyphRect = new Rectangle(x, y, dim.width, dim.height);
	rect.add(glyphRect);
    }
    // Same but glyphs are clipped.
    public void render(OptionalGlyphs glyphs, int x, int y, Area clipped) {
	Dimension dim = glyphs.dimension();
	Area glyphRect = new Area(new Rectangle(x, y, dim.width, dim.height));
	glyphRect.intersect(clipped);
	rect.add(glyphRect.getBounds());
    }
    // Mirroring of text is here ignored.
    public void renderStraight(OptionalGlyphs glyphs, int x, int y) {
	render(glyphs, x, y);
    }

    // Add rectangle.
    public void fillRect(Color color, int x, int y, int width, int height) {
	Rectangle added = new Rectangle(x, y, width, height);
	rect.add(added);
    }
    public void fillRect(Color color, Rectangle rect) {
	fillRect(color, rect.x, rect.y, rect.width, rect.height);
    }

    // Add area for shading.
    public void shade(Rectangle shaded, HieroRenderContext context) {
	rect.add(shaded);
    }

}
