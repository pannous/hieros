/***************************************************************************/
/*                                                                         */
/*  HiPart.java                                                            */
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

// Hieroglypic part of tier.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.util.*;

import nederhof.interlinear.*;
import nederhof.res.*;
import nederhof.res.format.*;

public class HiPart extends EgyptianTierAwtPart {

    // Parsing context.
    public static final HieroRenderContext hieroContext =
	new HieroRenderContext(20); // fontsize arbitrary
    private static final ParsingContext parsingContext =
        new ParsingContext(hieroContext, true);

    // Hieroglyphic as string.
    public String hi;

    // Footnotes attached.
    private Vector<Footnote> notes = new Vector<Footnote>();
    // Set footnotes.
    public void setNotes(Vector<Footnote> notes) {
	this.notes = notes;
    }
    public Vector<Footnote> getNotes() {
	return notes;
    }

    // The context used to format it the last time.
    // null if none.
    private HieroRenderContext lastHieroContext;
    // Parsed RES. The original, and with footnotes.
    public ResFragment parsed;
    public ResFragment noted;
    // Formatted RES.
    private FormatFragment formatted;
    // Number of symbols therein.
    private int nSymbols;
    // Breakable symbols.
    private boolean[] breakables;

    public HiPart(String hi, boolean note) {
	this.hi = hi;
	setFootnote(note);
        parsed = ResFragment.parse(hi, parsingContext);
	noted = parsed;
	if (parsed != null) {
	    nSymbols = parsed.nGlyphs();
	    breakables = parsed.glyphAfterPaddable();
	} else {
	    nSymbols = 0;
	    breakables = null;
	}
    }

    // If not formatted with latest context, then do again.
    private void ensureFormatted() {
	if (lastHieroContext != context()) {
	    lastHieroContext = context();
	    if (noted != null)
		formatted = new FormatFragment(noted, lastHieroContext);
	}
    }

    // How many symbols.
    // Pretend there is one symbol if none.
    public int nSymbols() {
	if (nSymbols == 0 && parsed.nGroups() > 0)
	    return 1;
	return nSymbols;
    }

    // Is position breakable?
    public boolean breakable(int i) {
	if (nSymbols == 0) 
	    return next == null || next.hasLeadSpace();
	if (i == nSymbols)
	    return next == null || next.hasLeadSpace() ||
		next instanceof CoordPart;
	else if (breakables == null)
	    return false;
	else 
	    return breakables[i];
    }

    // Penalty of line break at breakable position.
    public double penalty(int i) {
	if (!breakable(i))
	    return Penalties.maxPenalty;
	else if (i == nSymbols()) {
	    if (isFootnote())
		return Penalties.spacePenalty;
	    else
		return 0;
	} else
	    return Penalties.hierobreakPenalty;
    }

    // Distance of position j from position i.
    // We look at location of symbol following position j,
    // with text from position i onward.
    // i <= j < nSymbols.
    public float dist(int i, int j) {
	ensureFormatted();
	if (formatted != null) {
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroup(j);
	    return formatted.widthDist(groupI, groupJ);
	} else 
	    return 0;
    }

    // Width from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float width(int i, int j) {
	ensureFormatted();
	if (i == j || formatted == null)
	    return 0;
	else {
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroupAndEmpty(j-1);
	    return formatted.width(groupI, groupJ);
	}
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	ensureFormatted();
	if (j == nSymbols() && formatted != null) {
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroup(j);
	    return formatted.width(groupI, groupJ) +
		context().emToPix(context().fontSep());
	} else
	    return dist(i, j);
    }

    // Space at beginning after other element.
    public float leadSpaceAdvance() {
	return context().emToPix(context().fontSep());
    }

    // Font metrics.
    public float leading() {
	return ascent() * 0.2f;
    }
    public float ascent() {
	ensureFormatted();
	if (formatted != null)
	    return formatted.height();
	else
	    return 0;
    }
    public float descent() {
	return 0;
    }

    // Assign markers to footnotes between boundaries.
    // Then place footnotes in hieroglyphic,
    // Avoid work if no footnotes within range.
    public void situate(int i, int j) {
	boolean change = false;
	for (int n = 0; n < notes.size(); n++) {
	    Footnote note = notes.get(n);
	    if (i <= note.symbol() && note.symbol() < j) {
		change = true;
		note.setMarker(renderParams.getMarker());
	    }
	}
	if (change) {
	    noted = (ResFragment) parsed.clone();
	    for (int n = 0; n < notes.size(); n++) {
		Footnote note = notes.get(n);
		noted.addNote(note.symbol(), note.makeMarker(), new Color16("blue"));
	    }
	    lastHieroContext = null;
	    ensureFormatted();
	}
    }

    public TreeSet<Footnote> getFootnotes(int i, int j) {
	TreeSet<Footnote> footnotes = new TreeSet<Footnote>();
	for (int n = 0; n < notes.size(); n++) {
	    Footnote note = notes.get(n);
	    if (i <= note.symbol() && note.symbol() < j) 
		footnotes.add(note);
	}
	return footnotes;
    }

    // Draw substring.
    public void draw(int i, int j, int x, int y, Graphics2D g) {
	ensureFormatted();
	if (i != j && formatted != null) {
	    int height = formatted.height();
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroupAndEmpty(j-1);
	    formatted.write(g, groupI, groupJ, x, y-height); 
	    drawHighlight(i, j, x, y, g);
	}
    }

    // Draw highlights.
    public void drawHighlight(int i, int j, int x, int y, Graphics2D g) {
	for (Iterator it = highlights.iterator(); it.hasNext(); ) {
	    int high = ((Integer) it.next()).intValue();
	    if (i <= high && high < j) {
		int height = formatted.height();
		Vector rects = formatted.glyphRectangles();
		Rectangle rect;
	        if (high < rects.size())	
		    rect = (Rectangle) rects.get(high);
		else
		    rect = new Rectangle(0, 0, 
			    formatted.width(), formatted.height());
		Rectangle highRect = new Rectangle(
			x + rect.x,
			y + rect.y - height,
			rect.width,
			rect.height);
		g.setColor(Color.BLUE);
		g.draw(highRect);
	    }
	}
	for (Iterator it = highlightsAfter.iterator(); it.hasNext(); ) {
	    int high = ((Integer) it.next()).intValue();
	    if (i <= high && high < j) {
		int height = formatted.height();
		Vector rects = formatted.glyphRectangles();
		Rectangle rect;
	        if (high < rects.size())	
		    rect = (Rectangle) rects.get(high);
		else
		    rect = new Rectangle(0, 0, 
			    formatted.width(), formatted.height());
		g.setColor(Color.BLUE);
		g.fillRect(x + rect.x + rect.width + 
				context().emToPix(context().fontSep()),
			y + rect.y - height,
			highlightBarWidth,
			rect.height);
	    }
	}
    }

    public int getPos(int i, int j, int x, int y) {
        ensureFormatted();
        if (i != j && formatted != null && x >= 0 && x < advance(i, j)) {
	    int height = formatted.height();
	    Vector rects = formatted.glyphRectangles();
	    if (rects.isEmpty())
		return 0;
	    else
		for (int k = i; k < j; k++) {
		    Rectangle rect = (Rectangle) rects.get(k);
		    if (rect.contains(x, y+height))
			return k-i;
		}
        } 
	return -1;
    }

    public Rectangle getRectangle(int i, int j) {
	if (formatted != null) {
	    int height = formatted.height();
	    Vector rects = formatted.glyphRectangles();
	    Rectangle rect = (Rectangle) rects.get(j);
	    rect = (Rectangle) rect.clone();
	    rect.translate(0, -height);
	    return rect;
	} else
	    return new Rectangle();
    }

    // Context depends on whether in footnote.
    public HieroRenderContext context() {
	if (isFootnote()) 
	    return renderParams.footHieroContext;
	else
	    return renderParams.hieroContext;
    }

    //////////////////////////
    // Editing.

    // Get (proper) prefix and suffix.
    // If they are not proper, return null.
    public HiPart prefixPart(int pos) {
	if (parsed != null) {
	    int len = parsed.glyphToGroup(pos);
	    if (len > 0 && len < parsed.nGroups()) {
		ResFragment prefix = parsed.prefixGroups(len);
		return new HiPart(prefix.toString(), isFootnote());
	    } else
		return null;
	} else
	    return null;
    }
    public HiPart suffixPart(int pos) {
	if (parsed != null) {
	    int len = parsed.glyphToGroup(pos);
	    if (len > 0 && len < parsed.nGroups()) {
		ResFragment suffix = parsed.suffixGroups(len);
		return new HiPart(suffix.toString(), isFootnote());
	    } else
		return null;
	} else
	    return null;
    }

}
