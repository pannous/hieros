/***************************************************************************/
/*                                                                         */
/*  FormatHorgroup.java                                                    */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
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

public class FormatHorgroup extends ResHorgroup 
	implements FormatTopgroup, FormatVertsubgroupPart {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatHorgroup(ResHorgroup group, HieroRenderContext context) {
	super(FormatHorsubgroup.makeGroups(group.groups, context),
		FormatOp.makeOps(group.ops, context),
		ResSwitch.clone(group.switches));
        this.context = context;
    }

    public FormatHorsubgroupPart fGroup(int i) {
        return (FormatHorsubgroupPart) group(i).group;
    }
    public FormatOp fOp(int i) {
        return (FormatOp) op(i);
    }

    //////////////////////////////////////////////////////////////////
    // Effective values.

    private int effectDir() {
	return context.effectDir(dirHeader());
    }

    private boolean effectIsH() {
	return ResValues.isH(effectDir());
    }

    // Unit size for height. If the size is specified at the
    // first operator, then take that, otherwise the global size.
    // If there is forced size, that overrides the above two values.
    private float effectSize() {
	return context.effectSize(size());
    }

    ////////////////////////////////////////////////////////////
    // Scaling and positioning.

    // Changed during scaling.
    private float dynScale = 1.0f;

    // How much scaled down?
    public float sideScaledLeft() {
	return fGroup(0).sideScaledLeft();
    }
    public float sideScaledRight() {
	return fGroup(nGroups()-1).sideScaledRight();
    }
    // Maximum of subgroups.
    public float sideScaledTop() {
	float scaled = 0.0f;
	for (int i = 0; i < nGroups(); i++) 
	    scaled = Math.max(scaled, fGroup(i).sideScaledTop());
	return scaled;
    }
    public float sideScaledBottom() {
	float scaled = 0.0f;
	for (int i = 0; i < nGroups(); i++) 
	    scaled = Math.max(scaled, fGroup(i).sideScaledBottom());
	return scaled;
    }

    // Return target height in pixels.
    private int targetHeight() {
	float unit = effectSize();
	if (unit == Float.MAX_VALUE)
	    return Integer.MAX_VALUE;
	else
	    return context.emToPix(dynScale * unit);
    }

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
	return width(0, nOps());
    }
    // Maximum of subgroups.
    public int height() {
	int height = 0;
	for (int i = 0; i < nGroups(); i++) 
	    height = Math.max(height, fGroup(i).height());
	return height;
    }

    // Width of groups i through j.
    public int width(int i, int j) {
	int width = 0;
	for (int k = i; k <= j; k++) {
	    width += fGroup(k).width();
	    if (k < j)
		width += fOp(k).size();
	}
	return width;
    }

    // Scale down.
    public void scale(float factor) {
	dynScale *= factor;
	for (int i = 0; i < nGroups(); i++) 
	    scale(fGroup(i), factor);
	for (int i = 0; i < nOps(); i++) {
	    fOp(i).scale(factor);
	    float sideScale1 = fGroup(i).sideScaledRight();
	    float sideScale2 = fGroup(i+1).sideScaledLeft();
	    float sideScale = Math.max(sideScale1, sideScale2);
	    if (dynScale > 0)
		fOp(i).dynSideScale = sideScale / dynScale;
	    else
		fOp(i).dynSideScale = sideScale;
	    if (fOp(i).fit())
		doFitting(i);
	}
    }

    // Scale down subgroup. Make sure it is within targetHeight.
    private void scale(FormatHorsubgroupPart group, float factor) {
	group.scale(factor);
	int targetHeight = targetHeight();
	for (int i = 0; i < context.maxScalingIterations; i++) {
	    int height = group.height();
	    if (height <= 1 || height <= targetHeight)
		break;
	    float newFactor = targetHeight * 1.0f / height;
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
	render(gBefore, rectBefore, j, i);
	render(gAfter, rectAfter, i+1, i+1);
	fOp(i).dynSize = PixelHelper.fitHor(context, imageBefore, imageAfter,
		fOp(i).nonFitSize(), sepMax);
    }

    // Computed by render(), later to be used by methods involving
    // area to be marked.
    // Area for shading.
    private Rectangle shadeRect;

    // Area covered by group.
    public Rectangle rectangle() {
        return shadeRect;
    }

    ////////////////////////////////////////////////////////////
    // Render.

    // Render. If necessary, make margin around actual groups, and/or
    // insert extra space between groups.
    public void render(UniGraphics image, 
	    Rectangle rect, Rectangle shadeRect, Area clipped, 
	    boolean isClipped, boolean fitting) {
	this.shadeRect = shadeRect;
	int width = width();
	int height = height();
	int horSurplus = rect.width - width;
	int vertSurplus = rect.height - height;
	int topSurplus = vertSurplus - vertSurplus / 2;
	int nPad = nPaddable();
	if (nPad < 1) {
	    int leftSurplus = horSurplus / 2;
	    rect = new Rectangle(rect.x + leftSurplus, rect.y + topSurplus,
		    width, height);
	    horSurplus = 0;
	} else 
	    rect = new Rectangle(rect.x, rect.y + topSurplus, rect.width, height);
	for (int i = 0; i < nGroups(); i++) {
	    FormatHorsubgroupPart group = fGroup(i);
	    int groupWidth = group.width();
	    Rectangle groupRect = FormatShadeHelper.chopStartH(rect, rect.x + groupWidth);
	    Rectangle groupShadeRect = 
		(i == nGroups() - 1) ? shadeRect :
		FormatShadeHelper.chopStartH(shadeRect, rect.x + groupWidth);
	    group.render(image, groupRect, groupShadeRect, clipped, isClipped, fitting);
	    shadeRect = FormatShadeHelper.chopEndH(shadeRect, rect.x + groupWidth);
	    rect = FormatShadeHelper.chopEndH(rect, rect.x + groupWidth);
	    if (i < nOps()) {
		FormatOp op = fOp(i);
		int opSize = op.size();
		if (!op.fix) {
		    int extra = horSurplus / nPad;
		    horSurplus -= extra;
		    nPad--;
		    opSize += extra;
		}
		Rectangle opRect = FormatShadeHelper.chopStartH(shadeRect, rect.x + opSize);
		op.render(opRect);
		shadeRect = FormatShadeHelper.chopEndH(shadeRect, rect.x + opSize);
		rect = FormatShadeHelper.chopEndH(rect, rect.x + opSize);
	    }
	}
    }

    // As above, but intended only for fitting, and only for groups i through j.
    private void render(UniGraphics image, Rectangle rect, int i, int j) {
	for (int k = i; k <= j; k++) {
	    FormatHorsubgroupPart group = fGroup(k);
	    int width = group.width();
	    Rectangle groupRect = FormatShadeHelper.chopStartH(rect, rect.x + width);
	    group.render(image, groupRect, groupRect, new Area(groupRect), false, true);
	    rect = FormatShadeHelper.chopEndH(rect, rect.x + width);
	    if (k < j) {
		int opSize = fOp(k).size();
		rect = FormatShadeHelper.chopEndH(rect, rect.x + opSize);
	    }
	}
    }

    ////////////////////////////////////////////////////////////
    // Notes and shading.

    // Place footnotes.
    public void placeNotes(FlexGraphics im, boolean under, boolean over) {
	for (int i = 0; i < nGroups(); i++) {
	    boolean groupUnder = under;
	    boolean groupOver = over;
	    if (!effectIsH() && 0 < i) 
		groupOver = true;
	    if (!effectIsH() && i < nGroups() - 1) 
		groupUnder = true;
	    fGroup(i).placeNotes(im, groupUnder, groupOver);
	}
    }

    // Render footnotes.
    public void renderNotes(UniGraphics im) {
	for (int i = 0; i < nGroups(); i++) 
	    fGroup(i).renderNotes(im);
    }

    // Make shading.
    public void shade(UniGraphics image) {
	for (int i = 0; i < nGroups(); i++) 
	    fGroup(i).shade(image);
	for (int i = 0; i < nOps(); i++) 
	    fOp(i).shade(image, ResValues.DIR_HLR, ResValues.DIR_HLR);
    }

    //////////////////////////////////////////////////////////////////////////////
    // RESlite.

    // Produce RESlite.
    public void toResLite(int x, int y,
	    Vector exprs, Vector notes, Vector shades, Rectangle clip) {
	for (int i = 0; i < nGroups(); i++)
	    fGroup(i).toResLite(x, y, exprs, notes, shades, clip);
	for (int i = 0; i < nOps(); i++) 
	    fOp(i).toResLite(x, y, shades, ResValues.DIR_HLR, ResValues.DIR_HLR);
    }

}
