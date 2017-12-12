package nederhof.ocr.images.distance;

import nederhof.ocr.images.*;

// Image Distortion Model, changed to give no penalty to extra black pixels.
public class AdditiveIDM {

	// Size of context.
	protected int context = 5;
	public void setContext(int context) {
		this.context = context;
	}

	// Warp range.
	protected int warp = 2;
	public void setWarp(int warp) {
		this.warp = warp;
	}

	// Get distance between two images.
	// We assume they are of same size.
	// The second image is sought in the first, but in the
	// first there may be extra pixels, so no penalty if a pixel
	// is white in im2 and black in im1.
	public float distort(BinaryImage im1, BinaryImage im2) {
		int diff = 0;
		for (int x = 0; x < im1.width(); x++)
			for (int y = 0; y < im1.height(); y++) {
				PixelDistortion dist = distort(im1, im2, x, y);
				diff += dist.cost;
			}
		return diff;
	}

	// As above, but paint black pixels in im3 that correspond to black pixels
	// in im2 in the best distortion to im1.
	public void distort(BinaryImage im1, BinaryImage im2, BinaryImage im3) {
		for (int x = 0; x < im1.width(); x++)
			for (int y = 0; y < im1.height(); y++) 
				if (im2.getSafe(x, y)) {
					PixelDistortion dist = distort(im1, im2, x, y);
					im3.setSafe(dist.x, dist.y, true);
				}
	}

	// Record of best distortion of pixel, including distortion cost
	// and coordinate of corresponding pixel in first image.
	private class PixelDistortion {
		public int cost;
		public int x;
		public int y;
		public PixelDistortion(int cost, int x, int y) {
			this.cost = cost;
			this.x = x;
			this.y = y;
		}
	}

	// Get minimum difference between possible contexts within warp range.
	// Add minimum distance.
	public PixelDistortion distort(BinaryImage im1, BinaryImage im2, int x, int y) {
		int minCost = (1 + 2 * context) * (1 + 2 * context);
		int minDist = 0;
		int xBest = x;
		int yBest = y;
		for (int xW = x - warp; xW <= x + warp; xW++)
			for (int yW = y - warp; yW <= y + warp; yW++) {
				int cost = distortContext(im1, im2, xW, yW, x, y);
				if (cost < minCost) {
					minCost = cost;
					minDist = Math.abs(x-xW) + Math.abs(y-yW);
					xBest = xW;
					yBest = yW;
				} else if (cost == minCost &&
						Math.abs(x-xW) + Math.abs(y-yW) < minDist) {
					minDist = Math.abs(x-xW) + Math.abs(y-yW);
					xBest = xW;
					yBest = yW;
				}
			}
		return new PixelDistortion(minCost + minDist, xBest, yBest);
	}

	// Compare blocks with context. 
	// Return number of pixels that are distinct.
	public int distortContext(BinaryImage im1, BinaryImage im2,
			int x1, int y1, int x2, int y2) {
		int cost = 0;
		for (int xDiff = - context; xDiff <= context; xDiff++)
			for (int yDiff = - context; yDiff <= context; yDiff++)
				if (im2.getSafe(x2 + xDiff, y2 + yDiff))
					cost += im1.getSafe(x1 + xDiff, y1 + yDiff) ? 0 : 1;
		return cost;
	}

}
