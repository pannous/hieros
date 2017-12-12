/***************************************************************************/
/*                                                                         */
/*  FormatVertgroup.java                                                   */
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

public class FormatVertgroup extends ResVertgroup 
	implements FormatTopgroup, FormatHorsubgroupPart {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatVertgroup(ResVertgroup group, HieroRenderContext context) {
        super(FormatVertsubgroup.makeGroups(group.groups, context),
                FormatOp.makeOps(group.ops, context),
                ResSwitch.clone(group.switches));
        this.context = context;
    }

    public FormatVertsubgroupPart fGroup(int i) {
        return (FormatVertsubgroupPart) group(i).group;
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
    // Maximum of subgroups.
    public float sideScaledLeft() {
	float scaled = 0.0f;
	for (int i = 0; i < nGroups(); i++)
	    scaled = Math.max(scaled, fGroup(i).sideScaledLeft());
	return scaled;
    }
    public float sideScaledRight() {
	float scaled = 0.0f;
	for (int i = 0; i < nGroups(); i++) 
	    scaled = Math.max(scaled, fGroup(i).sideScaledRight());
	return scaled;
    }
    public float sideScaledTop() {
	return fGroup(0).sideScaledTop();
    }
    public float sideScaledBottom() {
	return fGroup(nGroups()-1).sideScaledBottom();
    }

    // Return target width in pixels.
    private int targetWidth() {
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
    // Maximum of subgroups.
    public int width() {
	int width = 0;
	for (int i = 0; i < nGroups(); i++)
	    width = Math.max(width, fGroup(i).width());
	return width;
    }
    public int height() {
	return height(0, nOps());
    }

    // Height of groups i through j.
    public int height(int i, int j) {
	int height = 0;
	for (int k = i; k <= j; k++) {
	    height += fGroup(k).height();
	    if (k < j)
		height += fOp(k).size();
	}
	return height;
    }

    // Scale down.
    public void scale(float factor) {
	dynScale *= factor;
	for (int i = 0; i < nGroups(); i++) 
	    scale(fGroup(i), factor);
	for (int i = 0; i < nOps(); i++) {
	    fOp(i).scale(factor);
	    float sideScale1 = fGroup(i).sideScaledBottom();
	    float sideScale2 = fGroup(i+1).sideScaledTop();
	    float sideScale = Math.max(sideScale1, sideScale2);
	    if (dynScale > 0)
		fOp(i).dynSideScale = sideScale / dynScale;
	    else 
		fOp(i).dynSideScale = sideScale;
	    if (fOp(i).fit())
		doFitting(i);
	}
    }

    // Scale down subgroup. Make sure it is within targetWidth.
    private void scale(FormatVertsubgroupPart group, float factor) {
	group.scale(factor);
	int targetWidth = targetWidth();
	for (int i = 0; i < context.maxScalingIterations; i++) {
	    int width = group.width();
	    if (width <= 1 || width <= targetWidth)
		break;
	    float newFactor = targetWidth * 1.0f / width;
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
	render(gBefore, rectBefore, j, i);
	render(gAfter, rectAfter, i+1, i+1);
	fOp(i).dynSize = PixelHelper.fitVert(context, imageBefore, imageAfter,
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

    // Render.
    public void render(UniGraphics image, 
	    Rectangle rect, Rectangle shadeRect, Area clipped, 
	    boolean isClipped, boolean fitting) {
	this.shadeRect = shadeRect;
	int width = width();
	int height = height();
	int horSurplus = rect.width - width;
	int vertSurplus = rect.height - height;
	int leftSurplus = horSurplus / 2;
	int nPad = nPaddable();
	if (nPad < 1) {
	    int topSurplus = vertSurplus - vertSurplus / 2;
	    rect = new Rectangle(rect.x + leftSurplus, rect.y + topSurplus,
		    width, height);
	    vertSurplus = 0;
	} else
	    rect = new Rectangle(rect.x + leftSurplus, rect.y, width, rect.height);
	for (int i = 0; i < nGroups(); i++) {
	    FormatVertsubgroupPart group = fGroup(i);
	    int groupHeight = group.height();
	    Rectangle groupRect = FormatShadeHelper.chopStartV(rect, rect.y + groupHeight);
	    Rectangle groupShadeRect = 
		(i == nGroups() - 1) ? shadeRect :
		FormatShadeHelper.chopStartV(shadeRect, rect.y + groupHeight);
	    group.render(image, groupRect, groupShadeRect, clipped, isClipped, fitting);
	    shadeRect = FormatShadeHelper.chopEndV(shadeRect, rect.y + groupHeight);
	    rect = FormatShadeHelper.chopEndV(rect, rect.y + groupHeight);
	    if (i < nOps()) {
		FormatOp op = fOp(i);
		int opSize = op.size();
		if (!op.fix) {
		    int extra = vertSurplus / nPad;
		    vertSurplus -= extra;
		    nPad--;
		    opSize += extra;
		}
		Rectangle opRect = FormatShadeHelper.chopStartV(shadeRect, rect.y + opSize);
		op.render(opRect);
		shadeRect = FormatShadeHelper.chopEndV(shadeRect, rect.y + opSize);
		rect = FormatShadeHelper.chopEndV(rect, rect.y + opSize);
	    }
	}
    }

    // As above, but intended only for fitting, and only for groups i through j.
    private void render(UniGraphics image, Rectangle rect, int i, int j) {
	for (int k = i; k <= j; k++) {
	    FormatVertsubgroupPart group = fGroup(k);
	    int height = group.height();
	    Rectangle groupRect = FormatShadeHelper.chopStartV(rect, rect.y + height);
	    group.render(image, groupRect, groupRect, new Area(groupRect), false, true);
	    rect = FormatShadeHelper.chopEndV(rect, rect.y + height);
	    if (k < j) {
		int opSize = fOp(k).size();
		rect = FormatShadeHelper.chopEndV(rect, rect.y + opSize);
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
	    if (effectIsH() && 0 < i)
		groupOver = true;
	    if (effectIsH() && i < nGroups() - 1)
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
	    fOp(i).shade(image, ResValues.DIR_VLR, ResValues.DIR_VLR);
    }

    //////////////////////////////////////////////////////////////////////////////
    // RESlite.

    // Produce RESlite.
    public void toResLite(int x, int y,
	    Vector exprs, Vector notes, Vector shades, Rectangle clip) {
	for (int i = 0; i < nGroups(); i++)
	    fGroup(i).toResLite(x, y, exprs, notes, shades, clip);
	for (int i = 0; i < nOps(); i++) 
	    fOp(i).toResLite(x, y, shades, ResValues.DIR_VLR, ResValues.DIR_VLR);
    }

}
