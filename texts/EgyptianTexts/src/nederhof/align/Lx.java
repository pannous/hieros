/***************************************************************************/
/*                                                                         */
/*  Lx.java                                                                */
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

// A lexical entry.

package nederhof.align;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;
import javax.swing.*;

import nederhof.res.*;

class Lx extends Elem {
    private String texthi;
    private String textal;
    private String texttr;
    private String textfo;
    private String cite;
    private String href;
    private String keyhi;
    private String keyal;
    private String keytr;
    private String keyfo;
    private String dicthi;
    private String dictal;
    private String dicttr;
    private String dictfo;

    // After computing size, size of box.
    // If negative, then have not been computed.
    private float width = -1f;
    private float height = -1f;
    // x coordinate of text within box.
    private float x;
    // y coordinates. Negative if none.
    private float texthiBase = -1f;
    private float textalBase = -1f;
    private float texttrBase = -1f;
    private float textfoBase = -1f;
    private float textLine = -1f; 
    private float citeBase = -1f;
    private float keyhiBase = -1f;
    private float keyalBase = -1f;
    private float keytrBase = -1f;
    private float keyfoBase = -1f;
    private float keyLine = -1f; 
    private float dicthiBase = -1f;
    private float dictalBase = -1f;
    private float dicttrBase = -1f;
    private float dictfoBase = -1f;
    private float dictLine = -1f; 

    // For hieroglyphic elements: 
    // The code.
    // Then the division and the image, and 
    // the context in which these were obtained.
    private RESorREScode texthiCode;
    private RESorREScode keyhiCode;
    private RESorREScode dicthiCode;
    private HieroRenderContext oldContext = null;
    private RESorREScodeDivision texthiDiv = null;
    private RESorREScodeDivision keyhiDiv = null;
    private RESorREScodeDivision dicthiDiv = null;

    // String to be printed when in abbreviated mode and textal missing.
    private static final String dummyAl = "???";

    // Blank string for href without cite.
    private static final String blankLabel = "    ";

    // Constructor of lx entry.
    public Lx(String texthi, String textal, String texttr, String textfo, 
	    String cite, String href,
	    String keyhi, String keyal, String keytr, String keyfo,
	    String dicthi, String dictal, String dicttr, String dictfo) {
	super(RenderContext.LX);
	this.texthi = texthi;
	this.textal = textal;
	this.texttr = texttr;
	this.textfo = textfo;
	this.cite = cite;
	this.href = href;
	this.keyhi = keyhi;
	this.keyal = keyal;
	this.keytr = keytr;
	this.keyfo = keyfo;
	this.dicthi = dicthi;
	this.dictal = dictal;
	this.dicttr = dicttr;
	this.dictfo = dictfo;
	texthiCode = codeOf(texthi);
	keyhiCode = codeOf(keyhi);
	dicthiCode = codeOf(dicthi);
    }

    // Produce RES or REScode from string.
    private static RESorREScode codeOf(String hi) {
	/*
	if (hi == null)
	    return null;
	else 
	    return RESorREScode.createRESorREScode(hi,
		    HieroElem.conversionContext);
		    */
	return null;
    }

    // See Elem.
    public boolean isPrintable() {
	return !isEmpty();
    }

    // See Elem.
    public boolean isContent() {
	return !isEmpty();
    }

    // See Elem.
    public float getWidth(RenderContext context) {
	if (isEmpty())
	    return 0.0f;
	else if (context.lxAbbreviated()) {
	    String str = textalStringOrElse(context);
	    int f = textalFontOrElse();
	    return Link.getButtonWidth(context, f, str);
	} else
	    return width(context);
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
	if (context.lxAbbreviated()) {
	    int f = textalFontOrElse();
	    return Link.getButtonHeight(context, f);
	} else
	    return height(context) + getLeading(context);
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	if (context.lxAbbreviated()) {
	    int f = textalFontOrElse();
	    return Link.getButtonDescent(context, f);
	} else
	    return height(context) - getAscent(context);
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	if (context.lxAbbreviated()) {
	    int f = textalFontOrElse();
	    return Link.getButtonAscent(context, f);
	} else
	    return getLxAscent(context);
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	if (context.lxAbbreviated()) {
	    int f = textalFontOrElse();
	    return Link.getButtonLeading(context, f);
	} else
	    return context.getLxLeading();
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
    // We assume width and height has been determined earlier.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	if (isEmpty())
	    return;
	if (context.lxAbbreviated()) {
	    drawAbbr(context, g, y);
	    return;
	}
	if (width < 0 || height < 0)
	    return;
	makeHiFormats(context);
	float top = y - getLxAscent(context);
	float thick = context.getLxLineThickness();
	// horizontal lines.
	g.fillRect(Color.DARK_GRAY, getX(), top, width, thick);
	if (textLine >= 0)
	    g.fillRect(Color.DARK_GRAY, getX(), top + textLine, width, thick);
	if (keyLine >= 0)
	    g.fillRect(Color.GRAY, getX(), top + keyLine, width, thick);
	if (dictLine >= 0)
	    g.fillRect(Color.DARK_GRAY, getX(), top + dictLine, width, thick);
	// vertical lines.
	g.fillRect(Color.DARK_GRAY, getX(), top, thick, height);
	g.fillRect(Color.DARK_GRAY, getX() + width - thick, top, thick, height);
	// elements.
	if (texthi != null)
	    drawHi(context, g, texthiDiv, top + texthiBase);
	if (textal != null)
	    drawAl(context, g, textal, top + textalBase);
	if (texttr != null)
	    drawTr(context, g, texttr, top + texttrBase);
	if (textfo != null)
	    drawFo(context, g, textfo, top + textfoBase);
	if (cite != null || href != null)
	    drawCite(context, g, cite, top + citeBase);
	if (keyhi != null)
	    drawHi(context, g, keyhiDiv, top + keyhiBase);
	if (keyal != null)
	    drawAl(context, g, keyal, top + keyalBase);
	if (keytr != null)
	    drawTr(context, g, keytr, top + keytrBase);
	if (keyfo != null)
	    drawFo(context, g, keyfo, top + keyfoBase);
	if (dicthi != null)
	    drawHi(context, g, dicthiDiv, top + dicthiBase);
	if (dictal != null)
	    drawAl(context, g, dictal, top + dictalBase);
	if (dicttr != null)
	    drawTr(context, g, dicttr, top + dicttrBase);
	if (dictfo != null)
	    drawFo(context, g, dictfo, top + dictfoBase);
    }

    // Draw abbreviated, e.g. only textal entry.
    // If there is hyperlink, a button must be placed instead of text.
    private void drawAbbr(RenderContext context, GeneralDraw g, float y) {
	String text = textalStringOrElse(context);
	int font = textalFontOrElse();
	if (href == null) 
	    g.drawString(font, Color.BLACK, text, getX(), y);
	else {
	    if (isIText(context)) {
		ITextDraw gIText = (ITextDraw) g;
		gIText.drawUrlButton(getX(), y + getDescent(context),
			getX() + getWidth(context), y - getAscent(context),
			getX() + Link.buttonMargin, y,
			font, text, href);
	    }
	} 
    }

    // Draw hieroglyphic.
    private void drawHi(RenderContext context, 
	    GeneralDraw g, RESorREScodeDivision hi, float y) {
	/*
	g.drawHiero(hi, getX() + x, y - ascentHi(context));
	*/
    }

    // Draw transliteration.
    private void drawAl(RenderContext context, GeneralDraw g, String al, float y) {
	g.drawString(RenderContext.EGYPT_FONT, Color.BLACK,
		alString(al, context), getX() + x, y);
    }

    // Draw translation.
    private void drawTr(RenderContext context, GeneralDraw g, String tr, float y) {
	g.drawString(RenderContext.LATIN_FONT, Color.BLACK, 
		trString(tr), getX() + x, y);
    }

    // Draw form.
    private void drawFo(RenderContext context, GeneralDraw g, String fo, float y) {
	g.drawString(RenderContext.LATIN_FONT, Color.BLACK, 
		fo, getX() + x, y);
    }

    // Draw cite.
    // If there is hyperlink, then we make button instead.
    // For AWT, this means drawing is to be down by placeButton.
    private void drawCite(RenderContext context, GeneralDraw g, String cite, float y) {
	if (href == null)
	    g.drawString(RenderContext.LATIN_FONT, Color.DARK_GRAY,
		    cite, getX() + x, y);
	else {
	    if (isIText(context)) {
		String text = (cite != null ? cite : blankLabel);
		ITextDraw gIText = (ITextDraw) g;
		int f = RenderContext.LATIN_FONT;
		int descent = Math.round(Link.getButtonDescent(context, f));
		int width = Math.round(Link.getButtonWidth(context, f, text));
		int ascent = Math.round(Link.getButtonAscent(context, f));
		gIText.drawUrlButton(getX() + x, y + descent, 
			getX() + x + width, y - ascent,
			getX() + x + Link.buttonMargin, y, f, text, href);
	    }
	}
    }

    // Place button for hyperlink at certain place, and attach listener.
    public void placeButton(RenderContext context, ActionListener listener,
	    AWTFontMapper mapper, AppletContext appletContext, JPanel panel,
	    float xOffset, float y) {
	if (isEmpty() || href == null)
	    return;
	int xInt = Math.round(xOffset + getX());
	int yInt = Math.round(y);
	if (context.lxAbbreviated()) {
	    String text = textalStringOrElse(context);
	    Font font = mapper.getFont(textalFontOrElse());
	    JButton but = Link.getButton(context, font, text);
	    but.addActionListener(new LxListener(appletContext, 
			texthi, textal, texttr, textfo,
			cite, href,
			keyhi, keyal, keytr, keyfo,
			dicthi, dictal, dicttr, dictfo));
	    panel.add(but);
	    but.setBounds(xInt, yInt - Math.round(getAscent(context)),
		    but.getPreferredSize().width,
		    but.getPreferredSize().height);
	} else {
	    String text = (cite != null ? cite : blankLabel);
	    float base = y - getLxAscent(context) + citeBase;
	    int top = Math.round(base - ascentCite(context));
	    Font font = mapper.getFont(RenderContext.LATIN_FONT);
	    JButton but = Link.getButton(context, font, text);
	    but.addActionListener(new LxListener(appletContext, 
			texthi, textal, texttr, textfo,
			cite, href,
			keyhi, keyal, keytr, keyfo,
			dicthi, dictal, dicttr, dictfo));
	    panel.add(but);
	    but.setBounds(Math.round(xInt + x), top,
		    but.getPreferredSize().width,
		    but.getPreferredSize().height);
	}
    }

    // Determine the horizontal dimensions.
    private float width(RenderContext context) {
	x = context.getLxLineThickness() + context.getLxInnerMargin();
	width = 0.0f;
	if (isEmpty())
	    return 0.0f;
	makeHiFormats(context);
	width = Math.max(width, widthHi(texthiDiv, context));
	width = Math.max(width, widthAl(textal, context));
	width = Math.max(width, widthTr(texttr, context));
	width = Math.max(width, widthFo(textfo, context));
	width = Math.max(width, widthCite(context));
	width = Math.max(width, widthHi(keyhiDiv, context));
	width = Math.max(width, widthAl(keyal, context));
	width = Math.max(width, widthTr(keytr, context));
	width = Math.max(width, widthFo(keyfo, context));
	width = Math.max(width, widthHi(dicthiDiv, context));
	width = Math.max(width, widthAl(dictal, context));
	width = Math.max(width, widthTr(dicttr, context));
	width = Math.max(width, widthFo(dictfo, context));
	width += 2 * context.getLxLineThickness() + 2 * context.getLxInnerMargin();
	return width;
    }

    // Determine the vertical dimensions.
    private float height(RenderContext context) {
	height = 0.0f;
	if (isEmpty())
	    return 0.0f;
	makeHiFormats(context);
	height = context.getLxLineThickness();
	float heightLeading = height + context.getLxInnerMargin();

	if (texthi != null) {
	    texthiBase = heightLeading + ascentHi(texthiDiv, context);
	    height = texthiBase + descentHi(texthiDiv, context);
	    heightLeading = height + leadingHi(texthiDiv, context);
	}
	if (textal != null) {
	    textalBase = heightLeading + ascentAl(textal, context);
	    height = textalBase + descentAl(textal, context);
	    heightLeading = height + leadingAl(textal, context);
	}
	if (texttr != null) {
	    texttrBase = heightLeading + ascentTr(texttr, context);
	    height = texttrBase + descentTr(texttr, context);
	    heightLeading = height + leadingTr(texttr, context);
	}
	if (textfo != null) {
	    textfoBase = heightLeading + ascentFo(textfo, context);
	    height = textfoBase + descentFo(textfo, context);
	    heightLeading = height + leadingFo(textfo, context);
	}
	if (texthi != null || textal != null || 
		texttr != null || textfo != null) {
	    textLine = height + context.getLxInnerMargin();
	    height = textLine + context.getLxLineThickness();
	    heightLeading = height + context.getLxInnerMargin();
	}

	if (cite != null || href != null) {
	    citeBase = heightLeading + ascentCite(context);
	    height = citeBase + descentCite(context);
	    heightLeading = height + leadingCite(context);
	}
	if (keyhi != null) {
	    keyhiBase = heightLeading + ascentHi(keyhiDiv, context);
	    height = keyhiBase + descentHi(keyhiDiv, context);
	    heightLeading = height + leadingHi(keyhiDiv, context);
	}
	if (keyal != null) {
	    keyalBase = heightLeading + ascentAl(keyal, context);
	    height = keyalBase + descentAl(keyal, context);
	    heightLeading = height + leadingAl(keyal, context);
	}
	if (keytr != null) {
	    keytrBase = heightLeading + ascentTr(keytr, context);
	    height = keytrBase + descentTr(keytr, context);
	    heightLeading = height + leadingTr(keytr, context);
	}
	if (keyfo != null) {
	    keyfoBase = heightLeading + ascentFo(keyfo, context);
	    height = keyfoBase + descentFo(keyfo, context);
	    heightLeading = height + leadingFo(keyfo, context);
	}
	if (cite != null || href != null ||
		keyhi != null || keyal != null || 
		keytr != null || keyfo != null) {
	    keyLine = height + context.getLxInnerMargin();
	    height = keyLine + context.getLxLineThickness();
	    heightLeading = height + context.getLxInnerMargin();
	} 

	if (dicthi != null) {
	    dicthiBase = heightLeading + ascentHi(dicthiDiv, context);
	    height = dicthiBase + descentHi(dicthiDiv, context);
	    heightLeading = height + leadingHi(dicthiDiv, context);
	}
	if (dictal != null) {
	    dictalBase = heightLeading + ascentAl(dictal, context);
	    height = dictalBase + descentAl(dictal, context);
	    heightLeading = height + leadingAl(dictal, context);
	}
	if (dicttr != null) {
	    dicttrBase = heightLeading + ascentTr(dicttr, context);
	    height = dicttrBase + descentTr(dicttr, context);
	    heightLeading = height + leadingTr(dicttr, context);
	}
	if (dictfo != null) {
	    dictfoBase = heightLeading + ascentFo(dictfo, context);
	    height = dictfoBase + descentFo(dictfo, context);
	    heightLeading = height + leadingFo(dictfo, context);
	}
	if (dicthi != null || dictal != null || 
		dicttr != null || dictfo != null) {
	    dictLine = height + context.getLxInnerMargin();
	    height = dictLine + context.getLxLineThickness();
	} 
	return height;
    }

    // Ascent is distance from top of box to baseline of
    // first element, assuming that is in transliteration font.
    public static float getLxAscent(RenderContext context) {
	if (context.lxAbbreviated())
	    return alFontMetrics(context).getAscent();
	else
	    return context.getLxLineThickness() +
		context.getLxInnerMargin() +
		alFontMetrics(context).getAscent();
    }

    // Metrics of hi elements.
    private static float widthHi(RESorREScodeDivision hi, RenderContext context) {
	/*
	if (hi == null) 
	    return 0.0f;
	else 
	    return hi.getWidthPt();
	    */
	return 0;
    }
    private static float heightHi(RESorREScodeDivision hi, RenderContext context) {
	/*
	if (hi == null) 
	    return 0.0f;
	else 
	    return hi.getHeightPt() + leadingHi(hi, context);
	    */
	return 0;
    }
    private static float descentHi(RESorREScodeDivision hi, RenderContext context) {
	/*
	if (hi == null) 
	    return 0.0f;
	else 
	    return hi.getHeightPt() - ascentHi(hi, context);
	    */
	return 0;
    }
    private static float ascentHi(RESorREScodeDivision hi, RenderContext context) {
	if (hi == null) 
	    return 0.0f;
	else {
	    return ascentHi(context);
	}
    }
    private static float leadingHi(RESorREScodeDivision hi, RenderContext context) {
	if (hi == null)
	    return 0.0f;
	else 
	    return 0.2f * ascentHi(context);
    }
    private static float ascentHi(RenderContext context) {
	HieroRenderContext hieroContext = context.getHieroContext();
	return hieroContext.emSizePt();
    }

    // Metrics of al elements.
    private static float widthAl(String al, RenderContext context) {
	if (al == null)
	    return 0.0f;
	else
	    return alFontMetrics(context).stringWidth(alString(al, context));
    }
    private static float heightAl(String al, RenderContext context) {
	if (al == null)
	    return 0.0f;
	else
	    return alFontMetrics(context).getHeight();
    }
    private static float descentAl(String al, RenderContext context) {
	if (al == null)
	    return 0.0f;
	else
	    return alFontMetrics(context).getDescent();
    }
    private static float ascentAl(String al, RenderContext context) {
	if (al == null)
	    return 0.0f;
	else
	    return alFontMetrics(context).getAscent();
    }
    private static float leadingAl(String al, RenderContext context) {
	if (al == null)
	    return 0.0f;
	else
	    return alFontMetrics(context).getLeading();
    }
    private static String alString(String al, RenderContext context) {
	TrMap mapping = context.getEgyptMap();
	return mapping.mapString(al);
    }
    private static GeneralFontMetrics alFontMetrics(RenderContext context) {
	return context.getFontMetrics(RenderContext.EGYPT_FONT);
    }
    // If textal is null, then take default string.
    private String textalStringOrElse(RenderContext context) {
	if (textal == null)
	    return dummyAl;
	else {
	    TrMap mapping = context.getEgyptMap();
	    return mapping.mapString(textal);
	}
    }
    // The corresponding font.
    private int textalFontOrElse() {
	if (textal == null)
	    return RenderContext.LATIN_FONT;
	else
	    return RenderContext.EGYPT_FONT;
    }

    // Metrics of tr elements.
    private static float widthTr(String tr, RenderContext context) {
	if (tr == null)
	    return 0.0f;
	else
	    return trFontMetrics(context).stringWidth(trString(tr));
    }
    private static float heightTr(String tr, RenderContext context) {
	if (tr == null)
	    return 0.0f;
	else
	    return trFontMetrics(context).getHeight();
    }
    private static float descentTr(String tr, RenderContext context) {
	if (tr == null)
	    return 0.0f;
	else
	    return trFontMetrics(context).getDescent();
    }
    private static float ascentTr(String tr, RenderContext context) {
	if (tr == null)
	    return 0.0f;
	else
	    return trFontMetrics(context).getAscent();
    }
    private static float leadingTr(String tr, RenderContext context) {
	if (tr == null)
	    return 0.0f;
	else
	    return trFontMetrics(context).getLeading();
    }
    private static String trString(String tr) {
	return "\"" + tr + "\"";
    }
    private static GeneralFontMetrics trFontMetrics(RenderContext context) {
	return context.getFontMetrics(RenderContext.LATIN_FONT);
    }

    // Metrics of fo elements.
    private static float widthFo(String fo, RenderContext context) {
	if (fo == null)
	    return 0.0f;
	else
	    return foFontMetrics(context).stringWidth(fo);
    }
    private static float heightFo(String fo, RenderContext context) {
	if (fo == null)
	    return 0.0f;
	else
	    return foFontMetrics(context).getHeight();
    }
    private static float descentFo(String fo, RenderContext context) {
	if (fo == null)
	    return 0.0f;
	else
	    return foFontMetrics(context).getDescent();
    }
    private static float ascentFo(String fo, RenderContext context) {
	if (fo == null)
	    return 0.0f;
	else
	    return foFontMetrics(context).getAscent();
    }
    private static float leadingFo(String fo, RenderContext context) {
	if (fo == null)
	    return 0.0f;
	else
	    return foFontMetrics(context).getLeading();
    }
    private static GeneralFontMetrics foFontMetrics(RenderContext context) {
	return context.getFontMetrics(RenderContext.LATIN_FONT);
    }

    // Metrics of cite elements.
    private float widthCite(RenderContext context) {
	if (cite == null && href == null)
	    return 0.0f;
	else if (href == null)
	    return citeFontMetrics(context).stringWidth(cite);
	else {
	    String text = (cite != null ? cite : blankLabel);
	    return Link.getButtonWidth(context, RenderContext.LATIN_FONT, text);
	}
    }
    private float heightCite(RenderContext context) {
	if (cite == null && href == null)
	    return 0.0f;
	else if (href == null)
	    return citeFontMetrics(context).getHeight();
	else
	    return Link.getButtonHeight(context, RenderContext.LATIN_FONT);
    }
    private float descentCite(RenderContext context) {
	if (cite == null && href == null)
	    return 0.0f;
	else if (href == null)
	    return citeFontMetrics(context).getDescent();
	else
	    return Link.getButtonDescent(context, RenderContext.LATIN_FONT);
    }
    private float ascentCite(RenderContext context) {
	if (cite == null && href == null)
	    return 0.0f;
	else if (href == null)
	    return citeFontMetrics(context).getAscent();
	else
	    return Link.getButtonAscent(context, RenderContext.LATIN_FONT);
    }
    private float leadingCite(RenderContext context) {
	if (cite == null)
	    return 0.0f;
	else if (href == null)
	    return citeFontMetrics(context).getLeading();
	else
	    return Link.getButtonLeading(context, RenderContext.LATIN_FONT);
    }
    private static GeneralFontMetrics citeFontMetrics(RenderContext context) {
	return context.getFontMetrics(RenderContext.LATIN_FONT);
    }

    // No elements.
    private boolean isEmpty() {
	return texthi == null && textal == null && texttr == null && textfo == null &&
	cite == null && href == null &&
	keyhi == null && keyal == null && keytr == null && keyfo == null &&
	dicthi == null && dictal == null && dicttr == null && dictfo == null;
    }

    // Make formatting of hieroglyphic elements.
    private void makeHiFormats(RenderContext context) {
	HieroRenderContext hieroContext = context.getHieroContext();
	if (hieroContext != oldContext) {
	    oldContext = hieroContext;
	    if (texthiCode != null)
		texthiDiv = texthiCode.createRESorREScodeDivision(hieroContext);
	    if (keyhiCode != null)
		keyhiDiv = keyhiCode.createRESorREScodeDivision(hieroContext);
	    if (dicthiCode != null)
		dicthiDiv = dicthiCode.createRESorREScodeDivision(hieroContext);
	}
    }

    // We need to distinguish between AWT and iText. In the former case,
    // the button needs to be placed only once. In the latter case,
    // the button can be placed with draw.
    private static boolean isIText(RenderContext context) {
	GeneralFontMetrics fm = context.getFontMetrics(RenderContext.NORMAL_FONT);
	return (fm instanceof ITextFontMetrics);
    }
}
