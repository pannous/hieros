package nederhof.ocr.images;

import java.awt.*;
import java.awt.color.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

// Analysis of shading (damaged areas) by diagonal lines.
public class Shade {

    // The image.
    private BinaryImage im;

    // Outcomes of analysis:
    // Length of longest diagonal line.
    private int maxLengthDiagonal;
    // Estimated angle of diagonal lines.
    private double angleDiagonal;
    // Perpendicular to shading.
    private double anglePerpendicular;
    // Estimated stroke width.
    private int strokeWidth;
    // Maximum shade width.
    private int shadeWidth;

    // The lines in the image.
    private BinaryImage lineIm;

    // Constructor.
    public Shade(BinaryImage im) {
	this.im = im;
	maxLengthDiagonal = longestDiagonalLength();
	angleDiagonal = mostFrequentAngle(maxLengthDiagonal * 3 / 4);
	anglePerpendicular = 0.5 * Math.PI - angleDiagonal;
	strokeWidth = medianStrokeWidth();
	shadeWidth = strokeWidth * 3 / 2;
	Vector<PartialLine> distinctLines = buildLines();
	removeLines();
	removeSmallComponents();
	connectSides(distinctLines);
    }

    // Minimum and maximum angles for lines of shading. 
    // Perfect 45 degree angle.
    // Step between angles.
    private double minAngle = 1.0 / 8 * Math.PI;
    private double maxAngle = 3.0 / 8 * Math.PI;
    private double midAngle = 2.0 / 8 * Math.PI;
    private double step = (maxAngle - minAngle) / 30;

    // Draw diagonals, and find longest consecutive line.
    private int longestDiagonalLength() {
	int bestLen = 0;
	for (double ang = minAngle; ang <= maxAngle; ang += step) {
	    int len = longestDiagonal(ang);
	    if (len > bestLen) {
		bestLen = len;
	    }
	}
	return bestLen;
    }

    // Determine for which angle lines with minimum length are most
    // frequent.
    private double mostFrequentAngle(int len) {
	int bestCount = 0;
	double bestAng = midAngle;
	for (double ang = minAngle; ang <= maxAngle; ang += step) {
	    int count = diagonalCount(ang, len);
	    if (count > bestCount) {
		bestCount = count;
		bestAng = ang;
	    }
	}
	return bestAng;
    }

    // Find maximum number of black pixels on diagonal.
    private int longestDiagonal(double ang) {
	int bestLen = 0; 
	for (int xStart = -im.height(); xStart < im.width(); xStart++) {
	    int len = diagonalLength(ang, xStart);
	    if (len > bestLen) {
		bestLen = len;
	    }
	}
	return bestLen;
    }

    // Find number of diagonals at angle with minimum length.
    private int diagonalCount(double ang, int minLen) {
	int n = 0;
	for (int xStart = -im.height(); xStart < im.width(); xStart++) 
	    if (diagonalLength(ang, xStart) >= minLen)
		n++;
	return n;
    }

    // Number of consecutive black pixels on diagonal.
    private int diagonalLength(double ang, int xStart) {
	int max = 0;
	int fill = 0;
	for (int y = 0; y < im.height(); y++) {
	    int x = (int) Math.round((im.height() - y) / Math.tan(ang));
	    if (im.getSafe(xStart + x, y))
		fill++;
	    else 
		fill = 0;
	    max = Math.max(max, fill);
	}
	return max;
    }

    // Median of stroke width.
    private int medianStrokeWidth() {
	Vector<Integer> lens = new Vector<Integer>();
	for (int y = 0; y < im.height(); y++) {
	    int len = 0;
	    for (int x = 0; x < im.width(); x++) {
		if (im.get(x, y))
		    len++;
		else {
		    if (len > 0)
			lens.add(len);
		    len = 0;
		}
	    }
	    if (len > 0)
		lens.add(len);
	}
	Collections.sort(lens);
	if (lens.size() > 0)
	    return lens.get(lens.size() / 2);
	else
	    return 0;
    }

    // Partial line, consisting of:
    // boundaries of top line
    // centre of bottom line
    // number of pixels
    private class PartialLine implements Comparable<PartialLine> {
	public PartialLine prev;
	public int yMin; // highest line
	public int yMax; // lowest line
	public int xMin; // leftmost highest line
	public int xMax; // rightmost highest line
	public int penalty; // what is less than ideal
	// Initial constructor.
	public PartialLine(int xMin, int xMax, int y) {
	    this.prev = null;
	    this.yMin = y;
	    this.yMax = y;
	    this.xMin = xMin;
	    this.xMax = xMax;
	    this.penalty = 0;
	}
	// For extended line.
	public PartialLine(PartialLine prev, int xMin, int xMax, int y, int penalty) {
	    this.prev = prev;
	    this.yMin = y;
	    this.yMax = prev.yMax;
	    this.xMin = xMin;
	    this.xMax = xMax;
	    this.penalty = prev.penalty + penalty;
	}
	// Is last part of line acceptable angle?
	public boolean correctAngle() {
	    LineEssence essence = new LineEssence(this);
	    return essence.correctAngle();
	}
	// Ordering by height, then by penalties. Best comes first.
	public int compareTo(PartialLine other) {
	    if (other.yMax - other.yMin == yMax - yMin)
		return penalty - other.penalty;
	    else
		return (other.yMax - other.yMin) - (yMax - yMin);
	}
	// For debugging.
	public String toString() {
	    return "yMin=" + yMin +
		";yMax=" + yMax +
		";xMin=" + xMin +
		";xMax=" + xMax +
		";penalty=" + penalty;
	}
    }

    // The most important properties of partial line.
    // Two such lines with the same properties exclude one
    // another (greatest height then lowest penalty is taken).
    private class LineEssence {
	// How far are we going back to measure angle.
	private final int historySteps = 8;
	// Allowable deviation of angle of shading.
	private final double angleError = 0.25 / 8 * Math.PI;

	public int yMin; // highest line
	public int yMax; // line some steps back
	public int xMin; // leftmost highest line
	public int xMax; // rightmost highest line
	public int lowMid; // middle of line some steps back
	public int highMid; // middle of highest line 
	public LineEssence(PartialLine line) {
	    this.yMin = line.yMin;
	    this.xMin = line.xMin;
	    this.xMax = line.xMax;
	    this.highMid = line.xMin + (line.xMax - line.xMin) / 2;
	    int steps = historySteps;
	    while (steps > 0 && line.prev != null) {
		line = line.prev;
		steps--;
	    }
	    this.yMax = line.yMin;
	    this.lowMid = line.xMin + (line.xMax - line.xMin) / 2;
	}
	// Is direction okay?
	public boolean correctAngle() {
	    int height = yMax - yMin;
	    double minTan = Math.tan(angleDiagonal - angleError);
	    double maxTan = Math.tan(angleDiagonal + angleError);
	    int xMax = lowMid + (int) Math.ceil(height / minTan);
	    int xMin = lowMid + (int) Math.floor(height / maxTan);
	    return xMin <= highMid && highMid <= xMax;
	}
	public boolean equals(Object obj) {
	    return (obj instanceof LineEssence) &&
		((LineEssence) obj).yMin == yMin &&
		((LineEssence) obj).yMax == yMax &&
		((LineEssence) obj).xMin == xMin &&
		((LineEssence) obj).xMax == xMax &&
		((LineEssence) obj).lowMid == lowMid;
	}
	public int hashCode() {
	    return 
		new Integer(yMin).hashCode() +
		new Integer(yMax).hashCode() +
		new Integer(xMin).hashCode() +
		new Integer(xMax).hashCode() +
		new Integer(lowMid).hashCode();
	}
    }

    // Min and Max value on horizontal line.
    private class Interval {
	public int min;
	public int max;
	public Interval(int min, int max) {
	    this.min = min;
	    this.max = max;
	}
	public boolean equals(Object obj) {
	    return (obj instanceof Interval) &&
		((Interval) obj).min == min &&
		((Interval) obj).max == max;
	}
	public int hashCode() {
	    return min * 1000 + max;
	}
    }

    // Incrementally construct (roughly) diagonal lines.
    // Start from bottom. Record middle. Then move upwards,
    // either straight up or diagonally.
    // Let lines with lower penalty take precedence over others.
    private Vector<PartialLine> buildLines() {
	Vector<PartialLine> deadEnd = new Vector<PartialLine>();
	Vector<PartialLine> prev = new Vector<PartialLine>();
	for (int y = im.height() - 1; y >= 0; y--) {
	    Vector<PartialLine> newPartials = extendPartials(y, prev, deadEnd);
	    newPartials.addAll(findNewPartials(y));
	    Map<LineEssence, PartialLine> bestLine = new HashMap<LineEssence, PartialLine>();
	    for (PartialLine newPartial : newPartials) {
		LineEssence ess = new LineEssence(newPartial);
		if (!bestLine.containsKey(ess) ||
			newPartial.compareTo(bestLine.get(ess)) < 0)
		    bestLine.put(ess, newPartial);
	    }
	    prev = new Vector<PartialLine>(bestLine.values());
	}
	return fillMinimalLines(deadEnd);
    }

    // Find bottom of partial line.
    private Vector<PartialLine> findNewPartials(int y) {
	Vector<PartialLine> partials = new Vector<PartialLine>();
	int x = 0;
	while (x < im.width()) {
	    if (im.get(x, y)) {
		int max = x+1;
		while (max < im.width() && im.get(max, y)) 
		    max++;
		for (int i = x + 1; i <= max; i++)
		    if (i - x <= shadeWidth)
			partials.add(new PartialLine(x, i, y));
		x = max;
	    } else
		x++;
	}
	return partials;
    }

    // Extend partial lines. If cannot be extended, then record.
    // Line cannot be too thick.
    private Vector<PartialLine> extendPartials(int y, Vector<PartialLine> prevLines,
	    Vector<PartialLine> deadEnd) {
	int minShade = maxLengthDiagonal / 4;
	double fillPortion = 0.7;

	Vector<PartialLine> nextLines = new Vector<PartialLine>();
	for (PartialLine prevLine : prevLines) {
	    double height = prevLine.yMax - y;
	    boolean hasNext = false;
	    Vector<Interval> intervals = new Vector<Interval>();
	    intervals.add(new Interval(prevLine.xMin, prevLine.xMax));
	    intervals.add(new Interval(prevLine.xMin, prevLine.xMax + 1));
	    intervals.add(new Interval(prevLine.xMin + 1, prevLine.xMax));
	    intervals.add(new Interval(prevLine.xMin + 1, prevLine.xMax + 1));
	    intervals.add(new Interval(prevLine.xMin + 1, prevLine.xMax + 2));
	    intervals.add(new Interval(prevLine.xMin + 2, prevLine.xMax + 2));
	    for (Interval in : intervals) {
		if (in.max <= in.min || in.min < 0 || in.max > im.width() ||
			in.max - in.min > shadeWidth)
		    continue;
		int fill = filling(y, in.min, in.max);
		boolean filled = fill * 1.0 / (in.max - in.min + 1) >= fillPortion;
		if (!filled)
		    continue;
		int leftPenalty = !im.getSafe(in.min-1, y) && im.getSafe(in.min, y) ?
		    	0 : 1;
		int rightPenalty = im.getSafe(in.max-1, y) && !im.getSafe(in.max, y) ?
		    	0 : 1;
		int penalty = leftPenalty + rightPenalty;
		PartialLine nextLine = new PartialLine(prevLine, in.min, in.max, y, 
			penalty);
		if (nextLine.correctAngle()) {
		    nextLines.add(nextLine);
		    hasNext = true;
		}
	    }
	    if (!hasNext && height > minShade)
		deadEnd.add(prevLine);
	}
	return nextLines;
    }

    // How many on line are black? 
    private int filling(int y, int xMin, int xMax) {
	int n = 0;
	for (int x = xMin; x < xMax; x++)
	    if (im.get(x, y))
		n++;
	return n;
    }

    // Draw lines.
    // Remove lines that overlap with other lines.
    private Vector<PartialLine> fillMinimalLines(Vector<PartialLine> all) {
	Vector<PartialLine> distincts = new Vector<PartialLine>();
	Collections.sort(all);
	lineIm = new BinaryImage(im.width(), im.height());
	for (PartialLine line : all) {
	    if (distinctLine(line)) {
		fillLine(line);
		distincts.add(line);
	    }
	}
	return distincts;
    }

    // Is line non-overlapping with previous ones?
    private boolean distinctLine(PartialLine line) {
	while (line != null) {
	    if (!isClear(line.yMin, line.xMin, line.xMax))
		return false;
	    else
		line = line.prev;
	}
	return true;
    }

    // Is all white?
    private boolean isClear(int y, int xMin, int xMax) {
	for (int x = xMin; x < xMax; x++)
	    if (lineIm.get(x, y))
		return false;
	return true;
    }

    // Draw line on image.
    private void fillLine(PartialLine line) {
	while (line != null) {
	    fillLine(line.yMin, line.xMin, line.xMax);
	    line = line.prev;
	}
    }

    // Draw one part of line.
    private void fillLine(int y, int xMin, int xMax) {
	for (int x = xMin; x < xMax; x++)
	    lineIm.set(x, y, true);
    }

    // Remove pixels with shade.
    private void removeLines() {
	for (int x = 0; x < im.width(); x++)
	    for (int y = 0; y < im.height(); y++)
		if (lineIm.get(x, y))
		    im.set(x, y, false);
    }

    // Remove components consisting of just a few pixels.
    private void removeSmallComponents() {
	int minPix = 4 * strokeWidth;
	Vector<Vector<Point>> components = ImageComponents.find(im);
	for (Vector<Point> comp : components)
	    if (comp.size() <= minPix) 
		for (Point p : comp) 
		    im.set(p.x, p.y, false);
    }

    // Connect both sides of a line.
    private void connectSides(Vector<PartialLine> lines) {
	for (PartialLine line : lines) 
	    connectSides(line);
    }
    private void connectSides(PartialLine line) {
	while (line != null) {
	    int x = line.xMin-1;
	    int y = line.yMin;
	    if (im.getSafe(x, y))
		connect(x, y, -1, -1, line);
	    line = line.prev;
	}
    }

    // Connect (x,y) under given angle to boundary point on the 
    // other side of the line.
    private void connect(int x, int y, int xConPrev, int yConPrev, PartialLine line) {
	int xConverse = line.xMax;
	int yConverse = line.yMin;
	if (crossAngle(x, y, xConverse, yConverse)) {
	    if (im.getSafe(xConverse, yConverse))
		connect(x, y, xConverse, yConverse);
	    if (xConPrev >= 0 && yConPrev >= 0 && im.getSafe(xConPrev, yConPrev))
		connect(x, y, xConPrev, yConPrev);
	} else if (line.prev != null && x < xConverse)
	    connect(x, y, xConverse, yConverse, line.prev);
    }

    // Is angle between points beyond line perpendicular to shading angle.
    private boolean crossAngle(int x1, int y1, int x2, int y2) {
	if (x2 <= x1)
	    return false;
	double tan = (y2 - y1) * 1.0 / (x2 - x1);
	return tan > Math.tan(anglePerpendicular);
    }

    // Draw line between points.
    private void connect(int x1, int y1, int x2, int y2) {
	double tan = (y2 - y1) * 1.0 / (x2 - x1);
	for (int x = x1 + 1; x < x2; x++) {
	    int yLow = y1 + (int) Math.floor((x-x1) * tan);
	    int yHigh = y1 + (int) Math.ceil((x-x1) * tan);
	    im.setSafe(x, yLow, true);
	    im.setSafe(x, yHigh, true);
	}
	for (int y = y1 + 1; y < y2; y++) {
	    int xLow = x1 + (int) Math.floor((y-y1) / tan);
	    int xHigh = x1 + (int) Math.ceil((y-y1) / tan);
	    im.setSafe(xLow, y, true);
	    im.setSafe(xHigh, y, true);
	}
    }

}
