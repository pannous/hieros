/***************************************************************************/
/*                                                                         */
/*  REScodeDivision.java                                                   */
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

// Divide a piece of REScode into an initial part and a remainder.
// The initial part should fit within given distance.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class REScodeDivision implements RESorREScodeDivision {

    // Complete REScode as input to constructor.
    private REScode original;

    // Context of rendering.
    private HieroRenderContext context;

    // The number of groups in the prefix that fit within limit.
    private int initialNumber;

    // Size of initial prefix, in 1000 * EM.
    private int initialSize;

    // Allowable padding inside prefix, in 1000 * EM. 
    private int pad = 0;

    // The remaining REScode that does not fit within limit.
    private REScode remainder;

    // Process complete input.
    public REScodeDivision(REScode code, HieroRenderContext context) {
	original = code;
	this.context = context;
	computeAll();
    }

    // Process input until len limit is reached.
    // len is in pt, convert to 1000 * EM. 
    // len can be infinite (= no limit).
    // Is padding allowed?
    public REScodeDivision(REScode code, float len, 
	    HieroRenderContext context, boolean paddingAllowed) {
	original = code;
	this.context = context;
	int lenMilEm;
	if (len == Float.MAX_VALUE)
	    lenMilEm = Integer.MAX_VALUE;
	else
	    lenMilEm = Math.round(1000.0f * len / context.emSizePt());
	computeLengthLimit(lenMilEm);
	if (paddingAllowed && lenMilEm != Integer.MAX_VALUE) {
	    pad = lenMilEm - initialSize;
	    if (pad > summedSep() * context.padding())
		pad = 0;
	}
    }

    // As above, but specify width and height.
    public REScodeDivision(REScode code, float width, float height,
	    HieroRenderContext context, boolean paddingAllowed) {
	this(code, toSize(code, width, height, context), context, paddingAllowed);
    }

    // Process the first nGroups of the code.
    public REScodeDivision(REScode code, int nGroups, HieroRenderContext context) {
	original = code;
	this.context = context;
	computeFirst(nGroups);
    }

    // Translate allowable width and height to size.
    private static float toSize(REScode code, float width, float height, 
	    HieroRenderContext context) {
	if (HieroRenderContext.isH(code.dir)) {
	    if (context.milEmToPt(code.size) <= height)
		return width;
	    else 
		return 0;
	} else {
	    if (context.milEmToPt(code.size) <= width)
		return height;
	    else
		return 0;
	}
    }

    // Process all groups.
    private void computeAll() {
	REScodeGroups groups = original.groups;
	initialNumber = 0;
	initialSize = 0;
	int start = 0;
	boolean isFirst = true;
	while (groups != null) {
	    int advance = (isFirst ? 0 : groups.advance);
	    start += advance;
	    initialNumber++;
	    initialSize = start + groups.length;
	    groups = groups.tl;
	    isFirst = false;
	}
	remainder = new REScode(original.dir, original.size, groups);
    }

    // Process until limit. Store remainder.
    // Express distances in 1000 EM, to minimize rounding-off errors.
    private void computeLengthLimit(int max) {
	REScodeGroups groups = original.groups;
	initialNumber = 0;
	initialSize = 0;
	int start = 0;
	boolean isFirst = true;
	while (groups != null) {
	    int advance = (isFirst ? 0 : groups.advance);
	    if (start + advance + groups.length <= max) {
		start += advance;
		initialNumber++;
		initialSize = start + groups.length;
		groups = groups.tl;
		isFirst = false;
	    } else 
		break;
	}
	remainder = new REScode(original.dir, original.size, groups);
    }

    // Process number of groups.
    private void computeFirst(int nGroups) {
	REScodeGroups groups = original.groups;
	initialNumber = 0;
	initialSize = 0;
	int start = 0;
	boolean isFirst = true;
	for (int i = 0; i < nGroups && groups != null; i++) {
	    int advance = (isFirst ? 0 : groups.advance);
	    start += advance;
	    initialNumber++;
	    initialSize = start + groups.length;
	    groups = groups.tl;
	    isFirst = false;
	}
	remainder = new REScode(original.dir, original.size, groups);
    }

    // Number of groups in prefix.
    public int getInitialNumber() {
	return initialNumber;
    }

    // Conceptual width, expressed in 1000 * EM.
    private int getWidth() {
	if (isH())
	    return initialSize + pad;
	else
	    return original.size;
    }
    private int getHeight() {
	if (isH())
	    return original.size;
	else
	    return initialSize + pad;
    }
    // Get dimensions of image, expressed in 1000 * EM.
    private Dimension getDimension() {
	return new Dimension(getWidth(), getHeight());
    }

    // Conceptual width in pixels.
    public int getWidthPixels() {
	return context.milEmToPix(getWidth());
    }
    public int getHeightPixels() {
	return context.milEmToPix(getHeight());
    }
    // Get dimensions of image in pixels.
    private Dimension getDimensionPix() {
	return new Dimension(getWidthPixels(), getHeightPixels());
    }

    // Conceptual width in points.
    public float getWidthPt() {
	return getWidthPixels() / context.resolution();
    }
    public float getHeightPt() {
	return getHeightPixels() / context.resolution();
    }

    // Code that is not included.
    public REScode getRemainder() {
	return remainder;
    }

    ////////////////////////////////////////////////////////////////////////
    // Turning prefix into graphical format.

    // Area that is big enough in all normal circumstances 
    // to contain all pixels. Is 5 EM around the virtual box.
    private Area bigEnoughArea() {
	Dimension dimPix = getDimensionPix();
	Rectangle rect = new Rectangle(
		-5 * context.emSizePix(),
		-5 * context.emSizePix(),
		10 * context.emSizePix() + dimPix.width,
		10 * context.emSizePix() + dimPix.height);
	return new Area(rect);
    }

    // Get rectangle around all pixels.
    private Rectangle getRectangle() {
	if (original.groups == null)
	    return new Rectangle(0, 0, 1, 1);
	else {
	    Dimension dim = getDimension();
	    Dimension dimPix = getDimensionPix();
	    ResizeGraphics graphics = new ResizeGraphics(dimPix, context,
		    context.isEffectLRREScode(original.dir));
	    original.groups.render(graphics, bigEnoughArea(), dim.width, dim.height,
		    original.dir, context, initialNumber, pad);
	    return graphics.getRect();
	}
    }

    // Get margins around image, in pixels.
    public Insets margins() {
	if (original.groups == null)
	    return new Insets(0, 0, 0, 0);
	else {
	    Dimension dimPix = getDimensionPix();
	    Rectangle pixSize = getRectangle();
	    int left;
	    int right;
	    if (isEffectLR()) {
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
    }

    // Turn prefix into image.
    public MovedBuffer toImage() {
	Dimension dim = getDimension();
	Dimension dimPix = getDimensionPix();
	Rectangle pixSize = getRectangle();
	BufferedImage image = MovedBuffer.whiteImage(context,
		pixSize.width, pixSize.height);
	TransGraphics graphics = new TransGraphics(image,
		-pixSize.x, -pixSize.y, !isEffectLR());
	if (original.groups != null)
	    original.groups.render(graphics, new Area(pixSize), dim.width, dim.height, 
		    original.dir, context, initialNumber, pad);
	MovedBuffer buf = new MovedBuffer(image, 
		-pixSize.x, -pixSize.y, dimPix.width, dimPix.height, !isEffectLR());
	return buf;
    }

    // Write prefix into graphics, at indicated position.
    public void write(Graphics2D gUnscaled, int x, int y) {
	if (original.groups == null) 
	    return;
	Graphics2D g = (Graphics2D) gUnscaled.create();
	g.scale(1.0 / context.resolution(), 1.0 / context.resolution());
	Dimension dim = getDimension();
	TransGraphics graphics = new TransGraphics(g, x, y, getWidthPixels(),
		!isEffectLR());
	original.groups.render(graphics, bigEnoughArea(), dim.width, dim.height,
		original.dir, context, initialNumber, pad);
    }

    // Summed separation between groups, in 1000 * EM.
    private int summedSep() {
	if (initialNumber <= 1 || original.groups == null)
	    return 0;
	else 
	    return original.groups.sepSum(initialNumber);
    }

    // Horizontal direction?
    private boolean isH() {
	return HieroRenderContext.isH(original.dir);
    }

    // Is effectively left-to-right, taking into account forced direction?
    private boolean isEffectLR() {
	return HieroRenderContext.isLR(context.effectDirREScode(original.dir));
    }
}
