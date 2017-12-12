/***************************************************************************/
/*                                                                         */
/*  PreviewPanel.java                                                      */
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

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.res.*;
import nederhof.res.format.*;

abstract class PreviewPanel extends JPanel {

    // Context for rendering.
    private HieroRenderContext context;

    // Formatted RES.
    private FormatFragment format;

    // Number of group or operator that is under focus.
    private int focusPos = 0;

    // Size of hieroglyphic and margin.
    private int width = 0;
    private int height = 0;
    private int margin = 0;

    // Padding for right-to-left text is on left.
    private int leftExtra() {
	return format.effectIsLR() ? 0 : getWidth() - width - 2 * margin; 
    }

    // Construct preview.
    public PreviewPanel(ResFragment frag, HieroRenderContext context) {
	this.context = context;
	setBackground(Color.WHITE);
	addMouseListener(new ClickListener());

	setHiero(frag);
    }

    // Set hieroglyphic.
    public void setHiero(ResFragment frag) {
	format = new FormatFragment(frag, context);
	width = format.width();
	height = format.height();
	margin = Math.round(Settings.previewMargin *
		context.fontSizePt());
	invalidate();
	scrollToFocus();
    }

    // Set number of group that is focus.
    public void setFocus(int focus) {
	focusPos = focus;
	scrollToFocus();
    }

    public void scrollToFocus() {
	Rectangle wider = new Rectangle(visibleGroupRect());
	final int WIDENING = 60;
	if (format.effectIsH()) {
	    wider.x = Math.max(0, wider.x - WIDENING);
	    wider.width += WIDENING * 2;
	} else {
	    wider.y = Math.max(0, wider.y - WIDENING);
	    wider.height += WIDENING * 2;
	}
	scrollRectToVisible(wider);
	repaint();
    }

    // Get number of group that is focus.
    public int getFocus() {
	return focusPos;
    }

    // Dimensions, to enclose hieroglyphic.
    public Dimension getMinimumSize() {
	return new Dimension(width + 2 * margin, height + 2 * margin);
    }
    public Dimension getMaximumSize() {
	return getMinimumSize();
    }
    public Dimension getPreferredSize() {
	return getMinimumSize();
    }

    // Paint in panel.
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D graphics = (Graphics2D) g;
	graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);
	format.write(graphics, margin + leftExtra(), margin);
	graphics.setPaint(Color.blue);
	graphics.setStroke(new BasicStroke(1.0f));
	graphics.draw(visibleGroupRect());
    }

    // Listens only to mouse clicks. Click leads to change of focus.
    private class ClickListener extends MouseInputAdapter {
	public void mouseClicked(MouseEvent event) {
	    int clickX = event.getX() - leftExtra();
	    int clickY = event.getY();
	    if (format.effectDir() == ResValues.DIR_HLR && clickX <= margin ||
		    format.effectDir() == ResValues.DIR_HRL && clickX >= margin + width ||
		    !format.effectIsH() && clickY <= margin) {
		focusPos = -1;
		repaint();
		transferFocus();
	    } else {
		int newFocus = format.pos(clickX - margin, clickY - margin);
		if (newFocus >= 0 && focusPos != newFocus) {
		    focusPos = newFocus;
		    repaint();
		    transferFocus();
		}
	    }
	}
    }

    // Make group visible group rectangle.
    // If rectangle is empty (operator with negative size), then
    // take end of previous group.
    private Rectangle visibleGroupRect() {
	if (format.nGroups() < 1 || focusPos < 0) {
	    if (format.effectIsH()) {
		if (format.effectIsLR())
		    return new Rectangle(margin, margin, 1, height);
		else
		    return new Rectangle(getWidth() - margin - 1, margin,
			    1, height);
	    } else
		return new Rectangle(margin, margin, width, 1);
	} else {
	    Rectangle rect = format.groupRectangle(focusPos);
	    if (rect.width < 1 || rect.height < 1) {
		if (focusPos % 2 == 1) { // odd, so operator
		    rect = new Rectangle(format.groupRectangle(focusPos-1));
		    if (!rect.equals(new Rectangle())) {
			if (format.effectIsH()) {
			    rect.x += rect.width;
			    rect.width = 1;
			} else {
			    rect.y += rect.height;
			    rect.height = 1;
			}
		    } else
			return rect;
		} else {
		    rect.width = Math.max(rect.width, 1);
		    rect.height = Math.max(rect.height, 1);
		}
	    }
	    if (format.effectIsLR()) 
		return new Rectangle(rect.x + margin, 
			rect.y + margin,
			rect.width, rect.height);
	    else 
		return new Rectangle(getWidth() - (rect.x + rect.width + margin),
			rect.y + margin, 
			rect.width, rect.height);
	}
    }

    //////////////////////////////////////////////////
    // Returning events back.

    // Export to user that there is change in group focus.
    public abstract void transferFocus();

}
