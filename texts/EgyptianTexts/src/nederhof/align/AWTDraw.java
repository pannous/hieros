/***************************************************************************/
/*                                                                         */
/*  AWTDraw.java                                                           */
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

// Wrapped AWT drawing methods, in generalized framework.

package nederhof.align;

import java.awt.*;

import nederhof.res.*;
import nederhof.res.format.*;

class AWTDraw implements GeneralDraw {

    // For mapping font code to AWT font.
    private AWTFontMapper mapper;

    // Graphics in AWT.
    private Graphics2D graphics;

    public AWTDraw(AWTFontMapper m, Graphics g) {
	mapper = m;
	graphics = (Graphics2D) g;
	graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);
    }

    public void drawString(int font, Color color, String s, float x, float y) {
	graphics.setFont(mapper.getFont(font));
	graphics.setColor(color);
	graphics.drawString(s, Math.round(x), Math.round(y));
    }

    // public void drawHiero(RESorREScodeDivision hiero, float x, float y) {
    public void drawHiero(FormatFragment hiero, float x, float y) {
	hiero.write(graphics, Math.round(x), Math.round(y));
    }

    public void fillRect(Color color, float x, float y, 
	    float width, float height) {
	graphics.setColor(color);
	graphics.fillRect(Math.round(x), Math.round(y), 
		Math.round(width), Math.round(height));
    }

    public void fillOval(Color color, float x, float y, 
	    float width, float height) {
	graphics.setColor(color);
	graphics.fillOval(Math.round(x), Math.round(y), 
		Math.round(width), Math.round(height));
    }

}
