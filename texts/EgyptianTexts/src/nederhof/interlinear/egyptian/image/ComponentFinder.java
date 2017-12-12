package nederhof.interlinear.egyptian.image;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

// Find connected components in polygon in image.
public class ComponentFinder {

    // Parameters of image analysis.
    // Required connectedness of black pixels.
    private int connect = SettingsWindow.connectDefault;
    // Minimum of component size.
    private int size = SettingsWindow.sizeDefault;

    // Constructor.
    public ComponentFinder(int connect, int size) {
	this.connect = connect;
	this.size = size;
    }

    // In shape in image, find connected components.
    public LinkedList<LinkedList<Point>> findComponents(BufferedImage image, 
	    Shape shape) {
	Rectangle rect = shape.getBounds();
	int xOffset = -rect.x;
	int yOffset = -rect.y;
	return moveComps(findComponentsInShape(image, shape), xOffset, yOffset);
    }
    // In shape in image, find bounding boxes of connected components.
    public LinkedList<Rectangle> findRectangles(BufferedImage image, 
	    Shape shape) {
	Rectangle rect = shape.getBounds();
	int xOffset = -rect.x;
	int yOffset = -rect.y;
	return moveRects(rects(findComponentsInShape(image, shape)), xOffset, yOffset);
    }
    // In image, find bounding boxes of connected components.
    public LinkedList<Rectangle> findRectangles(BufferedImage image) {
	return rects(findComponents(image));
    }
    // In shape in image, find connected components, positioned relative to shape.
    public LinkedList<LinkedList<Point>> findComponentsInShape(BufferedImage image,
	    Shape shape) {
	BufferedImage cutout = cutout(image, shape);
	return findComponents(cutout);
    }

    // Cut out shape. Make rest white.
    public BufferedImage cutout(BufferedImage image, Shape shape) {
	Rectangle rect = shape.getBounds();
	BufferedImage cutout = new BufferedImage(
		rect.width, rect.height, image.getType());
	Graphics2D g = cutout.createGraphics();
	g.setBackground(Color.WHITE);
	g.clearRect(0, 0, cutout.getWidth(), cutout.getHeight());
	g.translate(-rect.x, -rect.y);
	g.setClip(shape);
	g.drawImage(image, 0, 0, null);
	g.dispose();
	return cutout;
    }

    // Using binary images only.
    public static int WHITE = 1;
    public static int BLACK = 0;

    // Find connected components
    public LinkedList<LinkedList<Point>> findComponents(BufferedImage image) {
	LinkedList<LinkedList<Point>> comps = new LinkedList<LinkedList<Point>>();
	WritableRaster raster = image.getRaster();
	boolean[][] visit = new boolean[image.getWidth()][image.getHeight()];
	for (int x = 0; x < raster.getWidth(); x++)
	    for (int y = 0; y < raster.getHeight(); y++)
		visit[x][y] = false;
	LinkedList<Point> comp = new LinkedList<Point>();
	for (int x = 0; x < raster.getWidth(); x++)
	    for (int y = 0; y < raster.getHeight(); y++) {
		visit(x, y, raster, visit, comp);
		if (comp.size() >= size) 
		    comps.add(comp);
		comp = new LinkedList<Point>();
	    }
	return comps;
    }

    // Visit neighbouring pixels.
    private void visit(int xFirst, int yFirst, 
	    WritableRaster raster, boolean[][] visit, LinkedList<Point> comp) {
	Vector<Point> todo = new Vector<Point>();
	todo.add(new Point(xFirst, yFirst));
	while (!todo.isEmpty()) {
	    Point next = todo.remove(todo.size()-1);
	    int x = next.x;
	    int y = next.y;
	    if (x < 0 || x >= raster.getWidth() || y < 0 || y >= raster.getHeight())
		continue;
	    if (!visit[x][y]) {
		visit[x][y] = true;
		if (raster.getSample(x, y, 0) == BLACK) {
		    comp.add(new Point(x, y));
		    if (connect <= 0) {
			todo.add(new Point(x - 1, y - 1));
			todo.add(new Point(x - 1, y + 1));
			todo.add(new Point(x + 1, y - 1));
			todo.add(new Point(x + 1, y + 1));
		    } else 
			for (int xDiff = -connect; xDiff <= connect; xDiff++)
			    for (int yDiff = -connect; yDiff <= connect; yDiff++)
				if (xDiff != 0 || yDiff != 0)
				    todo.add(new Point(x + xDiff, y + yDiff));
		}
	    }
	}
    }

    // Turn components into bounding rectangles.
    public LinkedList<Rectangle> rects(LinkedList<LinkedList<Point>> comps) {
	LinkedList<Rectangle> rects = new LinkedList<Rectangle>();
	for (LinkedList<Point> comp : comps) {
	    int xMin = Integer.MAX_VALUE;
	    int xMax = 0;
	    int yMin = Integer.MAX_VALUE;
	    int yMax = 0;
	    for (Point p : comp) {
		xMin = Math.min(xMin, p.x);
		xMax = Math.max(xMax, p.x);
		yMin = Math.min(yMin, p.y);
		yMax = Math.max(yMax, p.y);
	    }
	    if (xMin <= xMax) {
		int w = xMax - xMin + 1;
		int h = yMax - yMin + 1;
		rects.add(new Rectangle(xMin, yMin, w, h));
	    }
	}
	return rects;
    }

    // Move all components.
    public LinkedList<LinkedList<Point>> moveComps(LinkedList<LinkedList<Point>> inComps,
	    int xOffset, int yOffset) {
	LinkedList<LinkedList<Point>> outComps = new LinkedList<LinkedList<Point>>();
	for (LinkedList<Point> inComp : inComps) {
	    LinkedList<Point> outComp = new LinkedList<Point>();
	    for (Point p : inComp)
		outComp.add(new Point(p.x + xOffset, p.y + yOffset));
	    outComps.add(outComp);
	}
	return outComps;
    }

    // Move all bounding rectangles.
    public LinkedList<Rectangle> moveRects(LinkedList<Rectangle> inRects,
	    int xOffset, int yOffset) {
	LinkedList<Rectangle> outRects = new LinkedList<Rectangle>();
	for (Rectangle inRect : inRects) 
	    outRects.add(new Rectangle(inRect.x + xOffset, inRect.y + yOffset,
			inRect.width, inRect.height));
	return outRects;
    }

    // Order components according to reading order.
    public LinkedList<Rectangle> order(LinkedList<Rectangle> inRects, String dir) {
	LinkedList<Rectangle> outRects = (LinkedList<Rectangle>) inRects.clone();
	Collections.sort(outRects, comparator(dir));
	return outRects;
    }

    // Give comparator belonging to direction.
    public Comparator<Rectangle> comparator(String dir) {
	if (dir.equals("hlr"))
	    return new HlrComparator();
	else if (dir.equals("hrl"))
	    return new HrlComparator();
	else if (dir.equals("vlr"))
	    return new VlrComparator();
	else
	    return new VrlComparator();
    }

    // Given collection of rectangles, give one that is next in the ordering,
    // after given rectangle.
    public Rectangle next(LinkedList<Rectangle> rects, Rectangle current, String dir) {
	Comparator<Rectangle> comp = comparator(dir);
	for (Rectangle rect : rects) 
	    if (comp.compare(rect, current) > 0)
		return rect;
	return null;
    }
    // Give previous.
    public Rectangle previous(LinkedList<Rectangle> rects, Rectangle current, String dir) {
	Comparator<Rectangle> comp = comparator(dir);
	Rectangle lastSmaller = null;
	for (Rectangle rect : rects) 
	    if (comp.compare(rect, current) < 0)
		lastSmaller = rect;
	return lastSmaller;
    }

    // Comparing glyphs with x position first.
    private class HlrComparator implements Comparator<Rectangle> {
        public int compare(Rectangle r1, Rectangle r2) {
            if (r1.x < r2.x)
                return -1;
            else if (r1.x > r2.x)
                return 1;
            else if (r1.y < r2.y)
                return -1;
            else if (r1.y > r2.y)
                return 1;
            else
                return 0;
        }
    }
    // Comparing glyphs with x position first. Mirrored.
    private class HrlComparator implements Comparator<Rectangle> {
        public int compare(Rectangle r1, Rectangle r2) {
            if (r1.x < r2.x)
                return 1;
            else if (r1.x > r2.x)
                return -1;
            else if (r1.y < r2.y)
                return -1;
            else if (r1.y > r2.y)
                return 1;
            else
                return 0;
        }
    }
    // Comparing glyphs with y position first.
    private class VlrComparator implements Comparator<Rectangle> {
        public int compare(Rectangle r1, Rectangle r2) {
            if (r1.y < r2.y)
                return -1;
            else if (r1.y > r2.y)
                return 1;
            else if (r1.x < r2.x)
                return -1;
            else if (r1.x > r2.x)
                return 1;
            else
                return 0;
        }
    }
    // Comparing glyphs with y position first. Mirrored.
    private class VrlComparator implements Comparator<Rectangle> {
        public int compare(Rectangle r1, Rectangle r2) {
            if (r1.y < r2.y)
                return -1;
            else if (r1.y > r2.y)
                return 1;
            else if (r1.x < r2.x)
                return 1;
            else if (r1.x > r2.x)
                return -1;
            else
                return 0;
        }
    }

}
