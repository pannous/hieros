/***************************************************************************/
/*                                                                         */
/*  TierSpan.java                                                          */
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

// Span in tier, consisting of tier and two input positions.

package nederhof.interlinear;

import java.awt.*;
import java.util.*;

import com.itextpdf.text.pdf.PdfContentByte;

public class TierSpan implements Comparable {

    public Tier tier;
    public int fromPos;
    public int toPos;

    // Span between two positions.
    public TierSpan(Tier tier, int fromPos, int toPos) {
	this.tier = tier;
	this.fromPos = fromPos;
	this.toPos = toPos;
    }

    // Empty span.
    public TierSpan(Tier tier, int pos) {
	this(tier, pos, pos);
    }

    // Empty span.
    public boolean isEmpty() {
	return fromPos == toPos;
    }

    // Equality.
    public boolean equals(Object o) {
	if (o instanceof TierSpan) {
	    TierSpan other = (TierSpan) o;
	    return other.tier == tier &&
		other.fromPos == fromPos &&
		other.toPos == toPos;
	} else
	    return false;
    }

    // Comparison.
    public int compareTo(Object o) {
	if (o instanceof TierSpan) {
	    TierSpan other = (TierSpan) o;
	    if (compareTo(tier.id(), other.tier.id()) != 0)
		return compareTo(tier.id(), other.tier.id());
	    else if (compareTo(fromPos, other.fromPos) != 0)
		return compareTo(fromPos, other.fromPos);
	    else 
		return compareTo(toPos, other.toPos);
	} else
	    return 1;
    }

    // Compare two numbers.
    private static int compareTo(int i1, int i2) {
	if (i1 < i2)
	    return -1;
	else if (i1 > i2)
	    return 1;
	else
	    return 0;
    }

    // What is width from begin position to end position?
    public float width() {
	return tier.width(fromPos, toPos);
    }
    // What is advance from begin position to end position?
    public float advance() {
	return tier.advance(fromPos, toPos);
    }
    // What is advance from begin position to position?
    public float advance(int pos) {
	return tier.advance(fromPos, pos);
    }
    // What is distance from begin position to end position?
    public float dist() {
	return tier.dist(fromPos, toPos);
    }
    // What is distance from begin position to position?
    public float dist(int pos) {
	return tier.dist(fromPos, pos);
    }

    // What is leading + ascent, descent, height.
    public float leadingAscent() {
	float sum = 0;
	Vector<TierPart> parts = tier.parts(fromPos, toPos);
	for (int i = 0; i < parts.size(); i++) {
	    TierPart part = parts.get(i);
	    sum = Math.max(sum, part.leading() + part.ascent());
	}
	return sum;
    }
    public float descent() {
	float sum = 0;
	Vector<TierPart> parts = tier.parts(fromPos, toPos);
	for (int i = 0; i < parts.size(); i++) {
	    TierPart part = parts.get(i);
	    sum = Math.max(sum, part.descent());
	}
	return sum;
    }
    public float height() {
	return leadingAscent() + descent();
    }

    public boolean includesContent() {
	return tier.includesContent(fromPos, toPos);
    }

    public void draw(Graphics2D g, int x, int y) {
	tier.draw(fromPos, toPos, x, y, g);
    }

    public void draw(PdfContentByte surface, float x, float y) {
	tier.draw(fromPos, toPos, x, y, surface);
    }

    public TierPos getTierPos(int x, int y) {
	return tier.getTierPos(fromPos, toPos, x, y);
    }

    public Rectangle getRectangle(int i) {
	return tier.getRectangle(fromPos, i);
    }

    // Spans which precede this span, by precedence relation.
    public TreeSet<TierSpan> precedingUnbreakableSpans() {
	TreeSet<TierSpan> spans = new TreeSet<TierSpan>();
	for (int i = fromPos; i < toPos; i++) {
	    TreeSet<TierPos> precedes = tier.precedings(i);
	    for (Iterator<TierPos> it = precedes.iterator(); it.hasNext(); ) {
		TierPos pos = it.next();
		spans.add(pos.unbreakableSpan());
	    }
	}
	return spans;
    }

    // Spans which are break-linked to this span.
    public TreeSet<TierSpan> linkedUnbreakableSpans() {
	TreeSet<TierSpan> spans = new TreeSet<TierSpan>();
	for (int i = fromPos; i < toPos; i++) {
	    TreeSet<TierPos> breaks = tier.breaks(i);
	    for (Iterator<TierPos> it = breaks.iterator(); it.hasNext(); ) {
		TierPos pos = it.next();
		spans.add(pos.unbreakableSpan());
	    }
	}
	return spans;
    }

    // Is there next unbreakable span? (Assuming 'this' is unbreakable.)
    public boolean hasNextUnbreakable() {
	return toPos < tier.nSymbols();
    }

    // Get unbreakable span following this one. Null if none.
    public TierSpan nextUnbreakable() {
	return hasNextUnbreakable() ?
	    tier.unbreakableSpan(toPos) : null;
    }

    // Is there previous unbreakable span? (Assuming 'this' is unbreakable.)
    public boolean hasPrevUnbreakable() {
	return fromPos > 0;
    }

    // Get unbreakable span preceding this one. Null if none.
    public TierSpan prevUnbreakable() {
	return hasPrevUnbreakable() ?
	    tier.unbreakableSpan(fromPos-1) : null;
    }

    // Which TierParts are involved?
    public Vector<TierPart> parts() {
	return tier.parts(fromPos, toPos);
    }

    // Take apart into located parts.
    public Vector<TierPart> parts(float location) {
	return tier.parts(fromPos, toPos, location);
    }

    // Situate all parts.
    public void situate() {
	tier.situate(fromPos, toPos);
    }

    // Get footnotes.
    public Vector getFootnotes() {
	return tier.getFootnotes(fromPos, toPos);
    }

    // For testing.
    public String toString() {
	return "tier.id=" + tier.id() + ", fromPos=" + fromPos + ", toPos=" + toPos;
    }

}
