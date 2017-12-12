/***************************************************************************/
/*                                                                         */
/*  FormatBox.java                                                         */
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

public class FormatBox extends ResBox implements FormatBasicgroup {

    // Context for rendering.
    private HieroRenderContext context;

    // Cached places of glyphs, according to context.
    public BoxPlaces places;

    // Constructor.
    public FormatBox(ResBox box, HieroRenderContext context) {
	super(box.type,
		box.direction,
		box.mirror,
		box.scale,
		box.color,
		box.shade,
		ResShadeHelper.clone(box.shades),
		box.size,
		box.opensep,
		box.closesep,
		box.undersep,
		box.oversep,
		box.switchs1,
		FormatHieroglyphic.makeHiero(box.hiero, context),
		FormatNote.makeNotes(box.notes, context),
		box.switchs2);
	this.context = context;
	places = context.getBox(type);
    }

    public FormatHieroglyphic fHiero() {
	return (FormatHieroglyphic) hiero;
    }

    public FormatNote fNote(int i) {
        return (FormatNote) note(i);
    }

    //////////////////////////////////////////////////////////////////
    // Effective values.

    private int effectDir() {
	return context.effectDir(dir());
    }

    private boolean effectIsH() {
	return ResValues.isH(effectDir());
    }

    // Is ideal direction horizontal, and effective direction vertical?
    private boolean effectSwapHV() {
	return ResValues.isH(dir()) && !effectIsH();
    }
    private boolean effectSwapVH() {
	return !ResValues.isH(dir()) && effectIsH();
    }

    // Should segments of box be rotated?
    private int effectSegmentRotate() {
	return effectIsH() ? 0 : 90;
    }

    // Rotation for opening or closing of box.
    private int effectOpencloseRotate() {
	if (effectIsH()) 
	    return 0;
	else if (mirror())
	    return 270;
	else
	    return 90;
    }

    // Possibly overridden by context.
    // In case of fitting, always take black.
    private Color effectColor(boolean fitting) {
	if (fitting)
	    return Color.BLACK;
	else
	    return context.effectColor(color()).getColor();
    }

    // Separation between glyphs at box.
    private float effectOpensep() {
	return opensep() * context.fontBoxSep();
    }
    private float effectClosesep() {
	return closesep() * context.fontBoxSep();
    }

    // Place for opening of box.
    private GlyphPlace openPlace() {
	if (effectIsH()) {
	    if (mirror()) 
		return places.close;
	    else
		return places.open;
	} else 
	    return places.open;
    }
    private GlyphPlace closePlace() {
	if (effectIsH()) {
	    if (mirror())
		return places.open;
	    else
		return places.close;
	} else
	    return places.close;
    }

    ////////////////////////////////////////////////////////////
    // Scaling and positioning.

    // Changed during scaling.
    private float dynScale = 1.0f;
    // Measures in pixels, cashed for efficiency.
    // For horizontal direction these mean:
    // dynOpenSize = width of open sign
    // dynOpenFitSep = distance between open sign and hieroglyphic. 
    //     Generally this is negative due to fitting.
    // dynSegmentSize = height of box, measured by means of segment sign.
    // dynUnderThickness = thickness of line underneath box.
    private int dynOpenSize;
    private int dynCloseSize;
    private int dynOpenFitSep;
    private int dynCloseFitSep;
    private int dynSegmentSize;
    private int dynUnderThickness;
    private int dynOverThickness;

    // How much scaled down?
    public float sideScaledLeft() {
	return dynScale;
    }
    public float sideScaledRight() {
	return dynScale;
    }
    public float sideScaledTop() {
	return dynScale;
    }
    public float sideScaledBottom() {
	return dynScale;
    }

    // For doing scaling anew.
    public void resetScaling() {
	dynScale = 1.0f;
	if (fHiero() != null)
	    fHiero().resetScaling();
    }

    // Dimensions in pixels.
    public int width() {
	if (!places.isKnown())
	    return 0;
	else if (effectIsH())
	    return dynOpenSize +
		dynOpenFitSep +
		(fHiero() == null ? 0 : fHiero().width()) +
		dynCloseFitSep +
		dynCloseSize;
	else 
	    return dynSegmentSize;
    }
    public int height() {
	if (!places.isKnown())
	    return 0;
	else if (!effectIsH())
	    return dynOpenSize +
		dynOpenFitSep +
		(fHiero() == null ? 0 : fHiero().height()) +
		dynCloseFitSep +
		dynCloseSize;
	else 
	    return dynSegmentSize;
    }

    // Scale down.
    public void scale(float factor) {
	dynScale *= factor;
	if (!places.isKnown()) {
	    dynOpenSize = dynCloseSize = dynOpenFitSep = dynCloseFitSep = dynSegmentSize = 0;
	    return;
	}
	OptionalGlyphs open = openGlyph(true);
	OptionalGlyphs close = closeGlyph(true);
	OptionalGlyphs segment = segmentGlyph(true);
	Dimension openDim = open.dimension();
	Dimension closeDim = close.dimension();
	Dimension segmentDim = segment.dimension();
	if (effectIsH()) {
	    dynOpenSize = openDim.width;
	    dynCloseSize = closeDim.width;
	    dynSegmentSize = segmentDim.height;
	    dynUnderThickness = segment.bottomEdge();
	    dynOverThickness = segment.topEdge();
	} else {
	    dynOpenSize = openDim.height;
	    dynCloseSize = closeDim.height;
	    dynSegmentSize = segmentDim.width;
	    dynUnderThickness = segment.leftEdge();
	    dynOverThickness = segment.rightEdge();
	}
	int innerDist = dynSegmentSize - dynUnderThickness - dynOverThickness;
	int targetSize = innerDist - 
	    overSepPix(innerDist) - underSepPix(innerDist);
	if (fHiero() != null) {
	    fHiero().scale(factor);
	    for (int i = 0; i < context.maxScalingIterations; i++) {
		int size = (effectIsH() ? fHiero().height() : fHiero().width());
		if (size <= 1 || size <= targetSize)
		    break;
		float newFactor = targetSize * 1.0f / size;
		fHiero().scale(newFactor);
	    }
	    dynOpenFitSep = 0;
	    dynCloseFitSep = 0;
	    if (effectIsH()) {
		Rectangle subRect = subRect(-dynOpenSize, 0);
		BufferedImage imageContent =
		    MovedBuffer.whiteImage(context, subRect.width, dynSegmentSize);
		TransGraphics gImage = new TransGraphics(imageContent, 0, 0, false);
		fHiero().render(gImage, subRect, subRect, new Area(subRect),
			false, true, 0);
		Rectangle rectOpen = new Rectangle(0, 0, dynOpenSize, dynSegmentSize);
		Rectangle rectClose = new Rectangle(0, 0, dynCloseSize, dynSegmentSize);
		BufferedImage imageOpen =
		    MovedBuffer.whiteImage(context, dynOpenSize, dynSegmentSize);
		BufferedImage imageClose = 
		    MovedBuffer.whiteImage(context, dynCloseSize, dynSegmentSize);
		TransGraphics gOpen = new TransGraphics(imageOpen, 0, 0, false);
		TransGraphics gClose = new TransGraphics(imageClose, 0, 0, false);
		gOpen.render(open, 0, 0);
		gClose.render(close, 0, 0);
		dynOpenFitSep = PixelHelper.fitHor(context, imageOpen, imageContent,
			                openSepPix(), dynOpenSize);
		dynCloseFitSep = PixelHelper.fitHor(context, imageContent, imageClose,
			                closeSepPix(), dynCloseSize);
	    } else {
		Rectangle subRect = subRect(0, -dynOpenSize);
		BufferedImage imageContent =
		    MovedBuffer.whiteImage(context, dynSegmentSize, subRect.height);
		TransGraphics gImage = new TransGraphics(imageContent, 0, 0, false);
		fHiero().render(gImage, subRect, subRect, new Area(subRect),
			false, true, 0);
		Rectangle rectOpen = new Rectangle(0, 0, dynSegmentSize, dynOpenSize);
		Rectangle rectClose = new Rectangle(0, 0, dynSegmentSize, dynCloseSize);
		BufferedImage imageOpen =
		    MovedBuffer.whiteImage(context, dynSegmentSize, dynOpenSize);
		BufferedImage imageClose =
		    MovedBuffer.whiteImage(context, dynSegmentSize, dynCloseSize);
		TransGraphics gOpen = new TransGraphics(imageOpen, 0, 0, false);
		TransGraphics gClose = new TransGraphics(imageClose, 0, 0, false);
		gOpen.render(open, 0, 0);
		gClose.render(close, 0, 0);
		dynOpenFitSep = PixelHelper.fitVert(context, imageOpen, imageContent,
			openSepPix(), dynOpenSize);
		dynCloseFitSep = PixelHelper.fitVert(context, imageContent, imageClose,
			closeSepPix(), dynCloseSize);
	    }
	} else {
	    dynOpenFitSep = 0;
	    dynCloseFitSep = 0;
	}
    }

    // Distances inside box. Explicit specifications as box take
    // precedence over global values. Translate to pixels.
    private int openSepPix() {
	return context.emToPix(dynScale * effectOpensep());
    }
    private int closeSepPix() {
	return context.emToPix(dynScale * effectClosesep());
    }
    private int underSepPix(int innerDist) {
	return mapSep(innerDist, undersep());
    }
    private int overSepPix(int innerDist) {
	return mapSep(innerDist, oversep());
    }

    // For practical reasons, the undersep and oversep are given by
    // two linear functions, one for values <= x and one for values >= x,
    // such that:
    // f(x) = x * s, for 0 <= x <= 1,
    // f(10) = 1/2 * d,
    // which implies for x >= 1:
    // f(x) = a * x + b, with
    // a = (1/2 * d - s) / 9
    // b = (10 * s - 1/2 * d) / 9,
    // Here:
    // s is the font-defined separation between glyphs and box,
    // d is the available distance between the inner boundaries of
    // the box.
    // Note that 10 is slightly bigger than the highest real in RES.
    private int mapSep(int d, float x) {
	float s = dynScale * context.emSizePix() * context.fontBoxSep();
	if (x <= 1)
	    return Math.round(x * s);
	else {
	    float a = (0.5f * d - s) / 9.0f;
	    float b = (10.0f * s - 0.5f * d) / 9.0f;
	    return Math.round(a * x + b);
	}
    }

    // Get enclosing glyphs of box. If something wrong, return error glyph.
    // For fitting, make black.
    // We need to establish first whether glyphs need to be mirrored and/or rotated.
    private OptionalGlyphs openGlyph(boolean fitting) {
	GlyphPlace p = openPlace();
	boolean m = mirror();
	int r = effectOpencloseRotate();
	if (!context.canDisplay(p)) 
	    return errorGlyph();
	Color c = effectColor(fitting);
	int s = Math.round(dynScale * scale * context.fontSize());
	return new Glyphs(p, m, r, c, s, context);
    }
    private OptionalGlyphs closeGlyph(boolean fitting) {
	GlyphPlace p = closePlace();
	boolean m = mirror();
	int r = effectOpencloseRotate();
	if (!context.canDisplay(p)) 
	    return errorGlyph();
	Color c = effectColor(fitting);
	int s = Math.round(dynScale * scale * context.fontSize());
	return new Glyphs(p, m, r, c, s, context);
    }
    private OptionalGlyphs segmentGlyph(boolean fitting) {
	if (!context.canDisplay(places.segment))
	    return errorGlyph();
	int r = effectSegmentRotate();
	Color c = effectColor(fitting);
	int s = Math.round(dynScale * scale * context.fontSize());
	return new Glyphs(places.segment, false, r, c, s, context);
    }

    // Get glyph for errors.
    private OptionalGlyphs errorGlyph() {
	return new ErrorGlyphs(Math.round(dynScale * context.emSizePix()), context);
    }

    // To avoid cracks between components of box, we let components overlap a
    // few pixels.
    private static final int overlap = 2;

    // Computed by render(), later to be used by e.g. shade().
    // Where was box placed?
    private Rectangle rect;
    // Where were open and close symbols placed, and where the segments?
    private Rectangle openRect;
    private Rectangle closeRect;
    private Vector segmentRects;
    // For the final segment, the used clip.
    private Rectangle lastSegmentClip;
    // Where was text placed within box?
    private Rectangle subRect;
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

    // Render box within rectangle.
    public void render(UniGraphics image, 
	    Rectangle rect, Rectangle shadeRect, Area clipped, 
	    boolean isClipped, boolean fitting) {
	rect = FormatBasicgroupHelper.placeCentre(rect, this, context);
	this.rect = rect;
	this.subRect = subRect(rect.x, rect.y);
	this.shadeRect = shadeRect;
	if (fHiero() != null) 
	    fHiero().render(image, subRect, subRect, clipped, isClipped, fitting, 0);
	OptionalGlyphs open = openGlyph(fitting);
	OptionalGlyphs close = closeGlyph(fitting);
	OptionalGlyphs segment = segmentGlyph(fitting);
	Dimension openDim = open.dimension();
	Dimension closeDim = close.dimension();
	Dimension segmentDim = segment.dimension();
	segmentRects = new Vector();
	lastSegmentClip = null;
	if (effectIsH()) {
	    openRect = new Rectangle(rect.x, rect.y, openDim.width, openDim.height);
	    closeRect = new Rectangle(rect.x + rect.width - closeDim.width,
		    rect.y, closeDim.width, closeDim.height);
	    if (isClipped) {
		image.render(open, rect.x, rect.y, clipped);
		image.render(close, rect.x + rect.width - closeDim.width, rect.y, clipped);
	    } else {
		image.render(open, rect.x, rect.y);
		image.render(close, rect.x + rect.width - closeDim.width, rect.y);
	    }
	    int incr = Math.max(1, segmentDim.width - overlap);
	    int end = rect.x + rect.width - closeDim.width + overlap;
	    for (int x = rect.x + openDim.width - overlap; x < end; x += incr) {
		segmentRects.add(new Rectangle(x, rect.y, 
			    segmentDim.width, segmentDim.height));
		if (x + segmentDim.width <= end) {
		    if (isClipped) 
			image.render(segment, x, rect.y, clipped);
		    else 
			image.render(segment, x, rect.y);
		} else {
		    lastSegmentClip = new Rectangle(x, rect.y,
			    end - x, segmentDim.height);
		    Area segClipped = new Area(lastSegmentClip);
		    if (isClipped) {
			Area joinClipped = (Area) clipped.clone();
			joinClipped.intersect(segClipped);
			image.render(segment, x, rect.y, joinClipped);
		    } else 
			image.render(segment, x, rect.y, segClipped);
		}
	    }
	} else {
	    openRect = new Rectangle(rect.x, rect.y, openDim.width, openDim.height);
	    closeRect = new Rectangle(rect.x, rect.y + rect.height - closeDim.height,
		    closeDim.width, closeDim.height);
	    if (isClipped) {
		image.render(open, rect.x, rect.y, clipped);
		image.render(close, rect.x, rect.y + rect.height - closeDim.height, clipped);
	    } else {
		image.render(open, rect.x, rect.y);
		image.render(close, rect.x, rect.y + rect.height - closeDim.height);
	    }
	    int incr = Math.max(1, segmentDim.height - overlap);
	    int end = rect.y + rect.height - closeDim.height + overlap;
	    for (int y = rect.y + openDim.height - overlap; y < end; y += incr) {
		segmentRects.add(new Rectangle(rect.x, y, 
			    segmentDim.width, segmentDim.height));
		if (y + segmentDim.height <= end) {
		    if (isClipped) 
			image.render(segment, rect.x, y, clipped);
		    else 
			image.render(segment, rect.x, y);
		} else {
		    lastSegmentClip = new Rectangle(rect.x, y,
			    segmentDim.width, end - y);
		    Area segClipped = new Area(lastSegmentClip);
		    if (isClipped) {
			Area joinClipped = (Area) clipped.clone();
			joinClipped.intersect(segClipped);
			image.render(segment, rect.x, y, joinClipped);
		    } else 
			image.render(segment, rect.x, y, segClipped);
		}
	    }
	}
	if (isColored() && context.lineMode() != ResValues.NO_LINE) 
	    this.lineRect = FormatBasicgroupHelper.lineRect(shadeRect, 
		    effectIsH(), sizeHeader(), context);
	else
	    this.lineRect = null;
    }
    ////////////////////////////////////////////////////////////
    // Notes and shading.

    // Rectangle within box that contains hieroglyphic.
    private Rectangle subRect(int x, int y) {
	if (fHiero() == null)
	    return new Rectangle(x, y, 0, 0);
	int innerDist = dynSegmentSize - dynUnderThickness - dynOverThickness;
	int targetSize = innerDist - 
	    overSepPix(innerDist) - underSepPix(innerDist);
	int contentWidth = fHiero().width();
	int contentHeight = fHiero().height();
	if (effectIsH()) {
	    int surplus = targetSize - contentHeight;
	    int topSurplus = surplus / 2;
	    int contentPos = dynOverThickness + overSepPix(innerDist) + topSurplus;
	    return new Rectangle(
		    x + dynOpenSize + dynOpenFitSep, 
		    y + contentPos, 
		    contentWidth, contentHeight);
	} else {
	    int surplus = targetSize - contentWidth;
	    int underSurplus = surplus / 2;
	    int contentPos = dynUnderThickness + underSepPix(innerDist) + underSurplus;
	    return new Rectangle(
		    x + contentPos, 
		    y + dynOpenSize + dynOpenFitSep, 
		    contentWidth, contentHeight);
	}
    }

    // Place footnotes.
    public void placeNotes(FlexGraphics im, 
	    boolean under, boolean over) {
	FormatNote.place(rect, notes, someShade(),
		under, over, effectIsH(), im, context);
	if (fHiero() != null)
	    fHiero().placeNotes(im, true, true);
    }

    // Render footnotes.
    public void renderNotes(UniGraphics im) {
	for (int i = 0; i < nNotes(); i++)
	    fNote(i).render(im, rect.x, rect.y);
	if (fHiero() != null)
	    fHiero().renderNotes(im);
    }

    // Make shading. If there is hieroglyphic, exclude that are from shading for
    // box. Also underline/overline.
    public void shade(UniGraphics image) { 
	if (fHiero() != null) 
	    fHiero().shade(image);
	if (shade()) 
	    shadeOmit(image, shadeRect, subRect);
	else 
	    for (int i = 0; i < nShades(); i++) {
		String s = shade(i);
		if (effectSwapHV())
		    s = FormatShadeHelper.turnPatternHV(s);
		else if (effectSwapVH())
		    s = FormatShadeHelper.turnPatternVH(s);
		Rectangle part = FormatShadeHelper.chopRectangle(shadeRect, s);
		shadeOmit(image, part, subRect);
	    }
	if (lineRect != null)
	    image.fillRect(context.lineColor(), lineRect);
    }

    // Shade rectangle, but omit other rectangle.
    // This leads to up to 8 smaller rectangles.
    private void shadeOmit(UniGraphics image, 
	    Rectangle rect, Rectangle omit) {
	Rectangle[] rects = omit(rect, omit);
	for (int i = 0; i < rects.length; i++) 
	    image.shade(rects[i], context);
    }

    // Omit rectangle from bigger rectangle. 
    // This leads to array of (up to) 8 rectangles.
    private static Rectangle[] omit(Rectangle rect, Rectangle omit) {
	if (omit.width <= 0 || omit.height <= 0)
	    return new Rectangle[0];
	int xMin = rect.x;
	int xMax = rect.x + rect.width;
	int yMin = rect.y;
	int yMax = rect.y + rect.height;
	int xMinOmit = omit.x;
	int xMaxOmit = omit.x + omit.width;
	int yMinOmit = omit.y;
	int yMaxOmit = omit.y + omit.height;
	Rectangle[] fragments = new Rectangle[8];
	fragments[0] = new Rectangle(
		xMin, yMin, 
		Math.min(xMax, xMinOmit) - xMin, 
		Math.min(yMax, yMinOmit) - yMin);
	fragments[1] = new Rectangle(
		xMin, Math.max(yMin, yMinOmit),
		Math.min(xMax, xMinOmit) - xMin, 
	       	Math.min(yMax, yMaxOmit) - Math.max(yMin, yMinOmit));
	fragments[2] = new Rectangle(
		xMin, Math.max(yMin, yMaxOmit),
		Math.min(xMax, xMinOmit) - xMin,
	       	yMax - Math.max(yMin, yMaxOmit));
	fragments[3] = new Rectangle(
		Math.max(xMin, xMinOmit), yMin, 
	       	Math.min(xMax, xMaxOmit) - Math.max(xMin, xMinOmit),
		Math.min(yMax, yMinOmit) - yMin);
	fragments[4] = new Rectangle(
		Math.max(xMin, xMinOmit), Math.max(yMin, yMaxOmit),
	       	Math.min(xMax, xMaxOmit) - Math.max(xMin, xMinOmit),
		yMax - Math.max(yMin, yMaxOmit));
	fragments[5] = new Rectangle(
		Math.max(xMin, xMaxOmit), yMin,
	       	xMax - Math.max(xMin, xMaxOmit),
	       	Math.min(yMax, yMinOmit) - yMin);
	fragments[6] = new Rectangle(
		Math.max(xMin, xMaxOmit), Math.max(yMin, yMinOmit),
	       	xMax - Math.max(xMin, xMaxOmit),
	       	Math.min(yMax, yMaxOmit) - Math.max(yMin, yMinOmit));
	fragments[7] = new Rectangle(
		Math.max(xMin, xMaxOmit), Math.max(yMin, yMaxOmit), 
		xMax - Math.max(xMin, xMaxOmit),
		yMax - Math.max(yMin, yMaxOmit));
	return fragments;
    }

    ////////////////////////////////////////////////////////////
    // RESlite.

    // Produce RESlite.
    public void toResLite(int x, int y, 
	    Vector exprs, Vector notesList, Vector shadesList,
	    Rectangle clip) {
	if (fHiero() != null) 
	    fHiero().toResLite(x, y, exprs, notesList, shadesList, clip);
	exprs.add(FormatBasicgroupHelper.toResLite(x, y, openRect, 
		    openPlace(), mirror(), effectOpencloseRotate(),
		    color(), dynScale * scale, 
		    clip, context));
	for (int i = 0; i < segmentRects.size(); i++) {
	    Rectangle segmentRect = (Rectangle) segmentRects.get(i);
	    int r = effectSegmentRotate();
	    Rectangle segmentClip = clip;
	    if (i == segmentRects.size() - 1 && lastSegmentClip != null)
		segmentClip = clip.intersection(lastSegmentClip);
	    exprs.add(FormatBasicgroupHelper.toResLite(x, y, segmentRect, 
			places.segment, false, r,
			color(), dynScale * scale, 
			segmentClip, context));
	}
	exprs.add(FormatBasicgroupHelper.toResLite(x, y, closeRect, 
		    closePlace(), mirror(), effectOpencloseRotate(),
		    color(), dynScale * scale, 
		    clip, context));
	for (int i = 0; i < nNotes(); i++)
	    fNote(i).toResLite(x, y, rect.x, rect.y, notesList);
	if (shade())
	    toResLite(x, y, shadesList, shadeRect);
	else
	    for (int i = 0; i < nShades(); i++) {
		String s = shade(i);
		if (effectSwapHV())
		    s = FormatShadeHelper.turnPatternHV(s);
		else if (effectSwapVH())
		    s = FormatShadeHelper.turnPatternVH(s);
		Rectangle part = FormatShadeHelper.chopRectangle(shadeRect, s);
		toResLite(x, y, shadesList, part);
	    }
    }

    // Auxiliary to the above, omitting inner rectangle from shaded area.
    private void toResLite(int x, int y, Vector shadesList,
	    Rectangle shadeRect) {
	Rectangle[] rects = omit(shadeRect, subRect);
	for (int i = 0; i < rects.length; i++)
	    FormatShadeHelper.shadeResLite(x, y, shadesList, context, rects[i]);
    }
}
