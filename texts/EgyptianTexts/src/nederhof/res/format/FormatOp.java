/***************************************************************************/
/*                                                                         */
/*  FormatOp.java                                                          */
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

public class FormatOp extends ResOp {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatOp(ResOp op, HieroRenderContext context) {
        super(op.sep,
                op.fit,
                op.fix,
                op.shade,
                ResShadeHelper.clone(op.shades),
		op.size);
        this.context = context;
    }

    // Make formatted ops.
    public static Vector makeOps(Vector ops, HieroRenderContext context) {
        Vector formatOps = new Vector(ops.size());
        for (int i = 0; i < ops.size(); i++) {
	    ResOp op = (ResOp) ops.get(i);
	    formatOps.add(new FormatOp(op, context));
        }
        return formatOps;
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
    public float dynScale = 1.0f;
    // Scaling on sides.
    public float dynSideScale = 1.0f;

    // For doing scaling anew.
    public void resetScaling() {
	dynScale = 1.0f;
	dynSideScale = 1.0f;
    }

    // Cashed for efficiency. Sep in pixels.
    public int dynSize = 0;

    // Can be width or height.
    public int size() {
	return dynSize;
    }

    // Scale down.
    // If fit(), then dynSize is later overwritten by a different distance
    // (by 'fitting' code outside this class).
    public void scale(float factor) {
	dynScale *= factor;
	dynSize = nonFitSize();
    }

    // Size ignoring fitting.
    public int nonFitSize() {
	return context.emToPix(dynSideScale * dynScale * 
		sep() * context.fontSep());
    }

    // Computed during rendering, later used by e.g. shade.
    // Area for shading.
    private Rectangle shadeRect;
    // Where should line be placed (null if none)?
    private Rectangle lineRect;

    // Rectangle.
    public Rectangle rectangle() {
        return shadeRect;
    }

    ////////////////////////////////////////////////////////////
    // Render.

    // Rendering here means merely recording area for shading and line, 
    // for later use.
    public void render(Rectangle shadeRect) {
	this.shadeRect = shadeRect;
	if (isColored() && context.lineMode() != ResValues.NO_LINE) 
	    lineRect = FormatBasicgroupHelper.lineRect(shadeRect, 
		    effectIsH(), sizeHeader(), context);
	else 
	    lineRect = null;
    }

    ////////////////////////////////////////////////////////////
    // Shading.

    // Make shading, and line. If direction of text was turned,
    // make corresponding turns of shading patterns.
    public void shade(UniGraphics image, 
	    int dir, int effectDir) {
	if (shade())
	    image.shade(shadeRect, context);
	else 
	    for (int i = 0; i < nShades(); i++) {
		String s = shade(i);
		if (HieroRenderContext.swapHV(dir, effectDir))
		    s = FormatShadeHelper.turnPatternHV(s);
		else if (HieroRenderContext.swapVH(dir, effectDir))
		    s = FormatShadeHelper.turnPatternVH(s);
		Rectangle part = FormatShadeHelper.chopRectangle(shadeRect, s);
		image.shade(part, context);
	    }
	if (lineRect != null)
	    image.fillRect(context.lineColor(), lineRect);
    }

    //////////////////////////////////////////////////////////////////////////////
    // RESlite.

    // Produce RESlite.
    public void toResLite(int x, int y, Vector shadesList, 
	    int dir, int effectDir) {
	if (shade())
	    FormatShadeHelper.shadeResLite(x, y, shadesList,
		    context, shadeRect);
	else 
	    for (int i = 0; i < nShades(); i++) {
		String s = shade(i);
		if (HieroRenderContext.swapHV(dir, effectDir))
		    s = FormatShadeHelper.turnPatternHV(s);
		else if (HieroRenderContext.swapHV(dir, effectDir))
		    s = FormatShadeHelper.turnPatternHV(s);
		Rectangle part = FormatShadeHelper.chopRectangle(shadeRect, s);
		FormatShadeHelper.shadeResLite(x, y, shadesList,
			context, part);
	    }
    }

}
