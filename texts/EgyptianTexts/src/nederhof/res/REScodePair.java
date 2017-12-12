/***************************************************************************/
/*                                                                         */
/*  REScodePair.java                                                       */
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

// type of expression in REScode can be pair of two expressions.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;

public class REScodePair extends REScodeExprs {
    public REScodeExprs list1;
    public REScodeExprs list2;

    public REScodePair() {
	list1 = null;
	list2 = null;
    }

    // Read pair. Make it failed if there is no valid pair.
    public static REScodeExprs read(ParseBuffer in) {
	int oldPos = in.pos;
	REScodePair pair = new REScodePair();
	if (!in.readChar('(')) {
	    in.parseError("Missing ( in REScode pair");
	    pair.failed = true;
	    return pair;
	}
	pair.list1 = REScodeExprs.read(in);
	if (pair.list1.failed) {
	    pair.failed = true;
	    return pair;
	}
	if (!in.readChar('o')) {
	    in.pos = oldPos;
	    in.parseError("Missing o in REScode pair");
	    pair.failed = true;
	    return pair;
	}
	pair.list2 = REScodeExprs.read(in);
	if (pair.list2.failed) {
	    pair.failed = true;
	    return pair;
	}
	if (!in.readChar(')')) {
	    in.pos = oldPos;
	    in.parseError("Missing ) in REScode pair");
	    pair.failed = true;
	    return pair;
	}
	return pair;
    }

    // Convert to string.
    public String toString() {
	return "( " + 
	    (list1 != null ? list1.toString() : "") + "o " +
	    (list2 != null ? list2.toString() : "") + ") " +
	    (tl != null ? tl.toString() : "");
    }

    // Render, but only in clipped area. Remove second group from that
    // clipped area before rendering first group.
    public void render(UniGraphics image, Rectangle rect, int dir,
	    Area clipped, boolean isClipped, HieroRenderContext context) {
	if (list1 != null) {
	    if (list2 != null) {
		Area clipped1 = (Area) clipped.clone();
		FillGraphics fill = new FillGraphics();
		list2.render(fill, rect, dir, clipped, isClipped, context);
		Area filling2 = fill.getFilling();
		clipped1.subtract(filling2);
		list1.render(image, rect, dir, clipped1, true, context);
	    } else
		list1.render(image, rect, dir, clipped, isClipped, context);
	}
	if (list2 != null)
	    list2.render(image, rect, dir, clipped, isClipped, context);
	if (tl != null)
	    tl.render(image, rect, dir, clipped, isClipped, context);
    }
}
