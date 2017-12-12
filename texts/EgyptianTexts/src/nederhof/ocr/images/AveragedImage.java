package nederhof.ocr.images;

import java.awt.*;
import java.awt.color.*;
import java.awt.geom.*;
import java.awt.image.*;

// Image resulting from overlaying a number of binary images.
public class AveragedImage {

	// Number of source images.
	protected int n = 0;

	// Chosen dimensions.
	protected int width;
	protected int height;

	// Values of pixels, as summed from source images.
	protected int[][] im;

	// Constructor.
	public AveragedImage(int width, int height) {
		this.width = width;
		this.height = height;
		im = new int[width][height];
	}

	// Add image.
	public void add(BufferedImage buffered) {
		buffered = ImageUtil.scale(buffered, width, height);
		BinaryImage bin = new BinaryImage(buffered);
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) 
				im[x][y] += bin.get(x, y) ? 1 : 0;
		n++;
	}

	// Produce binary image that is averaged, with threshold one half.
	public BinaryImage averaged() {
		BinaryImage av = new BinaryImage(width, height);
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				av.set(x, y, im[x][y] * 2 >= n);
		return av;
	}

	// Produce binary image where pixels can take mass from neighboring pixels to
	// get to threshold.
	public BinaryImage centerAveraged() {
		int threshold = (int) Math.ceil(n * 3.0 / 5);
		BinaryImage av = new BinaryImage(width, height);
		int[][] copy = new int[width][height];
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				copy[x][y] = im[x][y];
		int window = 0;
		int windowMax = (int) Math.ceil(Math.min(width, height) / 15.0);
		while (sum(copy) >= threshold && window < windowMax) {
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++) {
					if (!av.get(x, y) && mass(copy, x, y, window) >= threshold) {
						av.set(x, y, true);
						deduct(copy, x, y, threshold);
					}

				}
			window += 1;
		}
		return av;
	}

	// Sum values of pixels.
	private int sum(int[][] copy) {
		int sum = 0;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				sum += copy[x][y];
		return sum;
	}

	// Sum mass around pixel, with window.
	private int mass(int[][] copy, int xCenter, int yCenter, int window) {
		int sum = 0;
		for (int x = Math.max(0, xCenter - window); x < Math.min(xCenter + window + 1, width); x++)
			for (int y = Math.max(0, yCenter - window); y < Math.min(yCenter + window + 1, height); y++)
				sum += copy[x][y];
		return sum;
	}

	// Deduct by spiraling around point.
	private void deduct(int[][] copy, int xCenter, int yCenter, int surplus) {
		int window = 0;
		while (surplus > 0) {
			for (int x = Math.max(0, xCenter - window); 
					x < Math.min(xCenter + window + 1, width); x++)
				for (int y = Math.max(0, yCenter - window); 
						y < Math.min(yCenter + window + 1, height); y++) {
					int val = Math.min(copy[x][y], surplus);
					copy[x][y] -= val;
					surplus -= val;
				}
			window++;
		}
	}

}
