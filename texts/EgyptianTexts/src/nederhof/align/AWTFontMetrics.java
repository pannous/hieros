/***************************************************************************/
/*                                                                         */
/*  AWTFontMetrics.java                                                    */
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

// Wrapped AWT font metrics, in generalized framework.

package nederhof.align;

import java.awt.*;

class AWTFontMetrics implements GeneralFontMetrics {

    // AWT font metrics.
    private FontMetrics awtMetrics;

    public AWTFontMetrics(FontMetrics fm) {
	awtMetrics = fm;
    }

    public float getAscent() {
	return awtMetrics.getAscent();
    }

    public float getAscent(String str) {
	return awtMetrics.getAscent();
    }

    public float getDescent() {
	return awtMetrics.getDescent();
    }

    public float getDescent(String str) {
	return awtMetrics.getDescent();
    }

    public float getHeight() {
	return awtMetrics.getHeight();
    }

    public float getLeading() {
	return awtMetrics.getLeading();
    }

    public float stringWidth(String str) {
	return awtMetrics.stringWidth(str);
    }

}
