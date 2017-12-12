/***************************************************************************/
/*                                                                         */
/*  InnerArea.java                                                         */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// The total surface of a character or set of characters, including embedded
// whitespace.

package nederhof.res;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;

class InnerArea extends Area {

    // Dissect text into paths. Take union of all.
    public InnerArea(TextLayout text) {
	Shape shape = text.getOutline(null);
	GeneralPath path = new GeneralPath();
	for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
	    float[] seg = new float[6];
	    int sType = it.currentSegment(seg);
	    switch(sType) {
		case PathIterator.SEG_MOVETO:
		    path.moveTo(seg[0], seg[1]);
		    break;
		case PathIterator.SEG_LINETO:
		    path.lineTo(seg[0], seg[1]);
		    break;
		case PathIterator.SEG_QUADTO:
		    path.quadTo(seg[0], seg[1], seg[2], seg[3]);
		    break;
		case PathIterator.SEG_CUBICTO:
		    path.curveTo(seg[0], seg[1], seg[2], seg[3], seg[4], seg[5]);
		    break;
		case PathIterator.SEG_CLOSE:
		    path.closePath();
		    Area area = new Area(path);
		    add(area);
		    path = new GeneralPath();
		    break;
	    }
	}
    }
}
