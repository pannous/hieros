package nederhof.ocr;

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

// Button containing scanned glyph.
public abstract class BlobButton extends JButton implements ActionListener {

	// The blob.
	private Blob blob;
	// The scaled image.
	private BufferedImage buffer;

	// The width and height. Margin.
	private int size = 50;
	private int margin = 4;

	// Is it selected?
	private boolean selected = false;

	// Constructor.
	public BlobButton(Blob blob) {
		this.blob = blob;
		this.size = size;
		setBackground(Color.WHITE);
		setActionCommand("edit");
		addActionListener(this);
		setSelected(false);
		makeImage();
	}

	// Which blob.
	public Blob getBlob() {
		return blob;
	}

	// Is it selected.
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean sel) {
		selected = sel;
		if (sel)
			setBorder(new LineBorder(Color.BLUE, 3));
		else
			setBorder(new LineBorder(Color.BLACK, 1));
	}
	public void toggleSelected() {
		setSelected(!isSelected());
	}

	// Convert blob to image.
	public void makeImage() {
		BinaryImage im = blob.imSafe();
		double scale = Math.min(
				size * 1.0 / im.width(), size * 1.0 / im.height());
		buffer = im.toBufferedImage(scale);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (buffer != null)
			g.drawImage(buffer, (getSize().width - buffer.getWidth()) / 2,
					(getSize().height - buffer.getHeight()) / 2, null);
	}

	// Dimensions, to enclose glyphs..
	public Dimension getMinimumSize() {
		return new Dimension(size + 2 * margin, size + 2 * margin);
	}
	public Dimension getMaximumSize() {
		return getMinimumSize();
	}
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	//////////////////////////////////////// 
	// Communication to caller.

	public void actionPerformed(ActionEvent e) {
		toggleSelected();
		clicked(blob, selected);
	}

	// Notify user that this is clicked.
	public abstract void clicked(Blob blob, boolean selected);

}
