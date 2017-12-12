/***************************************************************************/
/*                                                                         */
/*  REScodeGroups.java                                                     */
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

// groups in REScode.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;

public class REScodeGroups {
    public int advance;
    public int length;
    public REScodeExprs exprs;
    public REScodeNotes notes;
    public REScodeShades shades;
    public REScodeShades intershades;
    public REScodeGroups tl;
    
    // Any problem during parsing?
    public boolean failed;

    public REScodeGroups() {
	advance = 1000;
	length = 1000;
	exprs = null;
	notes = null;
	shades = null;
	intershades = null;
	tl = null;
	failed = false;
    }

    // Construct copy from original.
    private REScodeGroups(REScodeGroups o) {
	advance = o.advance;
	length = o.length;
	exprs = o.exprs;
	notes = o.notes;
	shades = o.shades;
	intershades = o.intershades;
	tl = null;
	failed = false;
    }

    // Clone first n groups (shallowly).
    public REScodeGroups clone(int n) {
	if (n > 0) {
	    REScodeGroups g = new REScodeGroups(this);
	    if (tl != null)
		g.tl = tl.clone(n-1);
	    return g;
	} else
	    return null;
    }

    // Read zero or more groups. Return pointer to list,
    // possibly null.
    public static REScodeGroups read(ParseBuffer in) {
	int oldPos = in.pos;
	if (!in.readChar('g'))
	    return null;
	REScodeGroups groups = new REScodeGroups();
	int advance = 0;
	int length = 0;
	if ((advance = in.readInt()) == Integer.MAX_VALUE ||
		(length = in.readInt()) == Integer.MAX_VALUE) {
	    in.pos = oldPos;
	    in.parseError("Ill-formed REScode group header");
	    groups.failed = true;
	    return groups;
	}
	groups.advance = advance;
	groups.length = length;
	groups.exprs = REScodeExprs.read(in);
	if (groups.exprs != null && groups.exprs.failed) {
	    groups.failed = true;
	    return groups;
	}
	groups.notes = REScodeNotes.read(in);
	if (groups.notes != null && groups.notes.failed) {
	    groups.failed = true;
	    return groups;
	}
	groups.shades = REScodeShades.read(in);
	if (groups.shades != null && groups.shades.failed) {
	    groups.failed = true;
	    return groups;
	}
	if (!in.readChar('i')) {
	    in.pos = oldPos;
	    in.parseError("Missing i in REScode group");
	    groups.failed = true;
	    return groups;
	}
	groups.intershades = REScodeShades.read(in);
	if (groups.intershades != null && groups.intershades.failed) {
	    groups.failed = true;
	    return groups;
	}
	groups.tl = read(in);
	if (groups.tl != null && groups.tl.failed)
	    groups.failed = true;
	return groups;
    }

    // Convert to string.
    public String toString(boolean isFirst) {
	return "g " + 
	    (isFirst ? "0 " : Integer.toString(advance) + " ") +
	    length + " " + 
	    (exprs != null ? exprs.toString() : "") +
	    (notes != null ? notes.toString() : "") + 
	    (shades != null ? shades.toString() : "") + "i " +
	    (intershades != null ? intershades.toString() : "") + 
	    (tl != null ? tl.toString(false) : "");
    }

    // Render n groups.
    // First render expressions, then shades, then notes.
    public void render(UniGraphics graphics, Area total,
	    int width, int height,
	    int dir, HieroRenderContext context, int n, int padding) {
	int widthPix = context.milEmToPix(width);
	int heightPix = context.milEmToPix(height);
	CrackFill fill = new CrackFill(HieroRenderContext.isH(dir), 
		widthPix, heightPix, context);
	Rectangle initRect = firstRect(dir, width, height);
	renderGroups(graphics, total, initRect, dir, context, n, padding, true);
	renderShades(graphics, initRect, fill, dir, context, n, padding, true);
	renderNotes(graphics, initRect, dir, context, n, padding, true);
    }

    // Render expressions of n groups.
    // The initial clip is the total surface of group.
    private void renderGroups(UniGraphics graphics, Area total, Rectangle rect,
	    int dir, HieroRenderContext context, 
	    int n, int padding, boolean isFirst) {
	if (n > 0) {
	    int pad = (isFirst ? 0 : padding / n);
	    padding -= pad;
	    Rectangle newRect = updateRect(rect, dir, isFirst, pad);
	    if (exprs != null) 
		exprs.render(graphics, newRect, dir, total, false, context);
	    if (tl != null)
		tl.renderGroups(graphics, total, newRect, dir, 
			context, n-1, padding, false);
	}
    }

    // Render shades of n groups.
    private void renderShades(UniGraphics graphics, Rectangle rect,
	    CrackFill fill, int dir, HieroRenderContext context, 
	    int n, int padding, boolean isFirst) {
	if (n > 0) {
	    int pad = (isFirst ? 0 : padding / n);
	    padding -= pad;
	    Rectangle newRect = updateRect(rect, dir, isFirst, pad);
	    fill.newGroup();
	    if (n != 1 && tl != null) {
		int nextPad = padding / (n-1);
		if (HieroRenderContext.isH(dir)) {
		    int nextX = newRect.x + tl.advance;
		    int pos = context.milEmToPix(nextX);
		    int newPos = context.milEmToPix(nextX + nextPad);
		    fill.recordPadding(pos, newPos);
		} else {
		    int nextY = newRect.y + tl.advance;
		    int pos = context.milEmToPix(nextY);
		    int newPos = context.milEmToPix(nextY + nextPad);
		    fill.recordPadding(pos, newPos);
		}
	    }
	    if (shades != null)
		shades.render(graphics, newRect, fill, context);
	    if (n != 1 && tl != null && intershades != null)
		intershades.render(graphics, newRect, fill, context);
	    if (tl != null)
		tl.renderShades(graphics, newRect, fill, dir, context, n-1, padding, false);
	}
    }

    // Render notes of n groups.
    private void renderNotes(UniGraphics graphics, Rectangle rect,
	    int dir, HieroRenderContext context, 
	    int n, int padding, boolean isFirst) {
	if (n > 0) {
	    int pad = (isFirst ? 0 : padding / n);
	    padding -= pad;
	    Rectangle newRect = updateRect(rect, dir, isFirst, pad);
	    if (notes != null)
		notes.render(graphics, newRect.x, newRect.y, context);
	    if (tl != null)
		tl.renderNotes(graphics, newRect, dir, context, n-1, padding, false);
	}
    }

    // Get rectangle before first group.
    private Rectangle firstRect(int dir, int width, int height) {
	if (HieroRenderContext.isH(dir))
	    width = 0;
	else
	    height = 0;
	return new Rectangle(0, 0, width, height);
    }

    // Get rectangle enclosing current group, based on the previous rectangle.
    private Rectangle updateRect(Rectangle rect, int dir, boolean isFirst, 
	    int pad) {
	int advance = (isFirst ? 0 : this.advance + pad);
	int x = rect.x;
	int y = rect.y;
	int width = rect.width;
	int height = rect.height;
	if (HieroRenderContext.isH(dir)) {
	    x += advance;
	    width = length;
	} else {
	    y += advance;
	    height = length;
	}
	return new Rectangle(x, y, width, height);
    }

    // Return sum of separations between groups, in first n groups.
    public int sepSum(int n) {
	if (n > 1) {
	    if (tl != null) {
		return tl.advance - length + tl.sepSum(n-1);
	    } else
		return 0;
	} else
	    return 0;
    }
}
