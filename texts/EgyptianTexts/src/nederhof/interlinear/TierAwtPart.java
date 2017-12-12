/***************************************************************************/
/*                                                                         */
/*  TierAwtPart.java                                                       */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// TierPart that can be printed in Awt.

package nederhof.interlinear;

import java.awt.*;
import java.util.*;

public interface TierAwtPart extends ITierPart {

    // Draw given positions.
    public abstract void draw(int i, int j, int x, int y, Graphics2D g);

    // Get position within range of clicked position.
    // Return -1 if clicked position not in range.
    public abstract int getPos(int i, int j, int x, int y);

    // Get rectangle around position j, starting at position i.
    public abstract Rectangle getRectangle(int i, int j);

    // Highlight position,
    public abstract void highlight(int i);
    // Undo highlighting.
    public abstract void unhighlight(int i);

    // Highlight after position,
    public abstract void highlightAfter(int i);
    // Undo highlighting after position.
    public abstract void unhighlightAfter(int i);

}
