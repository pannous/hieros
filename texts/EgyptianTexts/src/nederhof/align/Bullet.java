/***************************************************************************/
/*                                                                         */
/*  Bullet.java                                                            */
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

// A bullet in the preamble at the beginning of an item.

package nederhof.align;

import java.awt.*;
import javax.swing.*;

class Bullet extends Elem {

    public Bullet(int type) {
	super(type);
    }

    // See Elem.
    public boolean isPrintable() {
	return true;
    }

    // See Elem.
    public boolean isContent() {
	return false;
    }

    // The space left for bullet, and place and size of bullet itself,
    // all relative to size of font in context. Also color.
    private static float bulletSpaceWidthFactor = 2.0f;
    private static float bulletSizeFactor = 0.4f;
    private static float bulletRaiseFactor = 0.2f;
    private static Color color = Color.BLACK;

    // See Elem.
    public float getWidth(RenderContext context) {
	return getFontSize(context) * bulletSpaceWidthFactor;
    }

    // See Elem.
    public float getWidth(RenderContext context, int index) {
	return getWidth(context);
    }

    // See Elem.
    public float getAdvance(RenderContext context) {
	return getWidth(context);
    }

    // See Elem.
    public float getAdvance(RenderContext context, int index) {
	return getAdvance(context);
    }

    // See Elem.
    public float getHeight(RenderContext context) {
	return getFontMetrics(context).getHeight();
    }

    // See Elem.
    public float getDescent(RenderContext context) {
	return getFontMetrics(context).getDescent();
    }

    // See Elem.
    public float getAscent(RenderContext context) {
	return getFontMetrics(context).getAscent();
    }

    // See Elem.
    public float getLeading(RenderContext context) {
	return getFontMetrics(context).getLeading();
    }

    // See Elem.
    // We never break at bullet.
    public boolean breakable() {
	return false;
    }

    // See Elem.
    public int firstBreak(RenderContext context, float len) {
	return -1;
    }

    // See Elem.
    public int firstBreak() {
	return -1;
    }

    // See Elem.
    public int lastBreak(RenderContext context, float len) {
	return firstBreak(context, len);
    }

    // See Elem.
    public int nextBreak() {
	return -1;
    }

    // See Elem.
    public Elem prefix(int index) {
	return null; // should not happen
    }

    // See Elem.
    public Elem suffix() {
	return null; // should not happen
    }

    // See Elem.
    public void draw(RenderContext context, GeneralDraw g, float y) {
	float size = getFontSize(context) * bulletSizeFactor;
	float x = getX() + 
	    (getFontSize(context) * bulletSpaceWidthFactor - size) / 2;
	float yBul = y - getFontSize(context) * bulletRaiseFactor -
	    size;
	g.fillOval(color, x, yBul, size, size);
    }

    // Get font metrics of font around item.
    private GeneralFontMetrics getFontMetrics(RenderContext context) {
	return context.getFontMetrics(getType());
    }

    // Size of font around item.
    private float getFontSize(RenderContext context) {
	return getFontMetrics(context).getAscent();
    }
}
