/***************************************************************************/
/*                                                                         */
/*  FootnoteMarkerPdf.java                                                 */
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

// Footnote marker. At beginning of footnote.

package nederhof.interlinear.egyptian.pdf;

import java.util.*;

import java.awt.*;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;

public class FootnoteMarkerPdf extends EgyptianTierPdfPart {

    public String marker;

    // Argument includes following space.
    public FootnoteMarkerPdf(String marker) {
	this.marker = marker;
    }

    // How many symbols.
    public int nSymbols() {
        return 1;
    }

    // Is position breakable?
    public boolean breakable(int i) {
        return false;
    }

    // Penalty of line break at breakable position.
    public double penalty(int i) {
	return Penalties.spacePenalty;
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
        else
	    return font().getWidthPointKerned(marker, size());
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
        if (j == nSymbols()) 
	    return font().getWidthPointKerned(marker, size());
        else
            return dist(i, j);
    }

    // Font metrics.
    public float leading() {
        return size() * renderParams.leadingFactor;
    }
    public float ascent() {
        return (1 + renderParams.raisingFactor) * 
	    font().getFontDescriptor(BaseFont.ASCENT, size());
    }
    public float descent() {
        return 0;
    }

    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
        if (i != j) {
	    surface.setFontAndSize(font(), size());
	    surface.setColorFill(color());
	    surface.setTextMatrix(x, 
		    y + renderParams.raisingFactor * 
		       font().getFontDescriptor(BaseFont.ASCENT, size()));
	    surface.showTextKerned(marker);
        }
    }

    protected BaseColor color() {
	if (renderParams.color)
	    return toBase(renderParams.footnoteMarkerColor());
	else
	    return BaseColor.BLACK;
    }

    protected BaseFont font() {
        return renderParams.footLatinFont;
    }

    protected float size() {
        return renderParams.footLatinSize;
    }

}
