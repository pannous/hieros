/***************************************************************************/
/*                                                                         */
/*  UniGraphics.java                                                       */
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

// Joins a number of real graphics objects and objects merely intended
// as auxiliary tools.

// Implementing classes:
// TransGraphics
// ResizeGraphics
// FillGraphics

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;

public interface UniGraphics {

    // Print glyphs in image.
    public void render(OptionalGlyphs glyphs, int x, int y);
    // Same but clipped.
    public void render(OptionalGlyphs glyphs, int x, int y, Area clipped);
    // If graphics is mirrored, also print text backwards, so that in the
    // end it appears straight.
    public void renderStraight(OptionalGlyphs glyphs, int x, int y);

    // Print rectangle.
    public void fillRect(Color color, int x, int y, int width, int height);
    public void fillRect(Color color, Rectangle rect);

    // Make shading.
    public void shade(Rectangle rect, HieroRenderContext context);

}
