/***************************************************************************/
/*                                                                         */
/*  Elem.java                                                              */
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

// An element in a stream or formatted paragraph.
//
// Subclasses:
// TextElem
// HieroElem
// Marker
// Lx
// Note
// Point
// Markup
// EmptyElem
// Link
// Bullet
// Header
// VersionLabel

package nederhof.align;

import java.awt.*;
import java.util.*;

abstract class Elem implements Cloneable {

    // x coordinate in formatted text.
    private float x;

    // Type of text in which element occurs.
    // Among others used to determine size of trailing space.
    private int type;

    // Trailing space after actual element.
    private boolean trailingSpace;

    // Particular elements are incrementally added to text, 
    // the particular prefixes are indicated by an index.
    // A negative index means that the prefix is the entire element.
    private int index;

    // Called by constructors of subtypes.
    public Elem(int type) {
	this.type = type;
	trailingSpace = false;
	index = -1;
    }

    // Set x coordinate in paragraph.
    public void setX(float x) {
	this.x = x;
    }

    // Get x coordinate in paragraph.
    public float getX() {
	return x;
    }

    // Get text type.
    public int getType() {
	return type;
    }

    // Set trailing space.
    public void setTrailingSpace(boolean trailingSpace) {
	this.trailingSpace = trailingSpace;
    }

    // Has trailing space? 
    public boolean hasTrailingSpace() {
	return trailingSpace;
    }

    // Reset prefix of element that is already prefix.
    // Overridden in TextElem and HieroElem.
    public void setPrefix(int index) {
	this.index = index;
    }

    // Get prefix.
    public int getPrefix() {
	return index;
    }

    // Is prefix?
    public boolean isPrefix() {
	return index >= 0;
    }

    // Leaves visible trace?
    public abstract boolean isPrintable();
    // Real text, not just points or unprintable matter?
    public abstract boolean isContent();
    // Various metrics. When index is given, then of corresponding prefix.
    public abstract float getWidth(RenderContext context);
    public abstract float getWidth(RenderContext context, int index); 
    public abstract float getAdvance(RenderContext context);
    public abstract float getAdvance(RenderContext context, int index); 
    public abstract float getHeight(RenderContext context);
    public abstract float getDescent(RenderContext context);
    public abstract float getAscent(RenderContext context);
    public abstract float getLeading(RenderContext context);
    // Line break possible somewhere in element?
    public abstract boolean breakable();
    // First index where can be broken into smaller parts, 
    // but prefix must not be longer than len (in pixels or points). 
    // If no such index exists, return negative number.
    // The exact meaning of the index differs among subclasses.
    public abstract int firstBreak(RenderContext context, float len);
    // First index where can be broken.
    // If no such index exists, return negative number.
    public abstract int firstBreak();
    // Last index where can be broken into smaller parts, 
    // but prefix must not be longer than len (in pixels or points). 
    // If no such index exists, return negative number.
    public abstract int lastBreak(RenderContext context, float len);
    // First break after prefix.
    // If no such index exists, return negative number.
    public abstract int nextBreak(); 
    // Split off prefix, given index (which must be non-negative). 
    public abstract Elem prefix(int index);
    // Make suffix from element that is prefix. 
    public abstract Elem suffix();
    // Draw in generalized graphics.
    public abstract void draw(RenderContext context, GeneralDraw g, float y);

    // Make visible or not. I.e. replace by empty element if not visible.
    // Overridden in Point.
    public Elem allowVisible(boolean visible) {
	if (visible)
	    return this;
	else
	    return new EmptyElem(breakable());
    }

    // Determine size of trailing space.
    public float spaceWidth(RenderContext context) {
	if (!hasTrailingSpace())
	    return 0;
	switch (type) {
	    case RenderContext.LX: 
		return context.getLxSep();
	    case RenderContext.HIERO_FONT:
		return context.getFontMetrics(RenderContext.LATIN_FONT).
		    stringWidth(" ");
	    case RenderContext.FOOT_HIERO_FONT:
		return context.getFontMetrics(RenderContext.FOOT_LATIN_FONT).
		    stringWidth(" ");
	    default: 
		return context.getFontMetrics(type).stringWidth(" ");
	}
    }

    // Make field-for-field copy of element.
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    return null;
	}
    }

    // List contains content element, so not only empty space or spurious
    // bullet.
    public static boolean isContent(LinkedList elems) {
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    if (elem.isContent())
		return true;
	}
	return false;
    }
}
