/***************************************************************************/
/*                                                                         */
/*  REScodeExprs.java                                                      */
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

// expressions in REScode.
// Subclasses: REScodeGlyph and REScodePair.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;

public abstract class REScodeExprs {
    public REScodeExprs tl;

    // Any problem during parsing?
    public boolean failed;

    // Implicitly called by constructors of subclasses.
    protected REScodeExprs() {
	tl = null;
	failed = false;
    }

    // Read zero or more expressions. Return pointer to list,
    // possibly null.
    public static REScodeExprs read(ParseBuffer in) {
	REScodeExprs exprs;
	if (in.peekChar('c')) {
	    exprs = REScodeGlyph.read(in);
	    if (!exprs.failed)
		exprs.tl = read(in);
	} else if (in.peekChar('(')) {
	    exprs = REScodePair.read(in);
	    if (!exprs.failed)
		exprs.tl = read(in);
	} else exprs = null;
	return exprs;
    }

    // Convert to string.
    public abstract String toString();

    // Render. 
    public abstract void render(UniGraphics image, Rectangle total,
	int dir, Area clipped, boolean isClipped, HieroRenderContext context);
}
