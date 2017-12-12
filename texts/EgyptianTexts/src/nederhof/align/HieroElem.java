/***************************************************************************/
/*                                                                         */
/*  HieroElem.java                                                         */
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

// Textual element in stream, representing hieroglyphic.

package nederhof.align;

import java.awt.*;

import nederhof.res.*;
import nederhof.res.format.*;

class HieroElem extends Elem {

    /*
    // Hieroglyphic as REScode.
    private REScode code;
    // How many groups in complete code.
    private int nGroups;
    // The context with which the division below was computed.
    private HieroRenderContext hieroContext;
    // Divided REScode, with respect to index.
    private REScodeDivision division;

    // RES is right away converted to REScode by the following context.
    private static final int conversionFontSizePt = 45;
    public static final HieroRenderContext conversionContext =
	new HieroRenderContext(conversionFontSizePt, true);
	*/

    // Parsing context.
    public static final HieroRenderContext hieroContext =
	new HieroRenderContext(30);
    private static final ParsingContext parsingContext =
	new ParsingContext(hieroContext, true);
    private FormatFragment formatted;

    public HieroElem(int type, String text) {
	super(type);
	ResFragment parsed = ResFragment.parse(text, parsingContext);
	formatted = new FormatFragment(parsed, hieroContext);
	/*
	if (RES.maybeRES(text)) {
	    RES res = RES.createRES(text, HieroElem.conversionContext);
	    ResDivision div = new ResDivision(res, HieroElem.conversionContext);
	    code = div.toREScode();
	} else
	    code = new REScode(text);
	if (code.getDir() != RES.DIR_HLR) {
	    System.err.println("Warning: only horizontal left-to-right hieroglyphic supported");
	    nGroups = 0;
	} else
	    nGroups = code.nGroups();
	hieroContext = null;
	division = null;
	*/
    }

    // For use below in taking suffix.
    private HieroElem(int type, REScode code) {
	super(type);
	/*
	this.code = code;
	nGroups = code.nGroups();
	hieroContext = null;
	division = null;
	*/
    }

    // Overrides the method in Elem.
    public void setPrefix(int index) {
	/*
	if (index >= nGroups)
	    super.setPrefix(-1);
	else
	    super.setPrefix(index);
	division = null;
	*/
    }

    // See Elem.
    public boolean isPrintable() {
	/*
	return !code.isEmpty();
	*/
	return true;
    }

    // See Elem.
    public boolean isContent() {
	/*
	return !code.isEmpty();
	*/
	return true;
    }

    // See Elem.
    public float getWidth(RenderContext context) {
	// makeFormat(context);
	return formatted.width();
    }

    // See Elem.
    public float getWidth(RenderContext context, int index) {
	return formatted.width();
	/*
	makeContext(context);
	REScodeDivision prefDivision;
	if (index < 0)
	    prefDivision = new REScodeDivision(code, hieroContext);
	else
	    prefDivision = new REScodeDivision(code, index, hieroContext);
	return prefDivision.getWidthPt();
	*/
    }

    // See Elem.
    public float getAdvance(RenderContext context) {
	return getAdvance(context, getPrefix());
    }

    // See Elem.
    public float getAdvance(RenderContext context, int index) {
	/*
	if (index >= 0 && index < nGroups)
	    return getWidth(context, index);
	else
	    return getWidth(context, index) + spaceWidth(context);
	    */
	return formatted.width() + spaceWidth(context);
    }

    // See Elem.
    public float getHeight(RenderContext context) {
	/*
	makeFormat(context);
	return division.getHeightPt() +
	    getLeading(context);
	    */
	return formatted.height() + getLeading(context);
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	/*
	makeFormat(context);
	return division.getHeightPt() -
	    hieroContext.emSizePt();
	    */
	return formatted.height() - hieroContext.emSizePt();
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	/*
	makeContext(context);
	return hieroContext.emSizePt();
	*/
	return hieroContext.emSizePt();
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	/*
	makeContext(context);
	*/
	return 0.2f * hieroContext.emSizePt();
    }

    // See Elem.
    public boolean breakable() {
	/*
	return nGroups > 1 || hasTrailingSpace();
	*/
	return false;
    }

    // See Elem.
    public int firstBreak(RenderContext context, float len) {
	/*
	int firstBreak = firstBreak();
	if (firstBreak >= 0 || getWidth(context, firstBreak) <= len)
	    return firstBreak;
	else 
	    return -1;
	    */
	return -1;
    }

    // See Elem.
    public int firstBreak() {
	/*
	if (breakable())
	    return Math.min(nGroups, 1);
	else
	    return -1;
	    */
	return -1;
    }

    // See Elem.
    public int lastBreak(RenderContext context, float len) {
	/*
	makeContext(context);
	float height = Float.MAX_VALUE;
	REScodeDivision div = new REScodeDivision(code, len, height, hieroContext, false);
	int pref = div.getInitialNumber();
	if (pref > 1 && pref < nGroups ||
		pref == nGroups && hasTrailingSpace())
	    return pref;
	else 
	    return -1;
	    */
	return -1;
    }

    // See Elem.
    public int nextBreak() {
	/*
	if (getPrefix() + 1 < nGroups || 
		getPrefix() + 1 == nGroups && hasTrailingSpace())
	    return getPrefix() + 1;
	else
	    return -1;
	    */
	return -1;
    }

    // See Elem.
    public Elem prefix(int index) {
	/*
	if (index >= nGroups)
	    return this;
	else {
	    HieroElem pref = (HieroElem) this.clone();
	    pref.setPrefix(index);
	    return pref;
	}
	*/
	return this;
    }

    // See Elem.
    public Elem suffix() {
	/*
	REScode tail = code.getTail(getPrefix());
	HieroElem suf = new HieroElem(getType(), tail);
	boolean suffixSpace = hasTrailingSpace() && getPrefix() != 0;
	suf.setTrailingSpace(suffixSpace);
	return suf;
	*/
	return this;
    }

    // See Elem.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	/*
	makeFormat(context);
	g.drawHiero(division, getX(), y - getAscent(context));
	*/
	g.drawHiero(formatted, getX(), y - getAscent(context));
    }

    // Leading of hieroglyphic is factor of line height.
    public static float getHieroLeading(RenderContext context) {
	HieroRenderContext hieroContext = context.getHieroContext();
	return 0.2f * hieroContext.emSizePt();
    }
    public static float getFootHieroLeading(RenderContext context) {
	HieroRenderContext hieroContext = context.getFootHieroContext();
	return 0.2f * hieroContext.emSizePt();
    }

    /*
    // Record current context for rendering hieroglyphic.
    private void makeContext(RenderContext context) {
	HieroRenderContext hieroContext = getHieroContext(context);
	if (hieroContext != this.hieroContext) {
	    this.hieroContext = hieroContext;
	    division = null;
	}
    }

    // Make formatting, so that dimensions become known.
    private void makeFormat(RenderContext context) {
	makeContext(context);
	if (division == null) {
	    if (getPrefix() < 0) 
		division = new REScodeDivision(code, hieroContext);
	    else
		division = new REScodeDivision(code, getPrefix(), hieroContext);
	}
    }

    // Get context for rendering hieroglyphic.
    private HieroRenderContext getHieroContext(RenderContext context) {
	if (getType() == RenderContext.HIERO_FONT)
	    return context.getHieroContext();
	else
	    return context.getFootHieroContext();
    }
    */

}
