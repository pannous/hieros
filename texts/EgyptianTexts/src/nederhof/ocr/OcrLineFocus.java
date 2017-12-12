package nederhof.ocr;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.alignment.generic.*;
import nederhof.ocr.admin.*;
import nederhof.ocr.images.*;
import nederhof.ocr.guessing.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Pane in which OCR for line can be manipulated.
public abstract class OcrLineFocus extends OcrLine {

	// GUI of part of page.
	protected ConnectedFocusPanel subPanel;
	// Choices after OCR.
	protected JPanel choicesPanel;
	protected SimpleHorScroller choicesScroller;
	// Formatted view.
	protected JPanel formatPanel;
	protected SimpleHorScroller formatScroller;

	protected OcrLineFocus(BinaryImage im, Line line, double scale) {
		super(im, line, scale);

		addGlyphsToPanel();

		choicesPanel = new JPanel();
		choicesPanel.setLayout(new BoxLayout(choicesPanel, 
					landscape ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
		choicesScroller = new SimpleHorScroller(choicesPanel);
		add(choicesScroller);
		addChoices();

		formatPanel = new JPanel();
		formatPanel.setLayout(new BoxLayout(formatPanel, 
					landscape ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
		formatScroller = new SimpleHorScroller(formatPanel);
		add(formatScroller);
		addFormat();

		setBorder(BorderFactory.createLineBorder(Color.BLUE, 4));
	}

	// Adjust layout.
	public void setLayout(boolean landscape) {
		choicesPanel.setLayout(new BoxLayout(choicesPanel, 
					landscape ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
		formatPanel.setLayout(new BoxLayout(formatPanel, 
					landscape ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
	}

	// Add panel with image part. Overrides superclass.
	protected void addSubPanel(double scale) {
		subPanel = new ConnectedFocusPanel(scale);
		subScroller = new SimpleHorScroller(subPanel);
	}

	//////////////////////////////////////////////////////
	// The subimage.

	// Image panel with focus connected to this.
	protected class ConnectedFocusPanel extends ClickPolyImagePanel<Blob> {
		public ConnectedFocusPanel(double scale) {
			super(subImage, scale, scale);
		}
		public void select(LabelledShape<Blob> selected) {
			selectBlob(selected.label);
			highlightChoice(selected.label);
			repaint();
		}
		public void uninclude(LabelledShape<Blob> shape) {
			unincludeBlob(shape.label);
			remove(shape);
		}
		public void cover(Rectangle rect) {
			editRect(rect);
		}
		protected Color color(LabelledShape<Blob> s) {
			if (s.label == null && !removal())
				return Color.GRAY;
			else if (s.label == null && removal())
				return Color.RED;
			else if (s.label == selected)
				return Color.GREEN;
			else
				return Color.BLUE;
		}
		protected Stroke stroke(LabelledShape<Blob> s) {
			if (s.label == selected)
				return new BasicStroke(5);
			else
				return super.stroke(s);
		}
	}

	// Add glyphs to image.
	private void addGlyphsToPanel() {
		Vector<LabelledShape<Blob>> shapes = new Vector<LabelledShape<Blob>>();
		for (Blob blob : line.aliveGlyphs()) {
			Polygon poly = toPoly(blob);
			shapes.add(new LabelledShape<Blob>(poly, blob));
		}
		subPanel.setShapes(shapes);
	}

	// Transform blob to polygon (square).
	private Polygon toPoly(Blob blob) {
		int x = blob.x() - xOffset;
		int y = blob.y() - yOffset;
		int width = blob.width();
		int height = blob.height();
		Polygon poly = new Polygon();
		poly.addPoint(x, y);
		poly.addPoint(x + width, y);
		poly.addPoint(x + width, y + height);
		poly.addPoint(x, y + height);
		return poly;
	}

	// Translate blob rectangle to position on subwindow.
	private int blobXPosition(Blob blob) {
		int x = blob.x() - xOffset;
		int halfWidth = blob.width() / 2;
		return (int) ((x + halfWidth) * subPanel.getXScale());
	}

	///////////////////////////////////////////////////////////////
	// The choices after OCR.

	protected HashMap<Blob,ChoiceBox> blobToBox = new HashMap<Blob,ChoiceBox>();

	// Give results of OCR.
	private void addChoices() {
		blobToBox.clear();
		choicesPanel.removeAll();
		for (Blob blob : line.aliveGlyphs()) {
			ChoiceBox box = createChoiceBox(blob);
			blobToBox.put(blob, box);
			choicesPanel.add(box);
		}
		choicesPanel.add(Box.createHorizontalGlue());
	}

	// Highlight button belonging to selected blob.
	private void highlightChoice(Blob blob) {
		ChoiceBox box = blobToBox.get(blob);
		if (box != null) {
			box.requestFocus();
			int x = box.getLocation().x;
			choicesScroller.getHorizontalScrollBar().setValue(
					Math.max(0, x - 4 * getWidth() / 10));
		}
	}

	// Subclass to implement.
	protected abstract ChoiceBox createChoiceBox(Blob blob);

	///////////////////////////////////////////////////////////////
	// Formatted text after Ocr.

	// Subclass to implement.
	protected abstract void addFormat();

	/////////////////////////////////////////////////////
	// Communication to caller.

	protected abstract void removeGlyph(Blob glyphs);

	/////////////////////////////////////////////////////
	// Actions.

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("left")) 
			moveLeft();
		else if (command.equals("right")) 
			moveRight();
		else if (command.equals("zoom")) 
			editRect();
		else if (command.equals("combine")) 
			combineParts();
		else if (command.equals("update")) {
			addGlyphsToPanel();
			addChoices();
			addFormat();
			revalidate();
			repaint();
		} else if (command.equals("format")) {
			doFormatNow(true);
			addFormat();
			revalidate();
			repaint();
		} else if (command.equals("hlr") ||
				command.equals("hrl") ||
				command.equals("vlr") ||
				command.equals("vrl")) {
			line.dir = command;
			setFocus(getLine());
		} else 
			super.actionPerformed(e);
	}

	// Set enabled.
	public void setEnabled(boolean allow) {
	}

	/////////////////////////////////////////////////////
	// Focus.

	// Blob selected by last click.
	protected Blob selected = null;

	// Select glyph.
	public void selectBlob(Blob blob) {
		selected = blob;
	}

	// Map blob to index.
	private int toIndex(Blob b) {
		for (int i = 0; i < line.glyphs.size(); i++) 
			if (line.glyphs.get(i) == b)
				return i;
		return -1;
	}

	// Move focus left/right.
	private void moveLeft() {
		int pos = toIndex(selected);
		if (line.glyphs.size() == 0)
			return;
		else if (pos < 0)
			pos = line.glyphs.size() - 1;
		else if (pos-1 < 0)
			return;
		else
			pos--;
		for (int i = pos; i >= 0; i--)
			if (!line.glyphs.get(i).isObsolete()) {
				selected = line.glyphs.get(i);
				subPanel.repaint();
				scrollToSelected();
				highlightChoice(selected);
				return;
			} 
	}
	private void moveRight() {
		int pos = toIndex(selected);
		if (line.glyphs.size() == 0)
			return;
		else if (pos < 0)
			pos = 0;
		else if (pos+1 >= line.glyphs.size())
			return;
		else
			pos++;
		for (int i = pos; i < line.glyphs.size(); i++) 
			if (!line.glyphs.get(i).isObsolete()) {
				selected = line.glyphs.get(i);
				subPanel.repaint();
				scrollToSelected();
				highlightChoice(selected);
				return;
			}
	}
	private void scrollToSelected() {
		if (selected == null)
			return;
		int x = blobXPosition(selected);
		subScroller.getHorizontalScrollBar().setValue(
				Math.max(0, x - 4 * getWidth() / 10));
	}

	// Put name in selected glyph.
	public void receiveName(String name) {
		if (selected != null) {
			ChoiceBox box = blobToBox.get(selected);
			if (box != null)
				box.receive(name);
		}
	}

	public void unincludeBlob(Blob blob) {
		removeGlyph(blob);
		line.glyphs.remove(blob);
		addChoices();
		if (!line.formatted.isEmpty())
			doFormatNow(true);
		revalidate();
		repaint();
	}

	////////////////////////////////
	// Editing of part of subimage.

	// Further scaling up of part of subimage.
	private final double partImageScaleup = 5.0;

	private void editRect(Rectangle rect) {
		BinaryImage partImage = subImage.subImage(rect);
		double partScale = scale * partImageScaleup;
		ConnectedEditor editor =
			new ConnectedEditor(partImage, rect.x, rect.y, partScale);
		allowEdits(false);
	}
	private void editRect() {
		editRect(new Rectangle(0, 0, subImage.width(), subImage.height()));
	}

	private class ConnectedEditor extends ImageEditor {
		public ConnectedEditor(BinaryImage im, int x, int y, double scale) {
			super(line, im, xOffset + x, yOffset + y, scale);
		}
		protected void save(Vector<Blob> oldGlyphs, Vector<Blob> glyphs) {
			replace(oldGlyphs, glyphs);
			if (getAutoOcr())
				doOcr(false);
			allowEdits(true);
			addGlyphsToPanel();
			addChoices();
			OcrLineFocus.this.revalidate();
			OcrLineFocus.this.repaint();
		}
		protected LayoutAnalyzer getAnalyzer() {
			return OcrLineFocus.this.getAnalyzer();
		}
	}

	// Replace glyphs in line.
	private void replace(Vector<Blob> oldGlyphs, Vector<Blob> glyphs) {
		for (Blob old : oldGlyphs) 
			if (!glyphs.contains(old)) {
				line.glyphs.remove(old);
				old.remove();
			}
		for (Blob b : glyphs) 
			if (!oldGlyphs.contains(b))
				line.glyphs.add(b);
		line.glyphs = getAnalyzer().order(line.glyphs, line.dir);
		if (glyphs.size() > 0)
			selected = glyphs.get(0);
	}

	// Combine parts of a glyph.
	private void combineParts() {
		GlyphCombiner combiner = createCombiner(line);
		getProcess().offer(combiner);
	}

}

