/***************************************************************************/
/*                                                                         */
/*  FormatHieroglyphic.java                                                */
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

package nederhof.res.format;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import nederhof.res.*;

public class FormatHieroglyphic extends ResHieroglyphic {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatHieroglyphic(ResHieroglyphic hiero, HieroRenderContext context) {
	super(FormatTopgroupHelper.makeGroups(hiero.groups, context),
		FormatOp.makeOps(hiero.ops, context),
		ResSwitch.clone(hiero.switches));
	this.context = context;
    }

    // Make formatted hieroglyphic, unless it is null.
    public static FormatHieroglyphic makeHiero(ResHieroglyphic hiero, 
	    HieroRenderContext context) {
	if (hiero == null)
	    return null;
	else
	    return new FormatHieroglyphic(hiero, context);
    }

    public FormatTopgroup fGroup(int i) {
        return (FormatTopgroup) group(i);
    }
    public FormatOp fOp(int i) {
        return (FormatOp) op(i);
    }

    //////////////////////////////////////////////////////////////////
    // Effective values.

    private int effectDir() {
	return context.effectDir(direction);
    }

    private boolean effectIsH() {
	return ResValues.isH(effectDir());
    }

    // Unit size for height of line or width or column.
    // Forced size overrides setting in encoding.
    private float effectSize() {
	return context.effectSize(size());
    }

    ////////////////////////////////////////////////////////////
    // Scaling and positioning.

    // Changed during scaling.
    private float dynScale = 1.0f;

    // For doing scaling anew.
    public void resetScaling() {
	dynScale = 1.0f;
	for (int i = 0; i < nGroups(); i++)
	    fGroup(i).resetScaling();
	for (int i = 0; i < nOps(); i++)
	    fOp(i).resetScaling();
    }

    // Dimensions.
    public int width() {
	return width(Integer.MAX_VALUE);
    }
    public int height() {
	return height(Integer.MAX_VALUE);
    }

    // Dimensions for first n groups.
    public int width(int nGroups) {
	if (effectIsH()) {
	    int width = 0;
	    for (int i = 0; i < Math.min(nGroups(), nGroups); i++) {
		width += fGroup(i).width();
		if (i < Math.min(nGroups(), nGroups) - 1)
		    width += fOp(i).size();
	    }
	    return width;
	} else
	    return context.emToPix(dynScale * effectSize());
    }
    public int height(int nGroups) {
	if (effectIsH()) 
	    return context.emToPix(dynScale * effectSize());
	else {
	    int height = 0;
	    for (int i = 0; i < Math.min(nGroups(), nGroups); i++) {
		height += fGroup(i).height();
		if (i < Math.min(nGroups(), nGroups) - 1)
		    height += fOp(i).size();
	    }
	    return height;
	} 
    }

    // Width of groups i through j, assuming horizontal direction.
    public int width(int i, int j) {
        int width = 0;
        for (int k = i; k <= j; k++) {
            width += fGroup(k).width();
            if (k < j)
                width += fOp(k).size();
        }
	return width;
    }
    public int height(int i, int j) {
        int height = 0;
        for (int k = i; k <= j; k++) {
            height += fGroup(k).height();
            if (k < j)
                height += fOp(k).size();
        }
	return height;
    }

    // Distance from beginning of group to beginning of group.
    public int widthDist(int i, int j) {
	int width = 0;
	for (int k = i; k < j; k++)
	    width += fGroup(k).width() + fOp(k).size();
	return width;
    }

    // Scale down.
    public void scale(float factor) {
	dynScale *= factor;
	for (int i = 0; i < nGroups(); i++)
	    scale(fGroup(i), factor);
	for (int i = 0; i < nOps(); i++) {
	    FormatTopgroup prev = fGroup(i);
	    FormatOp op = fOp(i);
	    FormatTopgroup next = fGroup(i+1);
	    op.scale(factor);
	    float sideScale1 =
		(effectIsH() ? prev.sideScaledRight() : prev.sideScaledBottom());
	    float sideScale2 =
		(effectIsH() ? next.sideScaledLeft() : next.sideScaledTop());
	    float sideScale = Math.max(sideScale1, sideScale2);
	    if (dynScale > 0)
		op.dynSideScale = sideScale / dynScale;
	    else
		op.dynSideScale = sideScale;
	    if (op.fit())
		doFitting(i);
	}
    }

    // Scale down subgroup. Make sure it is within targetSize.
    private void scale(FormatTopgroup group, float factor) {
	group.scale(factor);
	int targetSize = context.emToPix(dynScale * effectSize());
	for (int i = 0; i < context.maxScalingIterations; i++) {
	    int size = (effectIsH() ? group.height() : group.width());
	    if (size <= 1 || size <= targetSize)
		break;
	    float newFactor = targetSize * 1.0f / size;
	    group.scale(newFactor);
	}
    }

    // Do fitting between the last few groups before the i-th operator
    // that have "fit" between them, and the next group.
    private void doFitting(int i) {
	int j;
	for (j = i; j > 0; j--) 
	    if (!fOp(j-1).fit())
		break;
	if (effectIsH()) {
	    int widthBefore = width(j, i);
	    int widthLast = width(i, i);
	    int widthAfter = width(i+1, i+1);
	    int height = height();
	    int sepMax = Math.min(widthLast, widthAfter);
	    Rectangle rectBefore = new Rectangle(0, 0, widthBefore, height);
	    Rectangle rectAfter = new Rectangle(0, 0, widthAfter, height);
	    BufferedImage imageBefore = MovedBuffer.whiteImage(context, widthBefore, height);
	    BufferedImage imageAfter = MovedBuffer.whiteImage(context, widthAfter, height);
	    TransGraphics gBefore = new TransGraphics(imageBefore, 0, 0, false);
	    TransGraphics gAfter = new TransGraphics(imageAfter, 0, 0, false);
	    renderHor(gBefore, rectBefore, j, i);
	    renderHor(gAfter, rectAfter, i+1, i+1);
	    fOp(i).dynSize = PixelHelper.fitHor(context, imageBefore, imageAfter, 
		    fOp(i).nonFitSize(), sepMax);
	} else {
	    int width = width();
	    int heightBefore = height(j, i);
	    int heightLast = height(i, i);
	    int heightAfter = height(i+1, i+1);
	    int sepMax = Math.min(heightLast, heightAfter);
	    Rectangle rectBefore = new Rectangle(0, 0, width, heightBefore);
	    Rectangle rectAfter = new Rectangle(0, 0, width, heightAfter);
	    BufferedImage imageBefore = MovedBuffer.whiteImage(context, width, heightBefore);
	    BufferedImage imageAfter = MovedBuffer.whiteImage(context, width, heightAfter);
	    TransGraphics gBefore = new TransGraphics(imageBefore, 0, 0, false);
	    TransGraphics gAfter = new TransGraphics(imageAfter, 0, 0, false);
	    renderVert(gBefore, rectBefore, j, i);
	    renderVert(gAfter, rectAfter, i+1, i+1);
	    fOp(i).dynSize = PixelHelper.fitVert(context, imageBefore, imageAfter, 
		    fOp(i).nonFitSize(), sepMax);
	}
    }

    // Number of groups that fit within length restriction on number of
    // pixels.
    public int boundedNGroups(int limit) {
	for (int i = 0; i < nGroups(); i++) {
	    limit -= 
	       (effectIsH() ? fGroup(i).width() : fGroup(i).height()) +	
	       (i > 0 ? fOp(i-1).size() : 0);
	    if (limit < 0)
		return i;
	}
	return nGroups();
    }

    ////////////////////////////////////////////////////////////
    // Render.

    // Render hieroglyphic.
    public void render(UniGraphics image, 
	    Rectangle rect, Rectangle shadeRect, Area clipped, 
	    boolean isClipped, boolean fitting, int padding) {
	render(image, 0, nGroups(), rect, shadeRect, clipped, isClipped, fitting, padding);
    }
    // Only for substring of groups.
    public void render(UniGraphics image, int group1, int group2,
	    Rectangle rect, Rectangle shadeRect, Area clipped, 
	    boolean isClipped, boolean fitting, int padding) {
	int nPad = nPaddable();
	for (int i = group1; i < nGroups() && i <= group2; i++) {
	    FormatTopgroup group = fGroup(i);
	    if (effectIsH()) {
		int width = group.width();
		Rectangle groupRect = FormatShadeHelper.chopStartH(rect, rect.x + width);
		Rectangle groupShadeRect = FormatShadeHelper.chopStartH(shadeRect, rect.x + width);
		group.render(image, groupRect, groupShadeRect,
			clipped, isClipped, fitting);
		shadeRect = FormatShadeHelper.chopEndH(shadeRect, rect.x + width);
		rect = FormatShadeHelper.chopEndH(rect, rect.x + width);
	    } else {
		int height = group.height();
		Rectangle groupRect = FormatShadeHelper.chopStartV(rect, rect.y + height);
		Rectangle groupShadeRect = FormatShadeHelper.chopStartV(shadeRect, rect.y + height);
		group.render(image, groupRect, groupShadeRect,
			clipped, isClipped, fitting);
		shadeRect = FormatShadeHelper.chopEndV(shadeRect, rect.y + height);
		rect = FormatShadeHelper.chopEndV(rect, rect.y + height);
	    }
	    if (i < nOps()) {
		FormatOp op = fOp(i);
		int opSize = op.size();
		if (!op.fix) {
		    int extra = padding / nPad;
		    padding -= extra;
		    opSize += extra;
		    nPad--;
		}
		if (effectIsH()) {
		    Rectangle opRect = FormatShadeHelper.chopStartH(shadeRect, rect.x + opSize);
		    op.render(opRect);
		    shadeRect = FormatShadeHelper.chopEndH(shadeRect, rect.x + opSize);
		    rect = FormatShadeHelper.chopEndH(rect, rect.x + opSize);
		} else {
		    Rectangle opRect = FormatShadeHelper.chopStartV(shadeRect, rect.y + opSize);
		    op.render(opRect);
		    shadeRect = FormatShadeHelper.chopEndV(shadeRect, rect.y + opSize);
		    rect = FormatShadeHelper.chopEndV(rect, rect.y + opSize);
		}
	    }
	}
    }

    // As above, but intended only for fitting, and only for groups i through j.
    private void renderHor(UniGraphics image, Rectangle rect, int i, int j) {
	for (int k = i; k <= j; k++) {
	    FormatTopgroup group = fGroup(k);
	    int width = group.width();
	    Rectangle groupRect = FormatShadeHelper.chopStartH(rect, rect.x + width);
	    group.render(image, groupRect, groupRect, 
		    new Area(groupRect), false, true);
	    rect = FormatShadeHelper.chopEndH(rect, rect.x + width);
	    if (k < j) {
		int opSize = fOp(k).size();
		rect = FormatShadeHelper.chopEndH(rect, rect.x + opSize);
	    }
	}
    }
    private void renderVert(UniGraphics image, Rectangle rect, int i, int j) {
	for (int k = i; k <= j; k++) {
	    FormatTopgroup group = fGroup(k);
	    int height = group.height();
	    Rectangle groupRect = FormatShadeHelper.chopStartV(rect, rect.y + height);
	    group.render(image, groupRect, groupRect,
		    new Area(groupRect), false, true);
	    rect = FormatShadeHelper.chopEndV(rect, rect.y + height);
	    if (k < j) {
		int opSize = fOp(k).size();
		rect = FormatShadeHelper.chopEndV(rect, rect.y + opSize);
	    }
	}
    }

    // Get rectangle of n-th top group or '-' operator.
    // If n is too big, return empty rectangle.
    public Rectangle rectangle(int pos) {
	if (pos % 2 == 0) { // even
	    pos /= 2;
	    if (pos < nGroups()) 
		return fGroup(pos).rectangle();
	} else { // odd
	    pos /= 2;
	    if (pos < nOps()) 
		return fOp(pos).rectangle();
	}
	return new Rectangle();
    }

    // Get number of top or '-' operator containing point.
    // If there is no such thing, then return -1.
    public int pos(int x, int y) {
	for (int i = 0; i < nGroups(); i++)
	    if (fGroup(i).rectangle().contains(x, y))
		return 2 * i;
	for (int i = 0; i < nOps(); i++)
	    if (fOp(i).rectangle().contains(x, y))
		return 2 * i + 1;
	return -1;
    }

    ////////////////////////////////////////////////////////////
    // Notes and shading.

    // Place footnotes.
    public void placeNotes(FlexGraphics im, boolean under, boolean over) {
	for (int i = 0; i < nGroups(); i++)
	    fGroup(i).placeNotes(im, under, over);
    }

    // Render footnotes.
    public void renderNotes(UniGraphics im) {
	for (int i = 0; i < nGroups(); i++)
	    fGroup(i).renderNotes(im);
    }
    // Only for some groups.
    public void renderNotes(UniGraphics im, int group1, int group2) {
	for (int i = group1; i < nGroups() && i <= group2; i++)
	    fGroup(i).renderNotes(im);
    }

    // Make shading.
    public void shade(UniGraphics image) {
	fGroup(0).shade(image);
	for (int i = 1; i < nGroups(); i++) {
	    fOp(i-1).shade(image, direction, effectDir());
	    fGroup(i).shade(image);
	}
    }
    // Only for some groups.
    public void shade(UniGraphics image, int group1, int group2) {
	    fGroup(group1).shade(image);
	for (int i = group1 + 1; i < nGroups() && i <= group2; i++) {
	    fOp(i-1).shade(image, direction, effectDir());
	    fGroup(i).shade(image);
	}
    }

    ///////////////////////////////////////////////////////////////////////////
    // RESlite.

    // Main hieroglyphic in RES fragment.
    public REScodeGroups toResLite(REScodeGlyph spec, int specLength) {
	Vector codeGroups = new Vector();
	Vector tightList = new Vector();
	FormatTopgroup first = fGroup(0);
	tightList.add(first);
	int advance = 0;
	int start = 0;
	int pos = specLength + (effectIsH() ? first.width() : first.height());
	for (int i = 0; i < nOps(); i++) {
	    FormatOp op = fOp(i);
	    int opLen = op.size();
	    if (!op.fix) {
		REScodeGroups gr = makeGroup(spec, tightList, op, start, pos, advance);
		codeGroups.add(gr);
		spec = null;
		tightList = new Vector();
		advance = pos-start + opLen;
		start = pos + opLen;
	    } else 
		tightList.add(op);
	    pos += opLen;
	    FormatTopgroup group = fGroup(i+1);
	    tightList.add(group);
	    pos += effectIsH() ? group.width() : group.height();
	}
	REScodeGroups last = makeGroup(spec, tightList, null, start, pos, advance);
	codeGroups.add(last);
	REScodeGroups list = null;
	for (int i = codeGroups.size()-1; i >= 0; i--) {
	    REScodeGroups group = (REScodeGroups) codeGroups.get(i);
	    group.tl = list;
	    list = group;
	}
	return list;
    }

    // Turn tight (alternating) list of groups and operators, plus perhaps
    // a specification of direction, into one REScode group.
    private REScodeGroups makeGroup(REScodeGlyph spec, Vector elems, FormatOp lastOp,
	    int start, int pos, int advance) {
	REScodeGroups gr = new REScodeGroups();
	gr.advance = context.pixToMilEm(advance);
	gr.length = context.pixToMilEm(pos-start);
	Vector exprs = new Vector();
	Vector notes = new Vector();
	Vector shades = new Vector();
	int x = effectIsH() ? start : 0;
	int y = effectIsH() ? 0 : start;
	Rectangle clip = bigEnoughClip(x, y);
	if (spec != null)
	    exprs.add(spec);
	for (int i = 0; i < elems.size(); i++) {
	    Object o = elems.get(i);
	    if (o instanceof FormatTopgroup) {
		FormatTopgroup group = (FormatTopgroup) o;
		group.toResLite(x, y, exprs, notes, shades, clip);
	    } else {
		FormatOp op = (FormatOp) o;
		op.toResLite(x, y, shades, direction, effectDir());
	    }
	}

	REScodeExprs exprList = null;
	for (int i = exprs.size()-1; i >= 0; i--) {
	    REScodeExprs expr = (REScodeExprs) exprs.get(i);
	    expr.tl = exprList;
	    exprList = expr;
	}
	gr.exprs = exprList;

	REScodeNotes noteList = null;
	for (int i = notes.size()-1; i >= 0; i--) {
	    REScodeNotes note = (REScodeNotes) notes.get(i);
	    note.tl = noteList;
	    noteList = note;
	}
	gr.notes = noteList;

	REScodeShades shadeList = null;
	for (int i = shades.size()-1; i >= 0; i--) {
	    REScodeShades sh = (REScodeShades) shades.get(i);
	    sh.tl = shadeList;
	    shadeList = sh;
	}
	gr.shades = shadeList;

	if (lastOp != null) {
	    Vector intershades = new Vector();
	    lastOp.toResLite(x, y, intershades, direction, effectDir());
	    REScodeShades interShadeList = null;
	    for (int i = intershades.size()-1; i >= 0; i--) {
		REScodeShades sh = (REScodeShades) intershades.get(i);
		sh.tl = interShadeList;
		interShadeList = sh;
	    }
	    gr.intershades = interShadeList;
	}

	return gr;
    }

    // Clip big enough to include the glyphs in a group under reasonable circumstances.
    private Rectangle bigEnoughClip(int x, int y) {
	int emSizePix = context.emSizePix();
	if (effectIsH())
	    return new Rectangle(- 10 * emSizePix, - 10 * emSizePix,
		    x + 20 * emSizePix, 20 * emSizePix);
	else 
	    return new Rectangle(- 10 * emSizePix, - 10 * emSizePix,
		    20 * emSizePix, y + 20 * emSizePix);
    }

    // Inside box.
    public void toResLite(int x, int y,
	    Vector exprs, Vector notes, Vector shades, Rectangle clip) {
	for (int i = 0; i < nGroups(); i++)
	    fGroup(i).toResLite(x, y, exprs, notes, shades, clip);
	for (int i = 0; i < nOps(); i++) 
	    fOp(i).toResLite(x, y, shades, direction, effectDir());
    }

}
