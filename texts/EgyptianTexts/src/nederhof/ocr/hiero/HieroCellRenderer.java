package nederhof.ocr.hiero;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.ocr.admin.*;
import nederhof.ocr.hiero.admin.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.format.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Cell including hieroglyphic.
public class HieroCellRenderer implements ListCellRenderer {

	// Contexts for hieroglyphic.
	private static HieroRenderContext hieroContext = null;
	private static ParsingContext parsingContext = null;
	// We have to get a graphics from somewhere, so we create a dummy image.
	private static BufferedImage dummyImage = null;
	private static Graphics dummyGraphics = null;
	private static FontMetrics dummyMetrics = null;

	// Maps name to cell. Cached.
	private TreeMap<String,HieroCell> cells = new TreeMap<String,HieroCell>();

	// Renderer for none, menu or extra.
	private DefaultListCellRenderer renderer = new DefaultListCellRenderer();

	// The panel for one hieroglyph.
	private class HieroCell extends JPanel {

		// Name and dimensions.
		private String name;
		private String shortName;
		// Formatted RES.
		private FormatFragment format;

		// Dimensions.
		private int margin = 3;
		private int nameWidth;
		private int nameHeight;
		private int glyphWidth;
		private int glyphHeight;

		// Construct.
		public HieroCell(String name) {
			this.name = name;
			shortName = NonHiero.modLongToShort(name);
			setBackground(Color.WHITE);
			getContexts();
			nameWidth = dummyMetrics.stringWidth(shortName);
			nameHeight = dummyMetrics.getAscent();
			ResFragment frag = ResFragment.parse(name, parsingContext);
			format = new FormatFragment(frag, hieroContext);
			glyphWidth = format.width();
			glyphHeight = format.height();
		}

		// Make contexts (only once).
		private void getContexts() {
			if (dummyMetrics == null) {
				hieroContext = new HieroRenderContext(HieroSettings.comboHieroFontSize, true);
				parsingContext = new ParsingContext(hieroContext, true);
				dummyImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
				dummyGraphics = dummyImage.createGraphics();
				dummyMetrics = dummyGraphics.getFontMetrics(HieroSettings.labelFont);
			}
		}

		// Get dimensions of hieroglyphic.
		public int getGlyphWidth() {
			return glyphWidth;
		}
		public int getGlyphHeight() {
			return glyphHeight;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setFont(HieroSettings.labelFont);
			g2.setColor(Color.BLACK);
			g2.drawString(shortName, 
					(int) (getSize().width * 0.5f - nameWidth / 2.0f),
					extraVertMargin() + margin + nameHeight);
			format.write(g2, 
					(int) (getSize().width * 0.5f - glyphWidth / 2.0f),
					extraVertMargin() + margin * 2 + nameHeight);
		}

		// Extra margin due to excess size.
		private int extraVertMargin() {
			return (int) ((getSize().height - getMinimumSize().height) / 2.0f);
		}

		// Preferred and minimum size.
		public Dimension getMinimumSize() {
			return new Dimension(
					margin * 2 + Math.max(nameWidth, glyphWidth),
					margin * 3 + nameHeight + glyphHeight);
		}
		public Dimension getPreferredSize() {
			return new Dimension(
					margin * 2 + Math.max(nameWidth, glyphWidth),
					margin * 3 + nameHeight + glyphHeight);
		}
	}

	// Get (cached) components for cells.
	public Component getListCellRendererComponent(JList list, Object obj,
			int row, boolean sel, boolean hasFocus) {
		String name = (String) obj;
		if (name.equals("?") || name.equals("menu") || name.equals("extra"))
			return renderer.getListCellRendererComponent(list, name,
					row, sel, hasFocus);
		else if (!cells.containsKey(name)) {
			if (NonHiero.isExtra(name)) {
				return renderer.getListCellRendererComponent(list, name, 
						row, sel, hasFocus);
			} else
				cells.put(name, new HieroCell(name));
		}
		return cells.get(name);
	}

}
