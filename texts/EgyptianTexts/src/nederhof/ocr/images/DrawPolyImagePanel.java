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
public class DrawPolyImagePanel extends ImagePanel<Integer> {

	// Responds to mouse actions?
	private boolean enabled = true;
	public void setEnabled(boolean allow) {
		enabled = allow;
	}

	// Create from binary image.
	public DrawPolyImagePanel(BinaryImage binaryImage) {
		this(binaryImage, 1.0, 1.0);
	}
	public DrawPolyImagePanel(BinaryImage binaryImage, 
			double xScale, double yScale) {
		super(binaryImage, xScale, yScale);
	}

	// Current selection. We count number clicked to
	// be able to take provisional points for mouse 
	// movements.
	private LabelledShape<Integer> currentShape = null;
	private Polygon currentPolygon = null;
	private int nCurrentClicked = 0;

	// If there is no current selection, there may be
	// focussed polygon. There may be point thereof that
	// is focussed too.
	private LabelledShape<Integer> focussedShape = null;
	private int focussedPointNum = -1;

	// Superclass may override.
	protected void addPolygon(LabelledShape<Integer> poly) {
	}
	protected void removePolygon(LabelledShape<Integer> poly) {
	}
	protected void reprocess(Polygon poly) {
	}
	protected void reorder(Vector<Polygon> polys) {
	}

	protected Color color(LabelledShape<Integer> s) {
		if (s == currentShape)
			return Color.GRAY;
		else if (s == focussedShape)
			return Color.GREEN;
		else 
			return Color.BLUE;
	}
	protected Stroke stroke(LabelledShape<Integer> s) {
		if (s == focussedShape)
			return new BasicStroke(4);
		else
			return new BasicStroke(2);
	}

	// While polygon is being created by clicking its
	// points, it is the 'current' one.
	// Left mouse click for adding points.
	// Right mouse click for removing polygons.
	// Left clicking inside polygon makes that focussed.
	public void mouseClicked(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {
		if (!enabled)
			return;
		Point pPanel = e.getPoint();
		Point p = panelToImage(pPanel);
		if (leftClickOnPoint(e))
			;
		else if (leftClickOnLine(e))
			;
		else if (leftClickInPoly(e))
			;
		else if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
			if (currentShape == null) {
				currentPolygon = new Polygon();
				currentShape = new LabelledShape(currentPolygon, 
						freshNumber());
				nCurrentClicked = 0;
				focussedShape = null;
				focussedPointNum = -1;
				add(currentShape);
			}
			if (nCurrentClicked < currentPolygon.npoints) {
				currentPolygon.xpoints[currentPolygon.npoints-1] = p.x;
				currentPolygon.ypoints[currentPolygon.npoints-1] = p.y;
			} else 
				currentPolygon.addPoint(p.x, p.y);
			currentPolygon.invalidate();
			nCurrentClicked = currentPolygon.npoints;
		} else if (currentShape != null) {
			if (currentPolygon.npoints < 3)
				remove(currentShape);
			else {
				addPolygon(currentShape);
				reorder(orderedPolygons());
			}
			currentShape = null;
		} else {
			for (LabelledShape<Integer> shape : shapes) 
				if (shape.shape.contains(p.x, p.y) && 
						shape.shape instanceof Polygon) {
					removePolygon(shape);
					remove(shape);
					break;
				}
			focussedShape = null;
		}
		repaint();
	}
	public void mouseReleased(MouseEvent e) {
		if (!enabled)
			return;
		if (focussedPointNum >= 0) {
			reprocess((Polygon) focussedShape.shape);
			focussedPointNum = -1;
		}
	}
	public void mouseDragged(MouseEvent e) {
		if (!enabled)
			return;
		Point pPanel = e.getPoint();
		Point p = panelToImage(pPanel);
		if (focussedPointNum >= 0) {
			Polygon poly = (Polygon) focussedShape.shape;
			poly.xpoints[focussedPointNum] = p.x;
			poly.ypoints[focussedPointNum] = p.y;
			poly.invalidate();
			repaint();
		}
	}
	public void mouseMoved(MouseEvent e) {
		if (!enabled)
			return;
		Point pPanel = e.getPoint();
		Point p = panelToImage(pPanel);
		if (currentShape != null &&
				e.getButton() == MouseEvent.NOBUTTON) {
			if (nCurrentClicked < currentPolygon.npoints) {
				currentPolygon.xpoints[currentPolygon.npoints-1] = p.x;
				currentPolygon.ypoints[currentPolygon.npoints-1] = p.y;
			} else 
				currentPolygon.addPoint(p.x, p.y);
			currentPolygon.invalidate();
			repaint();
		} 
	}

	// Radius for moving points.
	private final int r = 15;

	// Is there left click on point? If so, record.
	private boolean leftClickOnPoint(MouseEvent e) {
		if (currentShape == null && 
				e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
			Point pPanel = e.getPoint();
			Point p = panelToImage(pPanel);
			Shape circle = new Ellipse2D.Double(p.x-r, p.y-r, 2*r, 2*r);
			for (LabelledShape<Integer> s : shapes) {
				if (s.shape instanceof Polygon) {
					Polygon poly = (Polygon) s.shape;
					for (int i = 0; i < poly.npoints; i++) {
						int px = poly.xpoints[i];
						int py = poly.ypoints[i];
						if (circle.contains(px, py)) {
							focussedShape = s;
							focussedPointNum = i;
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	// Is there left click on line? If so, add point.
	private boolean leftClickOnLine(MouseEvent e) {
		if (currentShape == null && 
				e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
			Point pPanel = e.getPoint();
			Point p = panelToImage(pPanel);
			Rectangle2D rect = new Rectangle2D.Double(p.x-r, p.y-r, 2*r, 2*r);
			for (LabelledShape<Integer> s : shapes) {
				if (s.shape instanceof Polygon) {
					Polygon poly = (Polygon) s.shape;
					for (int i = 0; i < poly.npoints; i++) {
						int px1 = poly.xpoints[i];
						int py1 = poly.ypoints[i];
						int px2 = poly.xpoints[(i+1)%poly.npoints];
						int py2 = poly.ypoints[(i+1)%poly.npoints];
						if (rect.intersectsLine(px1, py1, px2, py2)) {
							if (i == poly.npoints - 1) {
								poly.addPoint(p.x, p.y);
							} else {
								poly.addPoint(poly.xpoints[poly.npoints-1], 
										poly.ypoints[poly.npoints-1]);
								for (int j = poly.npoints - 2; j > i+1; j--) {
									poly.xpoints[j] = poly.xpoints[j-1];
									poly.ypoints[j] = poly.ypoints[j-1];
								}
								poly.xpoints[i+1] = p.x;
								poly.ypoints[i+1] = p.y;
							}
							poly.invalidate();
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	// Is there left click inside polygon? Is so, record it.
	private boolean leftClickInPoly(MouseEvent e) {
		if (currentShape == null && 
				e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
			Point pPanel = e.getPoint();
			Point p = panelToImage(pPanel);
			for (LabelledShape<Integer> s : shapes) 
				if (s.shape.contains(p.x, p.y)) {
					if (focussedShape == s)
						focussedShape = null;
					else if (focussedShape != null) {
						orderFocus(focussedShape, s);
						focussedShape = s;
					} else
						focussedShape = s;
					return true;
				}
		}
		return false;
	}

	// Make clicked point follower of previous one. Move all positions up.
	// As a special case, if the follower is number 0, swap positions.
	private void orderFocus(LabelledShape<Integer> first, LabelledShape<Integer> second) {
		int i = first.label;
		int j = second.label;
		if (j > i) {
			for (int k = j-1; k > i; k--) 
				for (LabelledShape<Integer> s : shapes) 
					if (s.label == k)
						s.label = k+1;
			second.label = i+1;
		} else if (j == 0 && i != j) {
			for (LabelledShape<Integer> s : shapes) {
				if (s.label == i)
					s.label = j;
				else if (s.label == j)
					s.label = i;
			}
		}
		reorder(orderedPolygons());
	}

	// Return shapes in order of label.
	public Vector<Polygon> orderedPolygons() {
		Collections.sort(shapes, new LabelledComparator());
		Vector<Polygon> sorted = new Vector<Polygon>();
		for (LabelledShape<Integer> s : shapes)
			sorted.add((Polygon) s.shape);
		return sorted;
	}
	public class LabelledComparator implements Comparator<LabelledShape<Integer>> {
		public int compare(LabelledShape<Integer> s1, LabelledShape<Integer> s2) {
			return s1.label.compareTo(s2.label);
		}
	}

	// Compute smallest number not in use.
	private int freshNumber() {
		for (int n = 0; n <= shapes.size(); n++) {
			boolean seen = false;
			for (LabelledShape<Integer> s : shapes) 
				if (s.label == n)
					seen = true;
			if (!seen)
				return n;
		}
		return shapes.size();
	}

	// Add numbers of rectangles.
	// Also points.
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.BLUE);
		for (LabelledShape<Integer> s : shapes) {
			if (s.shape instanceof Polygon) {
				Polygon poly = (Polygon) s.shape;
				for (int i = 0; i < poly.npoints; i++) {
					int px = poly.xpoints[i];
					int py = poly.ypoints[i];
					Shape circle = new Ellipse2D.Double(px-r, py-r, 2*r, 2*r);
					g2.draw(circle);
				}
				int xMax = Integer.MIN_VALUE;
				int xMin = Integer.MAX_VALUE;
				int yMax = Integer.MIN_VALUE;
				int yMin = Integer.MAX_VALUE;
				for (int i = 0; i < poly.npoints; i++) {
					int px = poly.xpoints[i];
					int py = poly.ypoints[i];
					xMax = Math.max(xMax, px);
					xMin = Math.min(xMin, px);
					yMax = Math.max(yMax, py);
					yMin = Math.min(yMin, py);
				}
				int x = xMin + (xMax-xMin)/2;
				int y = yMin + (yMax-yMin)/2;
				Font f = new Font("Times New Roman", Font.BOLD, 
						(int) (20 / xScale));
				g2.setFont(f);
				g2.drawString("" + s.label, x, y);
			}
		}
	}

	// Testing.
	public static void main(String[] args) {
		BinaryImage image = new BinaryImage(30, 30);
		ImagePanel panel = new DrawPolyImagePanel(image, 15, 15);
		JFrame frame = new JFrame("test");
		Container content = frame.getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
		content.add(panel);
		frame.pack();
		frame.setVisible(true);
	}

}
