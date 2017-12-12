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
import nederhof.ocr.guessing.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Panel for one line (not focussed).
public abstract class OcrLine extends JPanel implements ActionListener {

	// The line.
	protected Line line;
	// Image itself. 
	protected BinaryImage subImage;
	// Scaling of subImage.
	protected double scale;
	// The offset of subImage in image.
	protected int xOffset;
	protected int yOffset;
	// GUI of page.
	private ConnectedPanel subPanel;
	protected SimpleHorScroller subScroller;

	// Horizontal or vertical?
	protected boolean landscape = true;

	public OcrLine(BinaryImage im, Line line, double scale) {
		this.line = line;
		this.scale = scale;
		landscape = line.dir.equals("hlr") || line.dir.equals("hrl");
		subImage = im.subImage(line.polygon);
		Point offset = im.subImageOffset(line.polygon);
		xOffset = offset.x;
		yOffset = offset.y;
		if (line.glyphs.size() == 0 && getAutoSegment()) 
			findComponents();
		if (getAutoOcr())
			doOcr(true);
		if (getAutoFormat())
			doFormatLater();

		setLayout(new BoxLayout(this, landscape ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
		addSubPanel(scale);
		add(subScroller);
	}

	// Get line belonging to this panel.
	public Line getLine() {
		return line;
	}

	// Add panel with image part.
	protected void addSubPanel(double scale) {
		subPanel = new ConnectedPanel(scale);
		subScroller = new SimpleHorScroller(subPanel);
	}

	// Image panel without focus connected to this.
	private class ConnectedPanel extends ImagePanel<Blob> {
		public ConnectedPanel(double scale) {
			super(subImage, scale, scale);
		}
		public void mousePressed(MouseEvent e) {
			setFocus(getLine());
		}
	}

	// Find strongly connected components in image.
	public void findComponents() {
		Vector<Blob> glyphs = new Vector<Blob>();
		Vector<Blob> newGlyphs = getAnalyzer().findBlobs(subImage,
				xOffset, yOffset);
		for (Blob glyph : newGlyphs) {
			glyph = replaceByOrphan(glyph);
			glyphs.add(glyph);
		}
		int unit = getAnalyzer().predictUnitSize(glyphs);
		glyphs = getAnalyzer().maybeGlyphs(glyphs, unit);
		glyphs = getAnalyzer().order(glyphs, line.dir);
		line.glyphs.addAll(glyphs);
	}

	// If is orphan of page, then take it.
	protected Blob replaceByOrphan(Blob glyph) {
		Blob same = null;
		for (Blob orphan : orphans()) 
			if (glyph.equalTo(orphan))
				same = orphan;
		if (same != null) {
			orphans().remove(same);
			return same;
		} else
			return glyph;
	}

	// Do OCR for components.
	// Do not compute unit more than once.
	protected void doOcr(boolean doCombine) {
		int unit = -1; // dummy value
		boolean newGlyph = false;
		for (Blob glyph : line.aliveGlyphs()) 
			if (glyph.getName().equals("") &&
					glyph.getGuessed() == null &&
					!glyph.inQueue()) {
				if (unit < 0) 
					unit = getAnalyzer().predictUnitSize(line.aliveGlyphs());
				glyph.setUnitSize(unit);
				glyph.setPage(line.page());
				getProcess().offer(glyph);
				newGlyph = true;
			}
		if (newGlyph && doCombine)
			getProcess().offer(createCombiner(line));
	}

	// Do formatting in thread.
	private void doFormatLater() {
		getProcess().offer(line);
	}

	// Do formatting for line.
	// If been formatted before, do not do again, unless forced.
	protected void doFormatNow(boolean force) {
		if (force || line.formatted.isEmpty()) {
			setWait(true);
			line.formatted = getFormatter().toFormats(line.aliveGlyphs(), line.dir);
			setWait(false);
		}
	}

	/////////////////////////////////////////////////////
	// Communication to caller.

	protected abstract OcrProcess getProcess();
	protected abstract LayoutAnalyzer getAnalyzer();
	protected abstract BlobFormatter getFormatter();
	protected abstract GlyphCombiner createCombiner(Line line);
	protected abstract boolean getAutoSegment();
	protected abstract boolean getAutoOcr();
	protected abstract boolean getAutoFormat();
	protected abstract void allowEdits(boolean allow); 
	protected abstract void setWait(boolean wait); 
	protected abstract void setFocus(Line line);
	protected abstract Vector<Blob> orphans();

	/////////////////////////////////////////////////////
	// Actions.

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("format all")) 
			doFormatNow(false);
	}

	// Set enabled.
	public void setEnabled(boolean allow) {
	}

	/////////////////////////////////////////////////////
	// Auxiliary GUI.

	// Simple scroll panes.
	protected static class SimpleHorScroller extends JScrollPane {
		public SimpleHorScroller(JComponent pane) {
			super(pane,
					JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			setFocusable(false);
			getHorizontalScrollBar().setUnitIncrement(10);
		}
	}

	public Dimension getMaximumSize() {
		Dimension pref = super.getPreferredSize();
		Dimension max = super.getMaximumSize();
		return new Dimension(max.width, pref.height);
	}

	// Horizontal glue.
	protected Component horGlue() {
		return Box.createHorizontalGlue();
	}

}

