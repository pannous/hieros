package nederhof.ocr;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.ocr.images.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Edits piece of line.
public abstract class ImageEditor extends JFrame implements ActionListener {

	// The line of which this is part.
	private Line line;

	// The original blobs in this part.
	private Vector<Blob> oldGlyphs = new Vector<Blob>();
	// The current ones.
	private Vector<Blob> glyphs = new Vector<Blob>();

	// The position of the part within the line.
	private int partX;
	private int partY;

	// The blobs focussed.
	private JPanel focusPanel = new JPanel();
	private SimpleScroller focusScroller;
	private HashSet<Blob> focusSet = new HashSet<Blob>();

	// The original image.
	private BinaryImage im;
	// The overlayed image with focussed glyphs.
	private BinaryImage overlayed;
	// The scaled, combined image.
	private EditImagePanel imPanel;
	// The scroller containing image.
	private SimpleScroller imScroller;

	// Constructor.
	public ImageEditor(Line line, 
			BinaryImage im, int x, int y,
			double scale) {
		this.line = line;
		this.im = im;
		this.partX = x;
		this.partY = y;
		this.overlayed = new BinaryImage(im.width(), im.height());
		setJMenuBar(new Menu(this));
		setTitle("Glyph editor");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseListener());

		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		computeOriginal();
		addBlobPanel();
		addImPanel(scale);

		setVisible(true);
		pack();
	}

	//////////////////////////////////////////////////////
	// Getting glyphs from line.

	// Get glyphs from line that fit within rectangle.
	private void computeOriginal() {
		for (Blob b : line.aliveGlyphs()) {
			if (b.x() >= partX && 
					b.x() + b.width() < partX + im.width() &&
					b.y() >= partY && 
					b.y() + b.height() < partY + im.height()) {
				oldGlyphs.add(b);
			}
		}
		glyphs.addAll(oldGlyphs);
		if (glyphs.size() == 1) {
			focusSet.add(glyphs.get(0));
			fillOverlay();
		}
	}

	//////////////////////////////////////////////////////
	// Panel with glyphs.

	// Add panel with blobs.
	private void addBlobPanel() {
		focusScroller = new SimpleScroller(focusPanel, false, true);
		setFocusButtons();
		add(focusScroller);
	}

	// Put glyphs in buttons.
	private void setFocusButtons() {
		focusPanel.removeAll();
		for (Blob b : glyphs) {
			ConnectedButton but = new ConnectedButton(b);
			focusPanel.add(but);
			but.setSelected(focusSet.contains(b));
		}
		focusPanel.add(Box.createHorizontalGlue());
		focusPanel.revalidate();
	}

	// Connected button.
	private class ConnectedButton extends BlobButton {
		public ConnectedButton(Blob blob) {
			super(blob);
		}
		public void clicked(Blob blob, boolean selected) {
			if (selected) {
				focusSet.add(blob);
				refreshOverlay();
			} else {
				focusSet.remove(blob);
				refreshOverlay();
			}
		}
	}

	//////////////////////////////////////////////////////
	// Image panel.

	// Add panel with image.
	private void addImPanel(double scale) {
		imPanel = new ConnectedImagePanel(scale);
		imScroller = new SimpleScroller(imPanel, true, true);
		add(imScroller);
		imPanel.setCircleMode(true);
	}

	private class ConnectedImagePanel extends EditImagePanel {
		public ConnectedImagePanel(double scale) {
			super(im, overlayed, scale, scale);
			setRadius(Settings.circleSize);
		}
		public void include(int x, int y) {
			includeInGlyphs(x, y);
		}
		public void exclude(int x, int y) {
			excludeFromGlyphs(x, y);
		}
		public void click(int x, int y) {
			selectComponent(x, y);
		}
	}

	private void refreshOverlay() {
		fillOverlay();
		imPanel.update();
	}

	// Fill overlay with focussed glyphs.
	private void fillOverlay() {
		clearOverlay();
		for (Blob b : focusSet) 
			BinaryImage.superimpose(overlayed, b.imSafe(),
					b.x() - partX, b.y() - partY);
	}

	// Set overlay to white.
	private void clearOverlay() {
		for (int x = 0; x < overlayed.width(); x++)
			for (int y = 0; y < overlayed.height(); y++) 
				overlayed.set(x, y, false);
	}

	///////////////////////////////////////////////////////////
	// Menu at top of window.

	private final ClickButton closeButton = new ClickButton(this,
			"clo<u>S</u>e", "close", KeyEvent.VK_S);
	private final JMenu resourceMenu = new EnabledMenu(
			"<u>R</u>esource", KeyEvent.VK_R);
	private final JMenuItem allItem = new EnabledMenuItem(this,
			"<u>A</u>ll", "all", KeyEvent.VK_A);
	private final JMenuItem mergeItem = new EnabledMenuItem(this,
			"<u>M</u>erge", "merge", KeyEvent.VK_M);
	private final JMenuItem splitItem = new EnabledMenuItem(this,
			"spli<u>T</u>", "split", KeyEvent.VK_T);
	private final JMenuItem copyItem = new EnabledMenuItem(this,
			"<u>C</u>opy", "copy", KeyEvent.VK_C);
	private final JMenuItem deleteItem = new EnabledMenuItem(this,
			"<u>D</u>elete", "delete", KeyEvent.VK_D);
	private final JMenuItem closureItem = new EnabledMenuItem(this,
			"c<u>L</u>osure", "closure", KeyEvent.VK_L);
	private final JMenuItem unshadeItem = new EnabledMenuItem(this,
			"unshad<u>E</u>", "unshade", KeyEvent.VK_E);
	private final JMenuItem paintItem = new EnabledMenuItem(this,
			"<u>P</u>aint", "paint", KeyEvent.VK_P);
	private final JMenuItem extraItem = new EnabledMenuItem(this,
			"paint <u>I</u>nclusive", "paint inclusive", KeyEvent.VK_I);
	private final JMenuItem upItem = new EnabledMenuItem(this,
			"radius up", "up", KeyEvent.VK_UP);
	private final JMenuItem downItem = new EnabledMenuItem(this,
			"radius down", "down", KeyEvent.VK_DOWN);

	// Menu containing buttons.
	private class Menu extends JMenuBar {

		// Distance between buttons.
		private static final int STRUT_SIZE = 10;

		public Menu(ActionListener lis) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(Color.LIGHT_GRAY);
			add(Box.createHorizontalStrut(STRUT_SIZE));

			// file
			add(closeButton);
			add(Box.createHorizontalStrut(STRUT_SIZE));

			// resource
			add(resourceMenu);
			add(Box.createHorizontalStrut(STRUT_SIZE));
			resourceMenu.add(allItem);
			resourceMenu.add(mergeItem);
			resourceMenu.add(splitItem);
			resourceMenu.add(copyItem);
			resourceMenu.add(deleteItem);
			resourceMenu.add(closureItem);
			resourceMenu.add(unshadeItem);
			resourceMenu.add(paintItem);
			resourceMenu.add(extraItem);
			resourceMenu.add(upItem);
			resourceMenu.add(downItem);

			add(Box.createHorizontalGlue());
		}
	}

	// In inclusive paint mode, pixels can be included that
	// are white in the main image.
	private boolean inclusive = false;

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("close")) {
			save();
			dispose();
		} else if (e.getActionCommand().equals("all")) {
			focusAll();
		} else if (e.getActionCommand().equals("merge")) {
			merge();
		} else if (e.getActionCommand().equals("split")) {
			split();
		} else if (e.getActionCommand().equals("copy")) {
			copy();
		} else if (e.getActionCommand().equals("delete")) {
			delete();
		} else if (e.getActionCommand().equals("closure")) {
			closure();
		} else if (e.getActionCommand().equals("unshade")) {
			unshade();
		} else if (e.getActionCommand().equals("paint")) {
			inclusive = false;
		} else if (e.getActionCommand().equals("paint inclusive")) {
			inclusive = true;
		} else if (e.getActionCommand().equals("up")) {
			imPanel.upRadius();
		} else if (e.getActionCommand().equals("down")) {
			imPanel.downRadius();
		}
	}

	// Listen if window to be closed or iconified.
	// Open quit, save.
	private class CloseListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			save();
			dispose();
		}
		public void windowIconified(WindowEvent e) {
			setState(Frame.ICONIFIED);
		}
		public void windowDeiconified(WindowEvent e) {
			setState(Frame.NORMAL);
		}
	}

	// Saving back to main program.
	private void save() {
		save(oldGlyphs, glyphs);
	}

	// Caller overrides.
	protected abstract void save(Vector<Blob> old, Vector<Blob> current);
	protected abstract LayoutAnalyzer getAnalyzer();

	//////////////////////////////////////////////
	// Changing glyphs.

	private void includeInGlyphs(int x, int y) {
		if (!overlayed.get(x, y) && (inclusive || im.get(x, y))) {
			overlayed.set(x, y, true);
			imPanel.update(x, y);
			if (focusSet.isEmpty()) {
				Blob b = new Blob(partX + x, partY + y, 1, 1);
				b.set(partX + x, partY + y, true);
				glyphs.add(b);
				focusSet.add(b);
			} else {
				makeSafeBlobs();
				for (Blob b : focusSet) 
					b.set(partX + x, partY + y, true);
			}
			setFocusButtons();
			repaint();
		}
	}

	private void excludeFromGlyphs(int x, int y) {
		if (overlayed.get(x, y)) {
			overlayed.set(x, y, false);
			imPanel.update(x, y);
			makeSafeBlobs();
			Vector<Blob> empties = new Vector<Blob>();
			for (Blob b : focusSet) {
				b.set(partX + x, partY + y, false);
				if (b.isEmpty()) 
					empties.add(b);
			}
			for (Blob b : empties) {
				glyphs.remove(b);
				focusSet.remove(b);
			}
			setFocusButtons();
			repaint();
		}
	}

	// Replace saved blobs by new ones.
	private void makeSafeBlobs() {
		Vector<Blob> safes = new Vector<Blob>();
		for (Blob b : focusSet) 
			if (b.isSaved()) {
				Blob c = b.pixelCopy();
				glyphs.remove(b);
				glyphs.add(c);
				safes.add(c);
			} else
				safes.add(b);
		focusSet.clear();
		focusSet.addAll(safes);
		glyphs = getAnalyzer().order(glyphs, line.dir);
	}

	// Select or unselect the blob containing the pixel, 
	// or create a new blob containing the pixel.
	private void selectComponent(int x, int y) {
		boolean found = false;
		for (Blob b : glyphs)
			if (b.get(partX + x, partY + y)) {
				if (focusSet.contains(b))
					focusSet.remove(b);
				else
					focusSet.add(b);
				found = true;
			}
		if (!found) {
			Vector<Point> comp = ImageComponents.find(im, x, y);
			if (comp.isEmpty())
				return;
			comp = move(comp, partX, partY);
			Blob compBlob = new Blob(comp);
			glyphs.add(compBlob);
			glyphs = getAnalyzer().order(glyphs, line.dir);
			focusSet.add(compBlob);
		}
		setFocusButtons();
		refreshOverlay();
		repaint();
	}

	// Set focus to include all.
	private void focusAll() {
		for (Blob b : glyphs)
			focusSet.add(b);
		setFocusButtons();
		refreshOverlay();
		repaint();
	}

	// Merge focussed glyphs into one.
	private void merge() {
		if (focusSet.size() < 2) {
			JOptionPane.showMessageDialog(this,
					"For merge, select 2 or more glyph",
					"Wrong selection", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int xMin = Integer.MAX_VALUE;
		int xMax = 0;
		int yMin = Integer.MAX_VALUE;
		int yMax = 0;
		for (Blob b : focusSet) {
			xMin = Math.min(xMin, b.x());
			xMax = Math.max(xMax, b.x() + b.width());
			yMin = Math.min(yMin, b.y());
			yMax = Math.max(yMax, b.y() + b.height());
		}
		Blob merged = new Blob(xMin, yMin, xMax-xMin, yMax-yMin);
		for (Blob b : focusSet) {
			BinaryImage.superimpose(merged.imSafe(), 
					b.imSafe(), b.x() - merged.x(), b.y() - merged.y());
			glyphs.remove(b);
		}
		glyphs.add(merged);
		glyphs = getAnalyzer().order(glyphs, line.dir);
		focusSet.clear();
		focusSet.add(merged);
		setFocusButtons();
		refreshOverlay();
		repaint();
	}

	// Split focussed glyph into several, according to connected
	// components.
	private void split() {
		if (focusSet.size() != 1) {
			JOptionPane.showMessageDialog(this,
					"For split, select 1 glyph",
					"Wrong selection", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Blob b = focusSet.iterator().next();
		Vector<Vector<Point>> comps = ImageComponents.find(b.imSafe());
		if (comps.size() != 1) {
			for (Vector<Point> comp : comps) 
				if (comp.size() > 5) {
					comp = move(comp, b.x(), b.y());
					Blob compBlob = new Blob(comp);
					glyphs.add(compBlob);
				}
			glyphs.remove(b);
			glyphs = getAnalyzer().order(glyphs, line.dir);
			focusSet.clear();
			setFocusButtons();
			refreshOverlay();
			repaint();
		}
	}

	// Remove lines that could be shading.
	private void unshade() {
		BinaryImage shaded = new BinaryImage(im.width(), im.height());
		for (Blob b : focusSet) {
			BinaryImage.superimpose(shaded, b.imSafe(),
					b.x() - partX, b.y() - partY);
		}
		new Shade(shaded);
		for (Blob b : focusSet) {
			BinaryImage.mask(shaded, b.imSafe(),
					b.x() - partX, b.y() - partY);
		}
		refreshOverlay();
		repaint();
	}

	// Make two copies of focussed glyph.
	// Set focus to second.
	private void copy() {
		if (focusSet.size() != 1) {
			JOptionPane.showMessageDialog(this,
					"For copy, select 1 glyph",
					"Wrong selection", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Blob b = focusSet.iterator().next();
		Blob c = b.pixelCopy();
		glyphs.add(c);
		glyphs = getAnalyzer().order(glyphs, line.dir);
		focusSet.clear();
		focusSet.add(c);
		setFocusButtons();
		refreshOverlay();
		repaint();
	}

	// Delete focussed glyphs.
	private void delete() {
		for (Blob b : focusSet) {
			glyphs.remove(b);
		}
		focusSet.clear();
		setFocusButtons();
		refreshOverlay();
		repaint();
	}

	// Add all pixels connected to already selected pixels.
	private void closure() {
		if (focusSet.size() != 1) {
			JOptionPane.showMessageDialog(this,
					"For closure, select 1 glyph",
					"Wrong selection", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Blob b = focusSet.iterator().next();
		Vector<Point> unclosed = b.points();
		Vector<Point> unclosedNorm = move(unclosed, -partX, -partY);
		Vector<Point> closedNorm = ImageComponents.find(im, unclosedNorm);
		Vector<Point> closed = move(closedNorm, partX, partY);
		closed.addAll(unclosed);
		if (closed.size() == unclosed.size())
			return;
		Blob closedBlob = new Blob(closed);
		glyphs.remove(b);
		focusSet.remove(b);
		glyphs.add(closedBlob);
		focusSet.add(closedBlob);
		glyphs = getAnalyzer().order(glyphs, line.dir);
		setFocusButtons();
		refreshOverlay();
		repaint();
	}

	///////////////////////////////////////////////////////
	// Generic.

	// Move points.
	private Vector<Point> move(Vector<Point> ps, int x, int y) {
		Vector<Point> moved = new Vector<Point>();
		for (Point p : ps) 
			moved.add(new Point(p.x + x, p.y + y));
		return moved;
	}

}
