/***************************************************************************/
/*                                                                         */
/*  NotePart.java                                                          */
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

package nederhof.interlinear.egyptian;

import java.util.*;
import java.awt.*;

import nederhof.interlinear.*;
import nederhof.interlinear.frame.*;

public class NotePart extends EgyptianTierAwtPart {

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

    public NotePart(Vector text, int symbol) {
	footnote = new Footnote(text, symbol) {
	    public Tier getTier() {
		Vector parts = new Vector();
		FootnoteMarker footMark = new FootnoteMarker(makeMarker());
		parts.add(footMark);
		parts.addAll(text);
		parts = TierConstructor.prepareFootnote(parts, renderParams, false);
		return new Tier(parts);
	    }
	};
    }

    // How many symbols.
    public int nSymbols() {
	if (symbol() < 0)
	    return 1;
	else
	    return 0;
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
	    return metrics().stringWidth(footnote.makeMarker());
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	if (j == nSymbols()) {
	    return metrics().stringWidth(footnote.makeMarker()) +
		(next == null ? 
		 metrics().stringWidth(" ") : 
		 next.leadSpaceAdvance());
	} else
	    return dist(i, j);
    }

    // Font metrics.
    public float leading() {
	return metrics().getLeading();
    }
    public float ascent() {
	return (1 + renderParams.raisingFactor) * metrics().getAscent();
    }
    public float descent() {
	return 0;
    }

    public void situate(int i, int j) {
	if (i != j) 
	    footnote.setMarker(renderParams.getMarker());
    }

    public TreeSet<Footnote> getFootnotes(int i, int j) {
	TreeSet<Footnote> footnotes = new TreeSet<Footnote>();
	if (i != j)
	    footnotes.add(footnote);
	return footnotes;
    }

    public void draw(int i, int j, int x, int y, Graphics2D g) {
	if (i != j) {
	    g.setFont(font());
	    if (highlights.isEmpty())
		g.setColor(color());
	    else
		g.setColor(Color.RED);
	    g.drawString(footnote.makeMarker(), 
		    x, 
		    y - renderParams.raisingFactor * metrics().getAscent());
	    if (!highlightsAfter.isEmpty()) {
		g.setColor(Color.BLUE);
		g.fillRect(x + Math.round(advance()),
			    y - Math.round(ascent()),
			    highlightBarWidth,
			    Math.round(ascent() + descent()));
	    }
	}
    }

    public int getPos(int i, int j, int x, int y) {
        if (i != j && x >= 0 && x < advance(i, j))
            return 0;
        else
            return -1;
    }

    protected Color color() {
        return renderParams.footnoteMarkerColor();
    }

    protected Font font() {
	return renderParams.footLatinFont;
    }

    protected FontMetrics metrics() {
	return renderParams.footLatinFontMetrics;
    }

}
