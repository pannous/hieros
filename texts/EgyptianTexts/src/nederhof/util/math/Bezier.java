package nederhof.util.math;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

// Path composed of a series of smooth Bezier curves, separated
// by ragged points.
// Initially, it is open, until it is closed.
public class Bezier {

    // The points of the path.
    private Vector<Point> points = new Vector<Point>();
    // Which of the points are smooth.
    private Vector<Boolean> smooth = new Vector<Boolean>();
    // Has been closed?
    private boolean closed = false;

    // Compiled path.
    private GeneralPath path = new GeneralPath();
    // And its segments.
    private Vector<CubicCurve2D.Double> segments = new Vector<CubicCurve2D.Double>();

    // Constructor of empty.
    public Bezier() {
    }
    // Constructor from rectangle.
    public Bezier(Rectangle rect) {
	add(new Point(rect.x, rect.y), false);
	add(new Point(rect.x + rect.width, rect.y), false);
	add(new Point(rect.x + rect.width, rect.y + rect.height), false);
	add(new Point(rect.x, rect.y + rect.height), false);
	close();
    }

    // Copy constructor.
    public Bezier(Bezier b) {
	points.addAll(b.points);
	smooth.addAll(b.smooth);
	closed = b.closed;
	compile();
    }

    // Sets to empty.
    public synchronized void reset() {
	points.clear();
	smooth.clear();
	closed = false;
	compile();
    }

    // Add smooth/ragged point.
    // Do not allow repeat of point.
    public synchronized void add(Point p, boolean sm) {
	if (closed)
	    return;
	for (int i = 0; i < points.size(); i++) 
	    if (points.get(i).equals(p))
		return;
	points.add(p);
	smooth.add(sm);
	compile();
    }
    // Add point after i-th.
    public synchronized void add(int i, Point p, boolean sm) {
	for (int j = 0; j < points.size(); j++) 
	    if (points.get(j).equals(p))
		return;
	if (i >= 0 && i < points.size()) {
	    points.add(i+1, p);
	    smooth.add(i+1, sm);
	    compile();
	}
    }


    // Close path, making initial/final point smooth/ragged.
    public synchronized void close() {
	if (closed || points.size() <= 2)
	    return;
	closed = true;
	compile();
    }

    // Get number of points.
    public int getPointSize() {
	return points.size();
    }

    // Get all points.
    public Vector<Point> getPoints() {
	return (Vector<Point>) points.clone();
    }

    // Get point.
    public synchronized Point getPoint(int i) {
	if (i >= 0 && i < points.size())
	    return points.get(i);
	else
	    return null;
    }

    // Set point.
    public synchronized void setPoint(int i, Point p) {
	if (i >= 0 && i < points.size())
	    points.set(i, p);
	compile();
    }

    // Set point to smooth/ragged.
    public synchronized void setSmooth(int i, boolean sm) {
	if (i >= 0 && i < points.size())
	    smooth.set(i, sm);
	compile();
    }

    // Get smooth-ness of point.
    public synchronized boolean getSmooth(int i) {
	return i >= 0 && i < points.size() && smooth.get(i);
    }

    // Is it closed?
    public boolean isClosed() {
	return closed;
    }

    // Get number of segments.
    public int getSegmentSize() {
	return segments.size();
    }

    // Get segment.
    public synchronized CubicCurve2D.Double getSegment(int i) {
	if (i >= 0 && i < segments.size()) 
	    return segments.get(i);
	else
	    return null;
    }

    // Remove point. Make open if not at least 3 points.
    public synchronized void remove(int i) {
	if (i >= 0 && i < points.size()) {
	    points.remove(i);
	    smooth.remove(i);
	}
	if (points.size() <= 2)
	    closed = false;
	compile();
    }

    // Gather points and create curve.
    private void compile() {
	final double tension = 0.5;
	path.reset();
	segments.clear();
	if (points.size() <= 1)
	    return;
	path.moveTo(points.get(0).x, points.get(0).y);
	int n = closed ? points.size() : points.size() - 1;
	for (int i = 0; i < n; i++) {
	    int nextI = modulo(i+1);
	    Point p = points.get(i);
	    Point nextP = points.get(nextI);
	    Point prevP = 
		smooth.get(i) && (i > 0 || closed) ?
		    points.get(modulo(i-1)) :
		smooth.get(nextI) && (nextI < points.size() - 1 || closed) ?
		    predictPoint(points.get(modulo(nextI+1)), nextP, p) :
		    new Point(2*p.x - nextP.x, 2*p.y - nextP.y);
	    Point nextNextP = 
		smooth.get(nextI) && (nextI < points.size() - 1 || closed) ?
		    points.get(modulo(nextI+1)) :
		smooth.get(i) && (i > 0 || closed) ?
		    predictPoint(points.get(modulo(i-1)), p, nextP) :
		    new Point(2*nextP.x - p.x, 2*nextP.y - p.y);
	    int xDiff0 = p.x - prevP.x; 
	    int yDiff0 = p.y - prevP.y; 
	    int xDiff1 = nextP.x - p.x; 
	    int yDiff1 = nextP.y - p.y; 
	    int xDiff2 = nextNextP.x - nextP.x; 
	    int yDiff2 = nextNextP.y - nextP.y; 
	    int sum0 = xDiff0*xDiff0 + yDiff0*yDiff0;
	    int sum1 = xDiff1*xDiff1 + yDiff1*yDiff1;
	    int sum2 = xDiff2*xDiff2 + yDiff2*yDiff2;
	    double dist0 = Math.sqrt(sum0);
	    double dist1 = Math.sqrt(sum1);
	    double dist2 = Math.sqrt(sum2);
	    double scale1 = sum0+sum1 == 0 ? 0 : tension * dist1 / (dist0+dist1);
	    double scale2 = sum1+sum2 == 0 ? 0 : tension * dist2 / (dist1+dist2);
	    float cntrX1 = (float) Math.round(p.x + scale1 * (nextP.x - prevP.x));
	    float cntrY1 = (float) Math.round(p.y + scale1 * (nextP.y - prevP.y));
	    float cntrX2 = (float) Math.round(nextP.x - scale2 * (nextNextP.x - p.x));
	    float cntrY2 = (float) Math.round(nextP.y - scale2 * (nextNextP.y - p.y));
	    path.curveTo(cntrX1, cntrY1, cntrX2, cntrY2, nextP.x, nextP.y);
	    segments.add(new CubicCurve2D.Double(
			p.x, p.y, cntrX1, cntrY1, cntrX2, cntrY2, nextP.x, nextP.y));
	}
	if (closed)
	    path.closePath();
    }

    // Give modulo number of points. To be always positive.
    private int modulo(int m) {
	while (m < 0)
	    m += points.size();
	while (m >= points.size())
	    m -= points.size();
	return m;
    }

    // Predict where p4 could be.
    private Point predictPoint(Point p1, Point p2, Point p3) {
	int xDiff0 = p1.x - p2.x;
	int yDiff0 = p1.y - p2.y;
	int xDiff1 = p3.x - p2.x;
	int yDiff1 = p3.y - p2.y;
	if (xDiff0 == 0 && yDiff0 == 0 || xDiff1 == 0 && yDiff1 == 0)
	    return p3;
	double dist = Math.sqrt(xDiff0*xDiff0 + yDiff0*yDiff0);
	double ang = Math.PI - Math.atan2(yDiff0, xDiff0) + 2 * Math.atan2(yDiff1, xDiff1);
	return new Point(
		(int) Math.round(p3.x + dist * Math.cos(ang)), 
		(int) Math.round(p3.y + dist * Math.sin(ang)));
    }

    // Get compiled path.
    public synchronized GeneralPath getPath() {
	return path;
    }

    // Rough estimation of area, based on bounding box of points.
    public synchronized int area() {
	if (points.isEmpty())
	    return 0;
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
	return (xMax-xMin) * (yMax-yMin);
    }

}

