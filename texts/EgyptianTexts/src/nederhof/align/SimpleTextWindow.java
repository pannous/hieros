/***************************************************************************/
/*                                                                         */
/*  SimpleTextWindow.java                                                  */
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

// Window consisting of a few simple paragraphs, using only a subset of
// elements.

package nederhof.align;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.w3c.dom.*;

import nederhof.fonts.*;
import nederhof.res.*;
import nederhof.util.*;

public class SimpleTextWindow extends JFrame implements 
	ActionListener, RenderContext, AWTFontMapper {

    // Context of browser. At present is always null.
    AppletContext appletContext = null;

    // The scroll pane, and the pane within that containing actual text.
    private JScrollPane scroll;
    protected TextView text;

    // List of paragraphs. 
    private PreamblePars pars;

    // Constructor of window showing nothing.
    public SimpleTextWindow(int width, int height) {
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
	makeFonts();

	clearText();
    }

    // Make no text.
    public void clearText() {
	setTitle("empty content");
	synchronized (this) {
	    pars = new PreamblePars();
	    pars.add(new TextElem(RenderContext.NORMAL_FONT, "Nothing selected"));
	    reformat();
	}
    }

    // Replace text with other text. This text consists of a definition
    // of a glyph.
    public void setText(String name, Element shortDef, Element longDef) {
	setTitle(name);
	synchronized (this) {
	    pars = new PreamblePars();
	    pars.add(new HieroElem(RenderContext.HIERO_FONT, name));
	    pars.add(new TextElem(RenderContext.NORMAL_FONT, " " + name + ": "));
	    if (shortDef != null)
		XMLfiles.add(pars, shortDef);
	    pars.makeFinished();
	    if (longDef != null)
		XMLfiles.add(pars, longDef);
	    pars.normalize();
	    reformat();
	}
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
    // The fonts to be used.

    private Font latinFont;
    private AWTFontMetrics latinFontMetrics;
    private Font italicFont;
    private AWTFontMetrics italicFontMetrics;
    private Font egyptFont;
    private AWTFontMetrics egyptFontMetrics;
    private TrMap egyptMap;
    private HieroRenderContext hieroContext;

    // Make default fonts.
    private void makeFonts() {
	latinFont = new Font(Settings.latinFontNameDefault,
		Settings.latinFontStyleDefault.intValue(),
		Settings.latinFontSizeDefault.intValue());
	latinFontMetrics = new AWTFontMetrics(getFontMetrics(latinFont));
	italicFont = new Font(Settings.latinFontNameDefault,
		Font.ITALIC,
		Settings.latinFontSizeDefault.intValue());
	italicFontMetrics = new AWTFontMetrics(getFontMetrics(italicFont));
	egyptFont = FontUtil.font(Settings.egyptFontFilePlain);
	egyptFont = egyptFont.deriveFont((float) Settings.egyptFontSizeDefault.intValue());
	if (egyptFont == null)
	    egyptFont = latinFont;
	egyptFontMetrics = new AWTFontMetrics(getFontMetrics(egyptFont));
	egyptMap = new TrMap(Settings.egyptMapFile);
	hieroContext = 
	    new HieroRenderContext(Settings.hieroFontSizeDefault.intValue(), true);
    }

    ////////////////////////////////////////////////////////////////////////
    // Implementation of RenderContext.

    // Left boundary of page.
    public float leftBound() {
	return Settings.preambleLeftMargin;
    }

    // Where text starts.
    public float textOffset() {
	return leftBound();
    }

    // Has no significance here.
    public float creatorWidth() {
	return 0;
    }

    // Right boundary of page.
    public float rightBound() {
	return lastFormatWidth - Settings.preambleRightMargin;
    }

    // Has no significance here.
    public boolean mentionCreator() {
	return false;
    }
    public boolean mentionVersion() {
	return false;
    }

    // Font metrics for above font codes.
    public GeneralFontMetrics getFontMetrics(int f) {
	switch (f) {
	    case RenderContext.LATIN_FONT:
		return latinFontMetrics;
	    case RenderContext.NORMAL_FONT:
		return latinFontMetrics;
	    case RenderContext.ITALIC_FONT:
		return italicFontMetrics;
	    case RenderContext.EGYPT_FONT:
		return egyptFontMetrics;
	    default:
		return latinFontMetrics;
	}
    }

    // Font for font code.
    public Font getFont(int f) {
	switch (f) {
	    case RenderContext.LATIN_FONT:
		return latinFont;
	    case RenderContext.NORMAL_FONT:
		return latinFont;
	    case RenderContext.ITALIC_FONT:
		return italicFont;
	    case RenderContext.EGYPT_FONT:
		return egyptFont;
	    default:
		return latinFont;
	}
    }

    public TrMap getEgyptMap() {
	return egyptMap;
    }

    // Irrelevant here.
    public float getFootRaise() {
	return 0;
    }

    public HieroRenderContext getHieroContext() {
	return hieroContext;
    }

    // Irrelevant here.
    public HieroRenderContext getFootHieroContext() {
	return hieroContext;
    }

    public boolean hieroColor() {
	return true;
    }

    // Irrelevant here.
    public Color getPointColor() {
	return Settings.pointColorDefault;
    }
    public Color getLabelColor() {
	return Settings.labelColorDefault;
    }
    public Color getNoteColor() {
	return Settings.noteColorDefault;
    }
    public float getLxSep() {
	return 0;
    }
    public float getLxLeading() {
	return 0;
    }
    public float getLxInnerMargin() {
	return 0;
    }
    public float getLxLineThickness() {
	return 0;
    }
    public boolean lxAbbreviated() {
	return false;
    }
    public boolean uniformAscent() {
	return false;
    }
    public boolean collectNotes() {
	return true;
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

    // Make new division into paragraphs, to account for new window width,
    // or new settings.
    public void reformat() {
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
	private Vector<Integer> ys = new Vector<Integer>();
	// Lines of paragraph.
	private Vector<Line> lines;

	// Make paragraph consisting of formatted lines.
	public Paragraph(int width, LinkedList streamOriginal) {
	    setBackground(Color.WHITE);
	    setLayout(null);
	    setOpaque(true);
	    this.width = width;
	    Insets insets = getInsets(); 
	    LinkedList stream = (LinkedList) streamOriginal.clone();
	    lines = new Vector<Line>();
	    while (!stream.isEmpty()) {
		Line line = StreamSystem.splitOffLine(SimpleTextWindow.this,
			stream, textOffset());
		int ascent = Math.round(line.getAscent(SimpleTextWindow.this));
		int descent = Math.round(line.getDescent(SimpleTextWindow.this));
		lines.add(line);
		if (height == 0)
		    height += Settings.preambleParSep / 2;
		else 
		    height += line.getLeading(SimpleTextWindow.this);
		ys.add(new Integer(height + ascent));
		line.placeButtons(SimpleTextWindow.this, SimpleTextWindow.this,
			SimpleTextWindow.this, appletContext, this, 
			insets.left, insets.top + height + ascent);
		height += ascent + descent;
	    }
	    if (height > 0)
		height += Settings.preambleParSep / 2;
	}

	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    AWTDraw drawer = new AWTDraw(SimpleTextWindow.this, g);
	    for (int i = 0; i < lines.size(); i++) {
		Line line = lines.elementAt(i);
		Integer yInt = ys.elementAt(i);
		int y = yInt.intValue();
		line.draw(SimpleTextWindow.this, drawer, y);
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
