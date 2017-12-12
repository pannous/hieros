/***************************************************************************/
/*                                                                         */
/*  HieroglyphicPanel.java                                                 */
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

// Panel in which there is hieroglyphic.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import nederhof.res.*;
import nederhof.res.format.*;
import nederhof.util.*;

abstract class HieroglyphicPanel extends JPanel {

    // Formatted RES.
    private FormatFragment format = null;

    // Size of hieroglyphic and margin.
    private int width = 0;
    private int height = 0;
    private int margin = 0;

    // Make view.
    public HieroglyphicPanel() {
	setBackground(Color.WHITE);
    }

    // Load content from temporary variables.
    public void refresh() {
	HieroRenderContext context = context();
	ResFragment frag = ResFragment.parse(hiero(), 
		new ParsingContext(context, true));
	format = new FormatFragment(frag, context);
	Insets insets = format.margins();
	int oldWidth = width;
	int oldHeight = height;
	width = format.width() + insets.left + insets.right;
	height = format.height() + insets.bottom + insets.top;
	margin = Math.round(Settings.treeMargin *
		context.fontSizePt());
	if (width != oldWidth || height != oldHeight)
	    refit();
	repaint();
    }

    // Propagate changed size of panel in application.
    protected void refit() {
	// By default, do nothing.
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
	if (format != null) {
	    Graphics2D graphics = (Graphics2D) g;
	    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
		    RenderingHints.VALUE_RENDER_QUALITY);
	    format.writeFactual(graphics, margin);
	}
    }

    // Interface to application to provide context and hieroglyphic.
    public abstract HieroRenderContext context();
    public abstract String hiero();

}
