/***************************************************************************/
/*                                                                         */
/*  REScodeShades.java                                                     */
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

// shades in REScode.

package nederhof.res;

import java.awt.*;

public class REScodeShades {
    public int x;
    public int y;
    public int width;
    public int height;
    public REScodeShades tl;

    // Any problem during parsing?
    public boolean failed;

    public REScodeShades() {
	x = 0;
	y = 0;
	width = 0;
	height = 0;
	tl = null;
	failed = false;
    }

    // Read zero or more shades. Return pointer to list,
    // possibly null.
    public static REScodeShades read(ParseBuffer in) {
	int oldPos = in.pos;
	if (!in.readChar('s'))
	    return null;
	REScodeShades shades = new REScodeShades();
	int x = shades.x;
	int y = shades.y;
	int width = shades.width;
	int height = shades.height;
	if ((x = in.readInt()) == Integer.MAX_VALUE ||
		(y = in.readInt()) == Integer.MAX_VALUE ||
		(width = in.readInt()) == Integer.MAX_VALUE ||
		(height = in.readInt()) == Integer.MAX_VALUE) {
	    in.pos = oldPos;
	    in.parseError("Ill-formed REScode shade");
	    shades.failed = true;
	    return shades;
	}
	shades.x = x;
	shades.y = y;
	shades.width = width;
	shades.height = height;
	shades.tl = read(in);
	if (shades.tl != null && shades.tl.failed)
	    shades.failed = true;
	return shades;
    }

    // Convert to string.
    public String toString() {
	return "s " + x + " " + y + " " +
	    width + " " + height + " " +
	    (tl != null ? tl.toString() : "");
    }

    // Render.
    public void render(UniGraphics image, Rectangle rect, CrackFill fill,
	    HieroRenderContext context) {
	Rectangle shadeRect = area(rect, context);
	fill.makeConnect(shadeRect);
	image.shade(shadeRect, context);
	if (tl != null) 
	    tl.render(image, rect, fill, context);
    }

    // Make rectangle to be shaded.
    private Rectangle area(Rectangle rect, HieroRenderContext context) {
	int width = context.milEmToPix(this.width);
	int height = context.milEmToPix(this.height);
	float xCenter = x + rect.x;
	float yCenter = y + rect.y;
	int xCorner = context.milEmToPix(xCenter - this.width / 2.0f);
	int yCorner = context.milEmToPix(yCenter - this.height / 2.0f);
	return new Rectangle(xCorner, yCorner, width, height);
    }
}
