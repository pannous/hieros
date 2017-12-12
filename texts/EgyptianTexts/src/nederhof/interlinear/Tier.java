/***************************************************************************/
/*                                                                         */
/*  Tier.java                                                              */
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

// A tier (stream) which is part of interlinear form.

package nederhof.interlinear;

import java.awt.*;
import java.util.*;

import com.itextpdf.text.pdf.PdfContentByte;

public class Tier {

    // Tiers considered together must be numbered by ids from 0 onward,
    // to allow them to have links back and forth.
    private int id;

    // The viewing mode of the tier.
    private String mode;

    // Tier is divided into parts.
    private int nParts;
    public TierPart[] parts;

    // There are a number of symbols in all parts together.
    private int nSymbols;

    // Positions corresponding to beginning of
    // part point to that part.
    public int[] positionToPart;

    // Mapping from parts to positions.
    public int[] partToPosition;

    // Some of the positions are breakable.
    private boolean[] breakable;

    // Penalty of line break at position.
    private double[] penalties;

    // Some of the positions are linked to others in other tiers,
    // for precedence or for breaking.
    // Precedence links added manually are given special
    // attention for the purpose of editing, although they
    // are further treated identically.
    private TreeSet[] precedenceLinks;
    private TreeSet[] manualPrecedenceLinks;
    private TreeSet<TierPos>[] breakLinks;

    // Construct tier from parts.
    // The vector should contain at least one TierPart.
    public Tier(int id, String mode, Vector<TierPart> tierParts) {
	this.id = id;
	this.mode = mode;
	nParts = tierParts.size();
	parts = new TierPart[nParts];
	partToPosition = new int[nParts];
	nSymbols = 0;
	for (int i = 0; i < nParts; i++) {
	    TierPart part = tierParts.get(i);
	    parts[i] = part;
	    partToPosition[i] = nSymbols;
	    nSymbols += part.nSymbols();
	}
	positionToPart = new int[nSymbols];
	breakable = new boolean[nSymbols];
	penalties = new double[nSymbols];
	precedenceLinks = new TreeSet[nSymbols];
	manualPrecedenceLinks = new TreeSet[nSymbols];
	breakLinks = new TreeSet[nSymbols];
	breakable[0] = true;
	penalties[0] = 0;
	int pastSymbols = 0;
	for (int i = 0; i < nParts; i++) {
	    TierPart part = parts[i];
	    for (int j = 0; j < part.nSymbols(); j++) {
		int pos = pastSymbols + j;
		positionToPart[pos] = i;
		if (j > 0) {
		    breakable[pos] = part.breakable(j);
		    penalties[pos] = part.penalty(j);
		}
		precedenceLinks[pos] = null;
		manualPrecedenceLinks[pos] = null;
		breakLinks[pos] = null;
	    }
	    pastSymbols += part.nSymbols();
	    if (pastSymbols < nSymbols) {
		breakable[pastSymbols] = part.breakable(part.nSymbols());
		penalties[pastSymbols] = part.penalty(part.nSymbols());
	    }
	}
    }
    // Constructor if id and mode are not relevant.
    public Tier(Vector<TierPart> tierParts) {
	this(0, TextResource.SHOWN, tierParts);
    }

    // Id (index) of this tier.
    public int id() {
	return id;
    }

    // Viewing mode of this tier.
    public String getMode() {
	return mode;
    }

    // Number of symbols.
    public int nSymbols() {
	return nSymbols;
    }

    // Position relative to tier from position relative to part.
    public int tierPos(int part, int pos) {
	return partToPosition[part] + pos;
    }

    // Is tier breakable at position? Final position always breakable.
    public boolean breakable(int i) {
	return i >= nSymbols || breakable[i];
    }

    // Penalty of line break at position.
    public double penalty(int i) {
	return i >= nSymbols ? 0 : penalties[i];
    }

    // What is next breakable position?
    public int nextBreakable(int i) {
	int j = i+1;
	while (!breakable(j))
	    j++;
	return j;
    }

    // What is previous breakable position, including present?
    public int prevBreakable(int i) {
	int j = i;
	while (!breakable(j))
	    j--;
	return j;
    }

    // What is enclosing unbreakable span?
    public TierSpan unbreakableSpan(int i) {
	return new TierSpan(this, prevBreakable(i), nextBreakable(i));
    }

    // What is width of span?
    public float width(int i, int j) {
	int startPart = positionToPart[i];
	int fromIndex = i - partToPosition[startPart];
	int endPart = positionToPart[j>0 ? j-1 : j];
	int toIndex = j - partToPosition[endPart];
	int part = startPart;
	float width = 0f;
	while (part < endPart) {
	    width += parts[part].advanceFrom(fromIndex);
	    fromIndex = 0;
	    part++;
	}
	width += parts[part].width(fromIndex, toIndex);
	return width;
    }

    // What is advance of span?
    public float advance(int i, int j) {
	int startPart = positionToPart[i];
	int fromIndex = i - partToPosition[startPart];
	int endPart = positionToPart[j>0 ? j-1 : j];
	int toIndex = j - partToPosition[endPart];
	int part = startPart;
	float advance = 0f;
	while (part < endPart) {
	    advance += parts[part].advanceFrom(fromIndex);
	    fromIndex = 0;
	    part++;
	}
	advance += parts[part].advance(fromIndex, toIndex);
	return advance;
    }

    // What is distance from position to position?
    public float dist(int i, int j) {
	int startPart = positionToPart[i];
	int fromIndex = i - partToPosition[startPart];
	int endPart = positionToPart[j];
	int toIndex = j - partToPosition[endPart];
	int part = startPart;
	float dist = 0f;
	while (part < endPart) {
	    dist += parts[part].advanceFrom(fromIndex);
	    fromIndex = 0;
	    part++;
	}
	dist += parts[part].dist(fromIndex, toIndex);
	return dist;
    }

    // Draw span.
    public void draw(int i, int j, int x, int y, Graphics2D g) {
	if (mode.equals(TextResource.ERASED))
	    return;
	int startPart = positionToPart[i];
	int fromIndex = i - partToPosition[startPart];
	int endPart = positionToPart[j>0 ? j-1 : j];
	int toIndex = j - partToPosition[endPart];
	int part = startPart;
	float width = 0f;
	while (part < endPart) {
	    TierAwtPart p = (TierAwtPart) parts[part];
	    p.draw(fromIndex, p.nSymbols(), x + Math.round(width), y, g);
	    width += p.advanceFrom(fromIndex);
	    fromIndex = 0;
	    part++;
	}
	TierAwtPart pLast = (TierAwtPart) parts[part];
	pLast.draw(fromIndex, toIndex, x + Math.round(width), y, g);
    }

    // Draw span.
    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
	if (mode.equals(TextResource.ERASED))
	    return;
	int startPart = positionToPart[i];
	int fromIndex = i - partToPosition[startPart];
	int endPart = positionToPart[j>0 ? j-1 : j];
	int toIndex = j - partToPosition[endPart];
	int part = startPart;
	float width = 0f;
	while (part < endPart) {
	    TierPdfPart p = (TierPdfPart) parts[part];
	    p.draw(fromIndex, p.nSymbols(), x + width, y, surface);
	    width += p.advanceFrom(fromIndex);
	    fromIndex = 0;
	    part++;
	}
	TierPdfPart pLast = (TierPdfPart) parts[part];
	pLast.draw(fromIndex, toIndex, x + width, y, surface);
    }

    // Find tier pos matching position of mouse click.
    public TierPos getTierPos(int i, int j, int x, int y) {
	int startPart = positionToPart[i];
	int fromIndex = i - partToPosition[startPart];
	int endPart = positionToPart[j>0 ? j-1 : j];
	int toIndex = j - partToPosition[endPart];
	int part = startPart;
	float width = 0f;
	while (part < endPart) {
	    TierAwtPart p = (TierAwtPart) parts[part];
	    int pos = p.getPos(fromIndex, p.nSymbols(), x - Math.round(width), y);
	    if (pos >= 0)
		return new TierPos(this, i + pos);
	    width += p.advanceFrom(fromIndex);
	    i += p.nSymbols() - fromIndex;
	    fromIndex = 0;
	    part++;
	}
	TierAwtPart pLast = (TierAwtPart) parts[part];
	int posLast = pLast.getPos(fromIndex, toIndex, x - Math.round(width), y);
	if (posLast >= 0)
	    return new TierPos(this, i + posLast);
	return null;
    }

    // Find rectangle around position.
    public Rectangle getRectangle(int i, int j) {
	int startPart = positionToPart[i];
	int fromIndex = i - partToPosition[startPart];
	int endPart = positionToPart[j];
	int toIndex = j - partToPosition[endPart];
	int part = startPart;
	float width = 0f;
	while (part < endPart) {
	    width += parts[part].advanceFrom(fromIndex);
	    fromIndex = 0;
	    part++;
	}
	TierAwtPart pLast = (TierAwtPart) parts[part];
	Rectangle rect = pLast.getRectangle(fromIndex, toIndex);
	if (rect != null) {
	    rect = (Rectangle) rect.clone();
	    rect.translate(Math.round(width), 0);
	}
	return rect;
    }

    // Highlight position.
    public void highlight(int i) {
	int part = positionToPart[i];
	int fromIndex = i - partToPosition[part];
	TierAwtPart p = (TierAwtPart) parts[part];
	p.highlight(fromIndex);
    }
    // Undo highlighting.
    public void unhighlight(int i) {
	int part = positionToPart[i];
	int fromIndex = i - partToPosition[part];
	TierAwtPart p = (TierAwtPart) parts[part];
	p.unhighlight(fromIndex);
    }

    // Highlight after position.
    public void highlightAfter(int i) {
	int part = positionToPart[i];
	int fromIndex = i - partToPosition[part];
	TierAwtPart p = (TierAwtPart) parts[part];
	p.highlightAfter(fromIndex);
    }
    // Undo highlighting after position.
    public void unhighlightAfter(int i) {
	int part = positionToPart[i];
	int fromIndex = i - partToPosition[part];
	TierAwtPart p = (TierAwtPart) parts[part];
	p.unhighlightAfter(fromIndex);
    }

    public boolean includesContent(int i, int j) {
	Vector<TierPart> parts = parts(i, j);
	for (int k = 0; k < parts.size(); k++) {
	    TierPart tierPart = parts.get(k);
	    if (tierPart.isContent())
		return true;
	}
	return false;
    }

    // Parts of spans in vector.
    public Vector<TierPart> parts(int i, int j) {
	int startPart = positionToPart[i];
        int endPart = positionToPart[j>0 ? j-1 : j];
	Vector<TierPart> tierParts = new Vector<TierPart>();
	for (int part = startPart; part <= endPart; part++)
	    tierParts.add(parts[part]);
	return tierParts;
    }

    // Parts of spans, located at locations, in vector.
    public Vector parts(int i, int j, float location) {
	Vector subparts = new Vector();
	int startPart = positionToPart[i];
        int fromIndex = i - partToPosition[startPart];
        int endPart = positionToPart[j>0 ? j-1 : j];
        int toIndex = j - partToPosition[endPart];
        int part = startPart;
        while (part < endPart) {
	    subparts.add(new LocatedTierPartSpan(parts[part],
			fromIndex, location));
            location += parts[part].advanceFrom(fromIndex);
            fromIndex = 0;
            part++;
        }
	subparts.add(new LocatedTierPartSpan(parts[part],
		    fromIndex, toIndex, location));
        return subparts;
    }

    // Situate parts. 
    public void situate(int i, int j) {
	int startPart = positionToPart[i];
	int fromIndex = i - partToPosition[startPart];
	int endPart = positionToPart[j>0 ? j-1 : j];
	int toIndex = j - partToPosition[endPart];
	int part = startPart;
	while (part < endPart) {
	    TierPart p = parts[part];
	    p.situate(fromIndex, p.nSymbols());
	    fromIndex = 0;
	    part++;
	}
	parts[part].situate(fromIndex, toIndex);
    }

    // Get footnotes.
    public Vector getFootnotes(int i, int j) {
	Vector notes = new Vector();
	int startPart = positionToPart[i];
	int fromIndex = i - partToPosition[startPart];
	int endPart = positionToPart[j>0 ? j-1 : j];
	int toIndex = j - partToPosition[endPart];
	int part = startPart;
	while (part < endPart) {
	    TierPart p = parts[part];
	    notes.addAll(p.getFootnotes(fromIndex, p.nSymbols()));
	    fromIndex = 0;
	    part++;
	}
	notes.addAll(parts[part].getFootnotes(fromIndex, toIndex));
	return notes;
    }

    // Constant dummy empty set. The assumption is that this set is
    // not changed by users.
    private static final TreeSet emptySet = new TreeSet();

    // Set which positions precede position.
    public void setPrecedings(int i, TreeSet precedes) {
	precedenceLinks[i] = precedes;
    }

    // Add position.
    public void addPreceding(int i, TierPos pos) {
	if (i >= precedenceLinks.length || i < 0)
	    return;
	if (precedenceLinks[i] == null)
	    precedenceLinks[i] = new TreeSet();
	precedenceLinks[i].add(pos);
    }

    // Add position in the case of manual link.
    public void addManualPreceding(int i, TierPos pos) {
	addPreceding(i, pos);
	if (i >= manualPrecedenceLinks.length || i < 0)
	    return;
	if (manualPrecedenceLinks[i] == null)
	    manualPrecedenceLinks[i] = new TreeSet();
	manualPrecedenceLinks[i].add(pos);
    }

    // Which positions precede position.
    public TreeSet<TierPos> precedings(int i) {
	if (precedenceLinks[i] == null)
	    return emptySet;
	else
	    return precedenceLinks[i];
    }

    // Which positions precede, with manual links.
    public TreeSet manualPrecedings(int i) {
	if (manualPrecedenceLinks[i] == null)
	    return emptySet;
	else
	    return manualPrecedenceLinks[i];
    }

    // Set which positions are linked to position for line breaks.
    public void setBreaks(int i, TreeSet<TierPos> breaks) {
	breakLinks[i] = breaks;
    }

    // Add break link.
    public void addBreak(int i, TierPos pos) {
	if (i >= breakLinks.length)
	    return;
	if (breakLinks[i] == null)
	    breakLinks[i] = new TreeSet<TierPos>();
	breakLinks[i].add(pos);
    }

    // Which positions are linked to position for line breaks.
    public TreeSet<TierPos> breaks(int i) {
	if (breakLinks[i] == null)
	    return emptySet;
	else
	    return breakLinks[i];
    }

}
