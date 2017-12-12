/***************************************************************************/
/*																		 */
/*  Glyphs.java															*/
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

// Represents one or more glyphs.

package nederhof.res;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;

public class Glyphs implements OptionalGlyphs {

	// The string, font, whether mirrored, color, image type.
	private String string;
	private Font font;
	private boolean mirror = false;
	private int rotate = 0;
	private Color color;
	private int size;
	private int imageType;

	// Pixelized box, created at construction.
	private Rectangle pixelBounds;

	// Create image from character.
	public Glyphs(char c, Font font, boolean mirror, int rotate, Color color, 
			int size, float xscale, float yscale,
			HieroRenderContext context) {
		this.string = String.valueOf(c);
		this.font = transform(font, mirror, rotate, size, xscale, yscale);
		this.mirror = mirror;
		this.rotate = rotate;
		this.color = color;
		this.size = size;
		this.imageType = context.imageType();
		getBounds(context.pedantic);
	}
	// Same, but without xscale/yscale.
	public Glyphs(char c, Font font, boolean mirror, int rotate, Color color, 
			int size, HieroRenderContext context) {
		this(c, font, mirror, rotate, color, size, 1f, 1f, context);
	}

	// Same, but with GlyphPlace.
	public Glyphs(GlyphPlace place, boolean mirror, int rotate, Color color, 
			int size, float xscale, float yscale, HieroRenderContext context) {
		this((char) place.index, context.getFont(place.file),
				mirror, rotate, color, size, xscale, yscale, context);
	}
	// Same, but without xscale/yscale.
	public Glyphs(GlyphPlace place, boolean mirror, int rotate, Color color,
			int size, HieroRenderContext context) {
		this(place, mirror, rotate, color, size, 1f, 1f, context);
	}

	// Create image from string.
	public Glyphs(String string, Font font, Color color, int size, 
			HieroRenderContext context) {
		this.string = stringify(string);
		this.font = font.deriveFont((float) size);
		this.color = color;
		this.imageType = context.imageType();
		getBounds(context.pedantic);
	}

	// Transform font.
	private static Font transform(Font font, 
			boolean mirror, int rotate, int size, float xscale, float yscale) {
		AffineTransform transform = new AffineTransform();
		double radians = Math.toRadians(rotate);
		transform.rotate(radians);
		if (mirror)
			transform.scale(-xscale, yscale);
		else
			transform.scale(xscale, yscale);
		font = font.deriveFont((float) size);
		font = font.deriveFont(transform);
		return font;
	}

	// Turn into real string by removing initial and final ", and
	// changing \" and \\ into " and \.
	private static String stringify(String str) {
		str = str.replaceFirst("^\"","");
		str = str.replaceFirst("\"$","");
		str = str.replaceAll("\\\\(\\\\|\")","$1");
		return str;
	}

	// Get bounds of character or string.
	// In pedantic mode, make image with margin, then see
	// if that margin is necessary and if not remove.
	private void getBounds(boolean pedantic) {
		FontRenderContext render = new FontRenderContext(null, true, true);
		TextLayout layout = new TextLayout(string, font, render);
		Rectangle2D bounds;
		try {
			bounds = layout.getBounds();
		} catch (ArrayIndexOutOfBoundsException e) {
			// For some mysterious and unpredictable reason, 
			// this exception occurred in older versions of Java.
			System.err.println("layout.getBounds() out of bounds for: " +
					((int) string.charAt(0)));
			bounds = getBoundsSimulator();
		}
		// This problem arose with Java 1.7 and went away for most signs when some
		// lines in the hieroglyphic font were redrawn. 
		// The cause of the problem is unclear.
		if (bounds.getWidth() == 0) {
			// System.err.println("layout.getBounds() problem for: " + ((int) string.charAt(0)));
			bounds = getBoundsSimulator();
		}
		pixelBounds = new Rectangle();
		pixelBounds.x = (int) Math.floor(bounds.getX());
		pixelBounds.y = (int) Math.floor(bounds.getY());
		int maxX = (int) Math.ceil(bounds.getX() + bounds.getWidth());
		int maxY = (int) Math.ceil(bounds.getY() + bounds.getHeight());
		pixelBounds.width = maxX - pixelBounds.x;
		pixelBounds.height = maxY - pixelBounds.y;
		// To prevent wrong fonts from blowing up the program:
		pixelBounds.width = Math.max(pixelBounds.width, 1);
		pixelBounds.height = Math.max(pixelBounds.height, 1);
		if (pedantic) {
			int margin = 1;
			BufferedImage im = getImage(margin, pixelBounds);
			Rectangle inlay = cutout(im);
			pixelBounds.x += inlay.x-margin;
			pixelBounds.y += inlay.y-margin;
			pixelBounds.width = inlay.width;
			pixelBounds.height = inlay.height;
		}
	}
	// Back-up when getBounds fails.
	private Rectangle2D getBoundsSimulator() {
		int safe = Math.max(1, 2*size);
		BufferedImage im = getImage(Math.max(1, safe), new Rectangle(0, 0, safe, safe));
		Rectangle inlay = cutout(im);
		return new Rectangle2D.Float(inlay.x-safe, inlay.y-safe,
				inlay.width, inlay.height);
	}

	// Get dimension.
	public Dimension dimension() {
		return new Dimension(pixelBounds.width, pixelBounds.height);
	}

	// Get area filled by glyphs when printed at (x,y),
	public Area filling(int x, int y) {
		if (color.equals(Color.WHITE))
			return new Area();
		FontRenderContext render = new FontRenderContext(null, true, true);
		TextLayout t = new TextLayout(string, font, render);
		Area inner = new InnerArea(t);
		AffineTransform aff = new AffineTransform();
		aff.translate(x-pixelBounds.x, y-pixelBounds.y);
		inner.transform(aff);
		return inner;
	}

	// Print glyph in graphics.
	public void render(TransGraphics graph, int x, int y) {
		graph.drawString(font, color, string, x-pixelBounds.x, 
				y-pixelBounds.y);
	}

	// If graphics is mirrored, also print text backwards, so that in the 
	// end it appears straight.
	public void renderStraight(TransGraphics graph, int x, int y) {
		if (graph.isMirrored()) {
			AffineTransform mirrorTrans = new AffineTransform();
			mirrorTrans.scale(-1, 1);
			Font mirrorFont = font.deriveFont(mirrorTrans);
			graph.drawString(mirrorFont, color, string, 
					x+pixelBounds.width+pixelBounds.x, y-pixelBounds.y);
		} else
			graph.drawString(font, color, string, x-pixelBounds.x, y-pixelBounds.y);
	}

	// Print glyph in graphics, only in clipped area.
	public void render(TransGraphics graph, int x, int y, Area clipped) {
		graph.drawString(font, color, string, 
				x-pixelBounds.x, y-pixelBounds.y, clipped);
	}

	// Set reasonable rendering properties.
	private static void setRendering(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	////////////////////////////////////////////////////////////////////////////////
	// Some of the below only relevant for "pedantic" rendering mode.

	private static int BACKGROUND = Color.WHITE.getRGB();

	// Get image, without margin.
	public BufferedImage getImage() {
		return getImage(0, pixelBounds);
	}
	private BufferedImage getImage(int margin, Rectangle bounds) {
		int width = bounds.width + 2 * margin;
		int height = bounds.height + 2 * margin;
		BufferedImage im = new BufferedImage(width, height, imageType);
		Graphics2D graph = im.createGraphics();
		setRendering(graph);
		graph.setFont(font);
		graph.setColor(Color.WHITE);
		graph.fillRect(0, 0, width, height);
		graph.setColor(Color.BLACK);
		graph.drawString(string, margin - bounds.x, margin - bounds.y);
		graph.dispose();
		return im;
	}

	// Remove all-white edges from image.
	// Make sure is non-empty.
	private static Rectangle cutout(BufferedImage im) {
		int width = im.getWidth();
		int height = im.getHeight();
		int left = 0;
		int right = 0;
		int top = 0;
		int bottom = 0;
		while (left < width && colWhite(im, left))
			left++;
		while (right < width && colWhite(im, width - 1 - right))
			right++;
		while (top < height && rowWhite(im, top))
			top++;
		while (bottom < height && rowWhite(im, height - 1 - bottom))
			bottom++;
		int cutWidth = Math.max(1, width - left - right);
		int cutHeight = Math.max(1, height - top - bottom);
		return new Rectangle(left, top, cutWidth, cutHeight);
	}

	// Is row all while?
	private static boolean rowWhite(BufferedImage im, int y) {
		int width = im.getWidth();
		for (int x = 0; x < width; x++)
			if (im.getRGB(x, y) != BACKGROUND)
				return false;
		return true;
	}

	// Is column all while?
	private static boolean colWhite(BufferedImage im, int x) {
		int height = im.getHeight();
		for (int y = 0; y < height; y++)
			if (im.getRGB(x, y) != BACKGROUND)
				return false;
		return true;
	}

	////////////////////////////////////////////////////////////////////
	// For segments in boxes (cartouches and such), we need to find out the
	// thickness of the boundaries. We draw lines from the center to an edge,
	// and consider the first non-white pixel.

	public int leftEdge() {
		BufferedImage im = getImage();
		int y = pixelBounds.height / 2;
		for (int x = pixelBounds.width / 2 - 1; x >= 0; x--) {
			if (im.getRGB(x, y) != BACKGROUND)
				return x+1;
		}
		return 0;
	}

	public int rightEdge() {
		BufferedImage im = getImage();
		int y = pixelBounds.height / 2;
		for (int x = pixelBounds.width / 2; x < pixelBounds.width; x++) {
			if (im.getRGB(x, y) != BACKGROUND)
				return pixelBounds.width - x;
		}
		return 0;
	}

	public int bottomEdge() {
		BufferedImage im = getImage();
		int x = pixelBounds.width / 2;
		for (int y = pixelBounds.height / 2; y < pixelBounds.height; y++) {
			if (im.getRGB(x, y) != BACKGROUND)
				return pixelBounds.height - y;
		}
		return 0;
	}

	public int topEdge() {
		BufferedImage im = getImage();
		int x = pixelBounds.width / 2;
		for (int y = pixelBounds.height / 2 - 1; y >= 0; y--) {
			if (im.getRGB(x, y) != BACKGROUND)
				return y+1;
		}
		return 0;
	}

}
