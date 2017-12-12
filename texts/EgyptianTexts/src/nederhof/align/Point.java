/***************************************************************************/
/*                                                                         */
/*  Point.java                                                             */
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

// Indication of point, i.e. coord or align tag.
// It can have empty content or be open or close.
//
// Subclasses:
// EmptyPoint
// BoundaryPoint

package nederhof.align;

import java.awt.*;

abstract class Point extends Elem {

    private Pos pos;
    private boolean isAlign;

    // Not in unselected stream?
    private boolean visible;

    // Constructor.
    public Point(int type, Pos pos, boolean isAlign) {
	super(type);
	this.pos = pos;
	this.isAlign = isAlign;
	visible = true;
    }

    public Pos getPos() {
	return pos;
    }

    public boolean isAlign() {
	return isAlign;
    }

    // See Elem.
    public boolean isContent() {
	return false;
    }

    // See Elem.
    public float getWidth(RenderContext context) {
	if (!visible)
	    return 0.0f;
	else if (isPrintable()) {
	    String tag = pos.getTag();
	    return Math.max(
		    barMetrics(context).stringWidth("|") + spaceWidth(context)
		    , 
		    labelMetrics(context).stringWidth(tag)
		    );
	} else
	    return spaceWidth(context);
    }

    // See Elem.
    public float getWidth(RenderContext context, int index) {
	return getWidth(context);
    }

    // See Elem.
    public float getAdvance(RenderContext context) {
	if (!visible)
	    return 0.0f;
	else if (isPrintable()) 
	    return barMetrics(context).stringWidth("|") + spaceWidth(context);
	else
	    return spaceWidth(context);
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
	if (!visible || !isPrintable())
	    return 0.0f;
	else if (getType() == RenderContext.HIERO_FONT)
	    return 0.0f;
	else
	    return lineMetrics(context).getDescent();
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	if (!visible || !isPrintable())
	    return 0.0f;
	else 
	    return lineAscent(context) +
		labelMetrics(context).getDescent() + 
		labelMetrics(context).getAscent();
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	/*
	if (!visible || !isPrintable())
	    return 0.0f;
	else if (getType() == RenderContext.HIERO_FONT)
	    return HieroElem.getHieroLeading(context);
	else 
	    return lineMetrics(context).getLeading();
	    */
	return 0;
    }

    // See Elem.
    // If printable position, then not breakable.
    // Otherwise, only breakable if space.
    public boolean breakable() {
	return !isPrintable() && hasTrailingSpace();
    }

    // See Elem.
    public int firstBreak(RenderContext context, float len) {
	if (breakable() && getWidth(context) <= len)
	    return 1;
	else
	    return -1;
    }

    // See Elem.
    public int firstBreak() {
	if (breakable())
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
    // A stroke with on top the position.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	if (isPrintable()) {
	    String tag = pos.getTag();
	    float raised = lineAscent(context) + labelMetrics(context).getDescent();
	    g.drawString(RenderContext.NORMAL_FONT, context.getPointColor(), 
		    "|", getX(), y);
	    g.drawString(RenderContext.NORMAL_FONT, context.getPointColor(), 
		    tag, getX(), y - raised);
	}
    }

    // Overrides the method in Elem.
    public Elem allowVisible(boolean visible) {
	if (visible)
	    return this;
	else {
	    Point hidden = (Point) clone();
	    hidden.visible = false;
	    return hidden;
	}
    }

    // Default height for point.
    public static float getDefaultHeight(RenderContext context) {
	return latinMetrics(context).getHeight() + 
	    latinMetrics(context).getDescent() + 
	    latinMetrics(context).getAscent(); 
    }

    // Default ascent for point.
    public static float getDefaultAscent(RenderContext context) {
	return latinMetrics(context).getAscent() + 
	    latinMetrics(context).getDescent() + 
	    latinMetrics(context).getAscent();
    }

    // Ascent of line, including bar.
    private float lineAscent(RenderContext context) {
	float ascent;
	switch (getType()) {
	    case RenderContext.HIERO_FONT:
		ascent = context.getHieroContext().emSizePt();
		break;
	    case RenderContext.LX:
		ascent = Lx.getLxAscent(context);
		break;
	    default:
		ascent = lineMetrics(context).getAscent();
		break;
	}
	ascent = Math.max(ascent, barMetrics(context).getAscent());
	return ascent;
    }

    // Get font metrics for normal text (not hi).
    // In the case of lx, this is the same as for al.
    private GeneralFontMetrics lineMetrics(RenderContext context) {
	if (getType() == RenderContext.LATIN_FONT)
	    return latinMetrics(context);
	else 
	    return context.getFontMetrics(RenderContext.EGYPT_FONT);
    }

    // Get font metrics for bar.
    private static GeneralFontMetrics barMetrics(RenderContext context) {
	return labelMetrics(context);
    }

    // Get font metrics for position tag.
    private static GeneralFontMetrics labelMetrics(RenderContext context) {
	return context.getFontMetrics(RenderContext.NORMAL_FONT);
    }

    // Get font metrics for Latin text.
    private static GeneralFontMetrics latinMetrics(RenderContext context) {
	return context.getFontMetrics(RenderContext.LATIN_FONT);
    }
}
