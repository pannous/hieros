/***************************************************************************/
/*                                                                         */
/*  HieroMatcher.java                                                      */
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

// Matching hieroglyphic and transliteration.
// Used in development and testing.

package nederhof.alignment.egyptian.develop;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.alignment.egyptian.*;
import nederhof.align.*;
import nederhof.fonts.*;
import nederhof.res.*;
import nederhof.hieroutil.*;
import nederhof.util.*;

public class HieroMatcher extends JFrame implements
	KeyListener, ActionListener {

    // Mapping signs to meanings.
    private SignMeanings meanings = new SignMeanings("hannigzeichenliste.txt");

    // To contain string of hieroglyphic and transliteration.
    private Vector hiStrings = new Vector();
    private Vector alStrings = new Vector();
    // Mapping from lines to sets of glyph positions where word starts.
    private TreeMap wordStarts = new TreeMap();
    // Same as above, but computed automatically.
    private TreeMap wordStartsAuto = new TreeMap();

    // Result of matching hieroglyphic and transliteration.
    private HieroTransMatching match = null;

    // Beam search size.
    // private int beam = 40;
    private int beam = 100;

    // Default window sizes.
    private int windowWidth = 700;
    private int windowHeight = 800;

    // Margins left and right of the actual text.
    private int leftMargin = 15;
    private int rightMargin = 20;
    // Paragraph separation.
    private int parSep = 30;
    // Line separation within paragraph.
    private int lineSep = 30;
    // Margin round glyph that has focus.
    private int focusMargin = 3;

    // Size for hieroglyphic.
    private static int defaultHieroFontSize = 34;
    private static int minimumHieroFontSize = 20;
    private int hieroFontSize = defaultHieroFontSize;

    // Renderer for hieroglyphic.
    private HieroRenderContext hieroContext;

    // Transliteration font.
    private String egyptMapFile = "data/fonts/jungemapping.txt";
    private TrMap egyptMap = new TrMap(egyptMapFile);
    private String egyptFontName = "data/fonts/Umsch_s.ttf";
    private Font egyptFontProto = FontUtil.getFontFrom(egyptFontName);
    private static double hieroTransRatio = 2.0;
    private int egyptFontSize = (int) Math.round(hieroFontSize / hieroTransRatio);
    private Font egyptFont = egyptFontProto.deriveFont((float) egyptFontSize);
    private FontMetrics metrics = getFontMetrics(egyptFont);
    private int alHeight = metrics.getHeight();
    private int alDescent = metrics.getDescent();
    // Number of lines of transliteration below one another.
    private int nAlLines = 2;

    // The scroll pane, and the pane within that containing actual text.
    private JScrollPane scroll;
    private TextView text;

    // Trace of matching process.
    private MatchTrace trace = new MatchTrace();

    // Replay of matching process.
    private HTMLWindow replay = null;

    public HieroMatcher() {
	setTitle("Matching");
	setJMenuBar(getMenu());
	setSize(windowWidth, windowHeight);
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
	text = new TextView();
	scroll = new JScrollPane(text,
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.getVerticalScrollBar().setUnitIncrement(10);
	content.add(scroll);
	addKeyListener(this);
	addWindowListener(new Listener());
	setFocusable(true);

	makeFonts();
	setUpContent();
	setVisible(true);
	ensureReformatted();
    }

    // Prepare hieroglyphic and transliteration fonts.
    private void makeFonts() {
	hieroContext = new HieroRenderContext(hieroFontSize, true);
	hieroContext.setSuppressErrors(true);

	egyptFontSize = (int) Math.round(hieroFontSize / hieroTransRatio);
	egyptFont = egyptFontProto.deriveFont((float) egyptFontSize);
	metrics = getFontMetrics(egyptFont);
	alHeight = metrics.getHeight();
	alDescent = metrics.getDescent();
    }

    // Normally componentResized() is called in the event thread, which calls reformat,
    // but it is not clear to me whether this is always the case.
    // To ensure the text is formatted at least once, the resized event is
    // called artificially.
    private void ensureReformatted() {
        if (!reformattedOnce)
            dispatchEvent(
                    new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));
    }

    // Listen if window to be closed.
    private class Listener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
	    // trace.dispose();
	    if (replay != null)
		replay.dispose();
            dispose();
        }
    }

    /////////////////////////////////////////////////////////////////

    // Buttons at top.
    private JMenuBar getMenu() {
        final int STRUT_SIZE = 10;
        JMenuBar box = new JMenuBar();
        box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
        box.setBackground(Color.LIGHT_GRAY);
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        // quit
        box.add(new ClickButton(this,
                    "<u>Q</u>uit", "quit", KeyEvent.VK_Q));
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        // save
        box.add(new ClickButton(this,
                    "<u>S</u>ave", "save", KeyEvent.VK_S));
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        // matching
        box.add(new ClickButton(this,
                    "<u>M</u>atch", "match", KeyEvent.VK_M));
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        // remove matching
        box.add(new ClickButton(this,
                    "<u>U</u>nmatch", "unmatch", KeyEvent.VK_U));
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        // trace of matching
        box.add(new ClickButton(this,
                    "<u>T</u>race", "trace", KeyEvent.VK_T));
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        // replay matching
        box.add(new ClickButton(this,
                    "<u>R</u>eplay", "replay", KeyEvent.VK_R));
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        // smaller/bigger font
	JMenu fontMenu = new JMenu("<html><u>F</u>ont</html>");
	fontMenu.setMnemonic(KeyEvent.VK_F);
	fontMenu.setBackground(Color.LIGHT_GRAY);
	box.add(fontMenu);
	JMenuItem smallerItem = new JMenuItem("<html>sm<u>A</u>ller<html>");
	smallerItem.setBackground(Color.LIGHT_GRAY);
	smallerItem.setActionCommand("smaller");
	smallerItem.setMnemonic(KeyEvent.VK_A);
	smallerItem.setAccelerator(GuiAux.shortcut(KeyEvent.VK_A));
	smallerItem.addActionListener(this);
	JMenuItem biggerItem = new JMenuItem("<html><u>B</u>igger</html>");
	biggerItem.setBackground(Color.LIGHT_GRAY);
	biggerItem.setActionCommand("bigger");
	biggerItem.setMnemonic(KeyEvent.VK_B);
	biggerItem.setAccelerator(GuiAux.shortcut(KeyEvent.VK_B));
	biggerItem.addActionListener(this);
	fontMenu.add(smallerItem);
	fontMenu.add(biggerItem);
	/*
        box.add(new ClickButton(this,
                    "<html>sm<u>A</u>ller</html>", "smaller", KeyEvent.VK_A));
        box.add(new ClickButton(this,
                    "<html><u>B</u>igger</html>", "bigger", KeyEvent.VK_B));
		    */
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        // help
        box.add(new ClickButton(this,
                    "<u>H</u>elp", "help", KeyEvent.VK_H));
        box.add(Box.createHorizontalGlue());
        return box;
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("quit")) {
            dispose();
        } else if (e.getActionCommand().equals("save")) {
	    saveStarts();
        } else if (e.getActionCommand().equals("match")) {
	    doMatching();
        } else if (e.getActionCommand().equals("unmatch")) {
	    undoMatching();
        } else if (e.getActionCommand().equals("trace")) {
	    makeTrace();
        } else if (e.getActionCommand().equals("replay")) {
	    makeReplay();
        } else if (e.getActionCommand().equals("smaller")) {
	    makeSmaller();
        } else if (e.getActionCommand().equals("bigger")) {
	    makeBigger();
        }
	requestFocus();
    }

    // Kill.
    public void dispose() {
	// trace.dispose();
	if (replay != null)
	    replay.dispose();
	super.dispose();
	// saveStarts();
    }

    // Write start positions to files.
    private void saveStarts() {
	GregorianCalendar cal = new GregorianCalendar();
	String day = "" + cal.get(Calendar.YEAR) + "-" +
	    cal.get(Calendar.MONTH) + "-" +
	    cal.get(Calendar.DAY_OF_MONTH) + "T" + 
	    cal.get(Calendar.HOUR_OF_DAY) + ":" +
	    cal.get(Calendar.MINUTE);
	String outName = targetDirectory + "/starts" + day + ".xml";
	saveStarts(startFile);
	saveStarts(outName);
    }

    // Write start position to files.
    private void saveStarts(String outName) {
	PrintWriter out = null;
	try {
	    out = new PrintWriter(new BufferedWriter(new FileWriter(outName)));
	} catch (FileNotFoundException e) {
	    System.err.println(e.getMessage());
	} catch (IOException e) {
	    System.err.println("In " + outName);
	    System.err.println(e.getMessage());
	}
	out.println("<?xml version=\"1.0\"?>");
	out.println("<starts>");
	Iterator lineIter = wordStarts.keySet().iterator();
	while (lineIter.hasNext()) {
	    String line = (String) lineIter.next();
	    TreeSet poss = (TreeSet) wordStarts.get(line);
	    Iterator posIter = poss.iterator();
	    while (posIter.hasNext()) {
		Integer pos = (Integer) posIter.next();
		out.println("<start line=\"" + line + "\" " +
			"pos=\"" + pos + "\"/>");
	    }
	}
	out.println("</starts>");
	out.close();
    }

    /////////////////////////////////////////////////////////////////

    // To become XML parsers with or without validation.
    private DocumentBuilder parser = XMLfiles.getParser();
    private SimpleParser simpleParser = new SimpleParser();

    // For testing for now.
    //
    private String hiFile = "workinprogress/ShipwreckedHi";
    private String alFile = "textxml/ShipwreckedTr";
    private String startFile = "workinprogress/ShipwreckedStarts.xml";
    //
    // private String hiFile = "textsrc/WestcarHi";
    // private String alFile = "textxml/WestcarTr";
    // private String startFile = "workinprogress/WestcarStarts.xml";

    private String targetDirectory = "workinprogress";

    private void setUpContent() {
	Document hiDoc = parse(hiFile);
	Document alDoc = parse(alFile);
	Document startDoc = simpleParser.parse(startFile);
	includeHiDoc(hiDoc);
	includeAlDoc(alDoc);
	includeStartDoc(startDoc);
    }

    // Parse XML file into document.
    private Document parse(String name) {
	Document doc = null;
	String ending = ".xml";
	if (name.endsWith(ending))
	    name = name.substring(0, name.length()-ending.length());
        String inName = name + ".xml";
        try {
            doc = parser.parse(inName);
        } catch (IOException e) {
            System.err.println("In " + inName);
            System.err.println(e.getMessage());
        } catch (SAXException e) {
        }
	return doc;
    }

    // Include hieroglyphic from document into matcher.
    private void includeHiDoc(Document doc) {
	NodeList list = doc.getElementsByTagName("texthi");
	int nElem = list.getLength();
	for (int i = 0; i < nElem; i++) {
	    Node elem = list.item(i);
	    NodeList children = elem.getChildNodes();
	    int nChild = children.getLength();
	    for (int j = 0; j < nChild; j++) {
		Node child = children.item(j);
		if (child instanceof CharacterData) {
		    CharacterData childData = (CharacterData) child;
		    String hi = childData.getData();
		    if (!hi.matches("\\s*")) 
			hiStrings.add(hi);
		}
	    }
	}
    }

    // Include transliteration from document into matcher.
    private void includeAlDoc(Document doc) {
	NodeList list = doc.getElementsByTagName("textal");
	int nElem = list.getLength();
	for (int i = 0; i < nElem; i++) {
	    Node elem = list.item(i);
	    NodeList children = elem.getChildNodes();
	    String phrase = "";
	    int nChild = children.getLength();
	    for (int j = 0; j < nChild; j++) {
		Node child = children.item(j);
		if (child instanceof CharacterData) {
		    CharacterData childData = (CharacterData) child;
		    String al = childData.getData();
		    phrase += al;
		}
	    }
	    String[] words = phrase.split("\\s+");
	    boolean withinBrackets = false;
	    for (int k = 0; k < words.length; k++) 
		if (!words[k].equals("") && !words[k].matches("^\\[.*\\]$") && 
			!words[k].matches("^\\(.*\\)$")) {
		    if (words[k].matches("^\\(.*$"))
			withinBrackets = true;
		    else if (words[k].matches("^[^\\(]*\\)$"))
			withinBrackets = false;
		    else if (!withinBrackets) {
		    String word = words[k].replaceAll("\\(.*\\)","");
			alStrings.add(word);
		    }
		}
	}
    }

    // Include mapping from lines to positions.
    private void includeStartDoc(Document startDoc) {
	NodeList list = startDoc.getElementsByTagName("start");
	int nElem = list.getLength();
	for (int i = 0; i < nElem; i++) {
	    Node elem = list.item(i);
	    NamedNodeMap attr = elem.getAttributes();
	    Node lineNode = attr.getNamedItem("line");
	    Node posNode = attr.getNamedItem("pos");
	    if (lineNode != null && posNode != null) {
		String line = lineNode.getNodeValue();
		String posString = posNode.getNodeValue();
		int pos = Integer.parseInt(posString);
		if (wordStarts.get(line) == null)
		    wordStarts.put(line, new TreeSet());
		TreeSet poss = (TreeSet) wordStarts.get(line);
		poss.add(new Integer(pos));
	    }
	}
    }

    //////////////////////////////////////////////////////////////////////////////

    // Non-validating parser.
    private class SimpleParser {
	DocumentBuilder parser = null;

	public SimpleParser() {
	    try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setCoalescing(false);
		factory.setExpandEntityReferences(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		parser = factory.newDocumentBuilder();
	    } catch (ParserConfigurationException e) {
		System.err.println(e.getMessage());
		System.exit(-1);
	    }
	}

	public Document parse(String f) {
	    Document doc = null;
	    try {
		doc = parser.parse(f);
	    } catch (IOException e) {
		System.err.println(e.getMessage());
	    } catch (SAXException e) {
	    }
	    return doc;
	}
    }

    //////////////////////////////////////////////////////////////////////////////

    // Every time the system is (re)formatted according to a screen width,
    // this value is reassigned.
    // When the screen width changes from this width by more than the size of
    // the right margin, the system is reformatted.
    private int lastFormatWidth = 0;

    // Textual part.
    private class TextView extends JPanel {
	public TextView() {
	    setBackground(Color.WHITE);
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    addComponentListener(new ResizeListener());
	}
    }

    // Class of listening to changes to window size.
    // If change big, then reformat.
    private class ResizeListener implements ComponentListener {
        public void componentResized(ComponentEvent e) {
            if (Math.abs(text.getSize().width - lastFormatWidth) >= rightMargin)
                reformat();
        }
        public void componentMoved(ComponentEvent e) {
        }
        public void componentShown(ComponentEvent e) {
        }
        public void componentHidden(ComponentEvent e) {
        }
    }
    
    // Make smaller/bigger font and reformat.
    private void makeSmaller() {
	hieroFontSize -= 2;
	if (hieroFontSize < minimumHieroFontSize) {
	    hieroFontSize = minimumHieroFontSize;
	}
	makeFonts();
	reformat();
    }
    private void makeBigger() {
	hieroFontSize += 2;
	makeFonts();
	reformat();
    }

    // Reformatted at least once?
    private boolean reformattedOnce = false;

    // List of equal length of JPanels where paragraphs are printed.
    private LinkedList formattedPanels = new LinkedList();

    // Distinguish panel that holds focus, if any.
    private Paragraph focusPanel = null;

    private void reformat() {
	lastFormatWidth = text.getSize().width;
	if (reformattedOnce == false) {
	    reformattedOnce = true;
	    formattedPanels = new LinkedList();
	    Iterator iter = hiStrings.iterator();
	    text.removeAll();
	    int lineNo = 0;
	    while (iter.hasNext()) {
		String line = (String) iter.next();
		JPanel par = new Paragraph(lastFormatWidth, line, "" + lineNo++);
		text.add(par);
		formattedPanels.addLast(par);
	    }
	    for (int i = 1; i < formattedPanels.size(); i++) {
		Paragraph cur = (Paragraph) formattedPanels.get(i);
		Paragraph prev = (Paragraph) formattedPanels.get(i-1);
		cur.setPrevious(prev);
	    }
	    if (formattedPanels.size() > 0) {
		Paragraph first = (Paragraph) formattedPanels.get(0);
		first.distribute(0);
	    }
	} else {
	    ListIterator iter = formattedPanels.listIterator();
	    while (iter.hasNext()) {
		Paragraph par = (Paragraph) iter.next();
		par.redivide(lastFormatWidth);
	    }
	}
	validate();
	repaint();
    }

    // Part of the panel containing one section of hieroglyphic,
    // possibly together with transliteration.
    private class Paragraph extends JPanel {
	public Paragraph prev = null;
	public Paragraph next = null;
	private int width;
	private int height;
	// private RES res;
	// RES divided into lines.
	private Vector divisions;

	// Line of hieroglyphic.
	private String line;

	// Positions were word starts.
	private TreeSet poss;

	// Positions were word starts, according to automatic algoritm.
	private TreeSet possAuto;

	// Has any position in this paragraph or a following
	// one been marked?
	private boolean treated = false;

	// Index of first word matching position in paragraph.
	// Negative means paragraph is new.
	private int alIndex = -1;

	// If focus is in paragraph, which glyph has
	// focus? This is -1 if none; 0 if first glyph.
	private final int NOFOCUS = -1;
	private int focus = NOFOCUS;

	// Prepare paragraph for showing text.
	public Paragraph(int width, String hi, String line) {
	    setBackground(Color.WHITE);
	    setLayout(null);
	    setOpaque(true);
	    // res = RES.createRES(hi, hieroContext);
	    this.width = width;
	    redivide(width);
	    this.line = line;
	    if (wordStarts.get(line) == null)
		wordStarts.put(line, new TreeSet());
	    poss = (TreeSet) wordStarts.get(line);
	    if (wordStartsAuto.get(line) == null)
		wordStartsAuto.put(line, new TreeSet());
	    possAuto = (TreeSet) wordStartsAuto.get(line);
	    addMouseListener(new ClickListener());
	}

	// Divide again with new width.
	public void redivide(int width) {
	    /*
	    height = parSep / 2;

	    divisions = new Vector();
	    int availableWidth = width - leftMargin - rightMargin;
	    ResDivision division = null;
		// new ResDivision(res, availableWidth, hieroContext, true);
	    int lineHeight = division.getHeightPixels();
	    divisions.add(division);
	    height += lineHeight;
	    height += nAlLines * alHeight;
	    RES remainder = division.getRemainder();
	    availableWidth -= leftMargin; // extra whitespace
	    while (!remainder.isEmpty()) {
		ResDivision subdivision = new ResDivision(remainder, 
			availableWidth, hieroContext, true);
		if (subdivision.getInitialNumber() == 0)
		    break;
		height += lineSep;
		int nextLineHeight = subdivision.getHeightPixels();
		divisions.add(subdivision);
		height += nextLineHeight;
		height += nAlLines * alHeight;
		remainder = subdivision.getRemainder();
	    }

	    height += alHeight;
	    height += parSep / 2;
	    // Make sure changes in dimension are noted.
	    invalidate();
	    */
	}

	// Set previous paragraph.
	public void setPrevious(Paragraph prev) {
	    prev.next = this;
	    this.prev = prev;
	    determineTreated();
	}

	// Get line corresponding to paragraph.
	public String getLine() {
	    return line;
	}

	// Clear focus from this panel.
	public void clearFocus() {
	    focus = NOFOCUS;
	    repaint();
	}

	// Put focus on i-th element
	public void putFocus(int i) {
	    if (focusPanel != null && focusPanel != this) 
		focusPanel.clearFocus();
	    focusPanel = this;
	    focus = i;
	    repaint();
	    makeTraceFocus(line, focus);
	}

	// Get position of focus.
	public int getFocus() {
	    return focus;
	}

	// Move focus left.
	public void moveLeft() {
	    /*
	    if (focus > 0) {
		focus--;
		repaint();
		makeTraceFocus(line, focus);
	    } else if (prev != null)
		prev.putFocus(prev.res.glyphs().size() - 1);
		*/
	}

	// Move focus right.
	public void moveRight() {
	    /*
	    if (focus < res.glyphs().size() - 1) {
		focus++;
		repaint();
		makeTraceFocus(line, focus);
	    } else if (next != null)
		next.putFocus(0);
		*/
	}

	// Move focus up.
	public void moveUp() {
	    /*
	    if (prev != null) {
		if (focus >= prev.res.glyphs().size())
		    prev.putFocus(prev.res.glyphs().size() - 1);
		else
		    prev.putFocus(focus);
	    }
	    */
	}

	// Move focus down.
	public void moveDown() {
	    /*
	    if (next != null) {
		if (focus >= next.res.glyphs().size())
		    next.putFocus(next.res.glyphs().size() - 1);
		else
		    next.putFocus(focus);
	    }
	    */
	}

	// Insert start of word.
	public void insertStart() {
	    poss.add(new Integer(focus));
	    determineTreated();
	    distribute(alIndex);
	    unmakeTrace();
	}

	// Remove start of word.
	public void deleteStart() {
	    poss.remove(new Integer(focus));
	    determineTreated();
	    distribute(alIndex);
	    unmakeTrace();
	}

	// Determine (again) whether paragraph is treated.
	public void determineTreated() {
	    boolean presentTreated = 
		next != null && next.treated ||
		!poss.isEmpty();
	    if (treated != presentTreated) {
		treated = presentTreated;
		if (prev != null)
		    prev.determineTreated();
	    }
	    repaint();
	}

	// Distribute words from transliteration over paragraphs,
	// starting with index.
	public void distribute(int index) {
	    alIndex = index;
	    int nextIndex = index + poss.size();
	    if (next != null && next.alIndex != nextIndex) 
		next.distribute(nextIndex);
	    repaint();
	}

	public void paintComponent(Graphics g) {
	    /*
	    super.paintComponent(g);
	    Graphics2D graphics = (Graphics2D) g;
	    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
		    RenderingHints.VALUE_RENDER_QUALITY);

	    int pastGlyphs = 0;
	    int y = parSep / 2;
	    int index = alIndex;
	    for (int i = 0; i < divisions.size(); i++) {
		ResDivision division = (ResDivision) divisions.get(i);
		int thisMargin = (i == 0) ? leftMargin : 2 * leftMargin;
		division.write(graphics, thisMargin, y);
		OverlapPrevent preventer = new OverlapPrevent();

		// Mark start positions of words.
		LinkedList rects = division.getRES().glyphRectangles();
		for (int pos = 0; pos < rects.size(); pos++) {
		    Integer totalPos = new Integer(pastGlyphs + pos);
		    if (poss.contains(totalPos)) {
			Rectangle rect = (Rectangle) rects.get(pos);
			Rectangle marginRect = new Rectangle(rect);
			marginRect.translate(thisMargin, y);
			if (possAuto.contains(totalPos))
			    graphics.setPaint(Color.green);
			else
			    graphics.setPaint(Color.blue);
			graphics.setStroke(new BasicStroke(1.0f));
			graphics.draw(marginRect);
			if (index >= 0 && index < alStrings.size()) {
			    String word = (String) alStrings.get(index);
			    preventer.writeAl(graphics, word, thisMargin + rect.x,
				    y + division.getHeightPixels() + alHeight - alDescent);
			    index++;
			}
		    } else if (possAuto.contains(totalPos)) {
			Rectangle rect = (Rectangle) rects.get(pos);
			Rectangle marginRect = new Rectangle(rect);
			marginRect.translate(thisMargin, y);
			graphics.setPaint(Color.red);
			graphics.setStroke(new BasicStroke(1.0f));
			graphics.draw(marginRect);
		    }
		}

		// Mark focus.
		if (pastGlyphs <= focus && focus - pastGlyphs < rects.size()) {
		    Rectangle rect = (Rectangle) rects.get(focus - pastGlyphs);
		    Rectangle marginRect = new Rectangle(rect);
		    marginRect.translate(thisMargin - focusMargin, y - focusMargin);
		    marginRect.setSize(marginRect.width + 2*focusMargin,
			    marginRect.height + 2*focusMargin);
		    graphics.setPaint(new Color(200, 100, 200, 100));
		    graphics.setStroke(new BasicStroke(1.0f));
		    graphics.fill(marginRect);
		}

		pastGlyphs += rects.size();
		y += division.getHeightPixels();
		y += nAlLines * alHeight;
		y += lineSep;
	    }
	    if (treated && (next == null || !next.treated))
		writeFrom(graphics, alIndex + poss.size(), leftMargin,
			y + alHeight - lineSep);
			*/
	}


        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            d.height = height;
            return d;
        }

        public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            d.height = height;
            return d;
        }

        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = height;
            return d;
        }

	// Listens only to mouse clicks. 
	private class ClickListener extends MouseInputAdapter {
	    public void mouseClicked(MouseEvent event) {
		/*
		int clickX = event.getX();
		int clickY = event.getY();
		int button = event.getButton();

		int pastGlyphs = 0;
		int y = parSep / 2;
		for (int i = 0; i < divisions.size(); i++) {
		    int thisMargin = (i == 0) ? leftMargin : 2 * leftMargin;
		    Point p = new Point(clickX - thisMargin, clickY - y);

		    ResDivision division = (ResDivision) divisions.get(i);
		    LinkedList rects = division.getRES().glyphRectangles();
		    for (int j = 0; j < rects.size(); j++) {
			Rectangle rect = (Rectangle) rects.get(j);
			if (rect.contains(p))
			    putFocus(j + pastGlyphs);
		    }

		    pastGlyphs += rects.size();
		    y += division.getHeightPixels();
		    y += nAlLines * alHeight;
		    y += lineSep;
		}
		if (button == 3)
		    focusPanel.insertStart();
		else if (button == 2)
		    focusPanel.deleteStart();
		    */
	    }
	}
    }

    // To avoid that two words overlap, a word is written lower 
    // if the end of the previous word ends too far to the right.
    private class OverlapPrevent {
	private int[] lines = new int[nAlLines];

	public OverlapPrevent() {
	    for (int i = 0; i < nAlLines; i++)
		lines[i] = 0;
	}

	// Write transliteration.
	// Take first line that is free.
	// If none, then append behind the one with minimum x.
	public void writeAl(Graphics2D g, String al, int x, int y) {
	    int line = 0;
	    int minX = lastFormatWidth;
	    for (int i = 0; i < nAlLines; i++)
		if (lines[i] < minX) {
		    line = i;
		    minX = lines[i];
		}
	    for (int i = 0; i < nAlLines; i++) 
		if (x >= lines[i]) {
		    line = i;
		    break;
		}
	    int xString = Math.max(x, lines[line]);
	    String fontString = egyptMap.mapString(al);
	    g.setFont(egyptFont);
	    g.setColor(Color.BLACK);
	    g.drawString(fontString, xString, y + line * alHeight);
	    int length = metrics.stringWidth(fontString + " ");
	    lines[line] = xString + length;
	}
    }

    // Write words from index.
    private void writeFrom(Graphics2D g, int index, int x, int y) {
	g.setFont(egyptFont);
	g.setColor(Color.BLUE);
	while (index < alStrings.size()) {
	    String al = (String) alStrings.get(index);
	    String fontString = egyptMap.mapString(al);
	    int length = metrics.stringWidth(fontString + " ");
	    if (x + length > lastFormatWidth)
		break;
	    else {
		g.drawString(fontString, x, y);
		x += length;
		index++;
	    }
	}
    }

    ///////////////////////////////////////////////////////////////
    // Processing keyboard input.

    public void keyTyped(KeyEvent e) {
	// ignored
    }

    public void keyPressed(KeyEvent e) {
	int code = e.getKeyCode();
	switch (code) {
	    case KeyEvent.VK_LEFT:
		if (focusPanel != null)
		    focusPanel.moveLeft();
		break;
	    case KeyEvent.VK_RIGHT:
		if (focusPanel != null)
		    focusPanel.moveRight();
		break;
	    case KeyEvent.VK_DOWN:
		if (focusPanel != null)
		    focusPanel.moveDown();
		break;
	    case KeyEvent.VK_UP:
		if (focusPanel != null)
		    focusPanel.moveUp();
		break;
	    case KeyEvent.VK_ENTER:
		if (focusPanel != null)
		    focusPanel.insertStart();
		break;
	    case KeyEvent.VK_DELETE:
		if (focusPanel != null)
		    focusPanel.deleteStart();
		break;
	    case KeyEvent.VK_BACK_SPACE:
		if (focusPanel != null)
		    focusPanel.deleteStart();
		break;
	}
    }

    public void keyReleased(KeyEvent e) {
	// ignored
    }

    ////////////////////////////////////////////////////////////////////
    // Automatic matching.

    // Glyphs as automaton.
    private class HieroState implements LinearFiniteAutomatonState {

	// State one symbol back.
	private HieroState prevState;

	// The section of RES, and the position within that section.
	// Position 0 is start of section.
	private int res;
	private int pos;

	// Cached glyphs within section,
	private LinkedList glyphs;

	// Cached next transitions (empty or singleton).
	private TreeMap nexts = new TreeMap();

	// Cached next state. Null if none.
	private LinearFiniteAutomatonState next = null;

	// Initial state.
	public HieroState() {
	    this(0, 0);
	    makeOtherStates();
	}

	// Make state without given cached glyphs.
	private HieroState(int res, int pos) {
	    this.res = res;
	    this.pos = pos;
	    cacheGlyphs();
	    jumpEpsilon();
	}

	// Make state with given cached glyphs.
	private HieroState(HieroState prevState, int res, int pos, LinkedList glyphs) {
	    this.prevState = prevState;
	    this.res = res;
	    this.pos = pos;
	    this.glyphs = glyphs;
	    jumpEpsilon();
	}

	// Get line number.
	public int getRes() {
	    return res;
	}

	// Get position number.
	public int getPos() {
	    return pos;
	}

	// Get state one symbol back.
	public HieroState getPrevious() {
	    return prevState;
	}

	// Identical to above.
	public LinearFiniteAutomatonState getPrevState() {
	    return prevState;
	}

	public TreeMap getOutTransitions() {
	    return nexts;
	}

	public LinearFiniteAutomatonState getNextState() {
	    return next;
	}

	public boolean isFinal() {
	    return pos >= glyphs.size();
	}

	// Next glyph in state, null if none.
	public String glyph() {
	    if (isFinal())
		return null;
	    else 
		return (String) glyphs.get(pos);
	}

	// Remember glyphs in currect section of RES.
	// Normalise names.
	private void cacheGlyphs() {
	    /*
	    glyphs = new LinkedList();
	    if (res < hiStrings.size()) {
		String hi = (String) hiStrings.get(res);
		LinkedList raw = RES.createRES(hi, hieroContext).glyphs();
		for (Iterator it = raw.iterator(); it.hasNext(); ) {
		    String name = (String) it.next();
		    name = hieroContext.nameToGardiner(name);
		    glyphs.add(name);
		}
	    } 
	    */
	}

	// Jump empty hieroglyphic sections.
	private void jumpEpsilon() {
	    while (pos >= glyphs.size() && res < hiStrings.size()) {
		res++;
		pos = 0;
		cacheGlyphs();
	    }
	}

	// Make other states.
	// Recursion is avoided, as this seems to lead to
	// stack overflows.
	// Special treatment for second/third occurrence of glyph:
	// then special transition for dualis/pluralis instead of 
	// second occurrence / second and third occurrences.
	private void makeOtherStates() {
	    HieroState prevPrevPrevState = null;
	    HieroState prevPrevState = null;
	    HieroState prevState = this;
	    while (!prevState.isFinal()) {
		int prevRes = prevState.res;
		int prevPos = prevState.pos;
		LinkedList prevGlyphs = prevState.glyphs;
		String glyph = prevState.glyph();
		HieroState nextState = new HieroState(prevState, prevRes, prevPos+1, prevGlyphs);
		prevState.next = nextState;
		Set toStates = new TreeSet();
		toStates.add(nextState);
		prevState.nexts.put(glyph, toStates);

		// dualis
		if (prevPrevState != null &&
			prevPrevState.glyph().equals(glyph)) {
		    Set dualStates = new TreeSet();
		    dualStates.add(nextState);
		    // TODO
		    // prevPrevState.nexts.put("dualis", dualStates);
		    prevState.nexts.put("dualis", dualStates);
		    // System.out.println(" " + prevRes + " " + prevPos + " " + glyph);
		    // System.out.println(" " + prevState + " " + prevState.glyph());
		    // System.out.println(" " + prevPrevState + " " + prevPrevState.glyph());

		    // pluralis
		    if (prevPrevPrevState != null &&
			    prevPrevPrevState.glyph().equals(glyph)) {
			Set plurStates = new TreeSet();
			plurStates.add(nextState);
			prevPrevState.nexts.put("pluralis", plurStates);
		    }
		}

		prevPrevPrevState = prevPrevState;
		prevPrevState = prevState;
		prevState = nextState;
	    }
	}

	public boolean equals(Object o) {
	    if (o instanceof HieroState) {
		HieroState other = (HieroState) o;
		return compareTo(other) == 0;
	    } else
		return false;
	}

	// Order by section number, then by position.
	public int compareTo(Object o) {
	    if (o instanceof HieroState) {
		HieroState other = (HieroState) o;
		if (res < other.res)
		    return -1;
		else if (res > other.res)
		    return 1;
		else if (pos < other.pos)
		    return -1;
		else if (pos > other.pos)
		    return 1;
		else
		    return 0;
	    } else
		return 1;
	}

	// For testing.
	public String toString() {
	    return "" + res + " " + pos;
	}
    }

    // Position in hieroglyphic text, identified by line and
    // position.
    private class HieroPos implements Comparable {
	private int line;
	private int pos;

	public HieroPos(int line, int pos) {
	    this.line = line;
	    this.pos = pos;
	}

	public int getLine() {
	    return line;
	}

	public int getPos() {
	    return pos;
	}

	public int compareTo(Object o) {
	    if (o instanceof HieroPos) {
		HieroPos other = (HieroPos) o;
		if (line < other.line)
		    return -1;
		else if (line > other.line)
		    return 1;
		else if (pos < other.pos)
		    return -1;
		else if (pos > other.pos)
		    return 1;
		else 
		    return 0;
	    } else
		return 1;
	}

	public String toString() {
	    return "" + line + " " + pos;
	}
    }

    // Get normalised glyphs from line (number).
    private LinkedList getNormalisedGlyphs(int line) {
	LinkedList glyphs = new LinkedList();
	/*
	if (line < hiStrings.size()) {
	    String hi = (String) hiStrings.get(line);
	    LinkedList raw = RES.createRES(hi, hieroContext).glyphs();
	    for (Iterator it = raw.iterator(); it.hasNext(); ) {
		String name = (String) it.next();
		name = hieroContext.nameToGardiner(name);
		glyphs.add(name);
	    }
	}
	*/
	return glyphs;
    }

    // Construct states of hieroglyphic. Return first such state.
    // States must point to position where glyph is located;
    // except the final state, which is one position beyond last
    // glyph.
    private DoubleLinearFiniteAutomatonState initialHieroState() {
	LabeledDoubleLinearFiniteAutomatonState first = null;
	LabeledDoubleLinearFiniteAutomatonState prev = null;
	String prevGlyph = null;
	int line = 0;
	int pos = 0;
	LinkedList glyphs = getNormalisedGlyphs(line);
	do {
	    while (pos >= glyphs.size() && line < hiStrings.size() - 1) {
		line++;
		pos = 0;
		glyphs = getNormalisedGlyphs(line);
	    }
	    LabeledDoubleLinearFiniteAutomatonState state =
		new LabeledDoubleLinearFiniteAutomatonState(new HieroPos(line, pos));
	    String glyph = null;
	    if (pos >= glyphs.size())
		state.setFinal(true);
	    else
		glyph = (String) glyphs.get(pos);
	    if (prev == null)
		first = state;
	    else {
		Set states = new TreeSet();
		states.add(state);
		prev.setNextState(state);
		prev.getOutTransitions().put(prevGlyph, states);
	    }
	    prev = state;
	    prevGlyph = glyph;
	    pos++;
	} while (!prev.isFinal());
	return first;
    }

    // Construct states of transliteration. Return first such state.
    private LinearFiniteAutomatonState initialTransState() {
	LabeledLinearFiniteAutomatonState first = null;
	LabeledLinearFiniteAutomatonState prev = null;
	for (int i = 0; i <= alStrings.size(); i++) {
	    LabeledLinearFiniteAutomatonState state =
		new LabeledLinearFiniteAutomatonState(new Integer(i));
	    if (i >= alStrings.size()) 
		state.setFinal(true);
	    if (i == 0)
		first = state;
	    else {
		String word = (String) alStrings.get(i-1);
		Set states = new TreeSet();
		states.add(state);
		prev.setNextState(state);
		prev.getOutTransitions().put(word, states);
	    }
	    prev = state;
	}
	return first;
    }

    // Do automatic matching.
    private void doMatching() {
	DoubleLinearFiniteAutomatonState hiInitial = initialHieroState();
	NumberMeanings.induceRepeat(hiInitial);
	NumberMeanings.induceNumbers(hiInitial);
	meanings.induceMeanings(hiInitial);
	LinearFiniteAutomatonState alInitial = initialTransState();
	match = new HieroTransMatching(hiInitial, alInitial, beam);
	HiAlConfiguration finalConfig = match.bestMatch();
	getAutomaticWordStarts(finalConfig);
	unmakeTrace();
    }

    // Remove automatic matching.
    private void undoMatching() {
	Set lines = wordStartsAuto.keySet();
	Iterator it = lines.iterator();
	while (it.hasNext()) {
	    String line = (String) it.next();
	    TreeSet poss = (TreeSet) wordStartsAuto.get(line);
	    poss.clear();
	}
	match = null;
	repaint();
    }

    // Get automatically computed word start positions.
    private void getAutomaticWordStarts(HiAlConfiguration config) {
	for ( ; config != null; config = config.prev()) {
	    if (config.isWordStart()) {
		LabeledDoubleLinearFiniteAutomatonState state =
		    (LabeledDoubleLinearFiniteAutomatonState) config.getHieroState();
		HieroPos hieroPos = (HieroPos) state.getLabel();
		String line = "" + hieroPos.getLine();
		int pos = hieroPos.getPos();
		if (wordStartsAuto.get(line) == null) 
		    wordStartsAuto.put(line, new TreeSet());
		TreeSet poss = (TreeSet) wordStartsAuto.get(line);
		poss.add(new Integer(pos));
	    }
	}
	repaint();
    }

    // Make trace visible.
    private void makeTrace() {
	int total = 0;
	int found = 0;
	int notFound = 0;
	int wrong = 0;
	// For manual word starts, how agrees with automatic.
	Iterator lineIter = wordStarts.keySet().iterator();
	while (lineIter.hasNext()) {
	    String line = (String) lineIter.next();
	    TreeSet poss = (TreeSet) wordStarts.get(line);
	    TreeSet possAuto = (TreeSet) wordStartsAuto.get(line);
	    Iterator posIter = poss.iterator();
	    while (posIter.hasNext()) {
		total++;
		Integer pos = (Integer) posIter.next();
		if (possAuto != null && possAuto.contains(pos))
		    found++;
		else
		    notFound++;
	    }
	}
	// For automatic word starts, how agrees with manual.
	lineIter = wordStartsAuto.keySet().iterator();
	while (lineIter.hasNext()) {
	    String line = (String) lineIter.next();
	    TreeSet poss = (TreeSet) wordStartsAuto.get(line);
	    TreeSet possManual = (TreeSet) wordStarts.get(line);
	    Iterator posIter = poss.iterator();
	    while (posIter.hasNext()) {
		Integer pos = (Integer) posIter.next();
		if (possManual == null || !possManual.contains(pos))
		    wrong++;
	    }
	}
	// trace.putStatistics(total, found, notFound, wrong);
	if (match != null && focusPanel != null) {
	    // trace.setVisible(true);
	    makeTraceFocus(focusPanel.getLine(), focusPanel.getFocus());
	} else {
	    // trace.clearTrace();
	    // trace.setVisible(true);
	}
    }

    // Make trace invisible.
    private void unmakeTrace() {
	// trace.setVisible(false);
    }

    // If there is trace window, set it to point to position in line.
    private void makeTraceFocus(String line, int pos) {
	/*
	if (trace.isVisible() && match != null) {
	    trace.clearTrace();
	    collectTrace(line, pos);
	}
	*/
    }

    // Build list of into configurations to collect rows of trace
    // around configuration that is focus.
    private void collectTrace(String line, int pos) {
	HiAlConfiguration config = match.bestMatch();
	int nBefore = 5;
	int nAfter = 15;
	LinkedList queue = new LinkedList();
	boolean found = false;
	while (config != null) {
	    queue.addFirst(config);
	    LabeledDoubleLinearFiniteAutomatonState state = 
		(LabeledDoubleLinearFiniteAutomatonState) config.getHieroState();
	    HieroPos hieroPos = (HieroPos) state.getLabel();
	    String thisLine = "" + hieroPos.getLine();
	    int thisPos = hieroPos.getPos();
	    if (line.equals(thisLine) && pos == thisPos) {
		config = config.prev();
		found = true;
		break;
	    } else if (queue.size() > nAfter)
		queue.removeLast();
	    config = config.prev();
	}
	if (!found) {
	    // trace.putNotFound();
	    // trace.validate();
	    // trace.repaint();
	    return;
	}
	for (int i = 0; i < nBefore && config != null; i++, config = config.prev()) 
	    queue.addFirst(config);
	DoubleLinearFiniteAutomatonState prevState = null;
	// Mark first state different from focus state.
	DoubleLinearFiniteAutomatonState prevStateFocus = null;
	// All actions possible in previous configuration.
	LinkedList allActions = new LinkedList();
	for (int i = 0; i < queue.size(); i++) {
	    config = (HiAlConfiguration) queue.get(i);
	    LabeledDoubleLinearFiniteAutomatonState state = 
		(LabeledDoubleLinearFiniteAutomatonState) config.getHieroState();
	    HieroPos hieroPos = (HieroPos) state.getLabel();
	    String thisLine = "" + hieroPos.getLine();
	    int thisPos = hieroPos.getPos();
	    String res = null;
	    res = collectScannedHiero(prevState, state);
	    String action = config.action();
	    double penalty = config.penalty();
	    WordMatch word = config.getWordMatch();
	    String allSteps = "";
	    if (prevState != null)
		allSteps = nextJumps(prevState);
	    boolean focus = false;
	    if (prevStateFocus != null && state != prevStateFocus) {
		focus = true;
		prevStateFocus = null;
	    }
	    // trace.putTraceLine(res, word, action, penalty, allSteps, focus);
	    if (line.equals(thisLine) && pos == thisPos) {
		prevStateFocus = state;
	    }
	    allActions = config.nextConfigs();
	    prevState = state;
	}
	// trace.validate();
	// trace.repaint();
    }

    // Collect hieroglyphs scanned since last configuration.
    // Take only the first possibility.
    private String collectScannedHiero(LinearFiniteAutomatonState prevState, 
	    LinearFiniteAutomatonState state) {
	if (prevState == state || prevState == null)
	    return "";
	else {
	    TreeMap nexts = prevState.getOutTransitions();
	    for (Iterator it = nexts.keySet().iterator(); it.hasNext(); ) {
		String symbol = (String) it.next();
		TreeSet states = (TreeSet) nexts.get(symbol);
		for (Iterator iter = states.iterator();
			iter.hasNext(); ) {
		    LinearFiniteAutomatonState next =
			(LinearFiniteAutomatonState) iter.next();
		    String suffix = collectScannedHiero(next, state);
		    if (suffix.equals(""))
			return symbol;
		    else
			return suffix + "-" + symbol;
		}
	    }
	    return "";
	}
    }

    // Look at all possible transitions from current state.
    // Order them by the glyphs leading to transitions.
    private String nextJumps(DoubleLinearFiniteAutomatonState state) {
	TreeMap sequenceToReadings = new TreeMap();
	TreeMap meanings = state.getInducedOutTransitions();
	for (Iterator it = meanings.keySet().iterator();
		it.hasNext(); ) {
	    HieroMeaning meaning = (HieroMeaning) it.next();
	    TreeSet states = (TreeSet) meanings.get(meaning);
	    for (Iterator iter = states.iterator();
		    iter.hasNext(); ) {
		DoubleLinearFiniteAutomatonState next =
		    (DoubleLinearFiniteAutomatonState) iter.next();
		String glyphSequence = collectScannedHiero(state, next);
		if (sequenceToReadings.get(glyphSequence) == null)
		    sequenceToReadings.put(glyphSequence, new TreeSet());
		TreeSet readings = (TreeSet) sequenceToReadings.get(glyphSequence);
		String type = meaning.getType();
		String phon = meaning.getPhonetic();
		if (type.equals("det"))
		    readings.add("det");
		else if (type.equals("phon"))
		    readings.add("'" + phon + "'");
		else if (type.equals("num"))
		    readings.add("'" + phon + "'");
		else if (type.equals("dualis"))
		    readings.add("dual");
		else if (type.equals("pluralis"))
		    readings.add("plur");
	    }
	}
	StringBuffer buf = new StringBuffer();
	buf.append("<html>");
	for (Iterator it = sequenceToReadings.keySet().iterator();
		it.hasNext(); ) {
	    String glyphSequence = (String) it.next();
	    TreeSet readings = (TreeSet) sequenceToReadings.get(glyphSequence);
	    buf.append("&nbsp;&nbsp;<font color=blue>" + glyphSequence + ":</font>&nbsp;");
	    Iterator readIt = readings.iterator();
	    while (readIt.hasNext()) {
		String read = (String) readIt.next();
		buf.append(read);
		if (readIt.hasNext())
		    buf.append(", ");
	    }
	}
	buf.append("</html>");
	return buf.toString();
    }

    // Maximum number of states for replay.
    private static final int replayLength = 8;

    // Make reply.
    private void makeReplay() {
	String replayStr;
	if (match == null) {
	    replayStr = "<html>Matching not yet done</html>";
	} else if (focusPanel == null) {
	    replayStr = "<html>No focus</html>";
	} else {
	    HiAlConfiguration config =
		configAtState(focusPanel.getLine(), focusPanel.getFocus());
	    if (config == null) {
		replayStr = "<html>No configuration on glyph</html>";
	    } else {
		String replayed = HieroTransMatching.replay(config, beam, replayLength);
		replayStr = "<html>" + replayed + "</html>";
	    }
	}
	if (replay == null)
	    replay = new HTMLWindow("Replay", replayStr);
	else
	    replay.setText(replayStr);
	replay.setVisible(true);
    }

    // Find configuration matching glyph position. 
    // Null if none.
    private HiAlConfiguration configAtState(String line, int pos) {
	HiAlConfiguration config = match.bestMatch();
	while (config != null) {
	    LabeledDoubleLinearFiniteAutomatonState state = 
		(LabeledDoubleLinearFiniteAutomatonState) config.getHieroState();
	    HieroPos hieroPos = (HieroPos) state.getLabel();
	    String thisLine = "" + hieroPos.getLine();
	    int thisPos = hieroPos.getPos();
	    if (line.equals(thisLine) && pos == thisPos) 
		return config;
	    config = config.prev();
	} 
	return null;
    }

    // Testing so far.
    public static void main(String[] args) {
	HieroMatcher matcher = new HieroMatcher();
    }

}
