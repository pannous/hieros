/***************************************************************************/
/*																		 */
/*  FormatFragment.java													*/
/*																		 */
/*  Copyright (c) 2009 Mark-Jan Nederhof								   */
/*																		 */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the				 */
/*  GNU General Public License (see doc/GPL.TXT).						  */
/*  By continuing to use, modify, or distribute this file you indicate	 */
/*  that you have read the license and understand and accept it fully.	 */
/*																		 */
/***************************************************************************/

// A fragment of RES.

package nederhof.res.format;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import nederhof.res.*;

public class FormatFragment extends ResFragment {

	// Context for rendering.
	private HieroRenderContext context;

	// Specification of direction is suppressed for suffixes.
	private boolean isSuffix = false;

	// Padding.
	private int pad = 0;

	// Constructor.
	public FormatFragment(ResFragment frag, HieroRenderContext context) {
		this(frag, context, false, 0);
	}

	// Constructor for suffix (or not), suppressing specification of direction.
	public FormatFragment(ResFragment frag, HieroRenderContext context,
			boolean isSuffix) {
		this(frag, context, isSuffix, 0);
	}

	// As above, but do padding.
	public FormatFragment(ResFragment frag, HieroRenderContext context,
			int pad) {
		this(frag, context, false, pad);
	}

	// General constructor.
	public FormatFragment(ResFragment frag, HieroRenderContext context,
			boolean isSuffix, int pad) {
		super(frag.direction,
				frag.size,
				frag.switchs,
				FormatHieroglyphic.makeHiero(frag.hiero, context));
		this.context = context;
		this.isSuffix = isSuffix;
		this.pad = pad;
		propagate();
		scale();
		computeRectangle();
	}

	public FormatHieroglyphic fHiero() {
		return (FormatHieroglyphic) hiero;
	}

	//////////////////////////////////////////////////////////////////
	// Effective values.

	public int effectDir() {
		return context.effectDir(dir());
	}

	// Is forced direction horizontal?
	public boolean effectIsH() {
		return ResValues.isH(effectDir());
	}

	// Is forced direction left-to-right?
	public boolean effectIsLR() {
		return ResValues.isLR(effectDir());
	}

	private float effectSize() {
		return context.effectSize(size());
	}

	// Should include specification of direction?
	private boolean effectSpec() {
		return context.directionSpec() && !isSuffix;
	}
	////////////////////////////////////////////////////////////
	// Scaling and positioning.

	// Dimensions in pixels.
	public int width() {
		return width(Integer.MAX_VALUE);
	}
	public int height() {
		return height(Integer.MAX_VALUE);
	}

	// Dimensions for first n groups.
	public int width(int nGroups) {
		int wPad = effectIsH() ? pad : 0;
		if (fHiero() == null) {
			if (effectIsH())
				return wPad;
			else
				return context.emToPix(effectSize()) + wPad;
		}
		if (effectSpec() && effectIsH()) {
			OptionalGlyphs spec = specGlyph();
			Dimension specDim = spec.dimension();
			return specDim.width + context.specDistPix() + 
				fHiero().width(nGroups) + wPad;
		} else
			return fHiero().width(nGroups) + wPad;
	}
	public int height(int nGroups) {
		int vPad = effectIsH() ? 0 : pad;
		if (fHiero() == null) {
			if (effectIsH())
				return context.emToPix(effectSize()) + vPad;
			else
				return vPad;
		}
		if (effectSpec() & !effectIsH()) {
			OptionalGlyphs spec = specGlyph();
			Dimension specDim = spec.dimension();
			return specDim.height + context.specDistPix() + 
				fHiero().height(nGroups) + vPad;
		} else
			return fHiero().height(nGroups) + vPad;
	}

	// Width of groups from i to j.
	public int width(int i, int j) {
		return fHiero() == null ? 0 : fHiero().width(i, j);
	}

	// Distance from group to beginning of group.
	public int widthDist(int i, int j) {
		if (i == j) 
			return 0;
		else
			return fHiero().widthDist(i, j);
	}

	// Computing scaling for RES fragment.
	public void scale() {
		if (fHiero() != null) {
			fHiero().resetScaling();
			fHiero().scale(1);
		}
	}

	// Number of groups that fit within length restriction on number of
	// pixels.
	public int boundedNGroups(int limit) {
		if (effectSpec()) {
			OptionalGlyphs spec = specGlyph();
			Dimension specDim = spec.dimension();
			if (effectIsH())
				limit -= specDim.width;
			else
				limit -= specDim.height;
			limit -= context.specDistPix();
		}
		if (fHiero() == null)
			return 0;
		else
			return fHiero().boundedNGroups(limit);
	}

	// Glyph that indicates direction. If something wrong, return error glyph.
	private OptionalGlyphs specGlyph() {
		GlyphPlace place = context.getSpec(dir());
		if (context.canDisplay(place))
			return new Glyphs(place, false, 0, 
					context.specColor().getColor(), context.fontSize(), context);
		else
			return new ErrorGlyphs(context.emSizePix(), context);
	}

	// The position of the spec symbol if any.
	private Rectangle specRect;

	// Get rectangle of n-th top group or '-' operator.
	public Rectangle groupRectangle(int pos) {
		if (fHiero() != null)
			return fHiero().rectangle(pos);
		else
			return new Rectangle();
	}

	// Get list of Rectangles for consecutive glyphs with 
	// Gardiner names.
	public Vector<Rectangle> glyphRectangles() {
		Vector<Rectangle> list = new Vector(50, 50);
		Vector<ResNamedglyph> glyphs = glyphs();
		for (int i = 0; i < glyphs.size(); i++) {
			FormatNamedglyph named = (FormatNamedglyph) glyphs.get(i);
			list.add(named.rectangle());
		}
		return list;
	}

	// Get number of top or '-' operator containing point.
	// If there is no such thing, then return -1.
	public int pos(int x, int y) {
		if (fHiero() != null) {
			if (effectIsLR())
				return fHiero().pos(x, y);
			else
				return fHiero().pos(width() - x, y);
		} else
			return -1;
	}

	////////////////////////////////////////////////////////////
	// Placement on panel.

	// Cached dimensions.
	private int width;
	private int height;
	private Dimension dimension;
	private Rectangle nominalRect;
	private Rectangle rectNoNotes;

	// Chache dimensions.
	private void computeDimensions() {
		width = width();
		height = height();
		dimension = new Dimension(width, height);
		nominalRect = new Rectangle(0, 0, width, height);
	}

	// Area that is big enough in all normal circumstances
	// to contain all pixels. Is 5 EM around the virtual box.
	// Is used as initial clip.
	private Area bigEnoughArea() {
		Rectangle rect = new Rectangle(
				-5 * context.emSizePix(),
				-5 * context.emSizePix(),
				10 * context.emSizePix() + width,
				10 * context.emSizePix() + height);
		return new Area(rect);
	}

	// Rectangle around hieroglyphic.
	private Rectangle rectangle;

	// Compute the rectangle.
	private void computeRectangle() {
		computeDimensions();
		ResizeGraphics graphicsNoNotes = 
			new ResizeGraphics(dimension, context, effectIsLR());
		render(graphicsNoNotes, nominalRect, bigEnoughArea(), pad); 
		shade(graphicsNoNotes);
		rectNoNotes = graphicsNoNotes.getRect();
		FlexGraphics graphics = new FlexGraphics(context,
				rectNoNotes.width, rectNoNotes.height, 
				-rectNoNotes.x, -rectNoNotes.y,
				width, height, !effectIsLR());
		render(graphics, nominalRect, new Area(rectNoNotes), pad); 
		placeNotes(graphics);
		rectangle = graphics.getRect();
	}

	// Get margins around image.
	public Insets margins() {
		int left;
		int right;
		if (effectIsLR()) {
			left = -rectangle.x;
			right = rectangle.width - width - left;
		} else {
			right = -rectangle.x;
			left = rectangle.width - width - right;
		}
		int top = -rectangle.y;
		int bottom = rectangle.height - height - top;
		return new Insets(top, left, bottom, right);
	}

	// Write into graphics at coordinate.
	public void write(Graphics2D gUnscaled, int x, int y) {
		Graphics2D g = (Graphics2D) gUnscaled.create();
		g.scale(1.0 / context.resolution(), 1.0 / context.resolution());
		TransGraphics graphics = new TransGraphics(g, x, y, width, !effectIsLR());
		render(graphics, nominalRect, bigEnoughArea(), pad); 
		shade(graphics);
		renderNotes(graphics);
	}

	// Write a substring of groups.
	public void write(Graphics2D gUnscaled, int group1, int group2, int x, int y) {
		Graphics2D g = (Graphics2D) gUnscaled.create();
		g.scale(1.0 / context.resolution(), 1.0 / context.resolution());
		TransGraphics graphics = new TransGraphics(g, x, y, width, !effectIsLR());
		render(graphics, group1, group2, nominalRect, bigEnoughArea(), pad); 
		shade(graphics, group1, group2);
		renderNotes(graphics, group1, group2);
	}

	// Write into graphics, with indicated margin around pixels.
	public void writeFactual(Graphics2D g, int margin) {
		Insets insets = margins();
		write(g, margin + insets.left, margin + insets.top);
	}

	// Write into graphics, with indicated margin around virtual bounding box.
	public void writeVirtual(Graphics2D g, int margin) {
		write(g, margin, margin);
	}

	// Turn into image.
	public MovedBuffer toImage() {
		FlexGraphics graphics = new FlexGraphics(context,
				rectNoNotes.width, rectNoNotes.height, 
				-rectNoNotes.x, -rectNoNotes.y,
				width, height, !effectIsLR());
		render(graphics, nominalRect, 
					new Area(rectNoNotes), pad); 
		shade(graphics);
		placeNotes(graphics);
		return graphics.toImage();
	}

	////////////////////////////////////////////////////////////
	// Render.

	// Write groups to graphics.
	public void render(UniGraphics image, 
			Rectangle rect, Area clipped, int padding) {
		render(image, 0, nGroups(), rect, clipped, padding);
	}

	// Write only groups between bounds.
	public void render(UniGraphics image, int group1, int group2,
			Rectangle rect, Area clipped, int padding) {
		specRect = null;
		if (fHiero() == null) 
			return;
		if (effectSpec() && group1 == 0) {
			OptionalGlyphs spec = specGlyph();
			Dimension specDim = spec.dimension();
			if (effectIsH()) {
				int height = context.emToPix(effectSize());
				int top = (height - specDim.height) / 2;
				int x = rect.x + specDim.width + context.specDistPix();
				specRect = new Rectangle(rect.x, rect.y + top, 
						specDim.width, specDim.height);
				image.render(spec, rect.x, rect.y + top);
				rect = FormatShadeHelper.chopEndH(rect, x);
			} else {
				int width = context.emToPix(effectSize());
				int left = (width - specDim.width) / 2;
				int y = rect.y + specDim.height + context.specDistPix();
				specRect = new Rectangle(rect.x + left, rect.y,
						specDim.width, specDim.height);
				image.render(spec, rect.x + left, rect.y);
				rect = FormatShadeHelper.chopEndV(rect, y);
			}
		}
		fHiero().render(image, group1, group2, 
				rect, rect, clipped, false, false, padding);
	}

	////////////////////////////////////////////////////////////
	// Notes and shading.

	// Place footnotes.
	public void placeNotes(FlexGraphics image) {
		if (fHiero() != null)
			fHiero().placeNotes(image, false, false);
	}

	// Render footnotes.
	public void renderNotes(UniGraphics image) {
		if (fHiero() != null)
			fHiero().renderNotes(image);
	}
	// Only for substring of groups.
	public void renderNotes(UniGraphics image, int group1, int group2) {
		if (fHiero() != null)
			fHiero().renderNotes(image, group1, group2);
	}

	// Write shading to graphics.
	public void shade(UniGraphics image) {
		if (fHiero() != null)
			fHiero().shade(image);
	}
	// Only for substring of groups.
	public void shade(UniGraphics image, int group1, int group2) {
		if (fHiero() != null)
			fHiero().shade(image, group1, group2);
	}

	///////////////////////////////////////////////////////////////////////////
	// RESlite.

	// Should only be called after scaling and placing notes
	// has been done.
	public REScode toResLite() {
		int dir = context.effectDir(dir());
		int sizePix = context.emToPix(effectSize());
		int sizeMilEm = context.pixToMilEm(sizePix);
		REScode code = new REScode(dir, sizeMilEm);
		if (fHiero() != null) {
			REScodeGlyph expr = null;
			int length = 0;
			if (effectSpec()) {
				GlyphPlace place = context.getSpec(dir());
				expr = FormatBasicgroupHelper.toResLite(0, 0, specRect,
						place, false, 0, context.specColor(), 1.0f, 
						specRect, context);
				OptionalGlyphs spec = specGlyph();
				Dimension specDim = spec.dimension();
				if (effectIsH())
					length = specDim.width + context.specDistPix();
				else
					length = specDim.height + context.specDistPix();
			}
			code.groups = fHiero().toResLite(expr, length);
		}
		return code;
	}

	//////////////////////////////////////////////////////////////////////////
	// For RES or RESlite.

	// Turn into division.
	public RESorREScodeDivision createRESorREScodeDivision() {
		// TODO
		// return new ResDivision(this, context);
		return null;
	}
}
