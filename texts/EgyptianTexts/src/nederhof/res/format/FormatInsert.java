/***************************************************************************/
/*                                                                         */
/*  FormatInsert.java                                                      */
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

public class FormatInsert extends ResInsert implements FormatBasicgroup {

    // Context for rendering.
    private HieroRenderContext context;

    // Constructor.
    public FormatInsert(ResInsert insert, HieroRenderContext context) {
	super(insert.place,
	    insert.x,
	    insert.y,
	    insert.fix,
	    insert.sep,
	    insert.switchs0,
	    FormatTopgroupHelper.makeGroup(insert.group1, context),
	    insert.switchs1,
	    FormatTopgroupHelper.makeGroup(insert.group2, context),
	    insert.switchs2);
        this.context = context;
    }

    public FormatTopgroup fGroup1() {
	return (FormatTopgroup) group1;
    }
    public FormatTopgroup fGroup2() {
	return (FormatTopgroup) group2;
    }

    ////////////////////////////////////////////////////////////
    // Effective values.

    private int effectDir() {
        return context.effectDir(dirHeader());
    }

    private boolean effectIsH() {
        return ResValues.isH(effectDir());
    }

    // Separation between groups.
    // Is multiplied by sep in font.
    private float effectSep() {
	return sep() * context.fontSep();
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
	return fGroup1().width();
    }
    public int height() {
	return fGroup1().height();
    }

    // Changed during scaling.
    // The position of the second group within the first.
    private int dynX;
    private int dynY;

    // Scale down.
    public void scale(float factor) {
	fGroup1().scale(factor);
	fGroup2().scale(1); /* to initialize variables */
	int width1 = fGroup1().width();
	int height1 = fGroup1().height();
	int width2 = fGroup2().width();
	int height2 = fGroup2().height();
	Rectangle rect1 = new Rectangle(0, 0, width1, height1);
	Rectangle rect2 = new Rectangle(0, 0, width2, height2);
	BufferedImage im1 = MovedBuffer.whiteImage(context, width1, height1);
	BufferedImage im2 = MovedBuffer.whiteImage(context, width2, height2);
	TransGraphics g1 = new TransGraphics(im1, 0, 0, false);
	TransGraphics g2 = new TransGraphics(im2, 0, 0, false);
	fGroup1().render(g1, rect1, rect1, new Area(rect1), false, true);
	fGroup2().render(g2, rect2, rect2, new Area(rect2), false, true);
	int xInit = Math.round(x * Math.max(0, width1 - 1));
	int yInit = Math.round(y * Math.max(0, height1 - 1));
	Point p = new Point(xInit, yInit);
	if (place.equals("t")) {
	    float sideScale = Math.max(Math.max(
			fGroup2().sideScaledBottom(),
			fGroup2().sideScaledLeft()),
		    fGroup2().sideScaledRight());
	    int aura = context.emToPix(sideScale * effectSep());
	    float subFactor = insertT(im1, im2, p, aura, fix);
	    fGroup2().scale(subFactor);
	    dynX = p.x - fGroup2().width() / 2;
	    dynY = 0;
	} else if (place.equals("b")) {
	    float sideScale = Math.max(Math.max(
			fGroup2().sideScaledTop(),
			fGroup2().sideScaledLeft()),
		    fGroup2().sideScaledRight());
	    int aura = context.emToPix(sideScale * effectSep());
	    float subFactor = insertB(im1, im2, p, aura, fix);
	    fGroup2().scale(subFactor);
	    dynX = p.x - fGroup2().width() / 2;
	    dynY = height1 - fGroup2().height();
	} else if (place.equals("s")) {
	    float sideScale = Math.max(Math.max(
			fGroup2().sideScaledTop(),
			fGroup2().sideScaledBottom()),
		    fGroup2().sideScaledRight());
	    int aura = context.emToPix(sideScale * effectSep());
	    float subFactor = insertS(im1, im2, p, aura, fix);
	    fGroup2().scale(subFactor);
	    dynX = 0;
	    dynY = p.y - fGroup2().height() / 2;
	} else if (place.equals("e")) {
	    float sideScale = Math.max(Math.max(
			fGroup2().sideScaledTop(),
			fGroup2().sideScaledBottom()),
		    fGroup2().sideScaledLeft());
	    int aura = context.emToPix(sideScale * effectSep());
	    float subFactor = insertE(im1, im2, p, aura, fix);
	    fGroup2().scale(subFactor);
	    dynX = width1 - fGroup2().width();
	    dynY = p.y - fGroup2().height() / 2;
	} else if (place.equals("ts")) {
	    float sideScale = Math.max(
			fGroup2().sideScaledBottom(),
			fGroup2().sideScaledRight());
	    int aura = context.emToPix(sideScale * effectSep());
	    float subFactor = insertTS(im1, im2, aura);
	    fGroup2().scale(subFactor);
	    dynX = 0;
	    dynY = 0;
	} else if (place.equals("te")) {
	    float sideScale = Math.max(
			fGroup2().sideScaledBottom(),
			fGroup2().sideScaledLeft());
	    int aura = context.emToPix(sideScale * effectSep());
	    float subFactor = insertTE(im1, im2, aura);
	    fGroup2().scale(subFactor);
	    dynX = width1 - fGroup2().width();
	    dynY = 0;
	} else if (place.equals("bs")) {
	    float sideScale = Math.max(
			fGroup2().sideScaledTop(),
			fGroup2().sideScaledRight());
	    int aura = context.emToPix(sideScale * effectSep());
	    float subFactor = insertBS(im1, im2, aura);
	    fGroup2().scale(subFactor);
	    dynX = 0;
	    dynY = height1 - fGroup2().height();
	} else if (place.equals("be")) {
	    float sideScale = Math.max(
			fGroup2().sideScaledTop(),
			fGroup2().sideScaledLeft());
	    int aura = context.emToPix(sideScale * effectSep());
	    float subFactor = insertBE(im1, im2, aura);
	    fGroup2().scale(subFactor);
	    dynX = width1 - fGroup2().width();
	    dynY = height1 - fGroup2().height();
	} else { /* equals "" */
	    float sideScale = Math.max(Math.max(Math.max(
			    fGroup2().sideScaledTop(),
			    fGroup2().sideScaledBottom()),
			fGroup2().sideScaledLeft()),
		    fGroup2().sideScaledRight());
	    int aura = context.emToPix(sideScale * effectSep());
	    float subFactor = insert(im1, im2, p, aura, fix);
	    fGroup2().scale(subFactor);
	    dynX = p.x - fGroup2().width() / 2;
	    dynY = p.y - fGroup2().height() / 2;
	}
    }

    // Inserting second image in first. Scaling down of second where needed.
    // As above, distance between pixels is relevant.
    // Return downscale factor.
    // Insertion at TS (top-left).
    private float insertTS(BufferedImage im1, BufferedImage im2, int sep) {
	return insertAt(im1, 0, 0, 
		im2, 0, 0, sep);
    }
    // Insertion at TE (top-right). 
    private float insertTE(BufferedImage im1, BufferedImage im2, int sep) {
	return insertAt(im1, im1.getWidth()-1, 0, 
		im2, im2.getWidth()-1, 0, sep);
    }
    // Insertion at BS (bottom-left). 
    private float insertBS(BufferedImage im1, BufferedImage im2, int sep) {
	return insertAt(im1, 0, im1.getHeight()-1, 
		im2, 0, im2.getHeight()-1, sep);
    }
    // Insertion at BE (bottom-right). 
    private float insertBE(BufferedImage im1, BufferedImage im2, int sep) {
	return insertAt(im1, im1.getWidth()-1, im1.getHeight()-1, 
		im2, im2.getWidth()-1, im2.getHeight()-1, sep);
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
	fGroup1().render(image, rect, shadeRect, clipped, isClipped, fitting);
	Rectangle rect2 = new Rectangle(rect.x + dynX, rect.y + dynY, 
		fGroup2().width(), fGroup2().height());
	fGroup2().render(image, rect2, rect2, clipped, isClipped, fitting);
    }

    ////////////////////////////////////////////////////////////////////////
    // Operations on pixel level.

    // In floating, this is the minimal improvement on the scaling factor
    // that we demand.
    private final float MIN_IMPROVE = 0.001f;

    // In the process of floating, by how much needs image be moved?
    private int moveDist() {
	return Math.max(1, Math.round(context.emSizePix() * 0.02f));
    }

    // Record from investigating whether an image can float in some direction.
    // The procedure tries to obtain a bigger scaling factor for the image that is
    // to be moved. The biggest such scaling factor determines the optimal move.
    private class MoveRecord {
	public int x; // x for best move
	public int y; // y for best move
	public float factor; // scaling factor for best move

	// Try to move im2 with aura of size sep in direction (dx,dy), 
	// with corresponding points (x1,y1) and (x2,y2). For each step
	// see if this improves scaling factor. Stop otherwise.
	public MoveRecord(HieroRenderContext context,
		BufferedImage im1, int x1, int y1,
		BufferedImage im2, int x2, int y2,
		BufferedImage aura, int sep, int dx, int dy) {
	    x = x1;
	    y = y1;
	    DirFactors factors = insertAt8(im1, x1, y1, im2, x2, y2, aura, sep);
	    factor = factors.min();
	    while (factors.canMove(dx, dy, factor)) {
		x1 += dx * moveDist();
		y1 += dy * moveDist();
		factors = insertAt8(im1, x1, y1, im2, x2, y2, aura, sep);
		float f = factors.min();
		if (f < factor - MIN_IMPROVE)
		    break;
		else if (f > factor + MIN_IMPROVE) {
		    x = x1;
		    y = y1;
		    factor = f;
		}
	    }
	}
    }

    // Insertion at T (top).
    // Unless fix is true, the second image may float left and right.
    // We take the second image in current size, by coarsely shinking image,
    // and see whether it can go left or right. We then see whether the factor
    // can be increased. If floating in both directions can increase factor, we
    // don't float.
    private float insertT(BufferedImage im1, BufferedImage im2, 
	    Point p, int sep, boolean fix) {
	BufferedImage aura = makeAura(im2, sep);
	float f = insertAt(im1, p.x, 0,
		im2, im2.getWidth()/2, 0, aura, sep);
	if (fix)
	    return f;
	MoveRecord left = new MoveRecord(context, im1, p.x, 0, 
		im2, im2.getWidth()/2, 0, aura, sep, -1, 0);
	MoveRecord right = new MoveRecord(context, im1, p.x, 0, 
		im2, im2.getWidth()/2, 0, aura, sep, +1, 0);
	if (left.factor > f + MIN_IMPROVE && right.factor < f + MIN_IMPROVE) {
	    p.x = left.x;
	    return left.factor;
	} else if (right.factor > f + MIN_IMPROVE && left.factor < f + MIN_IMPROVE) {
	    p.x = right.x;
	    return right.factor;
	} else
	    return f;
    }

    // Insertion at B (bottom).
    private float insertB(BufferedImage im1, BufferedImage im2, 
	    Point p, int sep, boolean fix) {
	BufferedImage aura = makeAura(im2, sep);
	float f = insertAt(im1, p.x, im1.getHeight()-1,
		im2, im2.getWidth()/2, im2.getHeight()-1, aura, sep);
	if (fix)
	    return f;
	MoveRecord left = new MoveRecord(context, im1, p.x, im1.getHeight()-1,
		im2, im2.getWidth()/2, im2.getHeight()-1, aura, sep, -1, 0);
	MoveRecord right = new MoveRecord(context, im1, p.x, im1.getHeight()-1,
		im2, im2.getWidth()/2, im2.getHeight()-1, aura, sep, +1, 0);
	if (left.factor > f + MIN_IMPROVE && right.factor < f + MIN_IMPROVE) {
	    p.x = left.x;
	    return left.factor;
	} else if (right.factor > f + MIN_IMPROVE && left.factor < f + MIN_IMPROVE) {
	    p.x = right.x;
	    return right.factor;
	} else
	    return f;
    }

    // Insertion at S (left).
    private float insertS(BufferedImage im1, BufferedImage im2, 
	    Point p, int sep, boolean fix) {
	BufferedImage aura = makeAura(im2, sep);
	float f = insertAt(im1, 0, p.y, 
		im2, 0, im2.getHeight()/2, aura, sep);
	if (fix)
	    return f;
	MoveRecord up = new MoveRecord(context, im1, 0, p.y,
		im2, 0, im2.getHeight()/2, aura, sep, 0, -1);
	MoveRecord down = new MoveRecord(context, im1, 0, p.y,
		im2, 0, im2.getHeight()/2, aura, sep, 0, +1);
	if (up.factor > f + MIN_IMPROVE && down.factor < f + MIN_IMPROVE) {
	    p.y = up.y;
	    return up.factor;
	} else if (down.factor > f + MIN_IMPROVE && up.factor < f + MIN_IMPROVE) {
	    p.y = down.y;
	    return down.factor;
	} else
	    return f;
    }

    // Insertion at E (right).
    private float insertE(BufferedImage im1, BufferedImage im2, 
	    Point p, int sep, boolean fix) {
	BufferedImage aura = makeAura(im2, sep);
	float f = insertAt(im1, im1.getWidth()-1, p.y,
		im2, im2.getWidth()-1, im2.getHeight()/2, aura, sep);
	if (fix)
	    return f;
	MoveRecord up = new MoveRecord(context, im1, im1.getWidth()-1, p.y,
		im2, im2.getWidth()-1, im2.getHeight()/2, aura, sep, 0, -1);
	MoveRecord down = new MoveRecord(context, im1, im1.getWidth()-1, p.y,
		im2, im2.getWidth()-1, im2.getHeight()/2, aura, sep, 0, +1);
	if (up.factor > f + MIN_IMPROVE && down.factor < f + MIN_IMPROVE) {
	    p.y = up.y;
	    return up.factor;
	} else if (down.factor > f + MIN_IMPROVE && up.factor < f + MIN_IMPROVE) {
	    p.y = down.y;
	    return down.factor;
	} else
	    return f;
    }

    // Insertion in middle. 
    // im 2 can move in all directions, i.e. we consider:
    // horizontal, vertical, and two diagonal directions that we will
    // call descent (top-left to bottom-right and v.v.) and
    // ascent (bottom-left to top-right and v.v.)
    // It is possible that three of those cannot give rise to increase
    // of scaling factor, but the fourth may. Therefore, we note which of 
    // the four directions are optimal, for the current position and work
    // on those that are not optimal, and try to move them and increase the
    // factor. If we move in one direction, the other three become
    // non-optimal.
    private float insert(BufferedImage im1, BufferedImage im2, 
	    Point p, int sep, boolean fix) {
	BufferedImage aura = makeAura(im2, sep);
	float f = insertAt(im1, p.x, p.y,
		im2, im2.getWidth()/2, im2.getHeight()/2, aura, sep);
	if (fix) 
	    return f;
	boolean horOptimal = false;
	boolean vertOptimal = false;
	boolean descOptimal = false;
	boolean ascOptimal = false;
	while (!horOptimal || !vertOptimal || ! descOptimal || ! ascOptimal) {
	    if (!horOptimal) {
		MoveRecord left = new MoveRecord(context, im1, p.x, p.x, 
			im2, im2.getWidth()/2, im2.getHeight()/2, aura, sep, -1, 0);
		MoveRecord right = new MoveRecord(context, im1, p.x, p.x, 
			im2, im2.getWidth()/2, im2.getHeight()/2, aura, sep, +1, 0);
		if (left.factor > f + MIN_IMPROVE && right.factor < f + MIN_IMPROVE) {
		    p.x = left.x;
		    f = left.factor;
		    vertOptimal = descOptimal = ascOptimal = false;
		} else if (right.factor > f + MIN_IMPROVE && left.factor < f + MIN_IMPROVE) {
		    p.x = right.x;
		    f = right.factor;
		    vertOptimal = descOptimal = ascOptimal = false;
		} 
		horOptimal = true;
	    }

	    if (!vertOptimal) {
		MoveRecord up = new MoveRecord(context, im1, p.x, p.x,
			im2, im2.getWidth()/2, im2.getHeight()/2, aura, sep, 0, -1);
		MoveRecord down = new MoveRecord(context, im1, p.x, p.x,
			im2, im2.getWidth()/2, im2.getHeight()/2, aura, sep, 0, +1);
		if (up.factor > f + MIN_IMPROVE && down.factor < f + MIN_IMPROVE) {
		    p.y = up.y;
		    f = up.factor;
		    horOptimal = descOptimal = ascOptimal = false;
		} else if (down.factor > f + MIN_IMPROVE && up.factor < f + MIN_IMPROVE) {
		    p.y = down.y;
		    f = down.factor;
		    horOptimal = descOptimal = ascOptimal = false;
		}
		vertOptimal = true;
	    }

	    if (!descOptimal) {
		MoveRecord forth = new MoveRecord(context, im1, p.x, p.x,
			im2, im2.getWidth()/2, im2.getHeight()/2, aura, sep, +1, +1);
		MoveRecord back = new MoveRecord(context, im1, p.x, p.x,
			im2, im2.getWidth()/2, im2.getHeight()/2, aura, sep, -1, -1);
		if (forth.factor > f + MIN_IMPROVE && back.factor < f + MIN_IMPROVE) {
		    p.x = forth.x;
		    f = forth.factor;
		    horOptimal = vertOptimal = ascOptimal = false;
		} else if (back.factor > f + MIN_IMPROVE && forth.factor < f + MIN_IMPROVE) {
		    p.x = back.x;
		    f = back.factor;
		    horOptimal = vertOptimal = ascOptimal = false;
		}
		descOptimal = true;
	    }

	    if (!ascOptimal) {
		MoveRecord forth = new MoveRecord(context, im1, p.x, p.x,
			im2, im2.getWidth()/2, im2.getHeight()/2, aura, sep, +1, -1);
		MoveRecord back = new MoveRecord(context, im1, p.x, p.x,
			im2, im2.getWidth()/2, im2.getHeight()/2, aura, sep, -1, +1);
		if (forth.factor > f + MIN_IMPROVE && back.factor < f + MIN_IMPROVE) {
		    p.x = forth.x;
		    f = forth.factor;
		    horOptimal = vertOptimal = descOptimal = false;
		} else if (back.factor > f + MIN_IMPROVE && forth.factor < f + MIN_IMPROVE) {
		    p.x = back.x;
		    f = back.factor;
		    horOptimal = vertOptimal = descOptimal = false;
		}
		ascOptimal = true;
	    }
	}
	return f;
    }

    // How much is im2 to be scaled down to fit within im1, at point of
    // reference, taking into account an aura of size sep.
    private float insertAt(
	    BufferedImage im1, int x1, int y1, 
	    BufferedImage im2, int x2, int y2,
	    int sep) {
	BufferedImage aura = makeAura(im2, sep);
	return insertAt(im1, x1, y1, im2, x2, y2, aura, sep);
    }
    // As above, but aura already computed.
    private float insertAt(
	    BufferedImage im1, int x1, int y1, 
	    BufferedImage im2, int x2, int y2,
	    BufferedImage aura, int sep) {
	if (im1.getWidth() <= 0 || im1.getHeight() <= 0 ||
		im2.getWidth() <= 0 || im2.getHeight() <= 0)
	    return 1.0f;
	float squareFactor = fitWithin(im1, x1, y1, im2, x2, y2);
	float auraFactor = fan(im1, x1, y1, aura, sep + x2, sep + y2);
	float minFactor = 0.01f;
	return Math.max(Math.min(squareFactor, auraFactor), minFactor);
    }
    // As above, but distinguish 8 directions.
    private DirFactors insertAt8(
	    BufferedImage im1, int x1, int y1, 
	    BufferedImage im2, int x2, int y2,
	    BufferedImage aura, int sep) {
	if (im1.getWidth() <= 0 || im1.getHeight() <= 0 ||
		im2.getWidth() <= 0 || im2.getHeight() <= 0)
	    return new DirFactors();
	DirFactors squareFactors = fitWithin8(im1, x1, y1, im2, x2, y2);
	DirFactors auraFactors = fan8(im1, x1, y1, aura, sep + x2, sep + y2);
	return squareFactors.min(auraFactors);
    }

    // We make an aura around an image. First a buffer is make around the
    // image. Then for each non-white pixel, a circle is drawn.
    private BufferedImage makeAura(BufferedImage im, int sep) {
	BufferedImage aura = MovedBuffer.whiteImage(context, 
		im.getWidth() + 2 * sep, im.getHeight() + 2 * sep);
	Graphics2D g = aura.createGraphics();
	g.setColor(Color.BLACK);
	for (int x = 0; x < im.getWidth(); x++)
	    for (int y = 0; y < im.getHeight(); y++)
		if (!PixelHelper.isBlank(im, x, y))
		    g.drawOval(x, y, 2 * sep, 2 * sep);
	g.dispose();
	return aura;
    }

    // 8 factors that indicate needed scaling down to fit,
    // distinguished in 8 slices of circle.
    // T = top = north, and 45 degrees from there.
    // TE = top-end = north-east, and 45 degrees from there.
    // Etc.
    private static class DirFactors {
	float t = 1.0f; 
	float te = 1.0f; 
	float e = 1.0f; 
	float be = 1.0f; 
	float b = 1.0f; 
	float bs = 1.0f; 
	float s = 1.0f; 
	float st = 1.0f; 

	// Improve factor is possible in slice of circle.
	// For this, the slope of the line needs to be determined.
	public void improve(int dx, int dy, float f) {
	    if (dx >= 0) {
		if (dy <= 0) {
		    if (-dy >= dx)
			t = Math.min(t, f);
		    else
			te = Math.min(te, f);
		} else {
		    if (dy <= dx)
			e = Math.min(e, f);
		    else
			be = Math.min(be, f);
		}
	    } else {
		if (dy >= 0) {
		    if (dy >= -dx)
			b = Math.min(b, f);
		    else
			bs = Math.min(bs, f);
		} else {
		    if (-dy <= -dx)
			s = Math.min(s, f);
		    else
			st = Math.min(st, f);
		}
	    }
	}

	// Get minimum of all factors.
	public float min() {
	    return Math.min(Math.min(Math.min(Math.min(Math.min(Math.min(Math.min(
					    t, te), e), be), b), bs), s), st);
	}

	// Take minimum of two.
	public DirFactors min(DirFactors d) {
	    t = Math.min(t, d.t);
	    te = Math.min(te, d.te);
	    e = Math.min(e, d.e);
	    be = Math.min(be, d.be);
	    b = Math.min(b, d.b);
	    bs = Math.min(bs, d.bs);
	    s = Math.min(s, d.s);
	    st = Math.min(st, d.st);
	    return this;
	}

	// With current factors, which direction is allowable,
	// as the factors are bigger than f?
	// dx and dy are -1, 0, 1.
	public boolean canMove(int dx, int dy, float f) {
	    switch (dx) {
		case -1:
		    switch (dy) {
			case -1:
			    return bs >= f && s >= f && st >= f && t >= f;
			case 0:
			    return b >= f && bs >= f && s >= f && st >= f;
			case 1:
			    return be >= f && b >= f && bs >= f && s >= f;
			default:
			    return false; /* should not happen */
		    }
		case 0:
		    switch (dy) {
			case -1:
			    return s >= f && st >= f && t >= f && te >= f;
			case 1:
			    return e >= f && be >= f && b >= f && bs >= f;
			default:
			    return false; /* should not happen */
		    }
		case 1:
		    switch (dy) {
			case -1:
			    return st >= f && t >= f && te >= f && e >= f;
			case 0:
			    return t >= f && te >= f && e >= f && be >= f;
			case 1:
			    return te >= f && e >= f && be >= f && b >= f;
			default:
			    return false; /* should not happen */
		    }
		default:
		    return false; /* should not happen */
	    }
	}
    }

    // Given two images with points of reference, how much must second be
    // scaled down to fit within first.
    private static float fitWithin(BufferedImage im1, int x1, int y1,
	    BufferedImage im2, int x2, int y2) {
	float leftRatio = (x1+1.0f) / (x2+1.0f);
	float rightRatio = (im1.getWidth()-x1) * 1.0f / (im2.getWidth()-x2);
	float topRatio = (y1+1.0f) / (y2+1.0f);
	float bottomRatio = (im1.getHeight()-y1) * 1.0f / (im2.getHeight()-y2);
	return Math.min(1.0f,
		Math.min(Math.min(Math.min(leftRatio, rightRatio), topRatio), bottomRatio));
    }
    // As above, but distinguished in 8 directions.
    private static DirFactors fitWithin8(BufferedImage im1, int x1, int y1,
	    BufferedImage im2, int x2, int y2) {
	DirFactors ratios = new DirFactors();
	ratios.bs = ratios.s = Math.min(1.0f, 
		(x1+1.0f) / (x2+1.0f));
	ratios.te = ratios.e = Math.min(1.0f,
		(im1.getWidth()-x1) * 1.0f / (im2.getWidth()-x2));
	ratios.st = ratios.t = Math.min(1.0f,
		(y1+1.0f) / (y2+1.0f));
	ratios.be = ratios.b = Math.min(1.0f,
		(im1.getHeight()-y1) * 1.0f / (im2.getHeight()-y2));
	return ratios;
    }

    // We draw lines from certain point to edges of aura.
    // We do same for parallel image.
    // We note furthest nonwhite point in aura and longest all-white path
    // in image. The minimum ratio is factor for scaling down.
    private static float fan(BufferedImage im, int x1, int y1, 
	    BufferedImage aura, int x2, int y2) {
	DirFactors ratios = fan8(im, x1, y1, aura, x2, y2);
	return ratios.min();
    }
    // As above, but distinguish in 8 directions.
    private static DirFactors fan8(BufferedImage im, int x1, int y1, 
	    BufferedImage aura, int x2, int y2) {
	DirFactors ratios = new DirFactors();
	for (int x = 0; x < aura.getWidth(); x++) {
	    int dist2 = lastNonWhite(aura, x2, y2, x, 0);
	    int dx = x - x2;
	    int dy = 0 - y2;
	    int dist1 = longestWhite(im, x1, y1, x1 + dx, y1 + dy);
	    if (dist1 >= 0 && dist2 > 0)
		ratios.improve(dx, dy, dist1 * 1.0f / dist2);
	    dist2 = lastNonWhite(aura, x2, y2, x, aura.getHeight()-1);
	    dx = x - x2;
	    dy = aura.getHeight()-1 - y2;
	    dist1 = longestWhite(im, x1, y1, x1 + dx, y1 + dy);
	    if (dist1 >= 0 && dist2 > 0)
		ratios.improve(dx, dy, dist1 * 1.0f / dist2);
	}
	for (int y = 0; y < aura.getHeight(); y++) {
	    int dist2 = lastNonWhite(aura, x2, y2, 0, y);
	    int dx = 0 - x2;
	    int dy = y - y2;
	    int dist1 = longestWhite(im, x1, y1, x1 + dx, y1 + dy);
	    if (dist1 >= 0 && dist2 > 0)
		ratios.improve(dx, dy, dist1 * 1.0f / dist2);
	    dist2 = lastNonWhite(aura, x2, y2, aura.getWidth()-1, y);
	    dx = aura.getWidth()-1 - x2;
	    dy = y - y2;
	    dist1 = longestWhite(im, x1, y1, x1 + dx, y1 + dy);
	    if (dist1 >= 0 && dist2 > 0)
		ratios.improve(dx, dy, dist1 * 1.0f / dist2);
	}
	return ratios;
    }

    // Go from one point to next in image, and record how many white pixels
    // are found before the first non-white pixel. 
    // Return negative number if path leads to outside image or if there are
    // no non-white pixels on the path.
    // We iterate over x or over y, depending on slope.
    private static int longestWhite(BufferedImage im, int x1, int y1, int x2, int y2) {
	float slope = (y2-y1) * 1.0f / (x2-x1);
	if (Math.abs(slope) < 1) {
	    if (x1 <= x2)
		for (int dx = 0; dx <= x2-x1; dx++) {
		    int x = x1 + dx;
		    int y = y1 + Math.round(dx * slope);
		    if (!inside(im, x, y))
			return -1;
		    else if (!PixelHelper.isBlank(im, x, y))
			return dx;
		}
	    else
		for (int dx = 0; dx >= x2-x1; dx--) {
		    int x = x1 + dx;
		    int y = y1 + Math.round(dx * slope);
		    if (!inside(im, x, y))
			return -1;
		    else if (!PixelHelper.isBlank(im, x, y))
			return -dx;
		}
	} else {
	    if (y1 <= y2)
		for (int dy = 0; dy <= y2-y1; dy++) {
		    int x = x1 + Math.round(dy / slope);
		    int y = y1 + dy;
		    if (!inside(im, x, y))
			return -1;
		    else if (!PixelHelper.isBlank(im, x, y))
			return dy;
		}
	    else
		for (int dy = 0; dy >= y2-y1; dy--) {
		    int x = x1 + Math.round(dy / slope);
		    int y = y1 + dy;
		    if (!inside(im, x, y))
			return -1;
		    else if (!PixelHelper.isBlank(im, x, y))
			return -dy;
		}
	}
	return -1;
    }

    // Get longest path ending on a non-white pixel. We start investigation at
    // second point. Return -1 if there are no non-white pixels.
    private static int lastNonWhite(BufferedImage im, int x1, int y1, int x2, int y2) {
	float slope = (y2-y1) * 1.0f / (x2-x1);
	if (Math.abs(slope) < 1) {
	    if (x1 <= x2)
		for (int dx = x2-x1; dx >= 0; dx--) {
		    int x = x1 + dx;
		    int y = y1 + Math.round(dx * slope);
		    if (!inside(im, x, y))
			return -1;
		    else if (!PixelHelper.isBlank(im, x, y))
			return dx;
		}
	    else 
		for (int dx = x2-x1; dx <= 0; dx++) {
		    int x = x1 + dx;
		    int y = y1 + Math.round(dx * slope);
		    if (!inside(im, x, y))
			return -1;
		    else if (!PixelHelper.isBlank(im, x, y))
			return -dx;
		}
	} else {
	    if (y1 <= y2)
		for (int dy = y2-y1; dy >= 0; dy--) {
		    int x = x1 + Math.round(dy / slope);
		    int y = y1 + dy;
		    if (!inside(im, x, y))
			return -1;
		    else if (!PixelHelper.isBlank(im, x, y))
			return dy;
		}
	    else
		for (int dy = y2-y1; dy <= 0; dy++) {
		    int x = x1 + Math.round(dy / slope);
		    int y = y1 + dy;
		    if (!inside(im, x, y))
			return -1;
		    else if (!PixelHelper.isBlank(im, x, y))
			return -dy;
		}
	}
	return -1;
    }

    // Is pixel inside image?
    private static boolean inside(BufferedImage im, int x, int y) {
	return x >= 0 && x < im.getWidth() &&
	    y >= 0 && y < im.getHeight();
    }

    ////////////////////////////////////////////////////////////
    // Notes and shading.

    // Place footnotes.
    public void placeNotes(FlexGraphics im, 
	    boolean under, boolean over) {
	boolean under2 = under;
	boolean over2 = over;
	if (place.equals("t")) {
	    if (effectIsH())
		under2 = true;
	} else if (place.equals("b")) {
	    if (effectIsH())
		over2 = true;
	} else if (place.equals("s")) {
	    if (!effectIsH())
		over2 = true;
	} else if (place.equals("e")) {
	    if (!effectIsH())
		under2 = true;
	} else if (place.equals("ts")) {
	    if (effectIsH())
		under2 = true;
	    else
		over2 = true;
	} else if (place.equals("te")) {
	    under2 = true;
	} else if (place.equals("bs")) {
	    over2 = true;
	} else if (place.equals("be")) {
	    if (effectIsH())
		over2 = true;
	    else
		under2 = true;
	} else { /* place equals "" */
	    under2 = true;
	    over2 = true;
	}
	fGroup1().placeNotes(im, under, over);
	fGroup2().placeNotes(im, under2, over2);
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
	    Vector exprs, Vector notes, Vector shades, Rectangle clip) {
	fGroup1().toResLite(x, y, exprs, notes, shades, clip);
	fGroup2().toResLite(x, y, exprs, notes, shades, clip);
    }

}
