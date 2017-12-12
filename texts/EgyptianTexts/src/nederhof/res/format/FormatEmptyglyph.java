/***************************************************************************/
/*                                                                         */
/*  FormatEmptyglyph.java                                                  */
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
import java.util.*;

import nederhof.res.*;

public class FormatEmptyglyph extends ResEmptyglyph implements FormatBasicgroup {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatEmptyglyph(ResEmptyglyph empty, HieroRenderContext context) {
	super(empty.width,
		empty.height,
		empty.shade,
		ResShadeHelper.clone(empty.shades),
		empty.firm,
		FormatNote.makeNote(empty.note, context),
		empty.switchs);
	this.context = context;
    }

    public FormatNote fNote() {
        return (FormatNote) note;
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

    // How much scaled down, from the viewpoint of
    // different sides?
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

    // Dimensions in pixels.
    public int width() {
	return context.emToPix(dynScale * width);
    }
    public int height() {
	return context.emToPix(dynScale * height);
    }

    // Scale down.
    public void scale(float factor) {
	dynScale *= factor;
    }

    // Computed by render(), later to be used by e.g. shade().
    // Where was empty glyph placed?
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

    // Nothing to be done for rendering empty glyph, except in fitting mode
    // and fitting, then black box, at least three pixels big.
    public void render(UniGraphics image, 
	    Rectangle rect, Rectangle shadeRect, Area clipped, 
	    boolean isClipped, boolean fitting) {
	rect = FormatBasicgroupHelper.placeCentre(rect, this, context);
	this.rect = rect;
	this.shadeRect = shadeRect;
	if (fitting && firm) {
	    Rectangle box = new Rectangle(rect);
	    if (box.width <= 0) {
		box.x -= 1;
		box.width = 3;
	    }
	    if (box.height <= 0) {
		box.y -= 1;
		box.height = 3;
	    }
	    image.fillRect(Color.BLACK, box);
	}
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
	if (fNote() != null)
	    fNote().place(rect, someShade(), under, over, im);
    }

    // Render footnotes.
    public void renderNotes(UniGraphics im) {
	if (fNote() != null)
	    fNote().render(im, rect.x, rect.y);
    }

    // Make shading. Also underline/overline.
    public void shade(UniGraphics image) {
	FormatShadeHelper.shade(image, context, shadeRect, shade(), shades);
	if (lineRect != null)
	    image.fillRect(context.lineColor(), lineRect);
    }

    ////////////////////////////////////////////////////////////
    // RESlite.

    // Produce RESlite.
    public void toResLite(int x, int y,
	    Vector exprs, Vector notes, Vector shadesList,
	    Rectangle clip) {
	if (fNote() != null)
	    fNote().toResLite(x, y, rect.x, rect.y, notes);
	FormatShadeHelper.shadeResLite(x, y, shadesList, context, 
		shadeRect, shade(), shades);
    }

}
