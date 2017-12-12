package nederhof.ocr;

import java.awt.*;
import java.io.*;
import java.util.*;

import nederhof.ocr.images.*;
import nederhof.util.*;

// Analysis of layout of lines and pages.
public abstract class LayoutAnalyzer {

	/////////////////////////////////////////////////////////////
	// Identifying glyphs.

	// From glyphs, predict what the unit size could be.
	public abstract int predictUnitSize(Vector<Blob> blobs);

	// Is potential glyph?
	public abstract boolean maybeGlyph(Blob blob, int unitSize);

	// Filter potential glyphs.
	public Vector<Blob> maybeGlyphs(Vector<Blob> blobs, int unitSize) {
		Vector<Blob> filtered = new Vector<Blob>();
		for (Blob b : blobs) 
			if (maybeGlyph(b, unitSize))
				filtered.add(b);
		return filtered;
	}

	// Find connected components, and turn them into blobs.
	public Vector<Blob> findBlobs(BinaryImage im) {
		return findBlobs(im, 0, 0);
	}
	// Correct coordinates with offsets.
	public Vector<Blob> findBlobs(BinaryImage im, int xOffset, int yOffset) {
		Vector<Vector<Point>> components = ImageComponents.find(im);
		Vector<Blob> glyphs = new Vector<Blob>();
		for (Vector<Point> comp : components) {
			Vector<Point> offsetComp = new Vector<Point>();
			for (Point p : comp) {
				int x = p.x + xOffset;
				int y = p.y + yOffset;
				offsetComp.add(new Point(x, y));
			}
			Blob glyph = new Blob(offsetComp);
			glyphs.add(glyph);
		}
		return glyphs;
	}

	/////////////////////////////////////////////////////////////
	// Page processing.

	// In page, find lines.
	public abstract Vector<Polygon> findLines(BinaryImage im);

    // Find blobs of right size to be glyphs. I.e. not too big relative to
    // page size.
    protected Vector<Blob> findSmall(BinaryImage im, Vector<Blob> blobs) {
        double maxSizeFactor = 0.05;
		double limit = maxSizeFactor * Math.min(im.width(), im.height());
		return findSmaller(blobs, limit);
	}
    protected Vector<Blob> findSmaller(Vector<Blob> blobs, double limit) {
        Vector<Blob> glyphs = new Vector<Blob>();
        for (Blob b : blobs)
            if (b.width() <= limit && b.height() <= limit)
                glyphs.add(b);
        return glyphs;
    }

    protected Vector<Blob> findBigger(Vector<Blob> blobs, double limit) {
        Vector<Blob> glyphs = new Vector<Blob>();
        for (Blob b : blobs)
            if (b.width() >= limit && b.height() >= limit)
                glyphs.add(b);
        return glyphs;
    }

	// Find blobs of which width/height ratio is below threshold.
	protected Vector<Blob> findRatioBelow(Vector<Blob> inBlobs, double ratio) {
        Vector<Blob> outBlobs = new Vector<Blob>();
		for (Blob b : inBlobs)
			if (b.width() * 1.0 / b.height() < ratio)
                outBlobs.add(b);
        return outBlobs;
	}

	protected Vector<Blob> findBetweenMargins(Vector<Blob> inBlobs, Rectangle margins) {
        Vector<Blob> outBlobs = new Vector<Blob>();
		for (Blob blob : inBlobs) 
			if (blob.x() >= margins.x && 
					blob.x() + blob.width() <= margins.x + margins.width &&
					blob.y() >= margins.y && 
					blob.y() + blob.height() <= margins.y + margins.height)
				outBlobs.add(blob);
		return outBlobs;
	}

    // If there is big component (line?) at top of page, it likely marks the
    // header, so ignore all that is above.
    // If there is big component (line?) at right, everything to the right is 
    // likely to be page numbers, so ignore all that.
    protected Rectangle findLineMargins(BinaryImage im, Vector<Blob> blobs) {
        double lineSizeFactor = 0.4;
        double leftMarginFactor = 0.25;
        double rightMarginFactor = 0.75;
        double headerFactor = 0.2;
        double footerFactor = 0.8;
		return findLineMargins(im, blobs, lineSizeFactor,
			leftMarginFactor, rightMarginFactor,
			headerFactor, footerFactor);
	}
    protected Rectangle findLineMargins(BinaryImage im, Vector<Blob> blobs, double lineSizeFactor,
			double leftMarginFactor, double rightMarginFactor,
			double headerFactor, double footerFactor) {
        int minX = 0;
        int maxX = im.width()-1;
        int minY = 0;
        int maxY = im.height()-1;
        for (Blob b : blobs) {
            if (b.width() > lineSizeFactor * im.width() &&
                    b.x() < leftMarginFactor * im.width() &&
                    (b.y() < headerFactor * im.height() ||
                     b.y() + b.height() > footerFactor * im.height()) ) {
				minX = minX == 0 ? b.x() : Math.min(minX, b.x());
			}
            if (b.width() > lineSizeFactor * im.width() &&
                    b.x() + b.width() > rightMarginFactor * im.width() &&
                    (b.y() < headerFactor * im.height() ||
                     b.y() + b.height() > footerFactor * im.height()) ) {
                maxX = maxX == im.width()-1 ? b.x() + b.width() : Math.max(maxX, b.x() + b.width());
			}
            if (b.height() > lineSizeFactor * im.height() &&
                    b.y() < headerFactor * im.height()) {
				minY = minY == 0 ? b.y() : Math.min(minY, b.y());
			}
            if (b.height() > lineSizeFactor * im.height() &&
                    b.y() + b.height() > footerFactor * im.height()) {
                maxY = maxY == im.height()-1 ? b.y() + b.height() : Math.max(maxY, b.y() + b.height());
			}
        }
        return new Rectangle(minX, minY, maxX-minX, maxY-minY);
    }

	// For each row, find how much is covered by glyphs.
	protected int[] findRowFill(BinaryImage im, Vector<Blob> glyphs) {
		int[] fill = new int[im.height()];
		for (Blob b : glyphs) {
			for (int y = 0; y < b.height(); y++)
				fill[b.y() + y] += b.width();
		}
		return fill;
	}

	// Find intervals of rows that could be within lines of text.
	// They should at least contain one unit worth of glyphs.
	protected Vector<Interval> findIntervals(BinaryImage im, int[] fill,
			Rectangle margins, int unit) {
		double lineThreshold = unit * 0.5;
		Vector<Interval> intervals = new Vector<Interval>();
		Interval interval = null;
		for (int y = 0; y < im.height(); y++) {
			if (fill[y] > lineThreshold) {
				if (interval == null)
					interval = new Interval(y, 1, margins.x + 1, margins.x + margins.width - 1);
				else
					interval.height++;
			} else if (interval != null) {
				if (interval.height > lineThreshold)
					intervals.add(interval);
				interval = null;
			}
		}
		if (interval != null && interval.height > lineThreshold)
			intervals.add(interval);
		return intervals;
	}

	// Distribute empty space between lines.
	protected void distributeEmpty(Vector<Interval> intervals, int unit) {
		for (int i = 0; i < intervals.size() - 1; i++) {
			Interval thisInter = intervals.get(i);
			Interval nextInter = intervals.get(i+1);
			int empty = nextInter.y - (thisInter.y + thisInter.height);
			thisInter.height += 2 * empty / 8;
			nextInter.y -= 4 * empty / 8;
			nextInter.height += 4 * empty / 8;
		}
		if (intervals.size() > 0) {
			Interval first = intervals.get(0);
			first.height += unit / 3;
			first.y -= unit / 3;

			Interval last = intervals.get(intervals.size()-1);
			last.height += unit / 4;
		}
	}

	// Determine width of lines, by means of glyphs within them.
	protected void widenWidth(BinaryImage im, Interval inter, Vector<Blob> glyphs, int unit) {
		inter.xMin = im.width();
		inter.xMax = 0;
		int margin = unit / 2;
		for (Blob glyph : glyphs) {
			inter.xMin = Math.min(inter.xMin, glyph.x() - margin);
			inter.xMax = Math.max(inter.xMax, glyph.x() + glyph.width() + margin);
		}
	}

	// Determine median of ratio between width and height in interval.
	protected double findMedianRatio(Vector<Blob> glyphs) {
		double[] ratios = new double[glyphs.size()];
		for (int i = 0; i < glyphs.size(); i++)
			ratios[i] = glyphs.get(i).width() * 1.0 / glyphs.get(i).height();
		Arrays.sort(ratios);
		return ratios.length > 0 ? ratios[ratios.length / 2] : 1;
	}

	// Interval of rows, with y coordinate of first row,
	// length, first and last x coordinates.
    public class Interval {
        public int y;
        public int height;
        public int xMin;
        public int xMax;
        public Interval(int y, int height,
                int xMin, int xMax) {
            this.y = y;
            this.height = height;
            this.xMin = xMin;
            this.xMax = xMax;
        }
		public String toString() {
			return "" + y + " " + height + " " + xMin + " " + xMax;
		}
    }

	protected boolean isIn(Blob glyph, Interval inter) {
		return glyph.x() >= inter.xMin && glyph.x() + glyph.width() <= inter.xMax &&
			glyph.y() >= inter.y && glyph.y() + glyph.height() <= inter.y + inter.height;
	}

	protected Vector<Blob> findGlyphsIn(Vector<Blob> inGlyphs, Interval inter) {
		Vector<Blob> outGlyphs = new Vector<Blob>();
		for (Blob glyph : inGlyphs)
			if (isIn(glyph, inter))
				outGlyphs.add(glyph);
		return outGlyphs;
	}

	// Turn interval into polygon. Keep away from edges.
	protected Polygon toPoly(BinaryImage im, Interval inter, int unit) {
		int xMin = Math.max(inter.xMin, unit);
		int xMax = Math.min(inter.xMax, im.width() - unit);
		Polygon poly = new Polygon();
		poly.addPoint(xMin, inter.y);
		poly.addPoint(xMax, inter.y);
		poly.addPoint(xMax, inter.y + inter.height);
		poly.addPoint(xMin, inter.y + inter.height);
		return poly;
	}

	/////////////////////////////////////////////////////////////
	// Ordering.

	// Ordering, assuming text direction.
	public abstract Vector<Blob> order(Vector<Blob> glyphs, String dir);

	// Simple orderings based on left or top edges.
	protected Vector<Blob> orderHorizontal(Vector<Blob> glyphs) {
		Vector<Blob> sorted = (Vector<Blob>) glyphs.clone();
		Collections.sort(sorted, new XComparator());
		return sorted;
	}
	protected Vector<Blob> orderVertical(Vector<Blob> glyphs) {
		Vector<Blob> sorted = (Vector<Blob>) glyphs.clone();
		Collections.sort(sorted, new YComparator());
		return sorted;
	}

	////////////////////////////////////////////////
	// Util.

	// Comparing glyphs with y position first.
	public class YComparator implements Comparator<Blob> {
		public int compare(Blob b1, Blob b2) {
			if (b1.y() < b2.y())
				return -1;
			else if (b1.y() > b2.y())
				return 1;
			else if (b1.x() < b2.x())
				return -1;
			else if (b1.x() > b2.x())
				return 1;
			else
				return 0;
		}
	}

	// Comparing glyphs with x position first.
	public class XComparator implements Comparator<Blob> {
		public int compare(Blob b1, Blob b2) {
			if (b1.x() < b2.x())
				return -1;
			else if (b1.x() > b2.x())
				return 1;
			else if (b1.y() < b2.y())
				return -1;
			else if (b1.y() > b2.y())
				return 1;
			else
				return 0;
		}
	}

}
