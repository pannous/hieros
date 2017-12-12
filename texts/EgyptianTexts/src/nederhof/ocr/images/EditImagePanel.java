package nederhof.ocr.images;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

// A panel intended for editing images.
public abstract class EditImagePanel extends JPanel
	implements MouseListener, MouseMotionListener {

    // The unscaled image.
    protected BinaryImage unscaled;
    // The overlayed image.
    protected BinaryImage overlay;
    // The combined, scaled image.
    protected BufferedImage image;

    // Factors to be scaled down.
    protected double xScale = 1.0;
    protected double yScale = 1.0;
    // Transform of scaling.
    private AffineTransform scaling;

    public EditImagePanel(BinaryImage unscaled, 
	    BinaryImage overlay, double xScale, double yScale) {
	this.unscaled = unscaled;
	this.overlay = overlay;
	this.xScale = xScale;
	this.yScale = yScale;
	makeScaled();
	update();
	addMouseListener(this);
	addMouseMotionListener(this);
	setVisible(true);
    }

    // Make scaled image.
    private void makeScaled() {
        int width = (int) Math.round(unscaled.width() * xScale);
        int height = (int) Math.round(unscaled.height() * yScale);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	scaling = AffineTransform.getScaleInstance(xScale, yScale);
    }

    // Write and combine, scaled.
    public void update() {
	Graphics2D g = scaledGraphics();
	for (int x = 0; x < unscaled.width(); x++)
	    for (int y = 0; y < unscaled.height(); y++) 
		set(g, x, y);
	g.dispose();
	repaint();
    }

    // Update pixel.
    public void update(int x, int y) {
	Graphics2D g = scaledGraphics();
	set(g, x, y);
	g.dispose();
	repaint();
    }

    // Scaled graphics.
    public Graphics2D scaledGraphics() {
	Graphics2D g = (Graphics2D) image.createGraphics();
	g.transform(scaling);
	return g;
    }

    // Overlay two images, determining color.
    private void set(Graphics2D g, int x, int y) {
	if (!unscaled.get(x, y) && !overlay.get(x,y))
	    g.setColor(Color.WHITE);
	else if (!unscaled.get(x, y) && overlay.get(x,y))
	    g.setColor(Color.BLUE);
	else if (unscaled.get(x, y) && !overlay.get(x,y))
	    g.setColor(Color.GRAY);
	else
	    g.setColor(Color.BLACK);
	g.fill(new Rectangle(x, y, 1, 1));
    }

    // Paint image, and if needed circle.
    public void paint(Graphics g) {
	g.drawImage(image, 0, 0, null);
	Graphics2D g2 = (Graphics2D) g;
	if (toShow) {
	    g2.setColor(Color.BLUE);
	    g2.draw(showCircle);
	}
	if (toInclude) {
	    g2.setColor(Color.GREEN);
	    g2.draw(includeCircle);
	}
	if (toExclude) {
	    g2.setColor(Color.RED);
	    g2.draw(excludeCircle);
	}
    }

    // Dimensions.
    public Dimension getMinimumSize() {
        return new Dimension(image.getWidth(this), image.getHeight(this));
    }
    public Dimension getMaximumSize() {
        return getMinimumSize();
    }
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    // Circle mode.
    // When to include/exclude by mouse movements.
    private boolean circleMode = false;

    // Circle showing radius.
    private Shape showCircle = new Ellipse2D.Double(0, 0, 0, 0);
    // Circle for including pixels.
    private Shape includeCircle = new Ellipse2D.Double(0, 0, 0, 0);
    // Circle for excluding pixels.
    private Shape excludeCircle = new Ellipse2D.Double(0, 0, 0, 0);

    // Should circles be drawn?
    private boolean toShow = false;
    private boolean toInclude = false;
    private boolean toExclude = false;

    // Radius of circles.
    private int radius = 10;

    // Set to circle mode.
    public void setCircleMode(boolean inMode) {
	circleMode = inMode;
	if (inMode) {
	    updateCircles();
	    toShow = true;
	    toInclude = false;
	    toExclude = false;
	} else {
	    toShow = false;
	    toInclude = false;
	    toExclude = false;
	}
	repaint();
    }
    public boolean getCircleMode() {
	return circleMode;
    }

    // Update depending on position.
    private void updateCircles(Point p) {
	p = makeInside(p);
	showCircle = 
	    new Ellipse2D.Double(p.x-radius, p.y-radius, 
		    2*radius, 2*radius);
	includeCircle = 
	    new Ellipse2D.Double(p.x-radius, p.y-radius, 
		    2*radius, 2*radius);
	excludeCircle = 
	    new Ellipse2D.Double(p.x-radius, p.y-radius, 
		    2*radius,2*radius);
	repaint();
    }
    // Taking mouse position.
    private void updateCircles() {
	updateCircles(mousePosition());
    }

    // Set radius.
    public void setRadius(int r) {
	int max = Math.min(image.getWidth() / 2 - 1, image.getHeight() / 2 - 1);
	radius = Math.min(max, r);
	updateCircles();
    }
    // Increase radius.
    public void upRadius() {
	int max = Math.min(image.getWidth() / 2 - 1, image.getHeight() / 2 - 1);
	if (radius > 10)
	    setRadius(Math.min(max, radius * 12 / 10));
	else
	    setRadius(Math.min(max, radius +1));
    }
    public void downRadius() {
	if (radius > 10)
	    setRadius(Math.max(1, radius * 10 / 12));
	else
	    setRadius(Math.max(1, radius - 1));
    }

    // Position of mouse.
    private Point mousePosition() {
	try {
	    Point mouse = MouseInfo.getPointerInfo().getLocation();
	    Point comp = this.getLocationOnScreen();
	    return new Point(mouse.x - comp.x, mouse.y - comp.y);
	} catch (IllegalComponentStateException e) {
	    return new Point(image.getWidth() / 2, image.getHeight() / 2);
	}
    }

    // Remembered mouse button number from last press.
    private int lastPress = 1;

    // Mouse events.
    public void mouseClicked(MouseEvent e) {
	Point p = e.getPoint();
	if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
	    if (!circleMode) {
		Point p2 = panelToImage(p);
		click(p2.x, p2.y);
	    }
	}
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
	Point p = e.getPoint();
	if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
	    lastPress = 1;
	    if (circleMode) {
		toShow = false;
		toInclude = true;
		toExclude = false;
		repaint();
	    }
	} else {
	    lastPress = 3;
	    if (circleMode) {
		toShow = false;
		toInclude = false;
		toExclude = true;
		repaint();
	    }
	}
    }
    public void mouseReleased(MouseEvent e) {
	Point p = e.getPoint();
	if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
	    if (circleMode) {
		toShow = true;
		toInclude = false;
		toExclude = false;
		repaint();
	    }
	} else {
	    if (circleMode) {
		toShow = true;
		toInclude = false;
		toExclude = false;
		repaint();
	    }
	}
    }
    public void mouseDragged(MouseEvent e) {
	Point p = e.getPoint();
	if (circleMode)
	    updateCircles(p);
	if (lastPress == 1)
	    includeInCircle(p);
	else 
	    excludeInCircle(p);
    }
    public void mouseMoved(MouseEvent e) {
	Point p = e.getPoint();
	if (circleMode) 
	    updateCircles(p);
    }

    // Make inside.
    private Point makeInside(Point p) {
	p = new Point(p);
	if (p.x < radius)
	    p.x = radius;
	if (p.x >= image.getWidth()-radius-1)
	    p.x = image.getWidth()-radius-2;
	if (p.y < radius)
	    p.y = radius;
	if (p.y >= image.getHeight()-radius-1)
	    p.y = image.getHeight()-radius-2;
	return p;
    }

    private void includeInCircle(Point p) {
	for (int x = Math.max(0, p.x-radius); 
		x < Math.min(p.x+radius, image.getWidth()); x++)
	    for (int y = Math.max(0, p.y-radius); 
		    y < Math.min(p.y+radius, image.getHeight()); y++) {
		Point p2 = new Point(x,y);
		if (includeCircle.contains(p2)) {
		    Point p3 = panelToImage(p2);
		    include(p3.x, p3.y);
		}
	    }
    }
    private void excludeInCircle(Point p) {
	for (int x = Math.max(0, p.x-radius); 
		x < Math.min(p.x+radius, image.getWidth()); x++)
	    for (int y = Math.max(0, p.y-radius); 
		    y < Math.min(p.y+radius, image.getHeight()); y++) {
		Point p2 = new Point(x,y);
		if (excludeCircle.contains(p2)) {
		    Point p3 = panelToImage(p2);
		    exclude(p3.x, p3.y);
		}
	    }
    }

    // Convert point on panel to point in image.
    protected Point panelToImage(Point p) {
        return new Point((int) Math.floor(p.x / xScale),
                (int) Math.floor(p.y / yScale));
    }
    protected int panelToImageX(int x) {
        return (int) Math.floor(x / xScale);
    }
    protected int panelToImageY(int y) {
        return (int) Math.floor(y / yScale);
    }

    ////////////////////////////////////////////////
    // Communication to caller.

    // Include/exclude from blobs.
    public abstract void include(int x, int y);
    public abstract void exclude(int x, int y);
    // Left-click.
    public abstract void click(int x, int y);

}
