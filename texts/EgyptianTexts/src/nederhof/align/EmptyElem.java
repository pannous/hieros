/***************************************************************************/
/*                                                                         */
/*  EmptyElem.java                                                         */
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

// Dummy empty element in stream or formatted paragraph.

package nederhof.align;

import java.awt.*;

class EmptyElem extends Elem {

    // Can there be line break at element?
    private boolean breakable;

    public EmptyElem(boolean breakable) {
	super(RenderContext.LATIN_FONT); // Font is arbitrary. Never used.
	this.breakable = breakable;
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
    public float getWidth(RenderContext context) {
	return 0;
    }

    // See Elem.
    public float getWidth(RenderContext context, int index) {
	return 0;
    }

    // See Elem.
    public float getAdvance(RenderContext context) {
	return 0;
    }

    // See Elem.
    public float getAdvance(RenderContext context, int index) {
	return 0;
    }

    // See Elem.
    public float getHeight(RenderContext context) {
	return 0;
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	return 0;
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	return 0;
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	return 0;
    }

    // See Elem.
    public boolean breakable() {
	return breakable;
    }

    // See Elem.
    public int firstBreak(RenderContext context, float len) {
	if (breakable)
	    return 1;
	else
	    return -1;
    }

    // See Elem.
    public int firstBreak() {
	if (breakable)
	    return 1;
	else
	    return -1;
    }

    // See Elem.
    public int lastBreak(RenderContext context, float len) {
	if (breakable)
	    return 1;
	else
	    return -1;
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
