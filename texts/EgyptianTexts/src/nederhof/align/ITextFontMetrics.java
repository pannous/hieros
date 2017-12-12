/***************************************************************************/
/*                                                                         */
/*  ITextFontMetrics.java                                                  */
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

// Wrapped iText font metrics, in generalized framework.

package nederhof.align;

import com.itextpdf.text.pdf.*;

class ITextFontMetrics implements GeneralFontMetrics {

    // Base font.
    private BaseFont font;

    // Font size.
    private float size;

    // Cashed dimensions.
    private float ascent;
    private float descent;
    private float leading;
    private float height;

    // We cash the dimensions of the some characters in some fonts.
    public ITextFontMetrics(BaseFont font, float size) {
	this.font = font;
	this.size = size;
	ascent = font.getFontDescriptor(BaseFont.ASCENT, size);
	descent = - font.getFontDescriptor(BaseFont.DESCENT, size);
	leading = size * 0.2f;
	height = ascent + descent + leading;
    }

    public float getAscent() {
	return ascent;
    }

    public float getAscent(String str) {
	return font.getAscentPoint(str, size);
    }

    public float getDescent() {
	return descent;
    }

    public float getDescent(String str) {
	return - font.getDescentPoint(str, size);
    }

    public float getHeight() {
	return height;
    }

    public float getLeading() {
	return leading; 
    }

    public float stringWidth(String str) {
	return font.getWidthPointKerned(str, size);
    }

}
