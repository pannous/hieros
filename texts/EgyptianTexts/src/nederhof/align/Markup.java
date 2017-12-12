/***************************************************************************/
/*                                                                         */
/*  Markup.java                                                            */
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

// Markup in a stream.

package nederhof.align;

import java.awt.*;

class Markup extends Elem {

    private String markupType;
    private String text;

    public Markup(int type, String markupType, String text) {
	super(type);
	this.markupType = markupType;
	this.text = text;
    }

    public String getText() {
	return text;
    }

    // See Elem.
    public boolean isPrintable() {
	return false;
    }

    // See Elem.
    public boolean isContent() {
	return false;
    }

    // See Elem.
    // Current implementation ignores markup.
    public float getWidth(RenderContext context) {
	return 0.0f;
    }

    // See Elem.
    public float getWidth(RenderContext context, int index) {
	return 0.0f;
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
	return 0.0f;
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	return 0.0f;
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	return 0.0f;
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	return 0.0f;
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
    }
}
