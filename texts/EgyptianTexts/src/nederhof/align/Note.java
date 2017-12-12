/***************************************************************************/
/*                                                                         */
/*  Note.java                                                              */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// A note in a stream.
// We also use this class for a footnote marker before the actual footnote text
// in formatted text.

package nederhof.align;

import java.awt.*;
import java.util.*;

class Note extends Elem {

    // Stream of footnote text.
    private LinkedList stream;

    // Footnote marker.
    // At first this is a dummy, which has about maximum width.
    // This is meant for situations where exact marker can only be determined
    // at a later stage.
    private String marker;
    private static final String dummyMarker = "88";

    // For occurrence of footnote marker in text.
    public Note(int type) {
	super(type);
	this.stream = new LinkedList();
	marker = dummyMarker;
    }

    // For occurrence of footnote marker before footnote text.
    public Note() {
	this(RenderContext.LATIN_FONT);
    }

    public LinkedList getStream() {
	return stream;
    }

    public void setMarker(String marker) {
	this.marker = marker;
    }

    public String getMarker() {
	return marker;
    }

    // See Elem.
    public boolean isPrintable() {
	return true;
    }

    // See Elem.
    public boolean isContent() {
	return true;
    }

    // See Elem.
    public float getWidth(RenderContext context) {
	return footMetrics(context).stringWidth(marker);
    }

    // See Elem.
    public float getWidth(RenderContext context, int index) {
	return getWidth(context);
    }

    // See Elem.
    public float getAdvance(RenderContext context) {
	return getWidth(context) + spaceWidth(context);
    }

    // See Elem.
    public float getAdvance(RenderContext context, int index) {
	return getAdvance(context);
    }

    // See Elem.
    public float getHeight(RenderContext context) {
	return getLeading(context) + getDescent(context) + getAscent(context);
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	if (getType() == RenderContext.HIERO_FONT)
	    return 0;
	else
	    return lineMetrics(context).getDescent();
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	switch (getType()) {
	    case RenderContext.HIERO_FONT:
		return context.getHieroContext().emSizePt() +
		    footMetrics(context).getAscent();
	    case RenderContext.LX:
		return Lx.getLxAscent(context) +
		    footMetrics(context).getAscent();
	    default:
		return Math.max(
			lineMetrics(context).getAscent()
			,
			(context.getFootRaise() * lineMetrics(context).getAscent()) +
			footMetrics(context).getAscent()
			);
	}
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	/*
	if (getType() == RenderContext.HIERO_FONT)
	    return HieroElem.getHieroLeading(context);
	else
	    return lineMetrics(context).getLeading();
	    */
	return 0;
    }

    // See Elem.
    public boolean breakable() {
	return hasTrailingSpace();
    }

    // See Elem.
    public int firstBreak(RenderContext context, float len) {
	if (hasTrailingSpace() && getWidth(context) <= len)
	    return 1;
	else
	    return -1;
    }

    // See Elem.
    public int firstBreak() {
	if (hasTrailingSpace())
	    return 1;
	else
	    return -1;
    }

    // See Elem.
    public int lastBreak(RenderContext context, float len) {
	return firstBreak(context, len);
    }

    // See Elem.
    public int nextBreak() {
	return -1;
    }

    // See Elem.
    public Elem prefix(int index) {
	return this;
    }

    // See Elem.
    public Elem suffix() {
	return null; // should not happen
    }

    // See Elem.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	float raised;
	switch (getType()) {
	    case RenderContext.HIERO_FONT:
		raised = context.getHieroContext().emSizePt();
		break;
	    case RenderContext.LX:
		raised = Lx.getLxAscent(context);
		break;
	    default:
		raised = context.getFootRaise() * lineMetrics(context).getAscent();
		break;
	}
	g.drawString(RenderContext.FOOT_LATIN_FONT, context.getNoteColor(), 
		marker, getX(), y - raised);
    }

    // Get font metrics for normal text (not hi).
    // In the case of lx, this is the same as for al.
    private GeneralFontMetrics lineMetrics(RenderContext context) {
	if (getType() == RenderContext.LATIN_FONT)
	    return latinMetrics(context);
	else 
	    return context.getFontMetrics(RenderContext.EGYPT_FONT);
    }

    // Get font metrics for Latin text.
    private GeneralFontMetrics latinMetrics(RenderContext context) {
	return context.getFontMetrics(RenderContext.LATIN_FONT);
    }

    // Get font metrics for footnote markers.
    private GeneralFontMetrics footMetrics(RenderContext context) {
	return context.getFontMetrics(RenderContext.FOOT_LATIN_FONT);
    }

    // Creation of footnote marker on line.
    // Line number to number, and marker to a, b, c, ..., aa, ab, ...
    public static String footnoteString(int line, int marker) {
	String markStr = "";
	do {
	    int digit = marker % 26;
	    marker /= 26;
	    markStr = Character.toString((char) (digit + 'a'));
	} while (marker > 0);
	return Integer.toString(line) + markStr;
    }
}
