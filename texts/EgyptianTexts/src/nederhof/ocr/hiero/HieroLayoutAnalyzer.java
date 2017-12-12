package nederhof.ocr.hiero;

import java.awt.*;
import java.io.*;
import java.util.*;

import nederhof.ocr.*;
import nederhof.ocr.images.*;
import nederhof.util.*;

// Which predicts whether blobs are hieroglyphs.
public class HieroLayoutAnalyzer extends LayoutAnalyzer {

	/////////////////////////////////////////////////////////////
	// Identifying glyphs.
	
	// Default predicted unit size.
	private static final int defaultSize = 20;

	// Size below which no glyph is considered.
	private static final int minSize = 6;

	// From blobs, predict what the unit size could be.
	public int predictUnitSize(Vector<Blob> blobs) {
		int unitSize = defaultSize;
		if (blobs.size() < 1)
			return unitSize;
		int[] sizes = new int[2 * blobs.size()];
		int n = 0;
		for (Blob blob : blobs) {
			if (blob.width() >= minSize && blob.height() >= minSize) {
				sizes[n++] = blob.height();
				sizes[n++] = blob.width();
			}
		}
		if (n > 0) {
			Arrays.sort(sizes, 0, n);
			unitSize = sizes[18*n/20];
		}
		return unitSize;
	}

	// Is potential sign.
	// Thin strokes in either direction are allowed, but not
	// small dots.
	public boolean maybeGlyph(Blob blob, int unitSize) {
		if (!blob.getName().equals(""))
			return true;
		double strokeSize = 1.0 / 8;
		double dotSize = 1.0 / 20;
		return 
			(blob.width() > unitSize * strokeSize && blob.height() > unitSize * dotSize) ||
			(blob.width() > unitSize * dotSize && blob.height() > unitSize * strokeSize);
	}

	/////////////////////////////////////////////////////////////
	// Page processing.

	// In page, find components, exclude lines in margin.
	// Then identify lines of hieroglyphic.
	public Vector<Polygon> findLines(BinaryImage im) {
		Vector<Blob> blobs = findBlobs(im);
		Rectangle margins = findLineMargins(im, blobs);
		blobs = findBetweenMargins(blobs, margins);
		Vector<Blob> glyphs = findSmall(im, blobs);
		int unit = predictUnitSize(glyphs);
		glyphs = maybeGlyphs(glyphs, unit);
		double narrowRatio = 2;
		Vector<Blob> narrowGlyphs = findRatioBelow(glyphs, narrowRatio);
		int[] fill = findRowFill(im, narrowGlyphs);
		Vector<Interval> intervals = findIntervals(im, fill, margins, unit);
		distributeEmpty(intervals, unit);
		Vector<Polygon> polys = new Vector<Polygon>();
		for (Interval inter : intervals) {
			Vector<Blob> lineBlobs = findGlyphsIn(blobs, inter);
			lineBlobs = findBigger(lineBlobs, unit * 0.5);
			Vector<Blob> singleBlobs = excludeStacked(lineBlobs);
			double ratio = findMedianRatio(singleBlobs);
			double maxHieroRatio = 1.5;
			if (ratio < maxHieroRatio) {
				Vector<Blob> lineGlyphs = findGlyphsIn(glyphs, inter);
				widenWidth(im, inter, lineGlyphs, unit);
				polys.add(toPoly(im, inter, unit));
			}
		}
		return polys;
	}

	// Remove glyphs on top of another. The idea is that narrow hieroglyphs
	// are stacked on top of each other. This is unlike alphabetic
	// handwriting.
	private Vector<Blob> excludeStacked(Vector<Blob> glyphs) {
		Vector<Blob> included = new Vector<Blob>();
		for (int i = 0; i < glyphs.size(); i++) {
			boolean include = true;
			for (int j = i+1; j < glyphs.size(); j++) {
				if (areStacked(glyphs.get(i), glyphs.get(j))) {
					include = false;
					break;
				}
			}
			if (include)
				included.add(glyphs.get(i));
		}
		return included;
	}
	private boolean areStacked(Blob b1, Blob b2) {
		return b1.x() < b2.x() + b2.width() && b2.x() < b1.x() + b1.width();
	}

	/////////////////////////////////////////////////////////////
	// Ordering.

	public Vector<Blob> order(Vector<Blob> glyphs, String dir) {
		if (dir.equals("hlr") || dir.equals("hrl")) 
			return orderHorizontal(glyphs);
		else
			return orderVertical(glyphs);
	}

	// The below are not currently used.

    // Separate in groups horizontally. A new group is started
    // if none of the following glyphs overlap more than a certain
    // portion of their length with the preceding glyphs.
    // The use of 'length' is for ensuring terminal in recursion.
    private Vector<Blob> groupHorizontally(Vector<Blob> inBlobs) {
        return groupHorizontally(inBlobs, inBlobs.size());
    }
    private Vector<Blob> groupHorizontally(Vector<Blob> inBlobs, int length) {
        // start of new group is marked by glyphs going beyond previous
        // group by given portion.
        final double beyond = 0.7;
        Vector<Blob> sortBlobs = (Vector<Blob>) inBlobs.clone();
        Collections.sort(sortBlobs, new XComparator());
        Vector<Blob> previous = new Vector<Blob>();
        Vector<Blob> currentGroup = new Vector<Blob>();
        int xEnd = 0;
        for (int i = 0; i < sortBlobs.size(); i++) {
            boolean nextGroup = true;
            for (int j = i; j < sortBlobs.size(); j++) {
                Blob next = sortBlobs.get(j);
                if (next.x() >= xEnd)
                    break;
                else if (1.0 * (next.x() + next.width() - xEnd) /
                        next.width() < beyond) {
                    nextGroup = false;
                    break;
                }
            }
            if (nextGroup && !currentGroup.isEmpty()) {
                previous.addAll(groupVertically(currentGroup, length));
                currentGroup = new Vector<Blob>();
            }
            Blob blob = sortBlobs.get(i);
            currentGroup.add(blob);
            xEnd = Math.max(xEnd, blob.x() + blob.width());
        }
        if (!currentGroup.isEmpty())
            previous.addAll(groupVertically(currentGroup, length));
        return previous;
    }

    // As above, but vertically. Do recursive call, unless the previous passes
    // have not reduced the number of components.
    private Vector<Blob> groupVertically(Vector<Blob> inBlobs, int length) {
        // start of new group is marked by glyphs going beyond previous
        // group by given portion.
        final double beyond = 0.7;
        Vector<Blob> sortBlobs = (Vector<Blob>) inBlobs.clone();
        Collections.sort(sortBlobs, new YComparator());
        Vector<Blob> previous = new Vector<Blob>();
        Vector<Blob> currentGroup = new Vector<Blob>();
        int yEnd = 0;
        for (int i = 0; i < sortBlobs.size(); i++) {
            boolean nextGroup = true;
            for (int j = i; j < sortBlobs.size(); j++) {
                Blob next = sortBlobs.get(j);
                if (next.y() >= yEnd)
                    break;
                else if (1.0 * (next.y() + next.height() - yEnd) /
                        next.height() < beyond) {
                    nextGroup = false;
                    break;
                }
            }
            if (nextGroup && !currentGroup.isEmpty()) {
                previous.addAll(groupPerhapsHorizontally(currentGroup, length));
                currentGroup = new Vector<Blob>();
            }
            Blob blob = sortBlobs.get(i);
            currentGroup.add(blob);
            yEnd = Math.max(yEnd, blob.y() + blob.height());
        }
        if (!currentGroup.isEmpty())
            previous.addAll(groupPerhapsHorizontally(currentGroup, length));
        return previous;
    }
    // As above, but only if the number of components is less than length.
    private Vector<Blob> groupPerhapsHorizontally(Vector<Blob> inBlobs, int length) {
        if (inBlobs.size() < length)
            return groupHorizontally(inBlobs, inBlobs.size());
        else
            return inBlobs;
    }

}
