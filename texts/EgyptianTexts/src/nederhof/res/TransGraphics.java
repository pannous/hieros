/***************************************************************************/
/*																		 */
/*  TransGraphics.java													 */
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

// Wraps Graphics2D, adding information about whether the image is
// mirrored. This information is needed for notes, which are then also
// to be mirrored.
// Furthermore, there is a point of reference that may differ from
// the left upper corner.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import com.itextpdf.awt.PdfGraphics2D;
import org.jibble.epsgraphics.EpsGraphics2D;

public class TransGraphics implements UniGraphics {

	// The graphics.
	protected Graphics2D graphics;

	// Is mirrored? Relevant for right-to-left hieroglyphic.
	private boolean mirror;

	// Is mirrored?
	public boolean isMirrored() {
		return mirror;
	}

	// For subclasses only.
	protected TransGraphics(boolean mirror) {
		this.mirror = mirror;
	}

	// Given is point of reference, and whether to be mirrored.
	public TransGraphics(BufferedImage image, int x, int y, boolean mirror) {
		graphics = image.createGraphics();
		if (mirror) {
			graphics.scale(-1, 1);
			graphics.translate(-image.getWidth() - x, y);
		} else
			graphics.translate(x, y);
		this.mirror = mirror;
		setGoodRender();
	}

	// Part of graphics is made into TransGraphics. Given is upper left
	// corner, and point of reference, and whether to be mirrored.
	public TransGraphics(Graphics2D g, int x, int y, int width, boolean mirror) {
		graphics = (Graphics2D) g.create();
		if (mirror) {
			graphics.scale(-1, 1);
			graphics.translate(-width - x, y);
		} else
			graphics.translate(x, y);
		this.mirror = mirror;
		setGoodRender();
	}

	// Print string with given font and color.
	public void drawString(Font font, Color color, String string, float x, float y) {
		drawString(font, color, string, x, y, null);
	}

	// Print string with given font and color, only in area.
	// To avoid printing white glyphs in case of grayscale images, check white.
	public void drawString(Font font, Color color, String string, float x, float y,
			Area area) {
		if (color.equals(Color.WHITE))
			return;
		Shape oldClip = graphics.getClip();
		if (area != null) 
			graphics.clip(area);
		graphics.setFont(font);
		graphics.setColor(color);
		graphics.drawString(string, x, y);
		graphics.setClip(oldClip);
	}

	// Print glyphs in image.
	public void render(OptionalGlyphs glyphs, int x, int y) {
		glyphs.render(this, x, y);
	}
	// Same but clipped.
	public void render(OptionalGlyphs glyphs, int x, int y, Area clipped) {
		glyphs.render(this, x, y, clipped);
	}
	// If graphics is mirrored, also print text backwards, so that in the
	// end it appears straight.
	public void renderStraight(OptionalGlyphs glyphs, int x, int y) {
		glyphs.renderStraight(this, x, y);
	}

	// Print rectangle.
	public void fillRect(Color color, int x, int y, int width, int height) {
		graphics.setColor(color);
		graphics.fillRect(x, y, width, height);
	}
	public void fillRect(Color color, Rectangle rect) {
		fillRect(color, rect.x, rect.y, rect.width, rect.height);
	}

	// Make shading. Consists of diagonal lines.
	// We divide the surface into 3 parts, based on the minimum of
	// width and height. How we handle the middle part depends on
	// whether width or height is bigger.
	public void shade(Rectangle rect, HieroRenderContext context) {
		if (rect.width <= 0 || rect.height <= 0)
			return;
		if (graphics instanceof PdfGraphics2D || 
				graphics instanceof EpsGraphics2D) {
			shadeWithoutPixelization(rect, context);
			return;
		}
		// hack for bug/feature in EPS class: extra length
		int xMin = rect.x;
		int xMax = rect.x + rect.width;
		int yMin = rect.y;
		int yMax = rect.y + rect.height;
		int minDim = Math.min(rect.width, rect.height);
		if (context.shadeFreq() < 1)
			return;
		int shadeStep = Math.max(1, context.emSizePix() / context.shadeFreq());
		int shadeWidth = Math.max(1, shadeStep / context.shadeCover());
		Color color = context.shadeColor();
		if (color.equals(Color.WHITE))
			return;
		graphics.setColor(color);
		for (int i = 0; i < minDim; i++) {
			if ((xMin+yMin+i) % shadeStep < shadeWidth)
				graphics.drawLine(xMin, yMin+i, xMin+i, yMin);
			if ((xMax-minDim+i+yMax-1) % shadeStep < shadeWidth)
				graphics.drawLine(xMax-minDim+i, yMax-1, xMax-1, yMax-minDim+i);
		}
		if (rect.width < rect.height) {
			for (int i = 0; i < rect.height - minDim; i++) 
				if ((xMin+yMin+minDim-1+i) % shadeStep < shadeWidth)
					graphics.drawLine(xMin, yMin+minDim-1+i, xMax-1, yMin+i);
		} else {
			for (int i = 0; i < rect.width - minDim; i++) 
				if ((xMin+i+yMax-1) % shadeStep < shadeWidth)
					graphics.drawLine(xMin+i, yMax-1, xMin+minDim-1+i, yMin);
		}
	}

	// As above, but with coordinates are infinitely thin. 
	// Avoids pixelization.
	// Go along imaginary diagonal of bounding square box.
	private void shadeWithoutPixelization(Rectangle rect, HieroRenderContext context) {
		int xMin = rect.x;
		int xMax = rect.x + rect.width;
		int yMin = rect.y;
		int yMax = rect.y + rect.height;
		int minDim = Math.min(rect.width, rect.height);
		int maxDim = Math.max(rect.width, rect.height);
		if (context.shadeFreq() < 1)
			return;
		int shadeStep = Math.max(1, context.emSizePix() / context.shadeFreq());
		int shadeWidth = Math.max(1, shadeStep / context.shadeCover());
		graphics.setColor(context.shadeColor());
		if (shadeWidth >= shadeStep)
			graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
		else {
			setStroke();
			for (int i = 0; i < minDim; i++) {
				if ((xMin+yMin+i) % shadeStep < shadeWidth)
					graphics.drawLine(xMin, yMin+i, xMin+i, yMin);
				if ((xMax-minDim+i+yMax) % shadeStep < shadeWidth)
					graphics.drawLine(xMax-minDim+i, yMax, xMax, yMax-minDim+i);
			}
			if (rect.width < rect.height) {
				for (int i = 0; i < rect.height - minDim; i++)
					if ((xMin+yMin+minDim+i) % shadeStep < shadeWidth)
						graphics.drawLine(xMin, yMin+minDim+i, xMax, yMin+i);
			} else {
				for (int i = 0; i < rect.width - minDim; i++)
					if ((xMin+i+yMax) % shadeStep < shadeWidth)
						graphics.drawLine(xMin+i, yMax, xMin+minDim+i, yMin);
			}
		}
	}

	// Set diagonal stroke such that if a stroke is drawn from each pixel, 
	// the total surface is covered.
	private void setStroke() {
		Stroke lineStroke = new BasicStroke(0.5f * (float) Math.sqrt(2));
		graphics.setStroke(lineStroke);
	}

	// Set reasonable rendering properties.
	protected void setGoodRender() {
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
}
