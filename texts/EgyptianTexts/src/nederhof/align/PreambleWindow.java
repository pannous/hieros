/***************************************************************************/
/*                                                                         */
/*  PreambleWindow.java                                                    */
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

// Window for preamble, which formats XML text.

package nederhof.align;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.w3c.dom.*;

import nederhof.res.*;
import nederhof.util.*;

class PreambleWindow extends JFrame implements 
	ActionListener, RenderContext, AWTFontMapper {

    // Initial dimensions.
    private static final int width = Settings.preambleWidthInit;
    private static final int height = Settings.preambleHeightInit;

    // RenderContext copied from main window, but some methods need adjusting.
    RenderContext externalRenderContext;

    // Context of browser. May be null.
    AppletContext appletContext;

    // FontMapper.
    AWTFontMapper fontMapper;

    // The scroll pane, and the pane within that containing actual text.
    private JScrollPane scroll;
    private TextView text;

    // List of paragraphs. 
    private PreamblePars pars;

    // Constructor.
    public PreambleWindow(String name, 
	    Element created, Element header, Element bibl,
	    RenderContext renderContext, AppletContext appletContext,
	    AWTFontMapper fontMapper) {
	pars = XMLfiles.getPreamble(name, created, header, bibl);
	this.externalRenderContext = renderContext;
	this.appletContext = appletContext;
	this.fontMapper = fontMapper;
	setTitle(name);
	setJMenuBar(new QuitMenu(this));
	setSize(width, height);
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
	text = new TextView();
	scroll = new JScrollPane(text,
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.getVerticalScrollBar().setUnitIncrement(10);
	content.add(scroll);
	addWindowListener(new Listener());
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	ensureReformatted();
    }

    // Normally componentResized() is called in the event thread, which calls
    // reformat, but it is not clear to me whether this is always the case.
    // To ensure the text is formatted at least once, the resized event is
    // called artificially.
    private void ensureReformatted() {
	if (!reformattedOnce)
	    dispatchEvent(
		    new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));
    }

    // Listen if window to be closed. Merely make invisible.
    private class Listener extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	    setVisible(false);
	}
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("quit")) 
	    setVisible(false);
    }

    ////////////////////////////////////////////////////////////////////////
    // Implementation of RenderContext. Most methods copied from main window.

    // Left boundary of page.
    public float leftBound() {
	return Settings.preambleLeftMargin;
    }

    // Where text starts.
    public float textOffset() {
	return leftBound();
    }

    // Has no significance for preamble.
    public float creatorWidth() {
	return 0;
    }

    // Right boundary of page.
    public float rightBound() {
	return lastFormatWidth - Settings.preambleRightMargin;
    }

    // Has no significance for preamble.
    public boolean mentionCreator() {
	return false;
    }
    public boolean mentionVersion() {
	return false;
    }

    // Font metrics for above font codes.
    public GeneralFontMetrics getFontMetrics(int f) {
	return externalRenderContext.getFontMetrics(f);
    }

    // Font for font code.
    public Font getFont(int f) {
	return fontMapper.getFont(f);
    }

    public TrMap getEgyptMap() {
	return externalRenderContext.getEgyptMap();
    }
    public float getFootRaise() {
	return externalRenderContext.getFootRaise();
    }
    public HieroRenderContext getHieroContext() {
	return externalRenderContext.getHieroContext();
    }
    public HieroRenderContext getFootHieroContext() {
	return externalRenderContext.getFootHieroContext();
    }
    public boolean hieroColor() {
	return externalRenderContext.hieroColor();
    }
    public Color getPointColor() {
	return externalRenderContext.getPointColor();
    }
    public Color getLabelColor() {
	return externalRenderContext.getLabelColor();
    }
    public Color getNoteColor() {
	return externalRenderContext.getNoteColor();
    }
    public float getLxSep() {
	return externalRenderContext.getLxSep();
    }
    public float getLxLeading() {
	return externalRenderContext.getLxLeading();
    }
    public float getLxInnerMargin() {
	return externalRenderContext.getLxInnerMargin();
    }
    public float getLxLineThickness() {
	return externalRenderContext.getLxLineThickness();
    }
    public boolean lxAbbreviated() {
	return externalRenderContext.lxAbbreviated();
    }

    // Lines are as high as necessary.
    public boolean uniformAscent() {
	return false;
    }

    public boolean collectNotes() {
	return externalRenderContext.collectNotes();
    }

    ////////////////////////////////////////////////////////////////////////

    // Every time the system is (re)formatted according to a screen width,
    // this value is reassigned.
    // When the screen width changes from this width by more than the size of
    // the right margin, the system is reformatted.
    private int lastFormatWidth = 0;

    // Textual part, main section of top window.
    private class TextView extends JPanel {
	public TextView() {
	    setBackground(Color.WHITE);
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    addComponentListener(new ResizeListener());
	}
    }

    // Class of listening to changes to window size.
    // If change big, then reformat.
    private class ResizeListener implements ComponentListener {
	public void componentResized(ComponentEvent e) {
	    if (Math.abs(text.getSize().width - lastFormatWidth) >= 
		    Settings.preambleRightMargin)
		reformat();
	}
	public void componentMoved(ComponentEvent e) {
	}
	public void componentShown(ComponentEvent e) {
	}
	public void componentHidden(ComponentEvent e) {
	}
    }

    // Reformatted at least once?
    private boolean reformattedOnce = false;

    // Make new division into paragraphs, to account for new window width,
    // or new settings.
    public void reformat() {
	reformattedOnce = true;
	lastFormatWidth = text.getSize().width;
	text.removeAll();
	for (int i = 0; i < pars.size(); i++) {
	    LinkedList elems = pars.getPar(i);
	    if (Elem.isContent(elems)) {
		JPanel panel = new Paragraph(lastFormatWidth, elems);
		text.add(panel);
	    }
	}
	validate();
	repaint();
    }

    // A paragraph contains a number of lines, the y's are
    // the vertical offset from top.
    private class Paragraph extends JPanel {
	private int width;
	private int height = 0;
	private Vector ys = new Vector();
	// Lines of paragraph.
	private Vector lines;

	// Make paragraph consisting of formatted lines.
	public Paragraph(int width, LinkedList streamOriginal) {
	    setBackground(Color.WHITE);
	    setLayout(null);
	    setOpaque(true);
	    this.width = width;
	    Insets insets = getInsets(); 
	    LinkedList stream = (LinkedList) streamOriginal.clone();
	    lines = new Vector();
	    while (!stream.isEmpty()) {
		Line line = StreamSystem.splitOffLine(PreambleWindow.this,
			stream, textOffset());
		int ascent = Math.round(line.getAscent(PreambleWindow.this));
		int descent = Math.round(line.getDescent(PreambleWindow.this));
		lines.add(line);
		if (height == 0)
		    height += Settings.preambleParSep / 2;
		else 
		    height += line.getLeading(PreambleWindow.this);
		ys.add(new Integer(height + ascent));
		line.placeButtons(PreambleWindow.this, PreambleWindow.this,
			PreambleWindow.this, appletContext, this, 
			insets.left, insets.top + height + ascent);
		height += ascent + descent;
	    }
	    if (height > 0)
		height += Settings.preambleParSep / 2;
	}

	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    AWTDraw drawer = new AWTDraw(fontMapper, g);
	    for (int i = 0; i < lines.size(); i++) {
		Line line = (Line) lines.elementAt(i);
		Integer yInt = (Integer) ys.elementAt(i);
		int y = yInt.intValue();
		line.draw(PreambleWindow.this, drawer, y);
	    }
	}

	public Dimension getMinimumSize() {
	    Dimension d = super.getMinimumSize();
	    d.height = height;
	    return d;
	}

	public Dimension getMaximumSize() {
	    Dimension d = super.getMaximumSize();
	    d.height = height;
	    return d;
	}

	public Dimension getPreferredSize() {
	    Dimension d = super.getPreferredSize();
	    d.height = height;
	    return d;
	}
    }
}
