/***************************************************************************/
/*                                                                         */
/*  FormatTopgroup.java                                                    */
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

public interface FormatTopgroup extends ResTopgroup {

    ////////////////////////////////////////////////////////////
    // Scaling and positioning.

    // Scale down.
    public void scale(float factor);

    // How much scaled down?
    public float sideScaledLeft();
    public float sideScaledRight();
    public float sideScaledTop();
    public float sideScaledBottom();

    // For doing scaling anew.
    public void resetScaling();

    // Dimensions.
    public int width();
    public int height();

    // Rectangle around group.
    public Rectangle rectangle();

    // Render.
    public void render(UniGraphics image, 
	    Rectangle rect, Rectangle shadeRect, Area clipped, 
	    boolean isClipped, boolean fitting);

    // Place footnotes.
    public void placeNotes(FlexGraphics im,
	    boolean under, boolean over);
    // Render footnotes.
    public void renderNotes(UniGraphics im);

    // Make shading.
    public void shade(UniGraphics image);

    // Produce RESlite.
    public void toResLite(int x, int y,
	    Vector exprs, Vector notes, Vector shades,
	    Rectangle clip);
}
