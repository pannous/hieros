package nederhof.ocr.hiero;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.ocr.*;
import nederhof.res.*;
import nederhof.res.format.*;

public abstract class HieroPreview extends JButton implements ActionListener, PreviewElem {

	// Formatted RES.
	private FormatFragment format;

	// The preview object.
	private ResFormat preview; 

	// Size of hieroglyphic and margin.
	private int width = 0;
	private int height = 0;
	private final int margin = 5;

	// Padding for right-to-left text is on left.
	private int leftExtra() {
		return format.effectIsLR() ? 0 : getWidth() - width - 2 * margin;
	}

	// Construct preview.
	public HieroPreview(ResFormat preview, FormatFragment format) {
		this.preview = preview;
		this.format = format;
		setBackground(Color.WHITE);
		setActionCommand("edit");
		addActionListener(this);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 ||
					e.isControlDown()) 
				rightClicked();
			}
		});
		width = format.width();
		height = format.height();
	}

	// Gardiner names in this.
	public Vector<String> names() {
		return format.glyphNames();
	}

	// Scroll to i-th named glyph.
	public void scrollToFocus(int i) {
		Rectangle wider = new Rectangle(visibleRect(i));
		final int WIDENING = 60;
		if (format.effectIsH()) {
			wider.x = Math.max(0, wider.x - WIDENING);
			wider.width += WIDENING * 2;
		} else {
			wider.y = Math.max(0, wider.y - WIDENING);
			wider.height += WIDENING * 2;
		}
		scrollRectToVisible(wider);
	}

	// Make group visible group rectangle.
	// If rectangle is empty (operator with negative size), then
	// take end of previous group.
	private Rectangle visibleRect(int i) {
		if (format.nGroups() < 1 || i < 0 || i >= format.nGlyphs()) {
			if (format.effectIsH()) {
				if (format.effectIsLR())
					return new Rectangle(margin, margin, 1, height);
				else
					return new Rectangle(getWidth() - margin - 1, margin,
							1, height);
			} else
				return new Rectangle(margin, margin, width, 1);
		} else {
			Vector rects = format.glyphRectangles();
			Rectangle rect = (Rectangle) rects.get(i);
			if (format.effectIsLR())
				return new Rectangle(rect.x + margin,
						rect.y + margin,
						rect.width, rect.height);
			else
				return new Rectangle(getWidth() - (rect.x + rect.width + margin),
						rect.y + margin,
						rect.width, rect.height);
		}
	}

	// Dimensions, to enclose hieroglyphic.
	public Dimension getMinimumSize() {
		return new Dimension(width + 2 * margin, height + 2 * margin);
	}
	public Dimension getMaximumSize() {
		return getMinimumSize();
	}
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	// Paint in panel.
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D graphics = (Graphics2D) g;
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		format.write(graphics, margin + leftExtra(), margin);
	}

	//////////////////////////////////////////////////
	// Communication to caller.

	public void actionPerformed(ActionEvent e) {
		selected(preview);
	}

	private void rightClicked() {
		rightSelected(preview);
	}

	// Notify user that this is clicked.
	public abstract void selected(ResFormat preview);
	public abstract void rightSelected(ResFormat preview);

}
