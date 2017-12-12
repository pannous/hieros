package nederhof.ocr;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.ocr.guessing.*;
import nederhof.ocr.images.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Pane in which OCR can be manipulated.
public abstract class OcrPage extends JTabbedPane implements ActionListener {

	// The page.
	protected Page page;
	// Image itself.
	protected BinaryImage im;
	// GUI of page.
	protected DrawPolyImagePanel imPanel;

	private double scale = 1.0;
	private final double subImageScaleup = 2.0;

	// Panel holding lines, one of which holds focus.
	protected JPanel linesPanel = new ScrollablePanel(true, false);
	// Scroller holding linesPanel.
	private SimpleScroller scroller;

	// Line holding focus.
	protected Line focusLine = null;

	// Constructor.
	public OcrPage(BinaryImage im, Page page) {
		this.im = im;
		this.page = page;
		if (im.height() > Settings.maxImageHeight) 
			scale = 1.0 * Settings.maxImageHeight / im.height();
		imPanel = new ConnectedDrawPanel(im, scale, scale);
		imPanel.setShapes(shapes(page.lines));
		addTab("lines", new SimpleScroller(imPanel, true, true));
		linesPanel.setLayout(new BoxLayout(linesPanel, BoxLayout.Y_AXIS));
		setLines();
		scroller = new SimpleScroller(linesPanel, true, false);
		addTab("glyphs", scroller);
	}

	// Get page of this.
	public Page getPage() {
		return page;
	}
	
	// DrawPolyImagePanel connected to this.
	private class ConnectedDrawPanel extends DrawPolyImagePanel {
		public ConnectedDrawPanel(BinaryImage im, double xScale, double yScale) { 
			super(im, xScale, yScale); 
		}
		public void addPolygon(LabelledShape<Integer> poly) {
			Polygon polygon = (Polygon) poly.shape;
			String dir = dir(polygon);
			Line line = new Line(dir, polygon, OcrPage.this.page.name);
			addLine(line);
		}
		public void removePolygon(LabelledShape<Integer> poly) {
			Line offending = null;
			for (Line line : OcrPage.this.page.lines)
				if (line.polygon == poly.shape)
					offending = line;
			if (offending != null) {
				removeLine(offending);
				setLines();
			}
		}
		public void reprocess(Polygon poly) {
			OcrPage.this.reprocess(poly);
			setLines();
		}
		public void reorder(Vector<Polygon> polys) {
			OcrPage.this.reorder(polys);
			setLines();
		}
	}

	// Communication to caller.
	protected abstract OcrProcess getProcess();
	protected abstract LayoutAnalyzer getAnalyzer();
	protected abstract GlyphCombiner createCombiner(Line line);
	protected abstract BlobFormatter getFormatter();
	protected abstract boolean getAutoSegment();
	protected abstract boolean getAutoOcr();
	protected abstract boolean getAutoFormat();
	protected abstract void allowEdits(boolean allow);
	protected abstract void setWait(boolean wait);

	// Convert polygons of lines to shapes.
	private Vector<LabelledShape<Integer>> shapes(Vector<Line> lines) {
		int nLines = 0;
		Vector<LabelledShape<Integer>> shapes = 
					new Vector<LabelledShape<Integer>>();
		for (Line line : lines) 
			shapes.add(new LabelledShape<Integer>(line.polygon, nLines++));
		return shapes;
	}

	/////////////////////////////////////////////////////////////
	// Setting/resetting lines.

	// Scroll to vertical position.
	private void scrollTo(int y) {
		SwingUtilities.invokeLater(new ScrollUpdater(y));
	}

	// Restore scroller.
	private class ScrollUpdater implements Runnable {
		private int y;
		public ScrollUpdater(int y) {
			this.y = y;
		}
		public void run() {
			if (scroller != null && scroller.getVerticalScrollBar() != null)
				scroller.getVerticalScrollBar().setValue(y);
		}
	}

	// Current scroll position.
	private int scrollPos() {
		if (scroller != null && scroller.getVerticalScrollBar() != null)
			return scroller.getVerticalScrollBar().getValue();
		else 
			return 0;
	}

	// Set lines in panel.
	public void setLines() {
		int y = scrollPos();
		linesPanel.removeAll();
		double subScale = scale * subImageScaleup;
		for (Line line : page.lines) 
			if (line == focusLine) 
				linesPanel.add(createOcrLineFocus(line, subScale));
			else
				linesPanel.add(createOcrLine(line, subScale));
		linesPanel.add(vertGlue());
		revalidate();
		scrollTo(y);
	}

	// Subclass to define.
	protected abstract OcrLine createOcrLine(Line line, double subScale);
	protected abstract OcrLineFocus createOcrLineFocus(Line line, double subScale);

	///////////////////////////////////////////////////////
	// Actions.

	// Send to lines.
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("up")) 
			moveFocusUp();
		else if (command.equals("down"))
			moveFocusDown();
		else if (command.equals("analyze") ||
				command.equals("analyze all"))
			findLines();
		else if (command.equals("delete"))
			deleteLines();
		else if (command.equals("format") ||
				command.equals("hlr") ||
				command.equals("hrl") ||
				command.equals("vlr") ||
				command.equals("vrl")) {
			OcrLineFocus line = currentLine();
			if (line != null)
				line.actionPerformed(e);
		} else if (command.equals("format all")) {
			for (Component comp : linesPanel.getComponents()) 
				if (comp instanceof OcrLine) {
					OcrLine line = (OcrLine) comp;
					line.actionPerformed(e);
				}
			setLines();
		} else // delegate to lines
			for (Component comp : linesPanel.getComponents()) 
				if (comp instanceof OcrLine) {
					OcrLine line = (OcrLine) comp;
					line.actionPerformed(e);
				}
	}

	// Get current line, or null.
	private OcrLineFocus currentLine() {
		for (Component comp : linesPanel.getComponents()) 
			if (comp instanceof OcrLineFocus) 
				return (OcrLineFocus) comp;
		return null;
	}

	// Set enabled.
	public void setEnabled(boolean allow) {
		super.setEnabled(allow);
		imPanel.setEnabled(allow);
		for (Component comp : linesPanel.getComponents()) 
			if (comp instanceof OcrLine) {
				OcrLine line = (OcrLine) comp;
				line.setEnabled(allow);
			}
	}

	////////////////////////////////////
	// Focus.

	// Move focus to line above/below.
	// Or to first/last line.
	private void moveFocusUp() {
		if (focusNum() >= 0)
			moveFocus(focusNum()-1);
		else
			moveFocus(page.lines.size()-1);
	}
	private void moveFocusDown() {
		if (focusNum() >= 0)
			moveFocus(focusNum()+1);
		else
			moveFocus(0);
	}

	// Move focus to line with number.
	// If wrong index, do nothing.
	private void moveFocus(int i) {
		Component[] comps = linesPanel.getComponents();
		if (comps != null && i >= 0 && i < comps.length) {
			Component comp = comps[i];
			if (comp instanceof OcrLine) {
				OcrLine line = (OcrLine) comp;
				focusLine = line.getLine();
				setLines();
				int y = Math.max(0, line.getLocation().y - 
						4 * getHeight() / 10);
				scrollTo(y);
			}
		}
	}

	// Get number of focussed line. Or -1 if none.
	private int focusNum() {
		int n = 0;
		for (Component comp : linesPanel.getComponents()) 
			if (comp instanceof OcrLineFocus) 
				return n;
			else
				n++;
		return -1;
	}

	// Send name to line with focus.
	public void sendNameToLine(String name) {
        for (Component comp : linesPanel.getComponents())
            if (comp instanceof OcrLineFocus) {
                OcrLineFocus line = (OcrLineFocus) comp;
                line.receiveName(name);
                return;
            }
    }

	/////////////////////////////////////////////////////
	// Lines.

	// Add line from first tabbed pane.
	// Update second tabbed pane.
	private void addLine(Line line) {
		page.lines.add(line);
	}

	// Remove line from first tabbed pane.
	// Move confirmed signs to orphans.
	// Also signs that are in OCR queue.
	// Update second tabbed pane.
	private void removeLine(Line line) {
		for (Blob glyph : line.glyphs) 
			removeGlyph(glyph);
		page.lines.remove(line);
		if (line == focusLine)
			focusLine = null;
	}

	protected void removeGlyph(Blob glyph) {
		if (!glyph.getName().equals("") || glyph.inQueue()) 
			page.orphanGlyphs.add(glyph);
		else
			glyph.remove();
	}

	// Remove all existing lines.
	private void removeAllLines() {
		Vector<Line> oldLines = new Vector<Line>(page.lines);
		for (Line line : oldLines)
			removeLine(line);
	}

	// Delete lines.
	public void deleteLines() {
		removeAllLines();
		imPanel.setShapes(new Vector<LabelledShape<Integer>>());
		imPanel.repaint();
		setLines();
	}

	// Find lines.
	public void findLines() {
		removeAllLines();
		Vector<Polygon> areas = getAnalyzer().findLines(im);
		for (Polygon poly : areas) {
			String dir = dir(poly);
			addLine(new Line(dir, poly, page.name));
		}
		imPanel.setShapes(shapes(page.lines));
		imPanel.repaint();
		setLines();
	}

	// Reprocess line, given shape.
	public void reprocess(Polygon poly) {
		Vector<Line> oldLines = new Vector<Line>(page.lines);
		Vector<Line> newLines = new Vector<Line>();
		for (Line line : oldLines) 
			if (equals(line.polygon, poly)) {
				removeLine(line);
				String dir = dir(poly);
				newLines.add(new Line(dir, poly, page.name));
			} else
				newLines.add(line);
		page.lines = newLines;
		setLines();
	}

	// Reorder lines according to order of areas.
	public void reorder(Vector<Polygon> polys) {
		Vector<Line> oldLines = new Vector<Line>(page.lines);
		Vector<Line> newLines = new Vector<Line>();
		for (Polygon poly : polys) 
			for (Line line : oldLines) 
				if (equals(line.polygon, poly)) {
					newLines.add(line);
					break;
				}
		page.lines = newLines;
		setLines();
	}

	// Strong equivalence of polygons.
	private boolean equals(Polygon p1, Polygon p2) {
		if (p1.npoints != p2.npoints)
			return false;
		for (int i = 0; i < p1.npoints; i++) 
			if (p1.xpoints[i] != p2.xpoints[i] ||
					p1.ypoints[i] != p2.ypoints[i])
				return false;
		return true;
	}

	// Direction is by default horizontal left-to-right.
	// Subclass may override.
	protected String dir(Polygon poly) {
		return "hlr";
	}

	// For debugging.
	private void print(Polygon poly) {
		System.out.println(">");
		for (int i = 0; i < poly.npoints; i++) {
			System.out.println(poly.xpoints[i]);
			System.out.println(poly.ypoints[i]);
		}
		System.out.println("<");
	}

	/////////////////////////////////////////////////////
	// Auxiliary GUI.

	// Horizontal glue.
	private Component vertGlue() {
		return Box.createVerticalGlue();
	}

	// By default nothing to dispose.
	public void dispose() {
	}

}
