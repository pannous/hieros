/***************************************************************************/
/*                                                                         */
/*  OptionalGlyphs.java                                                    */
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

// Implemented by:
// Glyphs
// ErrorGlyphs

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public interface OptionalGlyphs  {

    public Dimension dimension();
    public Area filling(int x, int y);
    public void render(TransGraphics graphics, int x, int y);
    public void renderStraight(TransGraphics graphics, int x, int y);
    public void render(TransGraphics graphics, int x, int y, Area clipped);
    public int leftEdge();
    public int rightEdge();
    public int bottomEdge();
    public int topEdge();
}
