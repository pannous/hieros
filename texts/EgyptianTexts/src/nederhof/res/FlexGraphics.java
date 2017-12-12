/***************************************************************************/
/*																		 */
/*  FlexGraphics.java													  */
/*																		 */
/*  Copyright (c) 2008 Mark-Jan Nederhof								   */
/*																		 */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the				 */
/*  GNU General Public License (see doc/GPL.TXT).						  */
/*  By continuing to use, modify, or distribute this file you indicate	 */
/*  that you have read the license and understand and accept it fully.	 */
/*																		 */
/***************************************************************************/

// Extends TransGraphics, by offering an image that may grow dynamically
// for notes.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class FlexGraphics extends TransGraphics {

	// Type of image.
	private int type;

	// Image containing the pixels.
	private BufferedImage image;

	// Rectangle inside ImageBuffer that is conceptual image.
	private Rectangle rect;

	// Given is physical size, rectangle within image that is conceptual
	// image, whether to be mirrored.
	public FlexGraphics(HieroRenderContext context,
			int totalWidth, int totalHeight,
			int x, int y, int width, int height, boolean mirror) {
		super(mirror);
		type = context.imageType();
		image = MovedBuffer.whiteImage(context, totalWidth, totalHeight);
		rect = new Rectangle(x, y, width, height);
		graphics = image.createGraphics();
		if (isMirrored()) {
			graphics.scale(-1, 1);
			graphics.translate(-image.getWidth() + x, y);
		} else
			graphics.translate(x, y);
		setGoodRender();
	}

	// Get image.
	public MovedBuffer toImage() {
		return new MovedBuffer(image,
				rect.x, rect.y, rect.width, rect.height, isMirrored());
	}

	// Return bounding box around rendered material.
	public Rectangle getRect() {
		return new Rectangle(-rect.x, -rect.y, image.getWidth(), image.getHeight());
	}

	// If graphics is mirrored, also print text backwards, so that in the
	// end it appears straight. This is used for notes.
	// When note printed outside image, everything is copied to larger image,
	// with extra whitespace in margins.
	// This is the only case where enlarging of image may be necessary.
	public void renderStraight(OptionalGlyphs glyphs, int x, int y) {
		Dimension dim = glyphs.dimension();
		Rectangle physical = new Rectangle(rect.x + x, rect.y + y, dim.width, dim.height);
		int start = physical.x >= 0 ? 0 : -physical.x;
		int end = physical.x + physical.width <= image.getWidth() ? 0 : 
			physical.x + physical.width - image.getWidth();
		int left = isMirrored() ? end : start;
		int right = isMirrored() ? start : end;
		int top = physical.y >= 0 ? 0 : -physical.y;
		int bottom = physical.y + physical.height <= image.getHeight() ? 0 :
			physical.y + physical.height - image.getHeight();
		if (left > 0 || top > 0 || right > 0 || bottom > 0) {
			BufferedImage im = new BufferedImage(
					image.getWidth() + left + right,
					image.getHeight() + top + bottom, type);
			graphics.dispose();
			graphics = im.createGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, left, im.getHeight());
			graphics.fillRect(im.getWidth() - right, 0, right, im.getHeight());
			graphics.fillRect(0, 0, im.getWidth(), top);
			graphics.fillRect(0, im.getHeight() - bottom, im.getWidth(), bottom);
			for (int x0 = 0; x0 < image.getWidth(); x0++) 
				for (int y0 = 0; y0 < image.getHeight(); y0++) {
					int rgb = image.getRGB(x0, y0);
					im.setRGB(x0 + left, y0 + top, rgb);
				}
			image = im;
			rect.x += start;
			rect.y += top;
			if (isMirrored()) {
				graphics.scale(-1, 1);
				graphics.translate(-image.getWidth() + rect.x, rect.y);
			} else
				graphics.translate(rect.x, rect.y);
			setGoodRender();
		}
		super.renderStraight(glyphs, x, y);
	}

	// What is white as int in RGB? Find out by one-pixel image.
	private static int whitePixel;
	static {
		BufferedImage im = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = im.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 1, 1);
		g.dispose();
		whitePixel = im.getRGB(0, 0);
	}

	// Is pixel white? (Or outside image?)
	public boolean isWhite(int x, int y) {
		AffineTransform trans = graphics.getTransform();
		Point2D p = trans.transform(new Point(x, y), null);
		int xIm = (int) Math.round(p.getX());
		int yIm = (int) Math.round(p.getY());
		return xIm < 0 || xIm >= image.getWidth() ||
			yIm < 0 || yIm >= image.getHeight() ||
			image.getRGB(xIm, yIm) == whitePixel;
	}
}
