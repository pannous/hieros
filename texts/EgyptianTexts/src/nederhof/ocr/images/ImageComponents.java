package nederhof.ocr.images;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

public class ImageComponents {

	// Visit every pixel. Connect points to neighbouring black pixels.
	public static Vector<Vector<Point>> find(BinaryImage im) {
		boolean[][] visited = new boolean[im.width()][im.height()];
		Vector<Vector<Point>> components = new Vector<Vector<Point>>();
		Vector<Point> pixels = new Vector<Point>();
		for (int x = 0; x < im.width(); x++) {
			for (int y = 0; y < im.height(); y++) {
				visitIterative(x, y, im, visited, pixels);
				if (pixels.size() > 0) {
					components.add(pixels);
					pixels = new Vector<Point>();
				}
			}
		}
		return components;
	}

	// Visit every pixel among the input vector. 
	// Return all reachable components merged.
	public static Vector<Point> find(BinaryImage im, 
			Vector<Point> fromPoints) {
		boolean[][] visited = new boolean[im.width()][im.height()];
		Vector<Point> components = new Vector<Point>();
		Vector<Point> pixels = new Vector<Point>();
		for (Point p : fromPoints) {
			visitIterative(p.x, p.y, im, visited, pixels);
			if (pixels.size() > 0) {
				components.addAll(pixels);
				pixels = new Vector<Point>();
			}
		}
		return components;
	}

	// Find component connected to pixel.
	public static Vector<Point> find(BinaryImage im, int x, int y) {
		boolean[][] visited = new boolean[im.width()][im.height()];
		Vector<Point> pixels = new Vector<Point>();
		visitIterative(x, y, im, visited, pixels);
		return pixels;
	}

	// Visit pixel and neighbouring ones.
	private static void visitIterative(int xFirst, int yFirst, 
			BinaryImage im, boolean[][] visited, Vector<Point> pixels) {
		Vector<Point> todo = new Vector<Point>();
		todo.add(new Point(xFirst, yFirst));
		while (!todo.isEmpty()) {
			Point next = todo.remove(todo.size()-1);
			int x = next.x;
			int y = next.y;
			if (x < 0 || x >= im.width() || y < 0 || y >= im.height())
				continue;
			if (!visited[x][y]) {
				visited[x][y] = true;
				if (im.get(x,y)) {
					pixels.add(new Point(x, y));
					for (int xDiff = -1; xDiff <= 1; xDiff++)
						for (int yDiff = -1; yDiff <= 1; yDiff++)
							if (xDiff != 0 || yDiff != 0)
								todo.add(new Point(x + xDiff, y + yDiff));
				}
			}
		}
	}

	// Create image out of pixels.
	public static BinaryImage constructImage(Vector<Point> points) {
		if (points.size() == 0) 
			return new BinaryImage(1, 1);
		int xMin = Integer.MAX_VALUE;
		int xMax = Integer.MIN_VALUE;
		int yMin = Integer.MAX_VALUE;
		int yMax = Integer.MIN_VALUE;
		for (Point p : points) {
			xMin = Math.min(xMin, p.x);
			xMax = Math.max(xMax, p.x);
			yMin = Math.min(yMin, p.y);
			yMax = Math.max(yMax, p.y);
		}
		int x = xMin;
		int y = yMin;
		int width = xMax - xMin + 1;
		int height = yMax - yMin + 1;
		BinaryImage im = new BinaryImage(width, height);
		for (Point p : points)
			im.set(p.x - x, p.y - y, true);
		return im;
	}

}
