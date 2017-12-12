/***************************************************************************/
/*                                                                         */
/*  Glyph.java                                                             */
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

// Buffered image in wrapping that represents just one glyph.
// Note that keeping many of these objects is space-consuming,
// and is discouraged. It is better to redraw each time, e.g. using
// ResDivision.write() inside a method paintComponent().

package nederhof.res;

import java.awt.*;
import java.awt.image.*;

public class Glyph {

    // Stored name of glyph.
    String name;

    public Glyph(String name) {
	this.name = name;
    }

    // Size of dummy image when name doesn't exist.
    private static final int DUMMY_SIZE = 8;

    // Get image from wrapping.
    public BufferedImage getImage(HieroRenderContext context) {
	GlyphPlace place = context.getGlyph(name);
	if (context.canDisplay(place)) {
	    Glyphs glyphs = new Glyphs(place, false, 0, Color.BLACK, 
		    context.fontSize(), context);
	    return glyphs.getImage();
	} else {
	    BufferedImage im = new BufferedImage(DUMMY_SIZE, DUMMY_SIZE, 
		    context.imageType());
	    Graphics2D g = im.createGraphics();
	    g.setColor(Color.BLACK);
	    g.fillRect(0, 0, DUMMY_SIZE, DUMMY_SIZE);
	    g.dispose();
	    return im;
	}
    }
}
