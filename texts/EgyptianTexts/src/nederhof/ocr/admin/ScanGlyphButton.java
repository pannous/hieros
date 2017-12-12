package nederhof.ocr.admin;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.xml.parsers.*;

import nederhof.ocr.images.*;
import nederhof.util.*;

// Glyph button for scanned-in sign.
public class ScanGlyphButton extends JPanel {

	// Glyph name.
	protected String name;
	// Prototype.
	protected Prototype proto;
	// The unscaled image.
	private BinaryImage im;
	// The scaled image.
	private BufferedImage buffer;
	// The scaling factor for last scaling.
	private double scale = 1;

	// Sizes of typed sign.
	private int typeWidth;
	private int typeHeight;

	// Has been removed?
	private boolean removed = false;

	// Constructor. Parameters are dimensions of typed sign.
	public ScanGlyphButton(String name, Prototype proto, int typeWidth, int typeHeight) {
		this.name = name;
		this.proto = proto;
		this.im = proto.im;
		this.typeWidth = typeWidth;
		this.typeHeight = typeHeight;
		setFocusable(false);
		addMouseListener(new DeleteListener());
	}

	// Remove. User can override to add actions.
	public void remove() {
		removed = true;
	}

	// Move. User should override.
	public void move() {
	}

	// The size determines margin.
	// Also restrict size based on typed sign times factor.
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (removed)
			return;
		Graphics2D g2 = (Graphics2D) g;
		int margin = (int) (getSize().height * 0.1);
		double typeFactor = 1.5;
		int w = Math.min(Math.max(1, getSize().width - 2 * margin), 
				(int) (typeWidth * typeFactor));
		int h = Math.min(Math.max(1, getSize().height - 2 * margin), 
				(int) (typeHeight * typeFactor));
		double reviseScale = Math.min(
				w * 1.0 / im.width(), h * 1.0 / im.height());
		if (reviseScale != scale || buffer == null) {
			scale = reviseScale;
			buffer = im.toBufferedImage(scale);
		}
		g2.drawImage(buffer, (getSize().width - buffer.getWidth()) / 2, 
				(getSize().height - buffer.getHeight()) / 2, null);
	}

	/////////////////////////////////////////
	// Mouse.

	private class DeleteListener extends MouseInputAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3 ||
					e.isControlDown())
				remove();
			else
				move();
		}
	}

}
