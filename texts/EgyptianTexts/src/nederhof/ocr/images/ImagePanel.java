package nederhof.ocr.images;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

// A panel with an image and shapes.
// Shapes have labels of class L.
public class ImagePanel<L> extends JPanel 
	implements MouseListener, MouseMotionListener {

    // The unscaled image.
    protected BinaryImage unscaled;
    // The image (scaled).
    protected BufferedImage image;

    // Factors to be scaled down.
    protected double xScale = 1.0;
    protected double yScale = 1.0;

    // Added shapes.
    protected Vector<LabelledShape<L>> shapes = new Vector<LabelledShape<L>>();

    protected ImagePanel() {
	addMouseListener(this);
	addMouseMotionListener(this);
    }

    // Construct.
    public ImagePanel(BinaryImage image) {
	this(image, 1.0, 1.0);
    }
    // Construct with scaling.
    public ImagePanel(BinaryImage unscaled, double xScale, double yScale) {
	this.unscaled = unscaled;
	this.image = scale(unscaled, xScale, yScale);
	this.xScale = xScale;
	this.yScale = yScale;
	addMouseListener(this);
	addMouseMotionListener(this);
    }

    // Make scaled image.
    private static BufferedImage scale(BinaryImage binaryImage,
            double xScale, double yScale) {
        BufferedImage unscaled = binaryImage.toBufferedImage();
        int width = (int) Math.round(unscaled.getWidth() * xScale);
        int height = (int) Math.round(unscaled.getHeight() * yScale);
        BufferedImage buffered = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffered.createGraphics();
        g.drawImage(unscaled, 0, 0, width, height, null);
        g.dispose();
        return buffered;
    }

    // Get binary image.
    public BinaryImage getBinaryImage() {
        return unscaled;
    }

    // Get values of scaling.
    public double getXScale() {
	return xScale;
    }
    public double getYScale() {
	return yScale;
    }

    // Set shapes (no repaint).
    public void setShapes(Vector<LabelledShape<L>> shapes) {
	this.shapes = shapes;
    }

    // Add shape.
    public void add(LabelledShape<L> s) {
	shapes.add(s);
	repaint();
    }
    // Remove shape.
    public void remove(LabelledShape<L> s) {
	shapes.remove(s);
	repaint();
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

    // Paint the image itself.
    public void paint(Graphics g) {
	g.drawImage(image, 0, 0, null);

	Graphics2D g2 = (Graphics2D) g;
	AffineTransform scaling = 
	    AffineTransform.getScaleInstance(xScale, yScale);
	g2.transform(scaling);
	for (LabelledShape<L> s : shapes) {
	    Color color = color(s);
	    Stroke stroke = stroke(s);
	    g2.setColor(color);
	    g2.setStroke(stroke);
	    g2.draw(s.shape);
	}
    }

    // Get color of shape.
    // Method can be overridden.
    protected Color color(LabelledShape<L> s) {
	return Color.BLACK;
    }

    // Get stroke of shape.
    // Method can be overridden.
    protected Stroke stroke(LabelledShape<L> s) {
	return new BasicStroke();
    }

    // Mouse events.
    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
    }
    public void mouseMoved(MouseEvent e) {
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

}
