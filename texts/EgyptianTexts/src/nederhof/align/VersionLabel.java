/***************************************************************************/
/*                                                                         */
/*  VersionLabel.java                                                      */
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

// The indication of a version at the beginning of a line.

package nederhof.align;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

class VersionLabel extends Elem {

    // The text of the label.
    private String label;

    public VersionLabel(String version, String scheme) {
	super(RenderContext.NORMAL_FONT);
	this.label = getVersionLabel(version, scheme);
    }

    // See Elem.
    public boolean isPrintable() {
	return true;
    }

    // See Elem.
    public boolean isContent() {
	return false;
    }

    // See Elem.
    public float getWidth(RenderContext context) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	return fm.stringWidth(label);
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
	if (label.equals(""))
	    return 0.0f;
	else {
	    GeneralFontMetrics fm = context.getFontMetrics(getType());
	    return fm.getHeight();
	}
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	if (label.equals(""))
	    return 0.0f;
	else {
	    GeneralFontMetrics fm = context.getFontMetrics(getType());
	    return fm.getDescent();
	}
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	if (label.equals(""))
	    return 0.0f;
	else {
	    GeneralFontMetrics fm = context.getFontMetrics(getType());
	    return fm.getAscent();
	}
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	if (label.equals(""))
	    return 0.0f;
	else {
	    GeneralFontMetrics fm = context.getFontMetrics(getType());
	    return fm.getLeading();
	}
    }

    // See Elem.
    public boolean breakable() {
	return false;
    }

    // See Elem.
    public int firstBreak(RenderContext context, float len) {
	return -1;
    }

    // See Elem.
    public int firstBreak() {
	return -1;
    }

    // See Elem.
    public int lastBreak(RenderContext context, float len) {
	return -1;
    }

    // See Elem.
    public int nextBreak() {
	return -1;
    }

    // See Elem.
    public Elem prefix(int index) {
	return null; // should not happen
    }

    // See Elem.
    public Elem suffix() {
	return null; // should not happen
    }

    // See Elem.
    // Nothing to (re)draw for AWT. Visible trace left by placeButton.
    // For iText, draw button and add link.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	g.drawString(getType(), context.getLabelColor(), 
		label, getX(), y);
    }

    // Appearance of label indicating version.
    public static String getVersionLabel(String version, String scheme) {
	if (version.equals("")) {
	    if (scheme.equals(""))
		return "";
	    else
		return " (" + scheme + ")";
	} else {
	    if (scheme.equals(""))
		return " " + version + "";
	    else
		return " " + version + "(" + scheme + ")";
	}
    }
}
