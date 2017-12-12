/***************************************************************************/
/*                                                                         */
/*  GlyphChooser.java                                                      */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Tabbed pane for choosing glyphs.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import nederhof.align.SimpleTextWindow;
import nederhof.res.*;
import nederhof.res.format.*;
import nederhof.util.*;

public abstract class GlyphChooser extends JFrame implements KeyListener, ActionListener {

	// All operations on RES and REScode assume the following context.
	private static final HieroRenderContext hieroContext =
		new HieroRenderContext(Settings.chooserHieroFontSize, true);

	// Parsing context.
	private static final ParsingContext parsingContext =
		new ParsingContext(hieroContext, true);

	// Tabbed pane, allowing programmatic traversal.
	// The tabs are for each glyph class, plus for tall, broad and narrow
	// signs.
	private JTabbedPane tabbed = new JTabbedPane(JTabbedPane.TOP);

	// Indexes of classes of glyphs within tabbed pane.
	private static final int LETTER_SIZE = 'Z' - 'A' + 2;
	private static final int AA_INDEX = LETTER_SIZE;
	private static final int NL_INDEX = 'N' - 'A';
	private static final int NU_INDEX = 'N' - 'A' + 1;
	private static final int TALL_INDEX = AA_INDEX + 1;
	private static final int BROAD_INDEX = AA_INDEX + 2;
	private static final int NARROW_INDEX = AA_INDEX + 3;

	// Auxiliary window with info on glyphs.
	protected SimpleTextWindow infoWindow = null;

	// Create window for choosing glyph.  This consists of tabbed pane indexed
	// by different subsets of the sign list.
	public GlyphChooser() {
		setTitle("Choose glyph");
		setJMenuBar(new Menu(this));
		setFocusable(false);
		tabbed.setFocusable(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
		content.add(tabbed);
		tabbed.addKeyListener(this);

		for (char c = 'A'; c <= 'I'; c++) {
			JComponent cl = new GlyphCategory(c, 
					SignLists.categoryDefinitions[c - 'A']);
			tabbed.addTab("" + c, new TabbedScroller(cl));
		}
		for (char c = 'K'; c <= 'N'; c++) {
			JComponent cl = new GlyphCategory(c, 
					SignLists.categoryDefinitions[c - 'A' - 1]);
			tabbed.addTab("" + c, new TabbedScroller(cl));
		}
		JComponent clNL = new GlyphCategory('l', 
				SignLists.categoryDefinitions['N' - 'A']);
		tabbed.addTab("<html>N<u>L</u></html>", new TabbedScroller(clNL));
		tabbed.setMnemonicAt(NL_INDEX, KeyEvent.VK_L);
		JComponent clNU = new GlyphCategory('u', 
				SignLists.categoryDefinitions['N' - 'A' + 1]);
		tabbed.addTab("<html>N<u>U</u></html>", new TabbedScroller(clNU));
		tabbed.setMnemonicAt(NU_INDEX, KeyEvent.VK_U);
		for (char c = 'O'; c <= 'Z'; c++) {
			JComponent cl = new GlyphCategory(c, 
					SignLists.categoryDefinitions[c - 'A' - 1 + 2]);
			tabbed.addTab("" + c, new TabbedScroller(cl));
		}
		JComponent clAA = new GlyphCategory('J', 
				SignLists.categoryDefinitions[AA_INDEX]);
		tabbed.addTab("Aa", new TabbedScroller(clAA));

		JComponent clTall = new SpecialClass("Tall narrow signs", 
				SignLists.tallSigns);
		tabbed.addTab("<html><u>T</u>all</html>", new TabbedScroller(clTall));
		tabbed.setMnemonicAt(TALL_INDEX, KeyEvent.VK_T);
		JComponent clBroad = new SpecialClass("Low broad signs", 
				SignLists.broadSigns);
		tabbed.addTab("<html><u>B</u>road</html>", new TabbedScroller(clBroad));
		tabbed.setMnemonicAt(BROAD_INDEX, KeyEvent.VK_B);
		JComponent clNarrow = new SpecialClass("Low narrow signs", 
				SignLists.narrowSigns);
		tabbed.addTab("<html>n<u>A</u>rrow</html>", new TabbedScroller(clNarrow));
		tabbed.setMnemonicAt(NARROW_INDEX, KeyEvent.VK_A);

		addWindowListener(new Listener());
		pack();
		setVisible(true);
		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				tabbed.requestFocus();
				}
				});
	}

	// Simple scroll pane within tabbed pane.
	private static class TabbedScroller extends JScrollPane {
		public TabbedScroller(JComponent pane) {
			super(pane, 
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			setFocusable(false);
			getVerticalScrollBar().setUnitIncrement(10);
		}
	}

	// Panel for one category of glyphs.
	private class GlyphCategory extends JPanel {
		public GlyphCategory(char c, String def) {
			setLayout(new BorderLayout());
			setFocusable(false);
			String cat;
			if (c == 'J')
				cat = "Aa";
			else if (c == 'l')
				cat = "NL";
			else if (c == 'u')
				cat = "NU";
			else
				cat = "" + c;
			JLabel label = new JLabel("<html><h2>&nbsp;" + cat + ". " + def + 
					"</h2></html>");
			add(label, BorderLayout.NORTH);

			JPanel list = new JPanel();
			Vector names = hieroContext.getCategory(c);
			int rows = (int) Math.ceil(names.size() * 1.0 / Settings.chooserCols);
			list.setLayout(new GridLayout(rows, 1));
			for (int i = 0; i < names.size(); i++) {
				String name = (String) names.get(i);
				list.add(new GlyphButton(name));
			}

			JPanel wrapper = new JPanel();
			wrapper.add(list);
			add(wrapper, BorderLayout.WEST);
		}
	} 

	// Panel for a special class of glyphs, arranged by shape.
	private class SpecialClass extends JPanel {
		public SpecialClass(String def, String[] names) {
			setLayout(new BorderLayout());
			JLabel label = new JLabel("<html><h2>&nbsp;" + def + "</h2></html>");
			add(label, BorderLayout.NORTH);

			JPanel list = new JPanel();
			int rows = (int) Math.ceil(names.length * 1.0 / Settings.chooserCols);
			list.setLayout(new GridLayout(rows, 1));
			for (int i = 0; i < names.length; i++) {
				list.add(new GlyphButton(names[i]));
			}

			JPanel wrapper = new JPanel();
			wrapper.add(list);
			add(wrapper, BorderLayout.WEST);
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// Buttons for glyphs.

	// Maximum dimension of glyphs and glyph names in buttons.
	private int maxWidthGlyph = 0;
	private int maxHeightGlyph = 0;
	private int maxWidthName = 0;
	private int maxHeightName = 0;

	// Button for one glyph.
	// Only a short string is used as member variable, to avoid
	// high memory consumption for huge sign lists.
	private class GlyphButton extends JButton implements ActionListener {

		// Name of glyph, also used as a small piece of RES.
		private String text; 

		public GlyphButton(String text) {
			this.text = text;
			setBackground(Color.WHITE);
			setFocusable(false);
			FormatFragment frag = getFormat();
			maxWidthGlyph = Math.max(maxWidthGlyph, frag.width());
			maxHeightGlyph = Math.max(maxHeightGlyph, frag.height());
			maxWidthName = Math.max(maxWidthName, labelWidth(text));
			maxHeightName = Math.max(maxHeightName, labelHeight(text));
			addActionListener(this);
			addMouseListener(new MouseTraverser());
		}

		// Paint glyph and name of glyph.
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			FormatFragment frag = getFormat();
			frag.write(g2, (int) (maxWidthGlyph * margin),
					(int) (maxHeightGlyph * margin));
			g2.setFont(Settings.chooserLabelFont);
			g2.setColor(Color.BLACK);
			g2.drawString(text, glyphPartWidth(),
					glyphPartHeight() * 0.5f + maxHeightName / 2.0f);
		}

		// Extra pixels to separate name from right border.
		public Dimension getPreferredSize() {
			return new Dimension(glyphPartWidth() + maxWidthName + 3, 
					glyphPartHeight());
		}

		// The dimension of the button covered by glyph.
		// An extra factor provides a margin.
		private static final float margin = 0.125f;
		private int glyphPartWidth() {
			return Math.round(maxWidthGlyph * (1 + 2 * margin));
		}
		private int glyphPartHeight() {
			return Math.round(maxHeightGlyph * (1 + 2 * margin));
		}

		// Send glyph off to application.
		public void actionPerformed(ActionEvent e) {
			submitGlyph(text);
		}

		// For stand-alone application, this is ignored.
		// Otherwise, information on glyph is present upon
		// traversing glyph.
		private class MouseTraverser extends MouseAdapter {
			public void mouseEntered(MouseEvent e) {
				if (infoWindow != null) 
					lookupSignInfo(text);
			}
			public void mouseExited(MouseEvent e) {
				if (infoWindow != null)
					infoWindow.clearText();
			}
		}

		// Get formatted hieroglyph.
		private FormatFragment getFormat() {
			ResFragment frag = ResFragment.parse(text, parsingContext);
			return new FormatFragment(frag, hieroContext);
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// Determine size of text in dummy graphics.

	// We have to get a graphics from somewhere, so we create a dummy image.
	private static BufferedImage dummyImage = 
		new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

	private static Graphics dummyGraphics = 
		dummyImage.createGraphics();

	private static FontMetrics dummyMetrics = 
		dummyGraphics.getFontMetrics(Settings.chooserLabelFont);

	// Width of string in pixels.
	private static int labelWidth(String text) {
		return dummyMetrics.stringWidth(text);
	}
	// Experience has shown that ascent gives better results than height.
	private static int labelHeight(String text) {
		return dummyMetrics.getAscent();
	}

	//////////////////////////////////////////////////////////////////////////////
	// Handling events in main window.

	// No arrow keys, and no alt or cntr.
	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		final int shiftMask = InputEvent.SHIFT_DOWN_MASK;
		if (e.getModifiersEx() != 0 &&
				(e.getModifiersEx() & shiftMask) != shiftMask)
			; // ignore
		else if (c == '\u0008') // backspace.
			backChosen();
		else if ((int) c == 10) { // 'enter'
			if (chosen.matches("([A-I]|[K-Z]|Aa|NL|NU)([0-9]+)([a-z]?)"))
				submitGlyph(chosen);
		} else
			appendChosen(c);
	}

	public void keyPressed(KeyEvent e) {
		// Ignored.
	}
	public void keyReleased(KeyEvent e) {
		// Ignored.
	}

	// Actions from menu.
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("quit")) 
			goAway();
		else if (e.getActionCommand().equals("info")) {
			makeOpenInfoWindow();
			infoWindow.setVisible(true);
		}
	}

	// Ensure info window is open and visible.
	protected void makeOpenInfoWindow() {
		if (infoWindow == null) {
			infoWindow = new SimpleTextWindow(Settings.infoWidthInit,
					Settings.infoHeightInit);
			Point point = getLocationOnScreen();
			Dimension size = getSize();
			infoWindow.setLocation(point.x, point.y + size.height);
			loadSignInfo();
		}
	}

	// Listen if window to be closed. Merely make invisible.
	private class Listener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			goAway();
		}
	}

	// Send glyph to application. If there is no application,
	// show info on glyph.
	protected void submitGlyph(String name) {
		if (Settings.chooserNonExistentChoose ||
				hieroContext.exists(name)) {
			receive(name);
			goAway();
		}
	}

	// Make invisible. 
	// (Overridden for stand-alone application to exit, or
	// to detect that nothing was selected.)
	public void goAway() {
		receiveNothing();
		resetChosen();
		setVisible(false);
		if (infoWindow != null) 
			infoWindow.setVisible(false);
	}

	// If to be disposed, also dispose auxiliary windows.
	public void dispose() {
		if (infoWindow != null)
			infoWindow.dispose();
		super.dispose();
	}

	//////////////////////////////////////////////////////////////////////////////
	// Menu at top of window.

	// Component that shows name of glyph so far.
	private ChosenGlyph chosenDisplay = new ChosenGlyph();

	// Class of component showing glyph name chosen so far.
	// For a stand-alone application, no name is shown.
	private class ChosenGlyph extends JButton {

		// Size of borders.
		private static final int BSIZE = 5;

		public ChosenGlyph() {
			setBorder(new EmptyBorder(BSIZE,BSIZE,BSIZE,BSIZE));
			setBackground(Color.LIGHT_GRAY);
			setFocusable(false);
			clearText();
		}

		// Clear name of glyph.
		public void clearText() {
			setText("");
			setMaximumSize(getPreferredSize());
		}

		// Show correct name of glyph.
		public void setCorrectText(String chosen) {
			setText("<html>Chosen: <font color=\"blue\">" + chosen +
					"</font></html>");
			setMaximumSize(getPreferredSize());
		}

		// Show incorrect name of glyph.
		public void setWrongText(String chosen) {
			setText("<html>Chosen: <font color=\"red\">" + chosen +
					"</font></html>");
			setMaximumSize(getPreferredSize());
		}
	}

	// Menu containing quit button, plus name of glyph so far.
	private class Menu extends JMenuBar {

		// Distance between buttons.
		private static final int STRUT_SIZE = 10;

		public Menu(ActionListener lis) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(Color.LIGHT_GRAY);
			add(Box.createHorizontalStrut(STRUT_SIZE));
			add(new ClickButton(lis, "<u>Q</u>uit", "quit", KeyEvent.VK_Q));
			add(Box.createHorizontalStrut(STRUT_SIZE));
			add(new ClickButton(lis, "<u>I</u>nfo", "info", KeyEvent.VK_I));
			add(Box.createHorizontalStrut(STRUT_SIZE));
			add(Box.createHorizontalStrut(STRUT_SIZE));
			add(chosenDisplay);
			add(Box.createHorizontalGlue());
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// Manipulation of glyph name so far.

	// Name of glyph so far.
	private String chosen = "";

	// Make chosen name consist of one character.
	private void setChosen(char c) {
		chosen = "";
		appendChosen(c);
	}

	// Make no chosen name.
	private void resetChosen() {
		chosen = "";
		chosenDisplay.clearText();
	}

	// Append character behind chosen name.
	// If chosen name is empty, lower-case is taken as upper-case.
	// For original upper-case, we jump to category.
	private void appendChosen(char c) {
		if (c == 'J') {
			chosen = "Aa";
			chosenDisplay.setWrongText(chosen);
			tabbed.setSelectedIndex(AA_INDEX);
		} else if ('A' <= c && c <= 'Z') {
			if (chosen.equals("N") && c == 'L') {
				chosen = "NL";
				tabbed.setSelectedIndex(NL_INDEX);
			} else if (chosen.equals("N") && c == 'U') {
				chosen = "NU";
				tabbed.setSelectedIndex(NU_INDEX);
			} else {
				if (c < 'J')
					tabbed.setSelectedIndex(c - 'A');
				else if (c <= 'N')
					tabbed.setSelectedIndex(c - 'A' - 1);
				else
					tabbed.setSelectedIndex(c - 'A' - 1 + 2);
				chosen = "" + c;
			}
			chosenDisplay.setWrongText(chosen);
		} else if (c == 'j' && chosen.equals("")) {
			chosen = "Aa";
			chosenDisplay.setWrongText(chosen);
		} else if (c == 'l' && chosen.equals("N")) {
			chosen = "NL";
			chosenDisplay.setWrongText(chosen);
		} else if (c == 'u' && chosen.equals("N")) {
			chosen = "NU";
			chosenDisplay.setWrongText(chosen);
		} else if ('a' <= c && c <= 'z' && chosen.equals("")) {
			chosen = "" + Character.toUpperCase(c);
			chosenDisplay.setWrongText(chosen);
		} else if (c == '0' && 
				chosen.matches("([A-I]|[K-Z]|Aa|NL|NU)([1-9][0-9]?)") ||
				'1' <= c && c <= '9' && 
				chosen.matches("([A-I]|[K-Z]|Aa|NL|NU)([1-9][0-9]?)?") ||
				'a' <= c && c <= 'z' &&
				chosen.matches("([A-I]|[K-Z]|Aa|NL|NU)([1-9]([0-9][0-9]?)?)")) {
			chosen += c;
			if (hieroContext.exists(chosen))
				chosenDisplay.setCorrectText(chosen);
			else
				chosenDisplay.setWrongText(chosen);
		}
	}

	// Remove one character from name.
	private void backChosen() {
		if (chosen.equals("Aa") || chosen.length() <= 1)
			resetChosen();
		else {
			chosen = chosen.substring(0, chosen.length() - 1);
			if (hieroContext.exists(chosen))
				chosenDisplay.setCorrectText(chosen);
			else
				chosenDisplay.setWrongText(chosen);
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// Loading glyph information.

	// XML parser.
	private static DocumentBuilder parser = null;

	static {
		constructParser();
	}

	// Construct XML parser. Once and for all.
	private static void constructParser() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setCoalescing(false);
			factory.setExpandEntityReferences(false);
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(false);
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			parser = null;
		}
	}

	// Structure containing sign information.
	private Document signInfo = null;

	// Mapping from names to elements in structure.
	private Map signFinder = new HashMap();

	// Parse sign information.
	private void loadSignInfo() {
		try {
			InputStream in = FileAux.addressToStream(Settings.infoFile);
			signInfo = parser.parse(in);
			NodeList signs = signInfo.getElementsByTagName("sign");
			for (int i = 0; i < signs.getLength(); i++) {
				Element sign = (Element) signs.item(i);
				String name = sign.getAttribute("name");
				signFinder.put(name, sign);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Could not read: " + Settings.infoFile);
			System.err.println(e.getMessage());
		}
	}

	// Send sign information to window.
	protected void lookupSignInfo(String sign) {
		Element elem = (Element) signFinder.get(sign);
		Element descr = null;
		Element info = null;
		if (elem != null) {
			NodeList descrs = elem.getElementsByTagName("descr");
			NodeList infos = elem.getElementsByTagName("info");
			if (descrs.getLength() > 0) 
				descr = (Element) descrs.item(0);
			if (infos.getLength() > 0) 
				info = (Element) infos.item(0);
		}
		infoWindow.setText(sign, descr, info);
	}

	/////////////////////////////////////////////////////////////
	// Interface to application. To be overridden there.

	protected abstract void receive(String name);

	protected void receiveNothing() {
	}

	///////////////////////////////////////////////////////////////

	// Stand-alone application.
	public static void main(String[] args) {
		new GlyphChooser() {
			protected void receive(String name) {
				// ignore
			}
			public void goAway() {
				System.exit(0);
			}
			// Show info on glyph.
			protected void submitGlyph(String name) {
				if (Settings.chooserNonExistentChoose ||
						hieroContext.exists(name)) {
					if (infoWindow == null) {
						makeOpenInfoWindow();
					}
					infoWindow.setVisible(true);
					lookupSignInfo(name);
				}
			}
		};
	}

}
