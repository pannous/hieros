/***************************************************************************/
/*                                                                         */
/*  FormatStack.java                                                       */
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

public class FormatStack extends ResStack implements FormatBasicgroup {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatStack(ResStack stack, HieroRenderContext context) {
	super(stack.x,
	    stack.y,
	    stack.onunder,
            stack.switchs0,
            FormatTopgroupHelper.makeGroup(stack.group1, context),
            stack.switchs1,
            FormatTopgroupHelper.makeGroup(stack.group2, context),
            stack.switchs2);
        this.context = context;
    }

    public FormatTopgroup fGroup1() {
        return (FormatTopgroup) group1;
    }
    public FormatTopgroup fGroup2() {
        return (FormatTopgroup) group2;
    }

    ////////////////////////////////////////////////////////////
    // Scaling and positioning.

    // How much scaled down?
    public float sideScaledLeft() {
	return Math.max(fGroup1().sideScaledLeft(), fGroup2().sideScaledLeft());
    }
    public float sideScaledRight() {
	return Math.max(fGroup1().sideScaledRight(), fGroup2().sideScaledRight());
    }
    public float sideScaledTop() {
	return Math.max(fGroup1().sideScaledTop(), fGroup2().sideScaledTop());
    }
    public float sideScaledBottom() {
	return Math.max(fGroup1().sideScaledBottom(), fGroup2().sideScaledBottom());
    }

    // For doing scaling anew.
    public void resetScaling() {
	fGroup1().resetScaling();
	fGroup2().resetScaling();
    }

    // Dimensions.
    public int width() {
	int width1 = fGroup1().width();
	int width2 = fGroup2().width();
	int minimal1 = 0;
	int maximal1 = width1;
	int x = (int) Math.floor((this.x * width1) + 0.4f);
	int left = width2 / 2;
	int right = width2 - left;
	int minimal2 = x - left;
	int maximal2 = x + right;
	int minimal = Math.min(minimal1, minimal2);
	int maximal = Math.max(maximal1, maximal2);
	return (maximal - minimal);
    }
    public int height() {
	int height1 = fGroup1().height();
	int height2 = fGroup2().height();
	int minimal1 = 0;
	int maximal1 = height1;
	int y = (int) Math.floor((this.y * height1) + 0.4f);
	int top = height2 / 2;
	int bottom = height2 - top;
	int minimal2 = y - top;
	int maximal2 = y + bottom;
	int minimal = Math.min(minimal1, minimal2);
	int maximal = Math.max(maximal1, maximal2);
	return (maximal - minimal);
    }

    // Scale down.
    public void scale(float factor) {
	fGroup1().scale(factor);
	fGroup2().scale(factor);
    }

    // Computed by render(), later to be used by methods involving
    // area to be marked.
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
	    Rectangle rect, Rectangle shadeRect, Area clipped, 
	    boolean isClipped, boolean fitting) {
	this.shadeRect = shadeRect;
	rect = FormatBasicgroupHelper.placeCentre(rect, this, context);
	int width1 = fGroup1().width();
	int width2 = fGroup2().width();
	int height1 = fGroup1().height();
	int height2 = fGroup2().height();
	int x = (int) Math.floor((this.x * width1) + 0.4f);
	int y = (int) Math.floor((this.y * height1) + 0.4f);
	int left = width2 / 2;
	int top = height2 / 2;
	int xMin2 = x - left;
	int yMin2 = y - top;
	int x0 = Math.min(0, xMin2);
	int y0 = Math.min(0, yMin2);
	int x1 = -x0;
	int y1 = -y0;
	int x2 = xMin2 - x0;
	int y2 = yMin2 - y0;
	Rectangle rect1 = new Rectangle(rect.x + x1, rect.y + y1, width1, height1);
	Rectangle rect2 = new Rectangle(rect.x + x2, rect.y + y2, width2, height2);
	Area clipped1 = clipped;
	Area clipped2 = clipped;
	boolean isClipped1 = isClipped;
	boolean isClipped2 = isClipped;
	if (onunder != null && onunder.equals("on")) {
	    clipped1 = (Area) clipped.clone();
	    FillGraphics fill = new FillGraphics();
	    fGroup2().render(fill, rect2, rect2, clipped, isClipped, fitting);
	    Area filling2 = fill.getFilling();
	    clipped1.subtract(filling2);
	    isClipped1 = true;
	} else if (onunder != null && onunder.equals("under")) {
	    clipped2 = (Area) clipped.clone();
	    FillGraphics fill = new FillGraphics();
	    fGroup1().render(fill, rect1, shadeRect, clipped, isClipped, fitting);
	    Area filling1 = fill.getFilling();
	    clipped2.subtract(filling1);
	    isClipped2 = true;
	}
	fGroup1().render(image, rect1, shadeRect, clipped1, isClipped1, fitting);
	fGroup2().render(image, rect2, rect2, clipped2, isClipped2, fitting);
    }

    ////////////////////////////////////////////////////////////
    // Notes and shading.

    // Place footnotes.
    public void placeNotes(FlexGraphics im, 
	    boolean under, boolean over) {
	fGroup1().placeNotes(im, under, over);
	fGroup2().placeNotes(im, under, over);
    }

    // Render footnotes.
    public void renderNotes(UniGraphics im) {
	fGroup1().renderNotes(im);
	fGroup2().renderNotes(im);
    }

    // Make shading.
    public void shade(UniGraphics image) {
	fGroup1().shade(image);
	fGroup2().shade(image);
    }

    //////////////////////////////////////////////////////////////////////////////
    // RESlite.

    // Produce RESlite.
    public void toResLite(int x, int y,
	    Vector exprs, Vector notes, Vector shades,
	    Rectangle clip) {
	if (onunder != null) {
	    Vector exprs1 = new Vector();
	    Vector exprs2 = new Vector();
	    fGroup1().toResLite(x, y, exprs1, notes, shades, clip);
	    fGroup2().toResLite(x, y, exprs2, notes, shades, clip);
	    REScodeExprs list1 = null;
	    for (int i = exprs1.size()-1; i >= 0; i--) {
		REScodeExprs expr = (REScodeExprs) exprs1.get(i);
		expr.tl = list1;
		list1 = expr;
	    }
	    REScodeExprs list2 = null;
	    for (int i = exprs2.size()-1; i >= 0; i--) {
		REScodeExprs expr = (REScodeExprs) exprs2.get(i);
		expr.tl = list2;
		list2 = expr;
	    }
	    REScodePair pair = new REScodePair();
	    if (onunder.equals("on")) {
		pair.list1 = list1;
		pair.list2 = list2;
	    } else {
		pair.list1 = list2;
		pair.list2 = list1;
	    }
	    exprs.add(pair);
	} else {
	    fGroup1().toResLite(x, y, exprs, notes, shades, clip);
	    fGroup2().toResLite(x, y, exprs, notes, shades, clip);
	}
    }

}
