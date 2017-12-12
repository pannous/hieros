/***************************************************************************/
/*                                                                         */
/*  StringPdfPart.java                                                     */
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

// Captures a number of tier parts that contain a string.

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;

public abstract class StringPdfPart extends EgyptianTierPdfPart {

    // The string.
    public String string;

    // Number of non-space characters.
    protected int nSymbols = 0;

    // Maps positions to indices.
    public int[] indices;

    // Construct.
    public StringPdfPart(String string) {
        this.string = string;
        countSymbols();
    }

    // Get string.
    public String str() {
	return string;
    }

    // How many symbols.
    public int nSymbols() {
        return nSymbols;
    }

    // Count symbols. Make points from positions to indices.
    private void countSymbols() {
        for (int i = 0; i < string.length(); i++)
            if (!isWhite(i))
                nSymbols++;
        indices = new int[nSymbols];
        int n = 0;
        for (int i = 0; i < string.length(); i++)
            if (!isWhite(i))
                indices[n++] = i;
    }

    // Is character index whitespace?
    private boolean isWhite(int i) {
        char c = string.charAt(i);
        return Character.isWhitespace(c);
    }

    // Is position breakable?
    // This is if preceded by space. For position in next tierpart this
    // is if current tierpart ends on space, or if next tierpart starts with
    // space.
    public boolean breakable(int i) {
        if (i == nSymbols)
            return isWhite(string.length()-1) ||
		next == null || next.hasLeadSpace();
        else
            return isWhite(indices[i]-1);
    }

    // Penalty of line break at breakable position.
    public double penalty(int i) {
        if (!breakable(i))
            return Penalties.maxPenalty;
	else if (i == nSymbols && next == null)
	    return Penalties.phrasePenalty;
        else
            return Penalties.spacePenalty;
    }

    // Distance of position j from position i.
    // We look at location of symbol following position j,
    // with text from position i onward.
    // i <= j < nSymbols.
    public float dist(int i, int j) {
	int indexI = indices[i];
	int indexJ = indices[j];
	String sub = string.substring(indexI, indexJ);
	return font().getWidthPointKerned(sub, size());
    }

    // Width from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float width(int i, int j) {
	if (i == j)
	    return 0;
	else {
	    int indexI = indices[i];
	    int indexJ = indices[j-1] + 1;
	    String sub = string.substring(indexI, indexJ);
	    return font().getWidthPointKerned(sub, size());
	}
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
        if (j == nSymbols) {
	    int indexI = indices[i];
	    String sub = string.substring(indexI);
            if (isWhite(string.length()-1))
		return font().getWidthPointKerned(sub, size());
	    else
		return font().getWidthPointKerned(sub, size()) +
		    (next == null ? 
		     font().getWidthPointKerned(" ", size()) :
		     next.leadSpaceAdvance());
	} else 
	    return dist(i, j);
    }

    // Advance includes space at end?
    public boolean hasTrailSpace(int i, int j) {
	return j == nSymbols && isWhite(string.length()-1);
    }

    // Starts with space? 
    public boolean hasLeadSpace() {
	return indices[0] > 0;
    }

    // Spaces at beginning.
    public float leadSpaceAdvance() {
	return indices[0] > 0 ? font().getWidthPointKerned(" ", size()) : 0f;
    }

    // Font metrics.
    public float leading() {
	return size() * renderParams.leadingFactor;
    }
    public float ascent() {
	if (renderParams.uniformAscent && !isFootnote())
	    return font().getFontDescriptor(BaseFont.ASCENT, size()) +
		CoordPdfPart.ascent(renderParams.footLatinFont, 
			renderParams.footLatinSize,
			renderParams.leadingFactor);
	else
	    return font().getFontDescriptor(BaseFont.ASCENT, size());
    }
    public float descent() {
	return - font().getFontDescriptor(BaseFont.DESCENT, size());
    }

    // Ascent without coordinate markers.
    public float exclusiveAscent() {
	return font().getFontDescriptor(BaseFont.ASCENT, size());
    }

    // Draw substring.
    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
	if (i != j) {
	    int indexI = indices[i];
	    int indexJ = indices[j-1] + 1;
	    String sub = string.substring(indexI, indexJ);
	    surface.setFontAndSize(font(), size());
	    surface.setColorFill(BaseColor.BLACK);
	    surface.setTextMatrix(x, y);
	    surface.showTextKerned(sub);
	}
    }

    // Different tier parts use different fonts.
    protected abstract BaseFont font();

    // Size of font.
    protected abstract float size();

}
