/***************************************************************************/
/*                                                                         */
/*  TextElem.java                                                          */
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

// Textual element in stream, in Latin font or transliteration font.
// Also used for error messages.

package nederhof.align;

import java.awt.*;

class TextElem extends Elem {

    // The text.
    private String text;

    public TextElem(int type, String text) {
	super(type);
	text = text.replaceAll("\\s+", " ");
	if (text.endsWith(" ")) {
	    text = text.replaceFirst(" $", "");
	    setTrailingSpace(true);
	}
	this.text = text;
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
	return fm.stringWidth(realText(context, index));
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
	return fm.getHeight();
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	return fm.getDescent();
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	GeneralFontMetrics fm = context.getFontMetrics(getType());
	return fm.getAscent();
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
	TrMap mapping = context.getEgyptMap();
	int textLen = text.length();
	for (int i = 0; i <= textLen; i++) 
	    if (i < textLen && Character.isWhitespace(text.charAt(i)) ||
		    i == textLen && hasTrailingSpace()) {
		String prefix = text.substring(0, i);
		if (isTranslit())
		    prefix = mapping.mapString(prefix);
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
	TrMap mapping = context.getEgyptMap();
	int textLen = text.length();
	for (int i = textLen; i >= 0; i--) {
	    if (i < textLen && Character.isWhitespace(text.charAt(i)) ||
		    i == textLen && hasTrailingSpace()) {
		String prefix = text.substring(0, i);
		if (isTranslit())
		    prefix = mapping.mapString(prefix);
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
	    TextElem pref = (TextElem) this.clone();
	    pref.setPrefix(index);
	    return pref;
	}
    }

    // See Elem.
    public Elem suffix() {
	String suffix = text.substring(getPrefix()+1, text.length());
	TextElem suf = new TextElem(getType(), suffix);
	suf.setTrailingSpace(hasTrailingSpace());
	return suf;
    }

    // See Elem.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	g.drawString(getType(), Color.BLACK, realText(context), getX(), y);
    }

    // Get string, maybe converted for transliteration to codes from
    // transliteration font.
    private String realText(RenderContext context) {
	return realText(context, getPrefix());
    }

    // As above, but for given prefix.
    private String realText(RenderContext context, int index) {
	if (isTranslit()) {
	    TrMap mapping = context.getEgyptMap();
	    return mapping.mapString(getText(index));
	} else
	    return getText(index);
    }

    // In transliteration.
    private boolean isTranslit() {
	return getType() == RenderContext.EGYPT_FONT ||
	    getType() == RenderContext.FOOT_EGYPT_FONT;
    }
}
