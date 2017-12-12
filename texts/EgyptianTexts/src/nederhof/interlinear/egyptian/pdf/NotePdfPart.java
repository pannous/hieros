/***************************************************************************/
/*                                                                         */
/*  NotePdfPart.java                                                       */
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

// Note part of tier.

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;
import java.util.*;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;

public class NotePdfPart extends EgyptianTierPdfPart {

    // The actual footnote.
    private Footnote footnote;

    public Footnote footnote() {
	return footnote;
    }

    // Text in footnote.
    public Vector text() {
	return footnote.text();
    }
    // Associated symbol.
    public int symbol() {
	return footnote.symbol();
    }

    public NotePdfPart(Vector text, int symbol) {
	footnote = new Footnote(text, symbol) {
	    public Tier getTier() {
		Vector parts = new Vector();
		FootnoteMarker footMark = new FootnoteMarker(makeMarker());
		parts.add(footMark);
		parts.addAll(text);
		parts = TierConstructor.prepareFootnote(parts, renderParams, true);
		return new Tier(parts);
	    }
	};
    }

    // How many symbols.
    public int nSymbols() {
	return 1;
    }

    // Is position breakable?
    public boolean breakable(int i) {
	return next == null || next.hasLeadSpace();
    }

    // Penalty of line break at breakable position.
    public double penalty(int i) {
        if (!breakable(i))
            return Penalties.maxPenalty;
        else if (i == nSymbols() && next == null)
            return Penalties.phrasePenalty;
        else
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
	    return font().getWidthPointKerned(footnote.makeMarker(), size());
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	if (j == nSymbols()) {
	    return font().getWidthPointKerned(footnote.makeMarker(), size()) +
		(next == null ? 
		 font().getWidthPointKerned(" ", size()) :
		 next.leadSpaceAdvance());
	} else
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

    public void situate(int i, int j) {
	if (i != j) 
	    footnote.setMarker(renderParams.getMarker());
    }

    public TreeSet getFootnotes(int i, int j) {
	TreeSet footnotes = new TreeSet();
	if (i != j)
	    footnotes.add(footnote);
	return footnotes;
    }

    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
	if (i != j) {
	    surface.setFontAndSize(font(), size());
	    if (renderParams.color)
		surface.setColorFill(color());
	    else
		surface.setColorFill(BaseColor.BLACK);
	    surface.setTextMatrix(x, 
		    y + renderParams.raisingFactor * 
			font().getFontDescriptor(BaseFont.ASCENT, size()));
	    surface.showTextKerned(footnote.makeMarker());
	}
    }

    protected BaseColor color() {
	Color color = renderParams.footnoteMarkerColor();
        return toBase(color);
    }

    protected BaseFont font() {
	return renderParams.footLatinFont;
    }

    protected float size() {
	return renderParams.footLatinSize;
    }

}
