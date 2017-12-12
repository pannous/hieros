/***************************************************************************/
/*                                                                         */
/*  GlyphSelectPanel.java                                                  */
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

// Panel for selecting individual glyphs from fragment.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.res.*;
import nederhof.res.format.*;

public abstract class GlyphSelectPanel extends JPanel {

    // Context for rendering.
    private HieroRenderContext context;

    // Formatted RES. 
    private FormatFragment format;

    // Number of glyph that is under focus.
    // Negative if none.
    private int focusPos = -1;

    // Size of hieroglyphic and margin.
    private int width = 0;
    private int height = 0;
    private int margin = 0;

    // Padding for right-to-left text is on left.
    private int leftExtra() {
	return format.effectIsLR() ? 0 : getWidth() - width - 2 * margin; 
    }

    // Is to respond to mouse clicks?
    private boolean enabled = true;

    // Construct preview.
    public GlyphSelectPanel(ResFragment res, HieroRenderContext context) {
	this.context = context;
	setBackground(Color.WHITE);
	addMouseListener(new ClickListener());

	setHiero(res);
    }

    // Set hieroglyphic.
    public void setHiero(ResFragment frag) {
	format = new FormatFragment(frag, context);
	width = format.width();
	height = format.height();
	margin = Math.round(Settings.previewMargin *
		context.fontSizePt());
	invalidate();
	repaint();
    }

    // Set enabled.
    public void setEnabled(boolean b) {
	enabled = b;
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
	Rectangle rect = focusRect();
	if (rect != null) {
	    graphics.setPaint(Color.blue);
	    graphics.setStroke(new BasicStroke(1.0f));
	    graphics.draw(rect);
	}
    }

    // Listens only to mouse clicks. Click leads to change of focus.
    private class ClickListener extends MouseInputAdapter {
	public void mouseClicked(MouseEvent event) {
	    if (!enabled)
		return;
	    int clickX = format.effectIsLR() ?
		event.getX() : getWidth() - event.getX();
	    int clickY = event.getY();
	    focusPos = -1;
	    Vector rects = format.glyphRectangles();
	    for (int i = 0; i < rects.size(); i++) {
		Rectangle rect = (Rectangle) rects.get(i);
		if (format.effectIsLR() &&
			rect.contains(clickX - margin, clickY - margin) ||
			!format.effectIsLR() &&
			rect.contains(clickX - margin, clickY - margin)) {
		    focusPos = i;
		    break;
		}
	    }
	    exportFocus();
	}
    }

    // Notify of focus.
    private void exportFocus() {
	notifyFocus(focusPos);
	repaint();
    }

    // Get rectangle for focus.
    private Rectangle focusRect() {
	Vector rects = format.glyphRectangles();
	if (focusPos >= 0 && focusPos < rects.size()) {
	    Rectangle rect = (Rectangle) rects.get(focusPos);
	    if (format.effectIsLR()) 
		return new Rectangle(rect.x + margin, 
			rect.y + margin,
			rect.width, rect.height);
	    else 
		return new Rectangle(getWidth() - (rect.x + rect.width + margin),
			rect.y + margin, 
			rect.width, rect.height);
	}
	return null;
    }

    // Move focus left.
    public void goLeft() {
	if (focusPos < 0) {
	    if (format.glyphRectangles().size() > 0) {
		focusPos = format.glyphRectangles().size() - 1;
		exportFocus();
	    }
	} else if (focusPos - 1 >= 0) {
	    focusPos--;
	    exportFocus();
	}
    }
    // Move focus right.
    public void goRight() {
	if (focusPos < 0) {
	    if (format.glyphRectangles().size() > 0) {
		focusPos = 0;
		exportFocus();
	    }
	} else if (focusPos + 1 < format.glyphRectangles().size()) {
	    focusPos++;
	    exportFocus();
	}
    }

    // Information back to user.
    protected abstract void notifyFocus(int focus);

}
