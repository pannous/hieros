/***************************************************************************/
/*                                                                         */
/*  Marker.java                                                            */
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

// An etcetera marker in a stream ("etc"), or
// a marker for open or close quotes ("open", "close").

package nederhof.align;

import java.awt.*;

class Marker extends Elem {

    // Can be: "etc", "open", "close".
    private String mark;

    public Marker(int type, String mark) {
	super(type);
	this.mark = mark;
    }

    // Get text.
    private String getText() {
	if (mark.equals("open"))
	    return "\"";
	else if (mark.equals("close"))
	    return "\"";
	else
	    return "etc.";
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
	return context.getFontMetrics(getType()).stringWidth(getText());
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
	return context.getFontMetrics(getType()).getHeight();
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	return context.getFontMetrics(getType()).getDescent();
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	return context.getFontMetrics(getType()).getAscent();
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	return context.getFontMetrics(getType()).getLeading();
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
	g.drawString(getType(), Color.BLACK, getText(), getX(), y);
    }
}
