/***************************************************************************/
/*                                                                         */
/*  Link.java                                                              */
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

// A hyper link. This can be either from button at beginning of line,
// which is a local link within the document/site, 
// or a URL from footnote or from preamble.

package nederhof.align;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

class Link extends Elem {

    // Margin around text in button. Only used for IText.
    public static final float buttonMargin = 2.0f;

    // For AWT, the sizes of the margins are (seemingly?) independent on the font. 
    // They are here measured on the basis of an actual JButton.
    private static int buttonMarginTop;
    private static int buttonMarginBottom;
    private static int buttonMarginLeft;
    private static int buttonMarginRight;
    static {
	JButton dummy = new JButton();
	dummy.setBorder(new EtchedBorder());
	Font font = new Font("SansSerif", Font.PLAIN, 20);
	dummy.setFont(font);
	dummy.setText("dummy");
	Insets margins = dummy.getInsets();
	buttonMarginTop = margins.top;
	buttonMarginBottom = margins.bottom;
	buttonMarginLeft = margins.left;
	buttonMarginRight = margins.right;
    }

    // The text of the label, and the url, or the local link.
    private String text;
    private String url;
    private String local;

    // A hyper link in preamble or note.
    public Link(int type, String text, String url) {
	super(type);
	this.text = text;
	this.url = url;
	this.local = null;
    }

    // A local link at beginning of line, indicating creator.
    public Link(String text, String local) {
	super(RenderContext.NORMAL_FONT);
	this.text = text;
	this.url = null;
	this.local = local;
    }

    // See Elem.
    public boolean isPrintable() {
	return true;
    }

    // See Elem.
    // Only links within the preamble of in footnotes are content.
    public boolean isContent() {
	return url != null;
    }

    // See Elem.
    public float getWidth(RenderContext context) {
	return getButtonWidth(context, getType(), text);
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
	return getButtonHeight(context, getType());
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	return getButtonDescent(context, getType());
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	return getButtonAscent(context, getType());
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	return getButtonLeading(context, getType());
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
    // Nothing to (re)draw for AWT; instead, visible trace left by placeButton.
    // For iText, draw button and add link.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	if (isIText(context)) {
	    ITextDraw gIText = (ITextDraw) g;
	    if (url != null) 
		gIText.drawUrlButton(getX(), y + getDescent(context),
			getX() + getWidth(context), y - getAscent(context),
			getX() + buttonMargin, y,
			getType(), text, url);
	    else if (local != null)
		gIText.drawLocalButton(getX(), y + getDescent(context),
			getX() + getWidth(context), y - getAscent(context),
			getX() + buttonMargin, y,
			getType(), text, local);
	}
    }

    // Get button that contains the text.
    public static JButton getButton(RenderContext context, Font font, String text) {
	JButton but = new JButton();
	but.setBorder(new EtchedBorder());
	but.setFocusPainted(false);
	but.setText(text);
	but.setFont(font);
	but.setMaximumSize(but.getPreferredSize());
	return but;
    }

    // Place button at certain place, and attach listener.
    public void placeButton(RenderContext context, ActionListener listener,
	    AWTFontMapper mapper, AppletContext appletContext, JPanel panel, 
	    float xOffset, float y) {
	int xInt = Math.round(xOffset + getX());
	int yInt = Math.round(y);
	Font font = mapper.getFont(getType());
	JButton but = getButton(context, font, text);
	if (url != null)
	    but.addActionListener(new LinkListener(appletContext));
	else if (local != null) {
	    but.setActionCommand(local);
	    but.addActionListener(listener);
	}
	panel.add(but);
	but.setBounds(xInt, yInt - Math.round(getAscent(context)), 
		but.getPreferredSize().width,
		but.getPreferredSize().height);
    }

    // Listener that opens browser on given link.
    private class LinkListener implements ActionListener {
	// AppletContext, which is null if there is no such context.
	private AppletContext context;
	
	// Construct, and remember appletContext.
	public LinkListener(AppletContext context) {
	    this.context = context;
	}

	// Upon action, open browser.
	public void actionPerformed(ActionEvent e) {
	    if (context != null) {
		try {
		    context.showDocument(new URL(url), "_blank");
		} catch (MalformedURLException err) {
		    System.err.println("In preamble: " + err.getMessage());
		}
	    }
	}
    }

    // We need to distinguish between AWT and iText. In the former case,
    // the button needs to be placed only once. In the latter case,
    // the button can be placed with draw.
    private static boolean isIText(RenderContext context) {
	GeneralFontMetrics fm = context.getFontMetrics(RenderContext.NORMAL_FONT);
	return (fm instanceof ITextFontMetrics);
    }

    // Turn name of creator into short name.
    public static String shortName(String name) {
	final int prefixLen = 3;
	return name.length() <= prefixLen ? name : name.substring(0, prefixLen);
    }

    // How big would buttons be?
    public static float getButtonWidth(RenderContext context, int type, String text) {
	GeneralFontMetrics fm = context.getFontMetrics(type);
	if (isIText(context)) 
	    return fm.stringWidth(text) + 2 * buttonMargin;
	else
	    return fm.stringWidth(text) + buttonMarginLeft + buttonMarginRight;
    }
    public static float getButtonHeight(RenderContext context, int type) {
	GeneralFontMetrics fm = context.getFontMetrics(type);
	if (isIText(context)) 
	    return fm.getHeight() + 2 * buttonMargin;
	else
	    return fm.getHeight() + buttonMarginTop + buttonMarginBottom +
		getButtonLeading(context, type);
    }
    public static float getButtonDescent(RenderContext context, int type) {
	GeneralFontMetrics fm = context.getFontMetrics(type);
	if (isIText(context)) 
	    return fm.getDescent() + buttonMargin;
	else
	    return fm.getDescent() + buttonMarginBottom;
    }
    public static float getButtonAscent(RenderContext context, int type) {
	GeneralFontMetrics fm = context.getFontMetrics(type);
	if (isIText(context)) 
	    return fm.getAscent() + buttonMargin;
	else
	    return fm.getAscent() + buttonMarginTop;
    }
    // As the leading of AWT fonts is 0, a little something is added.
    public static float getButtonLeading(RenderContext context, int type) {
	GeneralFontMetrics fm = context.getFontMetrics(type);
	if (isIText(context))
	    return fm.getLeading();
	else
	    return 0.2f * fm.getHeight();
    }
}
