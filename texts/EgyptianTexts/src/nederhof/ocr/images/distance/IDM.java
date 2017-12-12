package nederhof.ocr.images.distance;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;

import nederhof.ocr.images.*;
import nederhof.util.*;

public class IDM extends DistortionModel {

	// Negative means not taken into account.
	private int normalWidth = -1;
	private int normalHeight = 18;

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

	// Preprocess image.
	public BinaryImage binImage(BufferedImage im) {
		BufferedImage scaled = rescale(im);
		return new BinaryImage(im);
	}

	// Scale to dimensions.
	public BufferedImage rescale(BufferedImage buffer) {
		if (normalWidth > 0 && normalHeight > 0)
			return ImageUtil.scale(buffer, normalWidth, normalHeight);
		else if (normalHeight > 0)
			return ImageUtil.scale(buffer, normalHeight, normalHeight);
		else
			return buffer;
	}

	// Get distance between two images.
	public float distort(BinaryImage im1, BinaryImage im2) {
		int diff = 0;
		for (int x = 0; x < im1.width(); x++)
			for (int y = 0; y < im1.height(); y++)
				diff += distort(im1, im2, x, y);
		return diff;
	}

	// Get minimum difference between possible contexts within warp range.
	public int distort(BinaryImage im1, BinaryImage im2, int x1, int y1) {
		int x2 = x1 * im2.width() / im1.width();
		int y2 = y1 * im2.height() / im1.height();
		int minCost = (1 + 2 * context) * (1 + 2 * context);
		// int minDist = 0;
		for (int x = x2 - warp; x <= x2 + warp; x++)
			for (int y = y2 - warp; y <= y2 + warp; y++) {
				int cost = distortContext(im1, im2, x1, y1, x, y);
				if (cost < minCost) {
					minCost = cost;
					// minDist = Math.abs(x2-x) + Math.abs(y2-y);
				} 
				/*
				else if (cost == minCost &&
						Math.abs(x2-x) + Math.abs(y2-y) < minDist) {
					minDist = Math.abs(x2-x) + Math.abs(y2-y);
				}
				*/
			}
		return minCost;
	}

	// Compare blocks with context. 
	// Return number of pixels that are distinct.
	public int distortContext(BinaryImage im1, BinaryImage im2,
			int x1, int y1, int x2, int y2) {
		int cost = 0;
		for (int xDiff = - context; xDiff <= context; xDiff++)
			for (int yDiff = - context; yDiff <= context; yDiff++)
				cost +=
					im1.getSafe(x1 + xDiff, y1 + yDiff) == 
									im2.getSafe(x2 + xDiff, y2 + yDiff) ?
					0 : 1;
		return cost;
	}

}

