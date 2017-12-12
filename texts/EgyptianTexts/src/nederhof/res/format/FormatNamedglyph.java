/***************************************************************************/
/*																		 */
/*  FormatNamedglyph.java												  */
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

package nederhof.res.format;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import nederhof.res.*;

public class FormatNamedglyph extends ResNamedglyph implements FormatBasicgroup {

	// Context for rendering.
	private HieroRenderContext context;

	// Cached place of glyph, according to context.
	public GlyphPlace place = new GlyphPlace();

	// Constructor.
	public FormatNamedglyph(ResNamedglyph glyph, HieroRenderContext context) {
		super(glyph.name,
				glyph.mirror,
				glyph.rotate,
				glyph.scale,
				glyph.xscale,
				glyph.yscale,
				glyph.color,
				glyph.shade,
				ResShadeHelper.clone(glyph.shades),
				FormatNote.makeNotes(glyph.notes, context),
				glyph.switchs);
		this.context = context;
		place = context.getGlyph(name);
	}

	public FormatNote fNote(int i) {
		return (FormatNote) note(i);
	}

	//////////////////////////////////////////////////////////////////
	// Effective values.

	private int effectDirHeader() {
		return context.effectDir(dirHeader());
	}

	private boolean effectIsH() {
		return ResValues.isH(effectDirHeader());
	}

	// Color. If no indication at glyph, take global value.
	// Possibly overridden by context.
	// In case of fitting, make black.
	private Color effectColor(boolean fitting) {
		if (fitting)
			return Color.BLACK;
		else 
			return context.effectColor(color()).getColor();
	}

	////////////////////////////////////////////////////////////
	// Scaling and positioning.

	// Changed during scaling.
	private float dynScale = 1.0f;

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
	}

	// For named glyphs that are in Latin font, we need to
	// increase the size so that the height equals 1 EM of 
	// the hieroglyphic font.
	private float dynScale() {
		if (name.charAt(0) == '\"')
			return dynScale * context.pixelLatinFontSizeIncrease();
		else
			return dynScale;
	}

	// Measured in pixels, cashed for efficiency.
	private int dynWidth = 0;
	private int dynHeight = 0;

	// Dimensions in pixels.
	public int width() {
		return dynWidth;
	}
	public int height() {
		return dynHeight;
	}

	// Scale down.
	public void scale(float factor) {
		dynScale *= factor;
		if (!place.isKnown()) {
			dynWidth = dynHeight = 0;
			return;
		} 
		OptionalGlyphs glyph = glyph(true);
		Dimension dim = glyph.dimension();
		dynWidth = dim.width;
		dynHeight = dim.height;
	}

	// Get glyph. If something wrong, return error glyph.
	// For fitting, make black.
	private OptionalGlyphs glyph(boolean fitting) {
		Color c = effectColor(fitting);
		int s = Math.round(dynScale() * scale * context.fontSize());
		if (!context.canDisplay(place))
			return new ErrorGlyphs(Math.round(dynScale * context.emSizePix()), 
					context);
		else 
			return new Glyphs(place, mirror(), rotate, c, s, 
					xscale, yscale, context);
	}

	// Computed by render(), later to be used by e.g. shade().
	// Where was glyph placed?
	private Rectangle rect;
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

	// Render glyph within rectangle.
	public void render(UniGraphics image, 
			Rectangle rect, Rectangle shadeRect, Area clipped, 
			boolean isClipped, boolean fitting) {
		rect = FormatBasicgroupHelper.placeCentre(rect, this, context);
		this.rect = rect;
		this.shadeRect = shadeRect;
		OptionalGlyphs glyph = glyph(fitting);
		if (isClipped)
			image.render(glyph, rect.x, rect.y, clipped);
		else
			image.render(glyph, rect.x, rect.y);
		if (isColored() && context.lineMode() != ResValues.NO_LINE) 
			this.lineRect = FormatBasicgroupHelper.lineRect(shadeRect, 
					effectIsH(), sizeHeader(), context);
		else 
			this.lineRect = null;
	}

	////////////////////////////////////////////////////////////
	// Notes and shading.

	// Place footnotes.
	public void placeNotes(FlexGraphics im, boolean under, boolean over) {
		FormatNote.place(rect, notes, someShade(),
				under, over, effectIsH(), im, context);
	}

	// Render footnotes.
	public void renderNotes(UniGraphics im) {
		for (int i = 0; i < nNotes(); i++)
			fNote(i).render(im, rect.x, rect.y);
	}

	// Make shading. Also underline/overline.
	public void shade(UniGraphics image) {
		FormatShadeHelper.shade(image, context, shadeRect, shade(), shades);
		if (lineRect != null)
			image.fillRect(context.lineColor(), lineRect);
	}

	//////////////////////////////////////////////////////////////////////////////
	// RESlite.

	// Produce RESlite.
	public void toResLite(int x, int y,
			Vector exprs, Vector notesList, Vector shadesList,
			Rectangle clip) {
		exprs.add(FormatBasicgroupHelper.toResLite(x, y, 
					rect, place, mirror(), rotate,
					color(), dynScale() * scale, 
					xscale, yscale, clip, context));
		for (int i = 0; i < nNotes(); i++)
			fNote(i).toResLite(x, y, rect.x, rect.y, notesList);
		FormatShadeHelper.shadeResLite(x, y, shadesList, context, 
				shadeRect, shade(), shades);
	}

}
