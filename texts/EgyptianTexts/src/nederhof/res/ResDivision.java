/***************************************************************************/
/*                                                                         */
/*  ResDivision.java                                                       */
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

// Divide a piece of RES into an initial part and a remainder.
// The initial part should fit within given distance.
//
// THIS CLASS IS OBSOLETE AND SHOULD BE REMOVED AT SOME POINT

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class ResDivision implements RESorREScodeDivision {

    /*
     * TODO remove

    // Complete RES as input to constructor.
    private RES original;

    // Context of rendering.
    private HieroRenderContext context;

    // The number of groups in the prefix that fit within limit.
    private int initialNumber;

    // Size of initial prefix, in pixels.
    private int initialWidth;
    private int initialHeight;

    // Allowable padding inside prefix, in pixels.
    private int pad = 0;

    // The prefix that fits within limit.
    private RES prefix;

    // Process complete input.
    // The RES code is first cloned, as scaling changes some
    // variables inside the structure. Some applications require
    // simultaneous scaling by different contexts.
    public ResDivision(RES code, HieroRenderContext context) {
	original = (RES) code.clone();
	original.scale(context);
	this.context = context;
	initialNumber = original.nGroups();
	initialWidth = original.width(context);
	initialHeight = original.height(context);
	prefix = original;
    }

    // Process input until len limit is reached.
    // len is in pt, convert to pixels.
    // len can be infinite (= no limit).
    // Is padding allowed?
    public ResDivision(RES code, float len, 
	    HieroRenderContext context, boolean paddingAllowed) {
	original = (RES) code.clone();
	original.scale(context);
	this.context = context;
	int lenPix;
	if (len == Float.MAX_VALUE)
	    lenPix = Integer.MAX_VALUE;
	else
	    lenPix = context.ptToPix(len);
	initialNumber = original.boundedNGroups(context, lenPix);
	initialWidth = original.width(context, initialNumber);
	initialHeight = original.height(context, initialNumber);
	if (paddingAllowed) {
	    if (original.isH(context))
		pad = lenPix - initialWidth;
	    else
		pad = lenPix - initialHeight;
	    int normalSepPix = original.nPaddable(initialNumber) * 
		context.emToPix(context.fontSep());
	    if (pad > normalSepPix * context.padding())
		pad = 0;
	}
	prefix = original.prefix(initialNumber);
    }

    // As above, but specify width and height.
    public ResDivision(RES code, float width, float height,
	    HieroRenderContext context, boolean paddingAllowed) {
	this(code, toSize(code, width, height, context), context, paddingAllowed);
    }

    // Process the first nGroups of the code.
    public ResDivision(RES code, int nGroups, HieroRenderContext context) {
	original = (RES) code.clone();
	original.scale(context);
	this.context = context;
	initialNumber = Math.min(nGroups, original.nGroups());
	initialWidth = original.width(context, initialNumber);
	initialHeight = original.height(context, initialNumber);
	prefix = original.prefix(initialNumber);
    }

    // Translate allowable width and height to size.
    private static float toSize(RES code, float width, float height,
	    HieroRenderContext context) {
	if (code.isH(context)) {
	    if (context.emToPt(code.size) <= height)
		return width;
	    else
		return 0;
	} else {
	    if (context.emToPt(code.size) <= width)
		return height;
	    else
		return 0;
	}
    }

    // Number of groups in prefix.
    public int getInitialNumber() {
	return initialNumber;
    }

    // Conceptual width in pixels.
    public int getWidthPixels() {
	return prefix.width(context) +
	    (prefix.isH(context) ? pad : 0);
    }
    // Conceptual height in pixels.
    public int getHeightPixels() {
	return prefix.height(context) +
	    (prefix.isH(context) ? 0 : pad);
    }
    // Get conceptual dimensions of image in pixels.
    private Dimension getDimensionPix() {
	return new Dimension(getWidthPixels(), getHeightPixels());
    }

    // Conceptual width in points.
    public float getWidthPt() {
	return getWidthPixels() / context.resolution();
    }
    // Conceptual height in points.
    public float getHeightPt() {
	return getHeightPixels() / context.resolution();
    }

    // Code that is included in prefix.
    public RES getRES() {
	return prefix;
    }

    // Code that is not included.
    public RES getRemainder() {
	return original.suffix(initialNumber);
    }

    ////////////////////////////////////////////////////////////////////////
    // Turning prefix into graphical format.

    // Area that is big enough in all normal circumstances
    // to contain all pixels. Is 5 EM around the virtual box.
    // Is used as initial clip.
    private Area bigEnoughArea() {
	Dimension dimPix = getDimensionPix();
	Rectangle rect = new Rectangle(
		-5 * context.emSizePix(),
		-5 * context.emSizePix(),
		10 * context.emSizePix() + dimPix.width,
		10 * context.emSizePix() + dimPix.height);
	return new Area(rect);
    }

    // Get rectangle comprising pixels, with indication of reference point.
    // This does not include notes.
    private Rectangle getRectangleNoNotes() {
	Dimension dimPix = getDimensionPix();
	Rectangle rect = new Rectangle(0, 0, dimPix.width, dimPix.height);
	ResizeGraphics graphics = new ResizeGraphics(dimPix, context,
		prefix.isLR(context));
	prefix.render(graphics, context, rect, bigEnoughArea(), pad);
	prefix.shade(graphics, context);
	return graphics.getRect();
    }
    // As above, but also including notes.
    // After this call, one can call prefix.renderNotes(...).
    public Rectangle getRectangle() {
	Dimension dimPix = getDimensionPix();
	Rectangle rect = new Rectangle(0, 0, dimPix.width, dimPix.height);
	Rectangle pixSize = getRectangleNoNotes();
	FlexGraphics graphics = new FlexGraphics(context,
		pixSize.width, pixSize.height, -pixSize.x, -pixSize.y,
		dimPix.width, dimPix.height, !prefix.isLR(context));
	prefix.render(graphics, context, rect, new Area(pixSize), pad);
	prefix.placeNotes(graphics, context);
	return graphics.getRect();
    }

    // Get margins around image, in pixels.
    public Insets margins() {
	Dimension dimPix = getDimensionPix();
	Rectangle pixSize = getRectangle();
	int left;
	int right;
	if (prefix.isLR(context)) {
	    left = -pixSize.x;
	    right = pixSize.width - dimPix.width - left;
	} else {
	    right = -pixSize.x;
	    left = pixSize.width - dimPix.width - right;
	}
	int top = -pixSize.y;
	int bottom = pixSize.height - dimPix.height - top;
	return new Insets(top, left, bottom, right);
    }

    // Turn prefix into image.
    public MovedBuffer toImage() {
	Dimension dimPix = getDimensionPix();
	Rectangle rect = new Rectangle(0, 0, dimPix.width, dimPix.height);
	Rectangle pixSize = getRectangleNoNotes();
	FlexGraphics graphics = new FlexGraphics(context,
		pixSize.width, pixSize.height, -pixSize.x, -pixSize.y,
		dimPix.width, dimPix.height, !prefix.isLR(context));
	prefix.render(graphics, context, rect, new Area(pixSize), pad);
	prefix.placeNotes(graphics, context);
	prefix.shade(graphics, context);
	return graphics.toImage();
    }

    // As above, but footnotes are placed over shading. 
    // This also makes it slower, because rendering has to occur twice.
    public MovedBuffer toImageImproved() {
	Dimension dimPix = getDimensionPix();
	Rectangle rect = new Rectangle(0, 0, dimPix.width, dimPix.height);
	Rectangle pixSize = getRectangle(); // also places notes
	FlexGraphics graphics = new FlexGraphics(context,
		pixSize.width, pixSize.height, -pixSize.x, -pixSize.y,
		dimPix.width, dimPix.height, !prefix.isLR(context));
	prefix.render(graphics, context, rect, new Area(pixSize), pad);
	prefix.shade(graphics, context);
	prefix.renderNotes(graphics, context);
	return graphics.toImage();
    }

    // Write prefix into graphics, at indicated position.
    public void write(Graphics2D gUnscaled, int x, int y) {
	getRectangle(); // places notes
	Graphics2D g = (Graphics2D) gUnscaled.create();
	g.scale(1.0 / context.resolution(), 1.0 / context.resolution());
	Dimension dimPix = getDimensionPix();
	Rectangle rect = new Rectangle(0, 0, dimPix.width, dimPix.height);
	TransGraphics graphics = new TransGraphics(g, x, y, getWidthPixels(), 
		!prefix.isLR(context));
	// mainly for eps output, for stetching bounding box.
	g.setColor(Color.white);
	g.fillRect(0, 0, dimPix.width, dimPix.height);
	prefix.render(graphics, context, rect, bigEnoughArea(), pad);
	prefix.shade(graphics, context);
	prefix.renderNotes(graphics, context);
    }

    // Get rectangle covered by n-th top group or '-' operation, 
    // with regard to start position (x,y).
    // If rectangle is empty (operator with negative size), then
    // take end of previous group.
    public Rectangle rectangle(int x, int y, int pos) {
	Dimension dimPix = getDimensionPix();
	if (prefix.nGroups() == 0 || pos < 0) {
	    if (prefix.isH(context)) {
		if (prefix.isLR(context))
		    return new Rectangle(x, y, 1, dimPix.height);
		else
		    return new Rectangle(x + dimPix.width - 1, y, 
			    1, dimPix.height);
	    } else
		return new Rectangle(x, y, dimPix.width, 1);
	}
	Rectangle rect = prefix.rectangle(pos);
	if (rect.width < 1 || rect.height < 1) {
	    if (pos % 2 == 1) { // odd
		rect = new Rectangle(prefix.rectangle(pos - 1));
		if (!rect.equals(new Rectangle())) {
		    if (prefix.isH(context)) {
			rect.x += rect.width;
			rect.width = 1;
		    } else {
			rect.y += rect.height;
			rect.height = 1;
		    }
		} else 
		    return rect;
	    } else {
		rect.width = Math.max(rect.width, 1);
		rect.height = Math.max(rect.height, 1);
	    }
	}
	if (prefix.isLR(context)) 
	    return new Rectangle(x + rect.x,
		    y + rect.y,
		    rect.width,
		    rect.height);
	else 
	    return new Rectangle(x + dimPix.width - (rect.x + rect.width),
		    y + rect.y,
		    rect.width,
		    rect.height);
    }

    // Get n such that n-th top group or '-' operation includes point.
    // If there is no such thing, then return -1.
    public int pos(int x, int y) {
	Dimension dimPix = getDimensionPix();
	if (prefix.isLR(context))
	    return prefix.pos(x, y);
	else
	    return prefix.pos(dimPix.width - x, y);
    }

    // Produce REScode. This requires first rendering.
    public REScode toREScode() {
	getRectangle(); // places notes, etc.
	return prefix.toREScode(context);
    }

    */
}
