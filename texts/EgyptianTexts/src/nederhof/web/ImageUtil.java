package nederhof.web;

import java.awt.*;
import java.util.*;

import nederhof.interlinear.egyptian.image.*;

public class ImageUtil {

    // Cluster rectangles in image together in one or more rectangles.
    // Determine maximum size. Then all that is that far apart, connect
    // into single rectangle.
    public static Vector<ImagePlace> cluster(Vector<ImagePlace> places) {
	int size = 0;
	for (ImagePlace place : places) {
	    size = Math.max(size, place.getWidth());
	    size = Math.max(size, place.getHeight());
	}
	Vector<ImagePlace> combined = new Vector<ImagePlace>(places);
	boolean change = true;
	while (change) {
	    Collections.sort(combined, new XComparator());
	    change = cluster(combined, size);
	    if (!change) {
		Collections.sort(combined, new YComparator());
		change = cluster(combined, size);
	    }
	}
	return combined;
    }

    // Try to combine consecutive rectangles, if they are not too far
    // apart. Return whether there is any change.
    private static boolean cluster(Vector<ImagePlace> places, int size) {
	boolean changed = false;
	for (int low = 0; low < places.size() - 1; low++) {
	    for (int high = low+1; high < places.size(); ) {
		ImagePlace lowRect = places.get(low);
		ImagePlace highRect = places.get(high);
		if (areClose(lowRect, highRect, size)) {
		    places.set(low, merge(lowRect, highRect));
		    places.remove(high);
		    changed = true;
		} else
		    high++;
	    }
	}
	return changed;
    }

    // Are two rectangles less than size apart?
    private static boolean areClose(ImagePlace p1, ImagePlace p2, int size) {
	if (p1.getNum() != p2.getNum())
	    return false;
	Rectangle rect1 = new Rectangle(p1.getX() - size, p1.getY() - size,
		p1.getWidth() + 2 * size, p1.getHeight() + 2 * size);
	Rectangle rect2 = new Rectangle(p2.getX(), p2.getY(),
		p2.getWidth(), p2.getHeight());
	return rect1.intersects(rect2);
    }

    // Merge two rectangles, taking smallest that contains both.
    private static ImagePlace merge(ImagePlace p1, ImagePlace p2) {
	int xMin = Math.min(p1.getX(), p2.getX());
	int xMax = Math.max(p1.getX() + p1.getWidth(), p2.getX() + p2.getWidth());
	int yMin = Math.min(p1.getY(), p2.getY());
	int yMax = Math.max(p1.getY() + p1.getHeight(), p2.getY() + p2.getHeight());
	return new ImagePlace(p1.getNum(), xMin, yMin, xMax - xMin, yMax - yMin);
    }


    // Comparing rectangles with y position.
    private static class YComparator implements Comparator<ImagePlace>  {
        public int compare(ImagePlace p1, ImagePlace p2) {
            if (p1.getNum() < p2.getNum())
                return -1;
	    else if (p1.getNum() > p2.getNum())
                return 1;
	    else if (p1.getY() < p2.getY())
                return -1;
            else if (p1.getY() > p2.getY())
                return 1;
            else if (p1.getX() < p2.getX())
                return -1;
            else if (p1.getX() > p2.getX())
                return 1;
            else
                return 0;
        }
    }

    // Comparing rectangles with x position.
    private static class XComparator implements Comparator<ImagePlace> {
        public int compare(ImagePlace p1, ImagePlace p2) {
            if (p1.getNum() < p2.getNum())
                return -1;
	    else if (p1.getNum() > p2.getNum())
                return 1;
	    else if (p1.getX() < p2.getX())
                return -1;
            else if (p1.getX() > p2.getX())
                return 1;
            else if (p1.getY() < p2.getY())
                return -1;
            else if (p1.getY() > p2.getY())
                return 1;
            else
                return 0;
        }
    }

    // Extract surfaces. Make them into Bezier curves.
    // Order from small surface to big surface.
    public static Vector<TaggedBezier> areas(TreeMap<String,Vector<ImagePlace>> lexPlaces) {
	Vector<TaggedBezier> beziers = new Vector<TaggedBezier>();
	for (Map.Entry<String,Vector<ImagePlace>> entry : lexPlaces.entrySet()) {
	    String name = entry.getKey();
	    Vector<ImagePlace> places = entry.getValue();
	    for (int i = 0; i < places.size(); i++) {
		ImagePlace place = places.get(i);
		String indexedName = name + ":" + i;
		Rectangle rect = new Rectangle(place.getX(), place.getY(),
			place.getWidth(), place.getHeight());
		TaggedBezier bezier = new TaggedBezier(rect, place.getNum(), indexedName);
		beziers.add(bezier);
	    }
	}
	Collections.sort(beziers, new AreaComparator());
	return beziers;
    }

    // Comparing beziers based on area.
    private static class AreaComparator implements Comparator<TaggedBezier> {
        public int compare(TaggedBezier b1, TaggedBezier b2) {
	    int num1 = b1.getNum();
	    int num2 = b2.getNum();
	    int area1 = b1.area();
	    int area2 = b2.area();
	    if (num1 < num2)
		return -1;
	    else if (num1 > num2)
		return 1;
	    else if (area1 < area2)
		return -1;
	    else if (area1 > area2)
		return 1;
            else 
                return 0;
        }
    }

}
