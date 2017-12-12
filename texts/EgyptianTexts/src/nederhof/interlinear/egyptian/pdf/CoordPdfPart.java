/***************************************************************************/
/*                                                                         */
/*  CoordPdfPart.java                                                      */
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

// Coordinate part of tier.

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;

public class CoordPdfPart extends EgyptianTierPdfPart {

    // The id.
    public String id;

    public CoordPdfPart(String id) {
	this.id = id;
    }

    // How many symbols.
    public int nSymbols() {
	return 1;
    }

    // Is position breakable?
    public boolean breakable(int i) {
	return next == null;
    }

    // Penalty of line break at breakable position.
    public double penalty(int i) {
	return 0;
    }

    // Distance of position j from position i.
    // We look at location of symbol following position j,
    // with text from position i onward.
    // i <= j < nSymbols.
    public float dist(int i, int j) {
	return 0;
    }

    // Width from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float width(int i, int j) {
	if (i == j)
	    return 0;
	else {
	    return font().getWidthPointKerned(id, size());
	}
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	if (j == nSymbols()) 
	    return 3 * lineWidth() +
		(next == null ?
		 font().getWidthPointKerned(" ", size()) :
		 next.leadSpaceAdvance());
	else
	    return dist(i, j);
    }

    // Dimensions.
    public float leading() {
	return size() * renderParams.leadingFactor;
    }
    public float ascent() {
	return lineHeight() + 
	    font().getFontDescriptor(BaseFont.ASCENT, size()) +
	    - font().getFontDescriptor(BaseFont.DESCENT, size()) +
	    size() * renderParams.leadingFactor;
    }
    public float descent() {
	return 0;
    }

    // For external use, how much would ascent be?
    public static float ascent(BaseFont font, float size, float leadingFactor) {
	return 
	    font.getFontDescriptor(BaseFont.ASCENT, size) +
	    - font.getFontDescriptor(BaseFont.DESCENT, size) +
	    size * leadingFactor;
    }

    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
        if (i != j) {
	    surface.setFontAndSize(font(), size());
	    BaseColor color = renderParams.color ? BaseColor.RED : BaseColor.BLACK;
	    surface.setColorFill(color);
	    surface.setTextMatrix(x, y + 
		    lineHeight() - 
		    font().getFontDescriptor(BaseFont.DESCENT, size()) + 
		    size() * renderParams.leadingFactor);
	    surface.showTextKerned(id);

	    surface.endText();
	    surface.setColorStroke(color);
	    surface.setLineWidth(lineWidth());
	    surface.moveTo(x + 1.5f * lineWidth(), y);
	    surface.lineTo(x + 1.5f * lineWidth(), y + lineHeight());
	    surface.stroke();
	    surface.beginText();
        }
    }

    // Is not content.
    public boolean isContent() {
	return false;
    }

    // Dimensions of dividing line.
    private float lineWidth() {
	return 1;
    }
    private float lineHeight() {
	if (next == null)
	    return Math.min(renderParams.latinFont.getFontDescriptor(BaseFont.ASCENT, 
			renderParams.latinSize),
		    renderParams.egyptLowerFont.getFontDescriptor(BaseFont.ASCENT,
			renderParams.egyptLowerSize));
	else 
	    return next.exclusiveAscent();
    }

    // Font for label.
    protected BaseFont font() {
	return renderParams.footLatinFont;
    }

    // Font size.
    protected float size() {
	return renderParams.footLatinSize;
    }

}
