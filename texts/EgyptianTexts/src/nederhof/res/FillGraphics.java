/***************************************************************************/
/*                                                                         */
/*  FillGraphics.java                                                      */
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

// Simulates graphics. Only computes joint surface filled by glyphs.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;

public class FillGraphics implements UniGraphics {

    // The area filled so far.
    private Area filling;

    // Initially, the area is empty.
    public FillGraphics() {
	filling = new Area();
    }

    // Return area that was filled.
    public Area getFilling() {
	return filling;
    }

    // Add area filled by glyphs.
    public void render(OptionalGlyphs glyphs, int x, int y) {
	Area glyphArea = glyphs.filling(x, y);
	filling.add(glyphArea);
    }
    // Same but glyphs are clipped.
    public void render(OptionalGlyphs glyphs, int x, int y, Area clipped) {
	Area glyphArea = glyphs.filling(x, y);
	glyphArea.intersect(clipped);
	filling.add(glyphArea);
    }

    // Mirrored text implies notes. Notes do not count here.
    public void renderStraight(OptionalGlyphs glyphs, int x, int y) {
    }

    // Rectangles do not count.
    public void fillRect(Color color, int x, int y, int width, int height) {
    }
    public void fillRect(Color color, Rectangle rect) {
    }

    // Shading does not count.
    public void shade(Rectangle shaded, HieroRenderContext context) {
    }

}
