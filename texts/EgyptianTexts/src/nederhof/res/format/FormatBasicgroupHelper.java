/***************************************************************************/
/*                                                                         */
/*  FormatBasicgroupHelper.java                                            */
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

package nederhof.res.format;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import nederhof.res.*;

public class FormatBasicgroupHelper {

    // Make formatted group.
    public static FormatBasicgroup makeGroup(ResBasicgroup group, 
	    HieroRenderContext context) {
	if (group instanceof ResNamedglyph) {
	    ResNamedglyph g = (ResNamedglyph) group;
	    return new FormatNamedglyph(g, context);
	} else if (group instanceof ResEmptyglyph) {
	    ResEmptyglyph g = (ResEmptyglyph) group;
	    return new FormatEmptyglyph(g, context);
	} else if (group instanceof ResBox) {
	    ResBox g = (ResBox) group;
	    return new FormatBox(g, context);
	} else if (group instanceof ResStack) {
	    ResStack g = (ResStack) group;
	    return new FormatStack(g, context);
	} else if (group instanceof ResInsert) {
	    ResInsert g = (ResInsert) group;
	    return new FormatInsert(g, context);
	} else if (group instanceof ResModify) {
	    ResModify g = (ResModify) group;
	    return new FormatModify(g, context);
	} else {
	    System.err.println("Missing subclass in FormatBasicgroupHelper");
	    return null;
	}
    }

    ////////////////////////////////////////////////////////////
    // Scaling and positioning.

    // Place square with given width and height in middle of rectangle.
    // All basic groups must apply this to determine rectangle where they are
    // to be placed within given rectangle.
    public static Rectangle placeCentre(Rectangle rect, FormatBasicgroup group,
            HieroRenderContext context) {
        int width = group.width();
        int height = group.height();
        int horSurplus = rect.width - width;
        int vertSurplus = rect.height - height;
        int leftSurplus = horSurplus / 2;
        int topSurplus = vertSurplus - vertSurplus / 2;
        return new Rectangle(rect.x + leftSurplus, rect.y + topSurplus, width, height);
    }

    ////////////////////////////////////////////////////////////
    // Line.

    // Line above or underneath text.
    // rect encloses the object giving rise to the underlining.
    // isH = effectively horizontal text.
    // size is the size from header.
    public static Rectangle lineRect(Rectangle rect, boolean isH, float size,
            HieroRenderContext context) {
        float effectSize = context.effectSize(size);
        if (context.lineMode() == ResValues.UNDERLINE) {
            if (isH) {
                int textHeight = context.emToPix(effectSize);
                return new Rectangle(rect.x, textHeight + context.lineDistPix(),
                        rect.width, context.lineThicknessPix());
            } else
                return new Rectangle(-context.lineDistPix() - context.lineThicknessPix(),
                        rect.y, context.lineThicknessPix(), rect.height);
        } else { /* OVERLINE */
            if (isH) 
                return new Rectangle(rect.x,
                        -context.lineDistPix() - context.lineThicknessPix(),
                        rect.width, context.lineThicknessPix());
            else {
                int textWidth = context.emToPix(effectSize);
                return new Rectangle(textWidth + context.lineDistPix(),
                        rect.y, context.lineThicknessPix(), rect.height);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // RESlite.

    // Given point of reference, translate clipped glyph into RESlite.
    public static REScodeGlyph toResLite(int x, int y, Rectangle rect,
            GlyphPlace place, boolean mirror, int rotate, Color16 color,
            float scale, float xscale, float yscale,
	    Rectangle clip, HieroRenderContext context) {
        REScodeGlyph g = new REScodeGlyph();
        g.fileNumber = place.file;
        g.glyphIndex = place.index;
        g.mirror = mirror;
        g.rotate = rotate;
        g.color = color;
        g.xscale = Math.round(1000.0f * scale * xscale);
        g.yscale = Math.round(1000.0f * scale * yscale);
        Rectangle moved = new Rectangle(rect);
        moved.x -= x;
        moved.y -= y;
        Rectangle sub = rect.intersection(clip);
        sub.x -= x;
        sub.y -= y;
        if (sub.width != 0 && sub.height != 0) {
            g.x = Math.round((1000.0f * moved.x + 500.0f * moved.width) /
                    context.emSizePix());
            g.y = Math.round((1000.0f * moved.y + 500.0f * moved.height) /
                    context.emSizePix());
            g.xMin = Math.round(1000.0f * (sub.x-moved.x) / moved.width);
            g.yMin = Math.round(1000.0f * (sub.y-moved.y) / moved.height);
            g.width = Math.round(1000.0f * sub.width / moved.width);
            g.height = Math.round(1000.0f * sub.height / moved.height);
        }
        return g;
    }

    // As above, but without xscale and yscale.
    public static REScodeGlyph toResLite(int x, int y, Rectangle rect,
	    GlyphPlace place, boolean mirror, int rotate, Color16 color,
	    float scale, Rectangle clip, HieroRenderContext context) {
	return toResLite(x, y, rect, place, mirror, rotate, color, scale,
		1f, 1f, clip, context);
    }

}
