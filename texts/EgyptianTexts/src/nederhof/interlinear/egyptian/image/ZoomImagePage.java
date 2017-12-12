package nederhof.interlinear.egyptian.image;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

import nederhof.util.math.*;
import nederhof.util.*;

// Zoomable image.
public class ZoomImagePage extends JPanel 
	implements ImagePage, ComponentListener, ActionListener {

    // Auxiliary class for manipulating resource.
    protected ImageResourceManipulator manipulator;

    // Bilevel version of image?
    protected boolean bilevel = false;

    // Mode of mouse actions.
    // MOVE : mouse is used to move image.
    // RECT : mouse is used to draw rectangles.
    // POLY : mouse is used to draw polygons.
    // TAG : shapes are drawn and tagged.
    // DUAL : left-mouse is used to select rectangle, right-mouse to move image.
    public enum MouseMode {MOVE, RECT, POLY, TAG, DUAL};
    protected MouseMode mouseMode;

    // Should components be shown in RECT mode?
    protected boolean showComponents = true;

    // For finding component. Can be "hlr", "hrl", "vlr", "vrl".
    protected String direction = "hlr";

    // Parameters of conversion to bilevel.
    protected int threshold = SettingsWindow.tDefault;
    protected int rWeight = SettingsWindow.rDefault;
    protected int gWeight = SettingsWindow.gDefault;
    protected int bWeight = SettingsWindow.bDefault;
    protected int componentConnect = SettingsWindow.connectDefault;
    protected int componentSize = SettingsWindow.sizeDefault;

    // The image.
    protected BufferedImage image;
    // Bilevel of the same.
    protected BufferedImage bilevelImage;
    // Image number in a series of images.
    protected int imageNumber = 0;

    // Increment of zooming.
    protected final float ZOOM_INCR = 1.2f;
    // Portion of window moved by buttons.
    protected final float NAVIGATE_PORTION = 0.2f;

    // Zooming and position of centre of image in cutout.
    protected float scale = 0f;
    protected float xPos = 0.5f; // between 0 and 1
    protected float yPos = 0.5f; // between 0 and 1

    // Navigation panel.
    protected JPanel navPanel;
    // Element on which cutout image is projected.
    protected JPanel imagePanel;

    // Constructor.
    public ZoomImagePage(BufferedImage image, ImageResourceManipulator manipulator) {
	this.image = image;
	this.manipulator = manipulator;
	setOpaque(false);
	setLayout(new BorderLayout());

	navPanel = new NavPanel(image);
	JButton zoomOutButton = new JButton("-");
	JButton zoomInButton = new JButton("+");
	JButton goUpButton = new JButton("\u2191");
	JButton goDownButton = new JButton("\u2193");
	JButton goLeftButton = new JButton("\u2190");
	JButton goRightButton = new JButton("\u2192");
	goUpButton.setActionCommand("go up");
	goDownButton.setActionCommand("go down");
	goLeftButton.setActionCommand("go left");
	goRightButton.setActionCommand("go right");
	zoomOutButton.addActionListener(this);
	zoomInButton.addActionListener(this);
	goUpButton.addActionListener(this);
	goDownButton.addActionListener(this);
	goLeftButton.addActionListener(this);
	goRightButton.addActionListener(this);
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout());
	buttonPanel.add(zoomOutButton);
	buttonPanel.add(zoomInButton);
	buttonPanel.add(navPanel);
	buttonPanel.add(goLeftButton);
	buttonPanel.add(goUpButton);
	buttonPanel.add(goDownButton);
	buttonPanel.add(goRightButton);
	add(buttonPanel, BorderLayout.NORTH);

	imagePanel = new ImagePanel();
	add(imagePanel, BorderLayout.CENTER);

	imagePanel.addComponentListener(this);
	setInitialMode();
	revalidate();
    }

    // Can be overridden by subclass.
    protected void setInitialMode() {
	mouseMode = MouseMode.DUAL;
    }

    // Set image number.
    public void setNumber(int num) {
	imageNumber = num;
    }

    //////////////////////////////////////////////
    // Navigation panel.

    // Depicts current cut-out of image, for navigation.
    protected class NavPanel extends JPanel 
    		implements MouseMotionListener {
	// Height. Width should adapt to that, proportional to image.
	private int height = 40;
	private int width;
	// Remember last position of mouse (before being dragged).
	private Point mousePoint = new Point(0, 0);
	// Constructor.
	public NavPanel(BufferedImage image) {
	    width = height * image.getWidth() / image.getHeight();
	    setBackground(Color.WHITE);
	    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	    addMouseMotionListener(this);
	}
	// Exact size.
	public Dimension getMinimumSize() {
	    return new Dimension(width, height);
	}
	public Dimension getPreferredSize() {
	    return new Dimension(width, height);
	}
	public Dimension getMaximumSize() {
	    return new Dimension(width, height);
	}
	// Paint white surface with black representing cut-out.
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Insets insets = getInsets();
	    int netWidth = width - insets.left - insets.right;
	    int netHeight = height - insets.top - insets.bottom;
	    int imageW = Math.round(scale * image.getWidth());
	    int imageH = Math.round(scale * image.getHeight());
	    int cutoutW = imagePanel.getWidth();
	    int cutoutH = imagePanel.getHeight();
	    if (imageW == 0 || imageH == 0)
		return;
	    int x = Math.round((xPos * imageW - (cutoutW / 2)) * netWidth / imageW);
	    int y = Math.round((yPos * imageH - (cutoutH / 2)) * netHeight / imageH);
	    int w = Math.round(cutoutW * netWidth / imageW);
	    int h = Math.round(cutoutH * netHeight / imageH);
	    x = Math.max(x, 0) + insets.left;
	    y = Math.max(y, 0) + insets.top;
	    w = Math.max(Math.min(x+w, width-insets.right-1) - x, 0);
	    h = Math.max(Math.min(y+h, height-insets.bottom-1) - y, 0);
	    g.setColor(Color.BLACK);
	    g.drawRect(x, y, w, h);
	}
	// MouseMotionListener
	public void mouseDragged(MouseEvent e) {
	    if (SwingUtilities.isLeftMouseButton(e)) {
		Point p = e.getPoint();
		moveDelta(p.x - mousePoint.x, p.y - mousePoint.y);
		mousePoint = e.getPoint();
	    }
	}
	public void mouseMoved(MouseEvent e) {
	    mousePoint = e.getPoint();
	}
	public void moveDelta(int xDelta, int yDelta) {
	    Insets insets = getInsets();
	    int netWidth = width - insets.left - insets.right;
	    int netHeight = height - insets.top - insets.bottom;
	    float xFactor = 1.0f * xDelta / netWidth;
	    float yFactor = 1.0f * yDelta / netHeight;
	    moveImage(xFactor, yFactor);
	}
    }

    //////////////////////////////////////////////
    // Image panel.

    // Depicts current cut-out of image.
    protected class ImagePanel extends JPanel 
    		implements MouseMotionListener, MouseWheelListener,
   			MouseListener {
	// Remember last position of mouse (before being dragged).
	private Point mousePoint = new Point(0, 0);
	// Constructor.
	public ImagePanel() {
	    addMouseMotionListener(this);
	    addMouseWheelListener(this);
	    addMouseListener(this);
	}
	// For bilevel, if total image has been converted, take that.
	// Otherwise, take only cutout and convert that, and convert
	// total image in background.
	protected void paintComponent(Graphics g) {
	    Graphics2D g2 = (Graphics2D) g;
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    Rectangle vis = visibleRect();
	    if (vis.width <= 0)
		return;
	    AffineTransform transform = transform(vis);
	    BufferedImage subimage = null;
	    boolean toMakeBilevelSometime = false;
	    if (bilevel) {
		// make copy to avoid concurrent setting to null
		BufferedImage copy = bilevelImage; 
		if (copy != null) 
		    subimage = copy.getSubimage(vis.x, vis.y, vis.width, vis.height);
		else {
		    subimage = image.getSubimage(vis.x, vis.y, vis.width, vis.height);
		    setBusy(true);
		    subimage = makeBilevel(subimage);
		    setBusy(false);
		    toMakeBilevelSometime = true;
		}
	    } else 
		subimage = image.getSubimage(vis.x, vis.y, vis.width, vis.height);
	    g2.drawImage(subimage, 0, 0, getWidth(), getHeight(), null);
	    if (mouseMode == MouseMode.RECT) {
		if (showComponents)
		    paintComponents(g2, transform, Color.RED, 1);
		if (focusRect != null)
		    paintRect(g2, focusRect, transform, Color.BLUE, 2);
		else
		    paintRect(g2, freshRect, transform, Color.DARK_GRAY, 2);
	    } else if (mouseMode == MouseMode.POLY) {
		paintPoly(g2, freshPoly, transform, polyDone ? Color.BLUE : Color.DARK_GRAY, 2);
		paintComponents(g2, transform, Color.RED, 2);
	    } else if (mouseMode == MouseMode.TAG) {
		paintAreas(g2, transform);
		paintBezier(g2, freshBezier, transform, Color.BLUE, 2);
	    } else if (mouseMode == MouseMode.DUAL) {
		if (focusRect != null)
		    paintRectCircle(g2, focusRect, transform, Color.BLUE, 2);
	    }
	    if (toMakeBilevelSometime)
		makeBilevelSometime();
	}
	// Paint proposed components.
	private void paintComponents(Graphics2D g2, 
		AffineTransform transform, Color color, int thick) {
	    for (Rectangle comp : components) 
		paintRect(g2, comp, transform, color, thick);
	}
	// Paint some rectangle with color and thickness.
	private void paintRect(Graphics2D g2, Rectangle rect, 
		AffineTransform transform, Color color, int thick) {
	    if (rect == null)
		return;
	    g2.setColor(color);
	    g2.setStroke(new BasicStroke(thick));
	    g2.draw(transform.createTransformedShape(rect));
	}
	// Paint circle enclosing rectangle.
	private void paintRectCircle(Graphics2D g2, Rectangle rect,
		AffineTransform transform, Color color, int thick) {
	    if (rect == null)
		return;
	    int margin = 5;
	    double radius = Math.sqrt(0.25 * rect.width * rect.width + 
		    0.25 * rect.height * rect.height) + margin;
	    int xCorner = (int) Math.round(rect.x + rect.width * 0.5f - radius);
	    int yCorner = (int) Math.round(rect.y + rect.height * 0.5f - radius);
	    Ellipse2D.Double circle = new Ellipse2D.Double(
		    xCorner, yCorner, radius*2, radius*2);
	    g2.setColor(color);
	    g2.setStroke(new BasicStroke(thick));
	    g2.draw(transform.createTransformedShape(circle));
	}
	// Paint tagged areas.
	private void paintAreas(Graphics2D g2, AffineTransform transform) {
	    for (TaggedBezier area : manipulator.getAreas()) {
		if (area.getNum() != imageNumber)
		    continue;
		paintBezier(g2, area, transform, Color.DARK_GRAY, 2);
		String name = area.getName();
		Point center = center(area, visibleRect());
		Rectangle2D rect = g2.getFontMetrics().getStringBounds(name, g2);
		int x = center.x - (int) rect.getWidth() / 2;
		int y = center.y + (int) rect.getHeight() / 2;
		g2.setColor(Color.BLUE);
		g2.drawString(name, x, center.y);
	    }
	}
	// Paint some polygon with color and thickness.
	private void paintPoly(Graphics2D g2, Polygon poly, 
		AffineTransform transform, Color color, int thick) {
	    if (poly == null)
		return;
	    g2.setColor(color);
	    g2.setStroke(new BasicStroke(thick));
	    g2.draw(transform.createTransformedShape(poly));
	    for (int i = 0; i < poly.npoints; i++) {
		float r = 5f / scale;
		int px = poly.xpoints[i];
		int py = poly.ypoints[i];
		Shape circle = new Ellipse2D.Double(px-r, py-r, 2*r, 2*r);
		g2.draw(transform.createTransformedShape(circle));
	    }
	}
	// Paint some Bezier curve.
	private void paintBezier(Graphics2D g2, Bezier bezier,
		AffineTransform transform, Color color, int thick) {
	    if (bezier == null)
		return;
	    g2.setColor(color);
	    g2.setStroke(new BasicStroke(thick));
	    g2.draw(transform.createTransformedShape(bezier.getPath()));
	    for (int i = 0; i < bezier.getPointSize(); i++) {
		Point p = bezier.getPoint(i);
		if (p == null)
		    continue;
		float r = 5f / scale;
		int px = p.x;
		int py = p.y;
		if (bezier.getSmooth(i)) {
		    Shape circle = new Ellipse2D.Double(px-r, py-r, 2*r, 2*r);
		    g2.draw(transform.createTransformedShape(circle));
		} else {
		    Rectangle2D.Double square = new Rectangle2D.Double(px-r, py-r, 2*r, 2*r);
		    g2.draw(transform.createTransformedShape(square));
		}
	    }
	}
	// Rectangle visible in window.
	private Rectangle visibleRect() {
	    if (scale == 0)
		return new Rectangle();
	    int imageW = Math.round(scale * image.getWidth());
	    int imageH = Math.round(scale * image.getHeight());
	    int x = Math.round((xPos * imageW - (getWidth() / 2.0f)) / scale);
	    int y = Math.round((yPos * imageH - (getHeight() / 2.0f)) / scale);
	    int w = Math.round(getWidth() / scale);
	    int h = Math.round(getHeight() / scale);
	    x = Math.max(x, 0);
	    y = Math.max(y, 0);
	    w = Math.min(x+w, image.getWidth()) - x;
	    h = Math.min(y+h, image.getHeight()) - y;
	    if (w < 1 || h < 1)
		return new Rectangle();
	    else
		return new Rectangle(x, y, w, h);
	}
	// MouseWheelListener 
	public void mouseWheelMoved(MouseWheelEvent e) {
	    boolean zoomIn = (e.getWheelRotation() < 0);
	    if (zoomIn)
		zoomIn(e.getPoint(), visibleRect());
	    else
		zoomOut();
	}
	// MouseMotionListener
	public void mouseDragged(MouseEvent e) {
	    if (mouseMode == MouseMode.MOVE) {
		if (SwingUtilities.isLeftMouseButton(e)) {
		    Point p = e.getPoint();
		    moveDelta(p.x - mousePoint.x, p.y - mousePoint.y);
		    mousePoint = e.getPoint();
		}
	    } else if (mouseMode == MouseMode.RECT) {
		if (SwingUtilities.isLeftMouseButton(e)) {
		    if (closeToRect(e.getPoint(), focusRect, visibleRect()))
			moveRect(e.getPoint(), visibleRect());
		    else
			makeRect(mousePoint, e.getPoint(), visibleRect());
		} else if (SwingUtilities.isRightMouseButton(e)) {
		    Point p = e.getPoint();
		    moveDelta(p.x - mousePoint.x, p.y - mousePoint.y);
		    mousePoint = e.getPoint();
		}
	    } else if (mouseMode == MouseMode.POLY) {
		if (SwingUtilities.isLeftMouseButton(e)) {
		    dragPoly(mousePoint, e.getPoint(), visibleRect());
		    mousePoint = e.getPoint();
		} else if (SwingUtilities.isRightMouseButton(e)) {
		    if (freshPoly == null || polyDone) {
			Point p = e.getPoint();
			moveDelta(p.x - mousePoint.x, p.y - mousePoint.y);
			mousePoint = e.getPoint();
		    }
		}
	    } else if (mouseMode == MouseMode.TAG) {
		if (SwingUtilities.isLeftMouseButton(e)) {
		    dragArea(mousePoint, e.getPoint(), visibleRect());
		    mousePoint = e.getPoint();
		} else if (SwingUtilities.isRightMouseButton(e)) {
		    if (freshPoly == null || polyDone) {
			Point p = e.getPoint();
			moveDelta(p.x - mousePoint.x, p.y - mousePoint.y);
			mousePoint = e.getPoint();
		    }
		}
	    } else if (mouseMode == MouseMode.DUAL) {
		if (SwingUtilities.isRightMouseButton(e)) {
		    Point p = e.getPoint();
		    moveDelta(p.x - mousePoint.x, p.y - mousePoint.y);
		    mousePoint = e.getPoint();
		}
	    }
	}
	public void mouseMoved(MouseEvent e) {
	    mousePoint = e.getPoint();
	    if (mouseMode == MouseMode.POLY) 
		movePoly(e.getPoint(), visibleRect());
	    else if (mouseMode == MouseMode.TAG)
		moveArea(e.getPoint(), visibleRect());
	}
        public void moveDelta(int xDelta, int yDelta) {
            float xFactor = 1.0f * xDelta / scale / image.getWidth();
            float yFactor = 1.0f * yDelta / scale / image.getHeight();
            moveImage(-xFactor, -yFactor);
        }
	// MouseListener
	public void mouseClicked(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
	    if (mouseMode == MouseMode.RECT) { 
		if (SwingUtilities.isLeftMouseButton(e)) {
		    clickRect(e.getPoint(), visibleRect());
		} 
	    } else if (mouseMode == MouseMode.POLY) {
		if (SwingUtilities.isLeftMouseButton(e)) {
		    clickPoly(e.getPoint(), visibleRect());
		} else if (SwingUtilities.isRightMouseButton(e)) {
		    if (!polyDone)
			finishPoly();
		}
	    } else if (mouseMode == MouseMode.TAG) {
		if (SwingUtilities.isLeftMouseButton(e)) {
		    clickArea(e.getPoint(), visibleRect());
		} else if (SwingUtilities.isRightMouseButton(e)) {
		    toggleArea(e.getPoint(), visibleRect());
		}
	    } else if (mouseMode == MouseMode.DUAL) {
		if (SwingUtilities.isLeftMouseButton(e)) {
		    selectSignFuzzy(e.getPoint(), visibleRect());
		}
	    }
	}
	public void mouseReleased(MouseEvent e) {
	    if (mouseMode == MouseMode.RECT &&
		    SwingUtilities.isLeftMouseButton(e)) {
		if (focusRect == null && freshRect != null) {
		    focusRect = freshRect;
		    freshRect = null;
		    repaint();
		    recordRect();
		}
	    }
	}

    }

    // Transformation from image to move/scaled image.
    protected AffineTransform transform(Rectangle vis) {
	AffineTransform transform = new AffineTransform();
	transform.scale(scale, scale);
	transform.translate(-vis.x, -vis.y);
	return transform;
    }

    // Translate from image to move/scale image.
    protected int transformX(int x, Rectangle vis) {
	return Math.round((x - vis.x) * scale);
    }
    protected int transformY(int y, Rectangle vis) {
	return Math.round((y - vis.y) * scale);
    }

    //////////////////////////////////////////////
    // Image operations.

    public static int WHITE = 0xFFFFFF;
    public static int RED = 0x00FF0000;
    public static int GREEN = 0x0000FF00;
    public static int BLUE = 0x000000FF;
    public static int BLACK = 0x000000;

    protected BufferedImage makeBilevel(BufferedImage inImage) {
	return makeBilevel(inImage, new ProceedFlag());
    }
    protected BufferedImage makeBilevel(BufferedImage inImage, ProceedFlag flag) {
	BufferedImage outImage = 
	    new BufferedImage(inImage.getWidth(), inImage.getHeight(), 
		    BufferedImage.TYPE_BYTE_BINARY);
	for (int x = 0; x < inImage.getWidth(); x++) {
	    if (!flag.isProceed()) 
		return null;
	    for (int y = 0; y < inImage.getHeight(); y++) {
		int rgb = inImage.getRGB(x, y);
		int r = (rgb & RED) >>> 16;
		int g = (rgb & GREEN) >>> 8;
		int b = (rgb & BLUE)  >>> 0;
		float gray = (r * rWeight + g * gWeight + b * bWeight) / 
		    Math.max(1, rWeight + gWeight + bWeight);
		if (gray < threshold * 255 / 100f)
		    outImage.setRGB(x, y, BLACK);
		else
		    outImage.setRGB(x, y, WHITE);
	    }
	}
	return outImage;
    }

    // In background, bilevel is made of total image.
    protected BilevelMaker bilevelMaker;

    // In background make bilevel of total image.
    protected void makeBilevelSometime() {
	stopBilevelMaker();
	bilevelMaker = new BilevelMaker();
    }

    // If already running (with different parameters, then stop).
    protected void stopBilevelMaker() {
	if (bilevelMaker != null)
	    bilevelMaker.stopMaking();
    }

    // Clean way of stopping bilevel maker.
    protected class ProceedFlag {
	private boolean proceed = true;
	public boolean isProceed() {
	    return proceed;
	}
	public void stop() {
	    proceed = false;
	}
    }

    // In background, conversion of image to bilevel.
    protected class BilevelMaker implements Runnable {
	private ProceedFlag proceedFlag = new ProceedFlag();
	public BilevelMaker() {
	    Thread thread = new Thread(this);
	    thread.start();
	}
	public void run() {
	    bilevelImage = makeBilevel(image, proceedFlag);
	}
	public void stopMaking() {
	    proceedFlag.stop();
	}
    }

    //////////////////////////////////////////////
    // Zooming and navigation.

    // Zoom in.
    protected void zoomIn() {
	scale *= ZOOM_INCR;
	adjustZoom();
	repaint();
    }
    // Zoom in, preserving position of mouse.
    protected void zoomIn(Point p, Rectangle vis) {
	double xDiff = (imagePanel.getWidth() / 2.0 - p.x) / scale;
	double yDiff = (imagePanel.getHeight() / 2.0 - p.y) / scale;
	scale *= ZOOM_INCR;
	xPos -= (ZOOM_INCR-1) * xDiff / image.getWidth();
	yPos -= (ZOOM_INCR-1) * yDiff / image.getHeight();
	adjustZoom();
	repaint();
    }
    // Zoom out.
    protected void zoomOut() {
	scale /= ZOOM_INCR;
	adjustZoom();
	repaint();
    }

    // Navigation by buttons.
    protected void goUp() {
	moveImage(0, -imagePanel.getHeight() * NAVIGATE_PORTION / scale / image.getHeight());
    }
    protected void goDown() {
	moveImage(0, imagePanel.getHeight() * NAVIGATE_PORTION / scale / image.getHeight());
    }
    protected void goLeft() {
	moveImage(-imagePanel.getWidth() * NAVIGATE_PORTION / scale / image.getWidth(), 0);
    }
    protected void goRight() {
	moveImage(imagePanel.getWidth() * NAVIGATE_PORTION / scale / image.getWidth(), 0);
    }

    // Move image.
    protected void moveImage(float xFactor, float yFactor) {
	xPos += xFactor;
	yPos += yFactor;
	adjustPos();
	repaint();
    }

    // Make zoom not too big and not too small.
    // Initially, make it just fit.
    protected void adjustZoom() {
	if (scale * image.getWidth() < imagePanel.getWidth() ||
		scale * image.getHeight() < imagePanel.getHeight())
	    scale = Math.max(1.0f * imagePanel.getWidth() / image.getWidth(),
		    1.0f * imagePanel.getHeight() / image.getHeight());
	else if (scale > imagePanel.getWidth() / 10 || scale > imagePanel.getHeight() / 10)
	    scale = Math.min(imagePanel.getWidth() / 10, imagePanel.getHeight() / 10);
	adjustPos();
    }

    // Move to make rectangle visible.
    public void showFocusRect() {
	if (focusRect == null)
	    return;
	int rectX = focusRect.x + focusRect.width / 2;
	int rectY = focusRect.y + focusRect.height / 2;
	xPos = 1.0f * rectX / image.getWidth();
	yPos = 1.0f * rectY / image.getHeight();
	adjustPos();
	repaint();
    }

    // Make position in center of cutout.
    protected void adjustPos() {
	float imageW = imagePanel.getWidth() / scale / image.getWidth();
	float imageH = imagePanel.getHeight() / scale / image.getHeight();
	xPos = Math.max(xPos, imageW / 2);
	xPos = Math.min(xPos, 1 - imageW / 2);
	yPos = Math.max(yPos, imageH / 2);
	yPos = Math.min(yPos, 1 - imageH / 2);
    }

    // Recalculate display.
    protected void makeFresh() {
	adjustZoom();
	repaint();
    }

    //////////////////////////////////////////////
    // Rectangle.

    // Rectangle that is focus.
    protected Rectangle focusRect = null;
    // Rectangle under construction.
    protected Rectangle freshRect = null;

    // Make rectangle with two corner points.
    protected Rectangle makeRectangle(Point p1, Point p2) {
	int xMin = Math.min(p1.x, p2.x);
	int xMax = Math.max(p1.x, p2.x);
	int yMin = Math.min(p1.y, p2.y);
	int yMax = Math.max(p1.y, p2.y);
	int w = xMax - xMin;
	int h = yMax - yMin;
	return new Rectangle(xMin, yMin, w, h);
    }

    // Make rectangle for two points.
    protected void makeRect(Point p1, Point p2, Rectangle vis) {
	focusRect = null;
	Rectangle rect = makeRectangle(p1, p2);
	if (rect.width == 0 || rect.height == 0)
	    freshRect = null;
	else 
	    freshRect = new Rectangle(
		    vis.x + Math.round(rect.x / scale),
		    vis.y + Math.round(rect.y / scale),
		    Math.round(rect.width / scale),
		    Math.round(rect.height / scale));
	repaint();
	recordRect();
    }

    // How close mouse should be to rectangle.
    protected final int closeToRect = 10;

    // Move edges of rectangle with mouse point.
    // At most one horizontal edge and at most one vertical edge is to change.
    protected void moveRect(Point p, Rectangle vis) {
	if (focusRect == null)
	    return;
	int dist = closeToRect;
	int xMinNew = focusRect.x;
	int xMaxNew = focusRect.x + focusRect.width;
	int yMinNew = focusRect.y;
	int yMaxNew = focusRect.y + focusRect.height;
	int widthNew = focusRect.width;
	int heightNew = focusRect.height;
	int xMin = transformX(xMinNew, vis);
	int xMax = transformX(xMaxNew, vis);
	int yMin = transformY(yMinNew, vis);
	int yMax = transformY(yMaxNew, vis);
	boolean changed = false;
	boolean xMinChanged = false;
	boolean xMaxChanged = false;
	boolean yMinChanged = false;
	boolean yMaxChanged = false;
	if (Math.abs(p.x - xMin) <= dist && 
		Math.abs(p.x - xMax) > Math.abs(p.x - xMin) &&
		yMin - dist <= p.y && p.y <= yMax + dist) {
	    xMin = Math.min(xMax - 1, p.x);
	    xMinNew = vis.x + Math.round(xMin / scale);
	    widthNew = xMaxNew - xMinNew;
	    changed = true;
	}
	else if (Math.abs(p.x - xMax) <= dist && 
		yMin - dist <= p.y && p.y <= yMax + dist) {
	    xMax = Math.max(xMin + 1, p.x);
	    widthNew = Math.round((xMax-xMin) / scale);
	    changed = true;
	}
	if (Math.abs(p.y - yMin) <= dist && 
		Math.abs(p.y - yMax) > Math.abs(p.y - yMin) &&
		xMin - dist <= p.x && p.x <= xMax + dist) {
	    yMin = Math.min(yMax - 1, p.y);
	    yMinNew = vis.y + Math.round(yMin / scale);
	    heightNew = yMaxNew - yMinNew;
	    changed = true;
	}
	else if (Math.abs(p.y - yMax) <= dist && 
		xMin - dist <= p.x && p.x <= xMax + dist) {
	    yMax = Math.max(yMin + 1, p.y);
	    heightNew = Math.round((yMax-yMin) / scale);
	    changed = true;
	}
	if (changed) {
	    focusRect = new Rectangle(xMinNew, yMinNew, widthNew, heightNew);
	    repaint();
	    recordRect();
	}
    }

    // Click in rectangle mode.
    // If close to current rectangle, do nothing (may be dragged,
    // handled by moveRect).
    // If inside some component in the polygon, then change focus
    // to that rectangle.
    protected void clickRect(Point p, Rectangle vis) {
	int x = vis.x + Math.round(p.x / scale);
	int y = vis.y + Math.round(p.y / scale);
	if (closeToRect(p, focusRect, vis))
	    return;
	if (showComponents)
	    for (Rectangle comp : components) {
		if (comp.contains(x, y)) {
		    freshRect = null;
		    focusRect = (Rectangle) comp.clone();
		    repaint();
		    recordRect();
		    return;
		}
	    }
	freshRect = null;
	focusRect = null;
	repaint();
	recordRect();
    }

    // Go to next sign and select next rectangle among components,
    // unless one is already associated with sign.
    protected void acceptRect() {
	if (mouseMode != MouseMode.RECT || !showComponents) 
	    return;
	int cur = manipulator.current();
	Rectangle curRect = focusRect;
	manipulator.right();
	if (cur != manipulator.current() &&
		curRect != null &&
		manipulator.placeForImage(imageNumber) == null) {
	    ComponentFinder finder = new ComponentFinder(
		    componentConnect, componentSize);
	    Rectangle comp = finder.next(components, curRect, direction);
	    if (comp != null) {
		freshRect = null;
		focusRect = (Rectangle) comp.clone();
		repaint();
		recordRect();
	    }
	}
    }

    // Get next component after current.
    // Or first if none.
    protected void downRect() {
	if (mouseMode != MouseMode.RECT || !showComponents) 
	    return;
	Rectangle comp = null;
	if (focusRect == null) {
	    if (components.size() > 0)
		comp = components.get(0);
	} else {
	    ComponentFinder finder = new ComponentFinder(
		    componentConnect, componentSize);
	    comp = finder.next(components, focusRect, direction);
	}
	if (comp != null) {
	    freshRect = null;
	    focusRect = (Rectangle) comp.clone();
	    repaint();
	    recordRect();
	}
    }

    // Get next component after current.
    // Or last if none.
    protected void upRect() {
	if (mouseMode != MouseMode.RECT || !showComponents) 
	    return;
	Rectangle comp = null;
	if (focusRect == null) {
	    if (components.size() > 0)
		comp = components.get(components.size()-1);
	} else {
	    ComponentFinder finder = new ComponentFinder(
		    componentConnect, componentSize);
	    comp = finder.previous(components, focusRect, direction);
	}
	if (comp != null) {
	    freshRect = null;
	    focusRect = (Rectangle) comp.clone();
	    repaint();
	    recordRect();
	}
    }

    // Is mouse close to rectangle?
    protected boolean closeToRect(Point p, Rectangle rect, Rectangle vis) {
	if (rect == null)
	    return false;
	int dist = closeToRect;
	int xMin = transformX(rect.x, vis);
	int xMax = transformX(rect.x + rect.width, vis);
	int yMin = transformY(rect.y, vis);
	int yMax = transformY(rect.y + rect.height, vis);
	return Math.abs(p.x - xMin) <= dist && yMin - dist <= p.y && p.y <= yMax + dist ||
	    Math.abs(p.x - xMax) <= dist && yMin - dist <= p.y && p.y <= yMax + dist ||
	    Math.abs(p.y - yMin) <= dist && xMin - dist <= p.x && p.x <= xMax + dist ||
	    Math.abs(p.y - yMax) <= dist && xMin - dist <= p.x && p.x <= xMax + dist;
    }

    // Put new rectangle in focussed glyph if any.
    protected void recordRect() {
	manipulator.removeImageNumber(imageNumber);
	if (focusRect != null)
	    manipulator.addPlace(new ImagePlace(imageNumber,
			focusRect.x, focusRect.y,
			focusRect.width, focusRect.height));
    }

    // Upon changing image or sign, the current rectangle is to be shown.
    public void changeFocus() {
	ImagePlace place = manipulator.placeForImage(imageNumber);
	if (place != null) {
	    freshRect = null;
	    focusRect = new Rectangle(place.getX(), place.getY(),
		    place.getWidth(), place.getHeight());
	    repaint();
	} else {
	    freshRect = null;
	    focusRect = null;
	    repaint();
	}
    }

    // Is there focus rectangle on page?
    public boolean hasFocusRect() {
	return focusRect != null;
    }

    // Select sign at (or near) mouse click.
    protected void selectSignFuzzy(Point p, Rectangle vis) {
	if (selectSign(p, vis))
	    return;
	int maxDist = 10;
	for (int j = 1; j < maxDist; j++) 
	    if (selectSignRect(p, vis, j))
		return;
    }

    // Select sign exactly distance away from point.
    // Return whether found.
    private boolean selectSignRect(Point p, Rectangle vis, int dist) {
	for (int k = -dist; k <= dist; k++) 
	    if (selectSign(new Point(p.x - dist, p.y + k), vis) ||
		    selectSign(new Point(p.x + dist, p.y + k), vis) ||
		    selectSign(new Point(p.x + k, p.y - dist), vis) ||
		    selectSign(new Point(p.x + k, p.y + dist), vis))
		return true;
	return false;
    }

    // Select sign belonging to mouse click.
    // Return whether found.
    private boolean selectSign(Point p, Rectangle vis) {
	int x = vis.x + Math.round(p.x / scale);
	int y = vis.y + Math.round(p.y / scale);
	for (int i = 0; i < manipulator.nSigns(); i++) 
	    for (ImagePlace place : manipulator.sign(i).getPlaces()) 
		if (place.getNum() == imageNumber &&
			place.getX() <= x &&
			x < place.getX() + place.getWidth() &&
			place.getY() <= y &&
			y < place.getY() + place.getHeight()) {
		    manipulator.setCurrent(i);
		    return true;
		}
	return false;
    }

    //////////////////////////////////////////////
    // Polygon.

    // Polygon under construction.
    protected Polygon freshPoly = null;
    // How many points have been clicked.
    protected int nPolyClicked = 0;
    // Is it done?
    protected boolean polyDone = false;

    // Update polygon after click.
    protected void clickPoly(Point p, Rectangle vis) {
	if (scale <= 0)
	    return;
	int x = vis.x + Math.round(p.x / scale);
	int y = vis.y + Math.round(p.y / scale);
	if (freshPoly != null && polyDone) {
	    int nearPoint = nearPoint(p, freshPoly, vis);
	    if (nearPoint >= 0)
		return;
	    int nearEdge = nearEdge(p, freshPoly, vis);
	    if (nearEdge >= 0) {
		addPoly(freshPoly, nearEdge, x, y);
		findComponents();
		repaint();
	    } else if (farFromEdge(p, freshPoly, vis) &&
			!insidePoly(p, freshPoly, vis)) {
		freshPoly = null;
		nPolyClicked = 0;
		polyDone = false;
		clearComponents();
		repaint();
	    }
	    return;
	} 
	if (freshPoly == null) {
	    freshPoly = new Polygon();
	    nPolyClicked = 0;
	    polyDone = false;
	    clearComponents();
	} 
	if (nPolyClicked < freshPoly.npoints) {
	    freshPoly.xpoints[freshPoly.npoints-1] = x;
	    freshPoly.ypoints[freshPoly.npoints-1] = y;
	} else
	    freshPoly.addPoint(x, y);
	freshPoly.invalidate();
	repaint();
	nPolyClicked = freshPoly.npoints;
    }
    // Update polygon after mouse move.
    protected void movePoly(Point p, Rectangle vis) {
	if (scale <= 0)
	    return;
	if (freshPoly == null || polyDone) 
	    return;
	int x = vis.x + Math.round(p.x / scale);
	int y = vis.y + Math.round(p.y / scale);
	if (nPolyClicked < freshPoly.npoints) {
	    freshPoly.xpoints[freshPoly.npoints-1] = x;
	    freshPoly.ypoints[freshPoly.npoints-1] = y;
	} else
	    freshPoly.addPoint(x, y);
	freshPoly.invalidate();
	repaint();
    }
    // Update polygon after right mouse click.
    // If there is already finished polygon, then clear.
    protected void finishPoly() {
	if (freshPoly != null && !polyDone) {
	    polyDone = true;
	    findComponents();
	    repaint();
	} 
    }

    // Drag point of polygon.
    protected void dragPoly(Point oldP, Point newP, Rectangle vis) {
	if (freshPoly == null || !polyDone)
	    return;
	int i = nearPoint(oldP, freshPoly, vis);
	if (i >= 0) {
	    int x = vis.x + Math.round(newP.x / scale);
	    int y = vis.y + Math.round(newP.y / scale);
	    freshPoly.xpoints[i] = x;
	    freshPoly.ypoints[i] = y;
	    freshPoly.invalidate();
	    findComponents();
	    repaint();
	}
    }

    // Give number of edge near point. Or negative if none.
    protected int nearEdge(Point p, Polygon poly, Rectangle vis) {
	int nearness = 10;
	Rectangle2D rect = new Rectangle2D.Double(p.x-nearness, p.y-nearness, 
		2*nearness, 2*nearness);
	for (int i = 0; i < poly.npoints; i++) {
	    int px1 = transformX(poly.xpoints[i], vis);
	    int py1 = transformY(poly.ypoints[i], vis);
	    int px2 = transformX(poly.xpoints[(i+1)%poly.npoints], vis);
	    int py2 = transformY(poly.ypoints[(i+1)%poly.npoints], vis);
	    if (rect.intersectsLine(px1, py1, px2, py2)) 
		return i;
	}
	return -1;
    }

    // Is it far from any edge?
    protected boolean farFromEdge(Point p, Polygon poly, Rectangle vis) {
	int farness = 20;
	Rectangle2D rect = new Rectangle2D.Double(p.x-farness, p.y-farness, 
		2*farness, 2*farness);
	for (int i = 0; i < poly.npoints; i++) {
	    int px1 = transformX(poly.xpoints[i], vis);
	    int py1 = transformY(poly.ypoints[i], vis);
	    int px2 = transformX(poly.xpoints[(i+1)%poly.npoints], vis);
	    int py2 = transformY(poly.ypoints[(i+1)%poly.npoints], vis);
	    if (rect.intersectsLine(px1, py1, px2, py2)) 
		return false;
	}
	return true;
    }

    // Give number of point of polygon near point. Or negative if none.
    protected int nearPoint(Point p, Polygon poly, Rectangle vis) {
	int nearness = 10;
	Shape circle = new Ellipse2D.Double(p.x-nearness, p.y-nearness,
		2*nearness, 2*nearness);
	for (int i = 0; i < poly.npoints; i++) {
	    int px = transformX(poly.xpoints[i], vis);
	    int py = transformY(poly.ypoints[i], vis);
	    if (circle.contains(px, py)) 
		return i;
	}
	return -1;
    }

    // Return whether point is inside polygon.
    protected boolean insidePoly(Point p, Polygon poly, Rectangle vis) {
	int x = vis.x + Math.round(p.x / scale);
	int y = vis.y + Math.round(p.y / scale);
	return poly.contains(x, y);
    }

    // Add point to polygon. After i-th point.
    protected void addPoly(Polygon poly, int i, int x, int y) {
	if (i == poly.npoints - 1) {
	    poly.addPoint(x, y);
	} else {
	    poly.addPoint(poly.xpoints[poly.npoints-1],
		    poly.ypoints[poly.npoints-1]);
	    for (int j = poly.npoints - 2; j > i+1; j--) {
		poly.xpoints[j] = poly.xpoints[j-1];
		poly.ypoints[j] = poly.ypoints[j-1];
	    }
	    poly.xpoints[i+1] = x;
	    poly.ypoints[i+1] = y;
	}
	poly.invalidate();
    }

    //////////////////////////////////////////////
    // Tagged areas.

    // The path under construction.
    protected Bezier freshBezier = null;
    // How many points have been clicked.
    protected int nBezierClicked = 0;

    // Update curve after click.
    protected void clickArea(Point p, Rectangle vis) {
        if (scale <= 0)
            return;
        int x = vis.x + Math.round(p.x / scale);
        int y = vis.y + Math.round(p.y / scale);
        if (freshBezier == null || freshBezier.isClosed()) {
	    for (TaggedBezier area : manipulator.getAreas()) {
		if (area.getNum() != imageNumber)
		    continue;
		int nearPoint = nearPoint(p, area, vis);
		if (nearPoint >= 0)
		    return;
	    }
	    for (TaggedBezier area : manipulator.getAreas()) {
		if (area.getNum() != imageNumber)
		    continue;
		int nearEdge = nearEdge(p, area, vis);
		if (nearEdge >= 0) {
		    area.add(nearEdge, new Point(x, y), true);
		    manipulator.setAreas(manipulator.getAreas());
		    repaint();
		    return;
		}
	    }
	    TaggedBezier renamed = null;
	    for (TaggedBezier area : manipulator.getAreas()) {
		if (area.getNum() != imageNumber)
		    continue;
		// if (insideBezier(p, area, vis)) {
		if (insideBezierName(p, area, vis)) {
		    renamed = area;
		    break;
		}
	    }
	    if (renamed != null) {
		freshBezier = renamed;
		String name = renamed.getName();
		manipulator.removeArea(renamed);
		repaint();
		finishArea(name);
		return;
	    }
	    freshBezier = new Bezier();
	    nBezierClicked = 0;
        } else if (freshBezier.getPointSize() > 0 &&
		    nearPoints(p, freshBezier.getPoint(0), vis)) {
	    if (nBezierClicked < freshBezier.getPointSize()) {
		int last = freshBezier.getPointSize()-1;
		freshBezier.setSmooth(0, freshBezier.getSmooth(last));
		freshBezier.remove(last);
	    }
	    if (freshBezier.getPointSize() <= 2) {
		freshBezier = null;
		nBezierClicked = 0;
		repaint();
		return;
	    }
	    freshBezier.close();
	    repaint();
	    finishArea("");
	    return;
	}
        if (nBezierClicked < freshBezier.getPointSize()) {
	    freshBezier.setPoint(freshBezier.getPointSize()-1, new Point(x, y));
        } else {
	    freshBezier.add(new Point(x, y), true);
	}
        repaint();
        nBezierClicked = freshBezier.getPointSize();
    }

    // Toggle smooth/ragged of point of area.
    protected void toggleArea(Point p, Rectangle vis) {
	if (freshBezier != null) {
	    int i = nearPoint(p, freshBezier, vis);
	    if (i >= 0) {
		freshBezier.setSmooth(i, !freshBezier.getSmooth(i));
		repaint();
		return;
	    }
	}
	TaggedBezier toBeRemoved = null;
	boolean found = false;
	int nearness = 10;
	for (int dist = 1; dist <= nearness && !found; dist++) 
	    for (TaggedBezier area : manipulator.getAreas()) {
		if (area.getNum() != imageNumber)
		    continue;
		int i = nearPoint(p, area, vis, dist);
		if (i >= 0) {
		    if (area.getSmooth(i))
			area.setSmooth(i, false);
		    else {
			area.remove(i);
			if (area.getPointSize() <= 2)
			    toBeRemoved = area;
		    }
		    manipulator.setAreas(manipulator.getAreas());
		    repaint();
		    found = true;
		    break;
		}
	    }
	if (toBeRemoved != null) {
	    manipulator.removeArea(toBeRemoved);
	    repaint();
	}
    }

    // Update area after mouse move.
    protected void moveArea(Point p, Rectangle vis) {
        if (scale <= 0)
            return;
        if (freshBezier == null || freshBezier.isClosed()) 
	    return;
        int x = vis.x + Math.round(p.x / scale);
        int y = vis.y + Math.round(p.y / scale);
        if (nBezierClicked < freshBezier.getPointSize()) {
	    freshBezier.setPoint(freshBezier.getPointSize()-1, new Point(x, y));
        } else {
	    freshBezier.add(new Point(x, y), true);
	}
        repaint();
    }

    // Update area after right mouse click.
    // Ask name for area. If none, then ignore.
    protected void finishArea(String initial) {
	if (freshBezier == null)
	    return;
	String name = (String) JOptionPane.showInputDialog(this,
		"Name of area:", initial);
	if (name == null && !initial.equals("")) {
	    TaggedBezier area = new TaggedBezier(freshBezier, imageNumber, initial);
	    manipulator.addArea(area);
	} else if (name != null && !name.equals("")) {
	    TaggedBezier area = new TaggedBezier(freshBezier, imageNumber, name);
	    manipulator.addArea(area);
	}
	freshBezier = null;
	nBezierClicked = 0;
	manipulator.setAreas(manipulator.getAreas());
	repaint();
    }

    // Drag point of area.
    protected void dragArea(Point oldP, Point newP, Rectangle vis) {
	int nearness = 10;
	for (int dist = 1; dist <= nearness; dist++) 
	    for (TaggedBezier area : manipulator.getAreas()) {
		if (area.getNum() != imageNumber)
		    continue;
		int i = nearPoint(oldP, area, vis, dist);
		if (i >= 0) {
		    int x = vis.x + Math.round(newP.x / scale);
		    int y = vis.y + Math.round(newP.y / scale);
		    area.setPoint(i, new Point(x, y));
		    manipulator.setAreas(manipulator.getAreas());
		    repaint();
		    return;
		}
	    }
    }

    // Give number of edge near point. Or negative if none.
    protected int nearEdge(Point p, Bezier bezier, Rectangle vis) {
	int x = vis.x + Math.round(p.x / scale);
	int y = vis.y + Math.round(p.y / scale);
	double nearness = 5 / scale;
	Rectangle2D rect = new Rectangle2D.Double(x-nearness, y-nearness, 
		2*nearness, 2*nearness);
	for (int i = 0; i < bezier.getSegmentSize(); i++) {
	    CubicCurve2D.Double segment = bezier.getSegment(i);
	    if (segment != null && bezier.getSegment(i).intersects(rect))
		return i;
	}
	return -1;
    }

    // Give number of point of area near point. Or negative if none.
    protected int nearPoint(Point p, Bezier bezier, Rectangle vis) {
	int nearness = 10;
	return nearPoint(p, bezier, vis, nearness);
    }
    protected int nearPoint(Point p, Bezier bezier, Rectangle vis, int nearness) {
	Shape circle = new Ellipse2D.Double(p.x-nearness, p.y-nearness,
		2*nearness, 2*nearness);
	for (int i = 0; i < bezier.getPointSize(); i++) {
	    Point p2 = bezier.getPoint(i);
	    if (p2 == null)
		return -1;
	    int px = transformX(p2.x, vis);
	    int py = transformY(p2.y, vis);
	    if (circle.contains(px, py)) 
		return i;
	}
	return -1;
    }

    // Return whether point is inside bezier.
    protected boolean insideBezier(Point p, Bezier bezier, Rectangle vis) {
	int x = vis.x + Math.round(p.x / scale);
	int y = vis.y + Math.round(p.y / scale);
	return bezier.getPath().contains(x, y);
    }
    // Return whether point is inside name of bezier.
    protected boolean insideBezierName(Point p, TaggedBezier bezier, Rectangle vis) {
	return centerRect(bezier, vis).contains(p.x, p.y);
    }

    // Return center point of polygon.
    protected Point center(Bezier bezier, Rectangle vis) {
	Rectangle rect = bezier.getPath().getBounds();
	int x = rect.x + rect.width / 2;
	int y = rect.y + rect.height / 2;
	int px = transformX(x, vis);
	int py = transformY(y, vis);
	return new Point(px, py);
    }
    // Return rectangle around name near center of polygon.
    protected Rectangle centerRect(TaggedBezier bezier, Rectangle vis) {
	Point p = center(bezier, vis);
	Graphics g = getGraphics();
	FontMetrics met = g.getFontMetrics();
	Rectangle2D rect = met.getStringBounds(bezier.getName(), g);
	int x = (int) rect.getX();
	int y = (int) rect.getY();
	int width = (int) rect.getWidth();
	int height = (int) rect.getHeight();
	return new Rectangle(x + p.x - width / 2, y + p.y - height / 2, width, height);
    }

    // Are two points near?
    protected boolean nearPoints(Point p1, Point p2, Rectangle vis) {
	int nearness = 10;
	Shape circle = new Ellipse2D.Double(p1.x-nearness, p1.y-nearness,
		2*nearness, 2*nearness);
	int px = transformX(p2.x, vis);
	int py = transformY(p2.y, vis);
	return circle.contains(px, py);
    }

    //////////////////////////////////////////////
    // Component finding with polygon.

    // Proposed connected components (may be signs).
    protected LinkedList<Rectangle> components = new LinkedList<Rectangle>();

    // Cut out piece within polygon.
    // Convert to bilevel. Find rectangles
    // around connected components.
    protected void findComponents() {
	Polygon poly = freshPoly;
	if (poly == null)
	    return;
	Rectangle rect = poly.getBounds();
	ComponentFinder finder = new ComponentFinder(
		componentConnect, componentSize);
	BufferedImage cutout = finder.cutout(image, poly);
	BufferedImage bin = makeBilevel(cutout);
	LinkedList<Rectangle> rects = finder.findRectangles(bin);
	rects = finder.moveRects(rects, rect.x, rect.y);
	components = finder.order(rects, direction);
    }
    
    // Remove components.
    protected void clearComponents() {
	components = new LinkedList<Rectangle>();
    }

    ////////////////////////////////////////////
    // Cleaning up.

    // Kill auxiliary windows.
    public void dispose() {
	// nothing to be done
    }

    //////////////////////////////////////////////
    // Listeners.

    // ComponentListener
    public void componentHidden(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentResized(ComponentEvent e) {
	makeFresh();
    }
    public void componentShown(ComponentEvent e) {}

    // ActionListener
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("-"))
	    zoomOut();
	else if (e.getActionCommand().equals("+"))
	    zoomIn();
	else if (e.getActionCommand().equals("go up"))
	    goUp();
	else if (e.getActionCommand().equals("go down"))
	    goDown();
	else if (e.getActionCommand().equals("go left"))
	    goLeft();
	else if (e.getActionCommand().equals("go right"))
	    goRight();
	else if (e.getActionCommand().equals("up"))
	    upRect();
	else if (e.getActionCommand().equals("down"))
	    downRect();
	else if (e.getActionCommand().equals("accept"))
	    acceptRect();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Appearance.

    // Set cursor.
    protected void setBusy(boolean busy) {
        if (busy) {
            imagePanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        } else {
            imagePanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
    }

    //////////////////////////////////////////////////////////////////////////////

    // For testing.
    public static void main(String[] args) {
	if (args.length == 0) 
	    System.exit(1);

	final String filename = args[0];

	JFrame frame = new JFrame();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	ZoomImagePage panel = null;
	try {
	    final BufferedImage image = ImageIO.read(FileAux.fromBase(filename));
	    panel = new ZoomImagePage(image, null);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null, e.getMessage(), "",
		JOptionPane.ERROR_MESSAGE);
	    System.exit(1);
	}

	frame.getContentPane().add(panel, BorderLayout.CENTER);
	frame.setSize(new Dimension(300, 500));
	frame.setVisible(true);
    }
}
