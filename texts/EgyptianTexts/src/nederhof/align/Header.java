/***************************************************************************/
/*                                                                         */
/*  Header.java                                                            */
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

// Header in preamble, of level 2 or 3.

package nederhof.align;

import java.awt.*;

class Header extends Elem {

    // The text of the header.
    private String text;

    // Extra space above and under all the lines of the header.
    // If header is split up, only extra space before first and after last.
    private boolean preSpace;

    // Public constructor.
    public Header(int type, String text) {
	this(type, text, true);
    }

    // Private constructor, used by public constructor above, and
    // used to create suffix of text.
    private Header(int type, String text, boolean hasPreSpace) {
	super(type);
	text = text.replaceAll("\\s+", " ");
	if (text.endsWith(" ")) {
	    text = text.replaceFirst(" $", "");
	    setTrailingSpace(true);
	}
	this.text = text;
	preSpace = hasPreSpace;
    }

    // Overrides the method in Elem.
    public void setPrefix(int index) {
	if (index >= text.length())
	    super.setPrefix(-1);
	else
	    super.setPrefix(index);
    }

    // Has space at beginning.
    public boolean hasLeadingSpace() {
	return text.startsWith(" ") || text.equals("") && hasTrailingSpace();
    }

    // Remove space at beginning.
    public void removeLeadingSpace() {
	text = text.replaceFirst("^ ", "");
	if (text.equals(""))
	    setTrailingSpace(false);
    }

    // Get text.
    public String getText() {
	return getText(getPrefix());
    }

    // As above, but for given prefix.
    public String getText(int index) {
	if (index >= 0)
	    return text.substring(0, index);
	else
	    return text;
    }

    // See Elem.
    public boolean isPrintable() {
	return !getText().matches("\\s*");
    }

    // See Elem.
    public boolean isContent() {
	return !getText().matches("\\s*");
    }

    // See Elem.
    public float getWidth(RenderContext context) {
	return getWidth(context, getPrefix());
    }

    // See Elem.
    public float getWidth(RenderContext context, int index) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	return fm.stringWidth(getText(index));
    }

    // See Elem.
    public float getAdvance(RenderContext context) {
	return getAdvance(context, getPrefix());
    }

    // See Elem.
    public float getAdvance(RenderContext context, int index) {
	if (index >= 0 && index < text.length())
	    return getWidth(context, index);
	else
	    return getWidth(context, index) + spaceWidth(context);
    }

    // See Elem.
    public float getHeight(RenderContext context) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	return fm.getHeight() + preSpace(fm.getHeight()) + postSpace(fm.getHeight());
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	return fm.getDescent() + postSpace(fm.getHeight());
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	return fm.getAscent() + preSpace(fm.getHeight());
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	return fm.getLeading();
    }

    // See Elem.
    public boolean breakable() {
	return firstBreak() >= 0 || hasTrailingSpace();
    }

    // See Elem.
    public int firstBreak(RenderContext context, float len) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	int textLen = text.length();
	for (int i = 0; i <= textLen; i++) 
	    if (i < textLen && Character.isWhitespace(text.charAt(i)) ||
		    i == textLen && hasTrailingSpace()) {
		String prefix = text.substring(0, i);
		if (fm.stringWidth(prefix) <= len)
		    return i;
		else
		    return -1;
	    }
	return -1;
    }

    // See Elem.
    public int firstBreak() {
	int textLen = text.length();
	for (int i = 0; i < textLen; i++) 
	    if (Character.isWhitespace(text.charAt(i))) 
		return i;
	if (hasTrailingSpace()) 
	    return textLen;
	return -1;
    }

    // See Elem.
    public int lastBreak(RenderContext context, float len) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	int textLen = text.length();
	for (int i = textLen; i >= 0; i--) {
	    if (i < textLen && Character.isWhitespace(text.charAt(i)) ||
		    i == textLen && hasTrailingSpace()) {
		String prefix = text.substring(0, i);
		if (fm.stringWidth(prefix) <= len)
		    return i;
	    }
	}
	return -1;
    }

    // See Elem.
    public int nextBreak() {
	int textLen = text.length();
	int prev = getPrefix();
	for (int i = prev + 1; i < textLen; i++) 
	    if (Character.isWhitespace(text.charAt(i)))
		return i;
	if (prev < textLen && hasTrailingSpace())
	    return textLen;
	return -1;
    }

    // See Elem.
    public Elem prefix(int index) {
	if (index >= text.length())
	    return this;
	else {
	    Header pref = (Header) this.clone();
	    pref.setPrefix(index);
	    return pref;
	}
    }

    // See Elem.
    public Elem suffix() {
	String suffix = text.substring(getPrefix()+1, text.length());
	Header suf = new Header(getType(), suffix, false);
	suf.setTrailingSpace(hasTrailingSpace());
	return suf;
    }

    // See Elem.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	g.drawString(getType(), Color.BLACK, getText(), getX(), y);
    }

    // Extra vertical space above, as factor of normal font height.
    private float preSpace(float height) {
	if (preSpace) {
	    switch (getType()) {
		case RenderContext.HEADER1_FONT:
		    return height * Settings.header1PreSep;
		case RenderContext.HEADER2_FONT:
		    return height * Settings.header2PreSep;
		case RenderContext.HEADER3_FONT:
		    return height * Settings.header3PreSep;
		default:
		    return 0;
	    }
	} else 
	    return 0;
    }

    // Extra vertical space below, as factor of normal font height.
    private float postSpace(float height) {
	if (!isPrefix()) {
	    switch (getType()) {
		case RenderContext.HEADER1_FONT:
		    return height * Settings.header1PostSep;
		case RenderContext.HEADER2_FONT:
		    return height * Settings.header2PostSep;
		case RenderContext.HEADER3_FONT:
		    return height * Settings.header3PostSep;
		default:
		    return 0;
	    }
	} else
	    return 0;
    }
}
