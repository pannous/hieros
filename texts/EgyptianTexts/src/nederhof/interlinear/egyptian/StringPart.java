/***************************************************************************/
/*                                                                         */
/*  StringPart.java                                                        */
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

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.util.*;

import nederhof.interlinear.*;

public abstract class StringPart extends EgyptianTierAwtPart {

    // The string.
    public String string;

    // Number of non-space characters.
    protected int nSymbols = 0;

    // Maps positions to indices.
    protected int[] indices;

    // Construct.
    public StringPart(String string) {
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
	return metrics().stringWidth(sub);
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
	    return metrics().stringWidth(sub);
	}
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
        if (j == nSymbols) {
	    int indexI = indices[i];
	    String sub = string.substring(indexI);
            if (isWhite(string.length()-1))
		return metrics().stringWidth(sub);
	    else
		return metrics().stringWidth(sub) +
		    (next == null ? 
		     metrics().stringWidth(" ") : 
		     next.leadSpaceAdvance());
	} else 
	    return dist(i, j);
    }

    // Starts with space? 
    public boolean hasLeadSpace() {
	return indices[0] > 0;
    }

    // Spaces at beginning.
    public float leadSpaceAdvance() {
	return indices[0] > 0 ? metrics().stringWidth(" ") : 0f;
    }

    // Font metrics.
    public float leading() {
        return metrics().getLeading();
    }
    public float ascent() {
	if (renderParams.uniformAscent && !isFootnote())
	    return metrics().getAscent() +
		CoordPart.ascent(renderParams.footLatinFontMetrics);
	else
	    return metrics().getAscent();
    }
    public float descent() {
        return metrics().getDescent();
    }

    // Ascent without coordinate markers.
    public float exclusiveAscent() {
	return metrics().getAscent();
    }

    // Draw substring. Draw rectangle around highlighted letters,
    // or after letters.
    public void draw(int i, int j, int x, int y, Graphics2D g) {
	if (i != j) {
	    int indexI = indices[i];
	    int indexJ = indices[j-1] + 1;
	    String sub = string.substring(indexI, indexJ);
	    g.setFont(font());
	    g.setColor(color());
	    g.drawString(sub, x, y);
	    drawHighlight(i, j, x, y, g);
	}
    }

    // Draw highlights.
    public void drawHighlight(int i, int j, int x, int y, Graphics2D g) {
	for (Iterator it = highlights.iterator(); it.hasNext(); ) {
	    int high = ((Integer) it.next()).intValue();
	    if (i <= high && high < j) {
		int indexI = indices[i];
		int indexHigh = indices[high];
		String prefixSub = string.substring(indexI, indexHigh);
		int prefixWidth = metrics().stringWidth(prefixSub);
		String highSub = string.substring(indexHigh, indexHigh+1);
		int highWidth = metrics().stringWidth(highSub);
		g.setColor(Color.BLUE);
		g.draw(new Rectangle(x + prefixWidth, 
			    y - Math.round(ascent()), 
			    highWidth,
			    Math.round(ascent() + descent())));
	    }
	}
	for (Iterator it = highlightsAfter.iterator(); it.hasNext(); ) {
	    int high = ((Integer) it.next()).intValue();
	    if (i <= high && high < j) {
		int indexI = indices[i];
		int indexHigh = indices[high];
		String prefixSub = string.substring(indexI, indexHigh);
		int prefixWidth = metrics().stringWidth(prefixSub);
		String highSub = string.substring(indexHigh, indexHigh+1);
		int highWidth = metrics().stringWidth(highSub);
		g.setColor(Color.BLUE);
		g.fillRect(x + prefixWidth + Math.round(advance(high, high+1)),
			// highWidth, 
			    y - Math.round(ascent()), 
			    highlightBarWidth,
			    Math.round(ascent() + descent()));
	    }
	}
    }

    public int getPos(int i, int j, int x, int y) {
        if (i != j && x >= 0 && x < advance(i, j)) {
	    for (int k = i; k < j; k++)
		if (x < advance(i, k+1)) 
		    return k-i;
	} 
	return -1;
    }

    public Rectangle getRectangle(int i, int j) {
	int indexI = indices[i];
	int indexJ = indices[j];
	String prefixSub = string.substring(indexI, indexJ);
	int prefixWidth = metrics().stringWidth(prefixSub);
	String highSub = string.substring(indexJ, indexJ+1);
	int highWidth = metrics().stringWidth(highSub);
	return new Rectangle(prefixWidth, 
		- Math.round(ascent()),
		highWidth,
		Math.round(ascent() + descent()));
    }

    // Different tier parts use different fonts.
    protected abstract Font font();

    // Different tier parts use different metrics.
    protected abstract FontMetrics metrics();

    // Different tier parts use different colors.
    // Default is black.
    protected Color color() {
	return Color.BLACK;
    }

    //////////////////////////
    // Editing.

    // Prefix and suffix, pos > 0 and pos < nSymbols.
    protected String prefix(int pos) {
	int index = indices[pos];
	return string.substring(0, index);
    }
    protected String suffix(int pos) {
	int index = indices[pos];
	return string.substring(index, string.length());
    }

    // Make string part for prefix and suffix.
    public abstract StringPart prefixPart(int pos);
    public abstract StringPart suffixPart(int pos);

}
