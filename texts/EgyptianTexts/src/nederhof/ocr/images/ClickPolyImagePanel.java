package nederhof.ocr.images;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

// A panel containing binary image. For drawing polygons.
// Polygons are associated with labels of class L.
public class ClickPolyImagePanel<L> extends ImagePanel<L> {

    // Responds to mouse actions?
    private boolean enabled = true;
    public void setEnabled(boolean allow) {
	enabled = allow;
    }

    // Create from binary image.
    public ClickPolyImagePanel(BinaryImage binaryImage) {
	this(binaryImage, 1.0, 1.0);
    }
    public ClickPolyImagePanel(BinaryImage binaryImage, 
	    double xScale, double yScale) {
	super(binaryImage, xScale, yScale);
    }

    // Temporary rectangle while dragging.
    public LabelledShape<L> dragRectangle = null;

    // Caller to override.
    public void select(LabelledShape<L> poly) {
    }
    public void include(LabelledShape<L> poly) {
    }
    public void cover(Rectangle rect) {
    }
    public void uninclude(LabelledShape<L> poly) {
    }

    // Points between click and release form
    // rectangle. 
    private Point unincludePoint = null;
    // Same but for selecting glyphs.
    private Point includePoint = null;
    // Which of the two are used?
    protected boolean removal() {
	return unincludePoint != null;
    }

    public void mouseClicked(MouseEvent e) {
	if (!enabled)
	    return;
	Point pPanel = e.getPoint();
	makeInside(pPanel);
	Point p = panelToImage(pPanel);
	if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
	    for (LabelledShape<L> shape : shapes) 
		if (shape.shape.contains(p.x, p.y) && 
			shape.shape instanceof Polygon) {
		    select(shape);
		    return;
		}
	} else {
	    LabelledShape<L> toBeRemoved = null;
	    for (LabelledShape<L> shape : shapes) 
		if (shape.shape.contains(p.x, p.y) && 
			shape.shape instanceof Polygon) {
		    toBeRemoved = shape;
		    break;
		}
	    if (toBeRemoved != null)
		uninclude(toBeRemoved);
	}
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
	if (!enabled)
	    return;
	Point pPanel = e.getPoint();
	makeInside(pPanel);
	Point p = panelToImage(pPanel);
	if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
	    includePoint = p;
	    unincludePoint = null;
	} else {
	    includePoint = null;
	    unincludePoint = p;
	}
	dragRectangle = new LabelledShape<L>(new Rectangle(p.x, p.y, 1, 1), null);
	add(dragRectangle);
    }
    public void mouseReleased(MouseEvent e) {
	if (!enabled)
	    return;
	Point pPanel = e.getPoint();
	makeInside(pPanel);
	Point p = panelToImage(pPanel);
	if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown() &&
		includePoint != null) {
	    Rectangle rect = getRect(includePoint, p);
	    for (LabelledShape<L> shape : shapes)
		if (shape.shape instanceof Polygon) {
		    Polygon poly = (Polygon) shape.shape;
		    if (contains(rect, poly)) 
			include(shape);
		}
	    if (rect.width >= 5 && rect.height >= 5)
		cover(rect);
	} else if ((e.getButton() == MouseEvent.BUTTON3 || e.isControlDown()) &&
		unincludePoint != null) {
	    Rectangle rect = getRect(unincludePoint, p);
	    Vector<LabelledShape<L>> toBeRemoved = 
		new Vector<LabelledShape<L>>();
	    for (LabelledShape<L> shape : shapes)
		if (shape.shape instanceof Polygon) {
		    Polygon poly = (Polygon) shape.shape;
		    if (contains(rect, poly))
			toBeRemoved.add(shape);
		}
	    for (LabelledShape<L> shape : toBeRemoved)
		uninclude(shape);
	}
	includePoint = null;
	unincludePoint = null;
	if (dragRectangle != null) 
	    remove(dragRectangle);
	dragRectangle = null;
    }
    public void mouseDragged(MouseEvent e) {
	Point pPanel = e.getPoint();
	if (!enabled)
	    return;
	makeInside(pPanel);
	if (dragRectangle != null) {
	    Point p = panelToImage(pPanel);
	    if (includePoint != null)
		dragRectangle.shape = getRect(includePoint, p);
	    else if (unincludePoint != null) 
		dragRectangle.shape = getRect(unincludePoint, p);
	    repaint();
	}
    }
    public void mouseMoved(MouseEvent e) {
    }

    // Is outside image?
    private boolean outside(Point p) {
	return p.x < 0 || p.y < 0 ||
	    p.x >= image.getWidth() || p.y >= image.getHeight();
    }

    // Make inside.
    private void makeInside(Point p) {
        if (p.x < 0)
            p.x = 0;
        if (p.x >= image.getWidth())
            p.x = image.getWidth()-1;
        if (p.y < 0)
            p.y = 0;
        if (p.y >= image.getHeight())
            p.y = image.getHeight()-1;
    }

    // Get rectangle from two points.
    private Rectangle getRect(Point p1, Point p2) {
	return new Rectangle(
		    Math.min(p1.x, p2.x),
		    Math.min(p1.y, p2.y),
		    Math.abs(p1.x - p2.x),
		    Math.abs(p1.y - p2.y));
    }

    // Are all points of polygon contained in rectangle?
    private boolean contains(Rectangle rect, Polygon poly) {
	for (int i = 0; i < poly.npoints; i++) 
	    if (!rect.contains(poly.xpoints[i], poly.ypoints[i]))
		return false;
	return true;
    }

    // Testing.
    public static void main(String[] args) {
	/*
	BinaryImage image = new BinaryImage(30, 30);
	ImagePanel panel = new ClickPolyImagePanel(image, 15, 15);
	JFrame frame = new JFrame("test");
	Container content = frame.getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
	content.add(panel);
	frame.pack();
	frame.setVisible(true);
	*/
    }

}
