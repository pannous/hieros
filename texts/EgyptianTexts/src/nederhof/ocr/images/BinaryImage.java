package nederhof.ocr.images;

import java.awt.*;
import java.awt.color.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

// Wrapper for 2D array representing binary image.
public class BinaryImage {

	// For manipulation of buffered images.
	public static int WHITE = 0xffffff;
	public static int BLACK = 0x000000;
	public static int GRAY = 0x777777;
	public static int BLUE = 0xff;
	public static int MID_COLOR = 0x80;

	// The binary image as 2D array.
	protected boolean[][] im;

	// Width and height in pixels of binary image.
	public static int width(boolean[][] im) {
		return im.length;
	}
	public static int height(boolean[][] im) {
		return im[0].length;
	}

	// Create wrapper.
	public BinaryImage(boolean[][] im) {
		this.im = im;
	}

	// Create binary image of given size, with only white pixels.
	// Make sure image contains at least one pixel.
	public BinaryImage(int width, int height) {
		if (width <= 0)
			width = 1;
		if (height <= 0)
			height = 1;
		im = new boolean[width][height];
		for (int x = 0; x < width; x++) 
			for (int y = 0; y < height; y++) 
				im[x][y] = false;
	}

	// Create binary image from possibly colored image.
	public BinaryImage(BufferedImage colorImage) {
		this(binarizeColor(colorImage));
	}

	// Create from file.
	public BinaryImage(File file) throws IOException {
		this(ImageIO.read(file));
	}

	// Get dimensions.
	public int width() {
		return width(im);
	}
	public int height() {
		return height(im);
	}

	// Set pixel.
	public void set(int x, int y, boolean val) {
		im[x][y] = val;
	}

	// Get pixel.
	public boolean get(int x, int y) {
		return im[x][y];
	}

	// Set pixel safely.
	public void setSafe(int x, int y, boolean val) {
		if (0 <= x && x < width() &&
				0 <= y && y < height())
			set(x, y, val);
	}

	// Get pixel safely.
	public boolean getSafe(int x, int y) {
		if (0 <= x && x < width() &&
				0 <= y && y < height())
			return get(x, y);
		else
			return false;
	}

	// Get pixel safely.
	// If outside image, assume white.
	public static boolean get(boolean[][] binary, int x, int y) {
		if (0 <= x && x < width(binary) &&
				0 <= y && y < height(binary))
			return binary[x][y];
		else
			return false;
	}

	// Subimage.
	public BinaryImage subImage(Rectangle rect) {
		BinaryImage sub = new BinaryImage(rect.width, rect.height);
		for (int x = rect.x; x < rect.x + rect.width; x++)
			for (int y = rect.y; y < rect.y + rect.height; y++)
				sub.set(x - rect.x, y - rect.y, get(x, y));
		return sub;
	}

	// Subimage cut out from polygon.
	public BinaryImage subImage(Polygon poly) {
		Rectangle rect = poly.getBounds();
		BinaryImage sub = new BinaryImage(rect.width, rect.height);
		for (int x = rect.x; x < rect.x + rect.width; x++)
			for (int y = rect.y; y < rect.y + rect.height; y++)
				if (poly.contains(x, y))
					sub.set(x - rect.x, y - rect.y, get(x, y));
		return sub;
	}

	// Offset of subimage cut out from polygon.
	public Point subImageOffset(Polygon poly) {
		Rectangle rect = poly.getBounds();
		return new Point(rect.x, rect.y);
	}

	// Add subimage at position. If white pixel becomes non-white,
	// make it special color.
	public static void superimpose(BufferedImage main,
			BinaryImage extra, int xExtra, int yExtra, int color) {
		boolean[][] ar = extra.array();
		for (int x = 0; x < width(ar); x++) 
			for (int y = 0; y < height(ar); y++) 
				if (ar[x][y]) {
					int xMain = xExtra + x;
					int yMain = yExtra + y;
					if (xMain >= 0 && xMain < main.getWidth() &&
							yMain >= 0 && yMain < main.getHeight()) {
						int c = main.getRGB(xMain, yMain);
						main.setRGB(xMain, yMain, 
								c == WHITE ? color : BLACK);
					}
				}
	}

	// Add subimage to image.
	public static void superimpose(BinaryImage main,
			BinaryImage extra, int xExtra, int yExtra) {
		for (int x = 0; x < extra.width(); x++) 
			for (int y = 0; y < extra.height(); y++) 
				if (extra.get(x, y)) {
					int xMain = xExtra + x;
					int yMain = yExtra + y;
					if (xMain >= 0 && xMain < main.width() &&
							yMain >= 0 && yMain < main.height()) 
						main.set(xMain, yMain, true);
				}
	}

	// If pixel is not black in main image, make it white in extra image.
	public static void mask(BinaryImage main,
			BinaryImage extra, int xExtra, int yExtra) {
		for (int x = 0; x < extra.width(); x++) 
			for (int y = 0; y < extra.height(); y++) {
				int xMain = xExtra + x;
				int yMain = yExtra + y;
				if (xMain >= 0 && xMain < main.width() &&
						yMain >= 0 && yMain < main.height()) 
					if (!main.get(xMain, yMain))
						extra.set(x, y, false);
			}
	}

	// Output wrapped image.
	public boolean[][] array() {
		return im;
	}

	// Binarize image that is possibly colored.
	public static boolean[][] binarizeColor(BufferedImage colored) {
		BufferedImage gray = ImageUtil.convertToGrayscale(colored);
		return binarize(gray);
	}

	// Binarize image and store in 2D array.
	// Make sure image contains at least one pixel.
	public static boolean[][] binarize(BufferedImage im) {
		int width = im.getWidth();
		int height = im.getHeight();
		if (width < 1)
			width = 1;
		if (height < 1)
			height = 1;
		boolean[][] binary = new boolean[width][height];
		for (int x = 0; x < im.getWidth(); x++) 
			for (int y = 0; y < im.getHeight(); y++) {
				int grayness = im.getRGB(x, y) & BLUE;
				binary[x][y] = (grayness < MID_COLOR);
			}
		if (im.getWidth() < 1 || im.getHeight() < 1)
			for (int x = 0; x < width; x++) 
				for (int y = 0; y < height; y++) 
					binary[x][y] = false;
		return binary;
	}

	// Turn binary representation into buffered image.
	public static BufferedImage toBufferedImage(boolean[][] binary) {
		return toBufferedImageColor(binary, BLACK);
	}
	public static BufferedImage toBufferedImageColor(boolean[][] binary, int color) {
		BufferedImage im = new BufferedImage(width(binary), height(binary),
				BufferedImage.TYPE_BYTE_BINARY);
		for (int x = 0; x < width(binary); x++) 
			for (int y = 0; y < height(binary); y++) 
				if (binary[x][y]) 
					im.setRGB(x, y, color);
				else
					im.setRGB(x, y, WHITE);
		return im;
	}

	// Turn image into buffered image.
	public BufferedImage toBufferedImage() {
		return toBufferedImage(im);
	}
	public BufferedImage toBufferedImageColor(int color) {
		return toBufferedImageColor(im, color);
	}
	// Same as above, but after scaling.
	public BufferedImage toBufferedImage(double scale) {
		BufferedImage buffer = toBufferedImage(im);
		return ImageUtil.scale(buffer, scale);
	}

	// Add a border with white pixels.
	public static boolean[][] addBorder(boolean[][] im, int border) {
		boolean[][] b = 
			new boolean[width(im) + 2 * border][height(im) + 2 * border];
		for (int x = 0; x < width(b); x++)
			for (int y = 0; y < border; y++) {
				b[x][y] = false;
				b[x][y + border + height(im)] = false;
			}
		for (int x = 0; x < border; x++)
			for (int y = border; y < height(im); y++) {
				b[x][y] = false;
				b[x + border + width(im)][y] = false;
			}
		for (int x = 0; x < width(im); x++)
			for (int y = 0; y < height(im); y++) {
				b[x + border][y + border] = im[x][y];
			}
		return b;
	}
	// As above but to this image.
	public void addBorder(int border) {
		im = addBorder(im, border);
	}

	// Write binary image to file.
	public static void write(boolean[][] binary, String formatName, File file) 
		throws IOException {
			BufferedImage buffer = toBufferedImage(binary);
			ImageIO.write(buffer, formatName, file);
		}
	// Write binary image to file. Default is png.
	public static void write(boolean[][] binary, File file) 
		throws IOException {
			write(binary, "png", file);
		}

	// Write this to file.
	public void write(String formatName, File file) throws IOException {
		write(im, formatName, file);
	}
	// Write this to file. Default is png
	public void write(File file) throws IOException {
		write("png", file);
	}

	// Scaling of binary image to have given width.
	public static boolean[][] scaleToWidth(boolean[][] im, int width) {
		BufferedImage buffer = BinaryImage.toBufferedImage(im);
		BufferedImage scaled = ImageUtil.scaleToWidth(buffer, width);
		return BinaryImage.binarize(scaled);
	}

	// Scaling of binary image to have given height.
	public static boolean[][] scaleToHeight(boolean[][] im, int height) {
		BufferedImage buffer = BinaryImage.toBufferedImage(im);
		BufferedImage scaled = ImageUtil.scaleToHeight(buffer, height);
		return BinaryImage.binarize(scaled);
	}

	// Scaling of binary image to have given dimensions.
	public static boolean[][] scale(boolean[][] im, int width, int height) {
		BufferedImage buffer = BinaryImage.toBufferedImage(im);
		BufferedImage scaled = ImageUtil.scale(buffer, width, height);
		return BinaryImage.binarize(scaled);
	}
	public BinaryImage scale(int width, int height) {
		return new BinaryImage(scale(im, width, height));
	}

	// Scaling by factor.
	public static boolean[][] scale(boolean[][] im, double scale) {
		BufferedImage buffer = BinaryImage.toBufferedImage(im);
		BufferedImage scaled = ImageUtil.scale(buffer, scale);
		return BinaryImage.binarize(scaled);
	}

	// Keep only outlines of black strokes. That is, there must be
	// bordering pixel that is white (or pixel is at edge).
	public static boolean[][] outline(boolean[][] im) {
		boolean[][] out = new boolean[width(im)][height(im)];
		for (int x = 0; x < width(im); x++) 
			for (int y = 0; y < height(im); y++)
				out[x][y] = 
					im[x][y] && (
							x == 0 || x == width(im) - 1 ||
							y == 0 || y == height(im) - 1 ||
							!im[x-1][y-1] || !im[x][y-1] || !im[x+1][y-1] ||
							!im[x-1][y] || !im[x+1][y] ||
							!im[x-1][y+1] || !im[x][y+1] || !im[x+1][y+1]);
		return out;
	}

	// Equal.
	public boolean equalTo(BinaryImage other) {
		if (other.width() != width() || other.height() != height())
			return false;
		for (int x = 0; x < width(); x++) 
			for (int y = 0; y < height(); y++) 
				if (other.get(x, y) != get(x, y))
					return false;
		return true;
	}

}
