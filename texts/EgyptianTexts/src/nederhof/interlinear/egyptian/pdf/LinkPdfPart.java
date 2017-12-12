/***************************************************************************/
/*                                                                         */
/*  LinkPdfPart.java                                                       */
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

// Part of tier that is hyperlink.

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;

public class LinkPdfPart extends StringPdfPart {

    // Address of hyperlink.
    public String ref;

    public LinkPdfPart(String string, String ref) {
	super(string);
	this.ref = ref;
    }

    // Draw substring. Connect hyperlink.
    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
        if (i != j) {
            int indexI = indices[i];
            int indexJ = indices[j-1] + 1;
            String sub = string.substring(indexI, indexJ);
            surface.setFontAndSize(font(), size());
	    if (renderParams.color)
		surface.setColorFill(toBase(renderParams.hyperlinkColor()));
	    else
		surface.setColorFill(BaseColor.BLACK);
            surface.setTextMatrix(x, y);
            surface.showTextKerned(sub);

	    PdfAction jump = new PdfAction(ref);
	    surface.setAction(jump, x, y-descent(), 
		    x + width(i, j), y + ascent());
        }
    }

    protected BaseFont font() {
        if (isFootnote())
            return renderParams.footBoldFont;
        else
            return renderParams.boldFont;
    }

    protected float size() {
        if (isFootnote())
            return renderParams.footBoldSize;
        else
            return renderParams.boldSize;
    }

}
