/***************************************************************************/
/*                                                                         */
/*  FormatModify.java                                                      */
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
import java.util.*;

import nederhof.res.*;

public class FormatModify extends ResModify implements FormatBasicgroup {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatModify(ResModify modify, HieroRenderContext context) {
	super(modify.width,
		modify.height,
		modify.above,
		modify.below,
		modify.before,
		modify.after,
		modify.omit,
		modify.shade,
		ResShadeHelper.clone(modify.shades),
		modify.switchs1,
		FormatTopgroupHelper.makeGroup(modify.group, context),
		modify.switchs2);
        this.context = context;
    }

    public FormatTopgroup fGroup() {
	return (FormatTopgroup) group;
    }

    //////////////////////////////////////////////////////////////////
    // Effective values.

    private int effectDir() {
        return context.effectDir(dirHeader());
    }

    private boolean effectIsH() {
        return ResValues.isH(effectDir());
    }

    ////////////////////////////////////////////////////////////
    // Scaling and positioning.

    // Changed during scaling.
    private float dynScale = 1.0f;

    // How much scaled down?
    public float sideScaledLeft() {
	return fGroup().sideScaledLeft();
    }
    public float sideScaledRight() {
	return fGroup().sideScaledRight();
    }
    public float sideScaledTop() {
	return fGroup().sideScaledTop();
    }
    public float sideScaledBottom() {
	return fGroup().sideScaledBottom();
    }

    // For doing scaling anew.
    public void resetScaling() {
	dynScale = 1.0f;
	fGroup().resetScaling();
    }

    // Dimensions in pixels.
    public int width() {
	float virtualShare = 1.0f / (before + 1.0f + after);
	int maxWidth = fGroup().width();
	if (!Float.isNaN(width)) {
	    // Semantics without following two lines seems to make more sense.
	    // int target = context.emToPix(dynScale * width);
	    // maxWidth = Math.max(maxWidth, target);
	    maxWidth = context.emToPix(dynScale * width);
	}
	return Math.round(virtualShare * maxWidth);
    }
    public int height() {
	float virtualShare = 1.0f / (above + 1.0f + below);
	int maxHeight = fGroup().height();
	if (!Float.isNaN(height)) {
	    // As above.
	    // int target = context.emToPix(dynScale * height);
	    // maxHeight = Math.max(maxHeight, target);
	    maxHeight = context.emToPix(dynScale * height);
	}
	return Math.round(virtualShare * maxHeight);
    }

    // Scale down subgroup. Make sure it is within targetWidth and targetHeight.
    public void scale(float factor) {
	dynScale *= factor;
	fGroup().scale(factor);
	int targetWidth = (Float.isNaN(width) ?
		Integer.MAX_VALUE :
		context.emToPix(dynScale * width));
	int targetHeight = (Float.isNaN(height) ?
		Integer.MAX_VALUE :
		context.emToPix(dynScale * height));
	for (int i = 0; i < context.maxScalingIterations; i++) {
	    int width = fGroup().width();
	    int height = fGroup().height();
	    if ((width <= 1 || width <= targetWidth) &&
		    (height <= 1 || height <= targetHeight))
		break;
	    float newFactor1 = ((width <= 1 || width <= targetWidth) ?
		    Float.MAX_VALUE :
		    targetWidth * 1.0f / width);
	    float newFactor2 = ((height <= 1 || height <= targetHeight) ?
		    Float.MAX_VALUE :
		    targetHeight * 1.0f / height);
	    float newFactor = Math.min(newFactor1, newFactor2);
	    fGroup().scale(newFactor);
	}
    }

    // Computed by render(), later to be used by e.g. shade().
    // Clipped area for omit (null otherwise).
    private Rectangle clip;
    // Area for shading.
    private Rectangle shadeRect;

    // Rectangle.
    public Rectangle rectangle() {
        return shadeRect;
    }

    ////////////////////////////////////////////////////////////
    // Render.

    // Render.
    public void render(UniGraphics image, 
	    Rectangle rect, Rectangle shadeRect, Area clipped, boolean 
	    isClipped, boolean fitting) {
	rect = FormatBasicgroupHelper.placeCentre(rect, this, context);
	this.shadeRect = shadeRect;
	float beforeShare = before / (before + 1.0f + after);
	float afterShare = after / (before + 1.0f + after);
	float aboveShare = above / (above + 1.0f + below);
	float belowShare = below / (above + 1.0f + below);
	int maxWidth = fGroup().width();
	if (!Float.isNaN(width)) {
	    // int target = context.emToPix(dynScale * width);
	    // maxWidth = Math.max(maxWidth, target);
	    maxWidth = context.emToPix(dynScale * width);
	}
	int maxHeight = fGroup().height();
	if (!Float.isNaN(height)) {
	    // int target = context.emToPix(dynScale * height);
	    // maxHeight = Math.max(maxHeight, target);
	    maxHeight = context.emToPix(dynScale * height);
	}
	int beforePart = Math.round(beforeShare * maxWidth);
	int afterPart = Math.round(afterShare * maxWidth);
	int abovePart = Math.round(aboveShare * maxHeight);
	int belowPart = Math.round(belowShare * maxHeight);
	if (omit && 
		(beforePart != 0 || afterPart != 0 ||
		 abovePart != 0 || belowPart != 0)) {
	    clip = new Rectangle(rect);
	    if (isClipped) {
		clipped = (Area) clipped.clone();
		clipped.intersect(new Area(rect));
	    } else 
		clipped = new Area(rect);
	    isClipped = true;
	} else
	    clip = null;
	rect = new Rectangle(rect.x - beforePart, rect.y - abovePart,
		maxWidth, maxHeight);
	fGroup().render(image, rect, shadeRect, clipped,
		isClipped, fitting);
    }

    ////////////////////////////////////////////////////////////
    // Notes and shading.

    // Place footnotes.
    public void placeNotes(FlexGraphics im, 
	    boolean under, boolean over) {
	if (omit) {
	    if (effectIsH()) {
	       if (above > 0)
		    over = true;
	       if (below > 0)
		    under = true;
	    } else {
	       if (before > 0)
		    under = true;
	       if (after > 0)
		    over = true;
	    }
	}
	fGroup().placeNotes(im, under, over);
    }

    // Render footnotes.
    public void renderNotes(UniGraphics im) {
	fGroup().renderNotes(im);
    }

    // Make shading.
    public void shade(UniGraphics image) {
	FormatShadeHelper.shade(image, context, shadeRect, shade(), shades);
	fGroup().shade(image);
    }

    //////////////////////////////////////////////////////////////////////////////
    // RESlite.

    // Produce RESlite.
    public void toResLite(int x, int y,
	    Vector exprs, Vector notes, Vector shadesList,
	    Rectangle clipped) {
	Rectangle sub = clipped;
	if (clip != null) {
	    sub = sub.intersection(clip);
	}
	fGroup().toResLite(x, y, exprs, notes, shadesList, sub);
	FormatShadeHelper.shadeResLite(x, y, shadesList, 
		context, shadeRect, shade(), shades);
    }

}
