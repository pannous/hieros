/***************************************************************************/
/*                                                                         */
/*  Line.java                                                              */
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

// A line in a formatted paragraph, consisting of formatted elements,
// with indication of their x coordinates.
// A line can also be a line belonging to a footnote.

package nederhof.align;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

final class Line implements Cloneable {

    private boolean isFootnote; 
    private int file;
    private String label; 

    // Is to become list of formatted elements.
    private LinkedList elems;

    // Constructor.
    public Line(int file, String creator,
	    String version, String scheme, RenderContext context) {
	isFootnote = false;
	this.file = file;
	label = VersionLabel.getVersionLabel(version, scheme);
	elems = new LinkedList();
	addPrelude(creator, version, scheme, context);
    }

    // Special constructor for footnotes and lines in preamble. 
    // Also used for cloning.
    public Line(boolean isFootnote) {
	this.isFootnote = isFootnote;
	file = -1;
	label = "";
	elems = new LinkedList();
    }

    // Clone. In particular, clone elements in list. 
    // This is to protect x coordinates.
    public Object clone() {
	Line l = new Line(isFootnote);
	l.file = file;
	l.label = label;
	l.elems = new LinkedList();
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    Elem elem2 = (Elem) elem.clone();
	    l.elems.addLast((Elem) elem.clone());
	}
	return l;
    }

    // Is footnote?
    public boolean isFootnote() {
	return isFootnote;
    }

    // Get file number corresponding to line. 
    public int getFile() {
	return file;
    }

    // Get label with version and scheme.
    public String getLabel() {
	return label;
    }

    public void addElem(Elem elem) {
	elems.addLast(elem);
    }

    public void removeLast() {
	elems.removeLast();
    }

    public Elem getLast() {
	if (elems.isEmpty())
	    return null;
	else
	    return (Elem) elems.getLast();
    }

    public LinkedList getElems() {
	return elems;
    }

    public boolean isEmpty() {
	return elems.isEmpty();
    }

    // Consists of printable elements.
    public boolean isPrintable() {
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    if (elem.isPrintable())
		return true;
	}
	return false;
    }

    // Consists of content elements (printable elements, but not positions).
    public boolean isContent() {
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    if (elem.isContent())
		return true;
	}
	return false;
    }

    // Height is maximum over all text elements.
    public float getHeight(RenderContext context) {
	float maxHeight = 0.0f;
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    maxHeight = Math.max(maxHeight, elem.getHeight(context));
	}
	if (context.uniformAscent())
	    return Math.max(maxHeight, Point.getDefaultHeight(context));
	else
	    return maxHeight;
    }

    // Descent is maximum over all text elements.
    public float getDescent(RenderContext context) {
	float maxDescent = 0.0f;
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    maxDescent = Math.max(maxDescent, elem.getDescent(context));
	}
	return maxDescent;
    }

    // Ascent is maximum over all text elements.
    public float getAscent(RenderContext context) {
	float maxAscent = 0.0f;
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    maxAscent = Math.max(maxAscent, elem.getAscent(context));
	}
	if (context.uniformAscent())
	    return Math.max(maxAscent, Point.getDefaultAscent(context));
	else
	    return maxAscent;
    }

    // Leading is maximum over all text elements.
    public float getLeading(RenderContext context) {
	float maxLeading = 0.0f;
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    maxLeading = Math.max(maxLeading, elem.getLeading(context));
	}
	return maxLeading;
    }

    // Drawing all elements.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    elem.draw(context, g, y);
	}
    }

    // Add label at beginning of line, if needed.
    private void addPrelude(String creator, String version, String scheme, 
	    RenderContext context) {
	if (context.mentionCreator()) {
	    String shortName = Link.shortName(creator);
	    Link link = new Link(shortName, "resource" + file);
	    link.setX(context.leftBound());
	    elems.addLast(link);
	}
	if (context.mentionVersion() && !label.equals("")) {
	    VersionLabel lab = new VersionLabel(version, scheme);
	    lab.setX(context.leftBound() + context.creatorWidth());
	    elems.addLast(lab);
	}
    }

    // Placing buttons, for links.
    public void placeButtons(RenderContext context, ActionListener listener,
	    AWTFontMapper mapper,
		AppletContext appletContext, JPanel panel, 
		float xOffset, float y) {
	ListIterator iter = elems.listIterator();
	while (iter.hasNext()) {
	    Elem elem = (Elem) iter.next();
	    if (elem instanceof Link) {
		Link link = (Link) elem;
		link.placeButton(context, listener, mapper,
			appletContext, panel, xOffset, y);
	    } else if (elem instanceof Lx) {
		Lx lx = (Lx) elem;
		lx.placeButton(context, listener, mapper,
			appletContext, panel, xOffset, y);
	    }
	}
    }
}
