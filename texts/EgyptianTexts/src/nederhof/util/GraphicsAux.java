/***************************************************************************/
/*                                                                         */
/*  GraphicsAux.java                                                       */
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

package nederhof.util;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class GraphicsAux {

    // Length of arrow.
    private static final double arrowLength = 10; 
    // Sharpness of arrow.
    private static final double arrowSharpness = 0.4;

    // Draw line with arrow at end.
    public static void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
	if (x1 == x2 && y1 == y2)
	    return;
	g.drawLine(x1, y1, x2, y2);
	double angle = Math.atan2(y2-y1, x2-x1);
	double xCorner1 = arrowLength * Math.cos(angle + arrowSharpness);
	double yCorner1 = arrowLength * Math.sin(angle + arrowSharpness);
	double xCorner2 = arrowLength * Math.cos(angle - arrowSharpness);
	double yCorner2 = arrowLength * Math.sin(angle - arrowSharpness);
	GeneralPath head = new GeneralPath();
	head.moveTo((int) x2, (int) y2);
	head.lineTo((int) (x2 - xCorner1), (int) (y2 - yCorner1));
	head.lineTo((int) (x2 - xCorner2), (int) (y2 - yCorner2));
	head.closePath();
	g.fill(head);
   }

}
