package nederhof.interlinear.egyptian.ortho;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.alignment.generic.*;
import nederhof.interlinear.egyptian.ortho.OrthoEditor.Mode;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.egyptian.*;
import nederhof.res.*;
import nederhof.res.editor.FragmentEditor;
import nederhof.res.format.FormatFragment;
import nederhof.res.operations.*;

// Part of orthographic editor GUI containing a single word (segment).
public class OrthoSegmentPane extends JPanel implements EditChainElement {

    // For hieroglyphic.
    private HieroRenderContext hieroContext;
    private ParsingContext parsingContext;

    // Fonts. Only once created.
    private static Font latinFont = null;
    private static Font italicFont = null;
    private static Font egyptLowerFont = null;
    private static Font egyptUpperFont = null;
    private static FontMetrics italicMetrics = null;
    private static FontMetrics latinMetrics = null;
    private static FontMetrics egyptLowerMetrics = null;
    private static FontMetrics egyptUpperMetrics = null;

    // Margin.
    private static final int MARGIN = 10;

    // The index of this segment.
    private int id;

    // Hieroglyphic.
    private String hi;
    // Formatted.
    private FormatFragment hiFormat;
    // Formatted, with padding.
    private FormatFragment hiFormatWide;
    // Names of glyphs (normalized).
    private Vector<String> hiNames;

    // Transliteration.
    private String al;
    // Transliteration, split into lower/upper case.
    private Vector<Object[]> alParts;
    // Transliteration, split into lower/upper case, with extra space.
    private Vector<Object[]> alPartsWide;

    // The orthographic functions.
    private Vector<OrthoElem> orthos;

    // Rectangles around elements.
    private Vector<Rectangle> hiRects;
    private Vector<Rectangle> funRects;
    private Vector<Rectangle> alRects;

    // Focussed elements.
    private TreeSet<Integer> hiFocussed = new TreeSet<Integer>();
    private TreeSet<Integer> funFocussed = new TreeSet<Integer>();
    private TreeSet<Integer> alFocussed = new TreeSet<Integer>();

    // Is current?
    private boolean isCurrent = false;
    // Text centered?
    private boolean center = false;

    /**
     * Constructor.
     * Current segment has highlight.
     * If not center, then right-justify.
     */
    public OrthoSegmentPane(int id, String hi, String al, Vector<OrthoElem> orthos,
	    boolean isCurrent, boolean center, 
	    HieroRenderContext hieroContext, ParsingContext parsingContext) {
	this.id = id;
	this.isCurrent = isCurrent;
	this.center = center;
	this.hieroContext = hieroContext;
	this.parsingContext = parsingContext;

	highlightSegment(isCurrent);
	setLayout(null);
	addMouseListener(new ClickListener());

	getFonts();

	setOrtho(orthos);
	setAl(al);
	setHi(hi);
	doFormatting();
	allowEditing(true);
    }

    /**
     * If the segment is the current one, then make its border blue;
     * otherwise make it black.
     */
    private void highlightSegment(boolean isCurrent) {
	if (isCurrent) {
	    setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));
	} else {
	    setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
	}
    }

    // Allow editing of this panel.
    public void allowEditing(boolean allow) {
	setEnabled(allow);
	if (allow)
	    setBackground(Color.WHITE);
	else
	    setBackground(Color.LIGHT_GRAY);
	repaint();
    }

    // Create fonts. Only once.
    private void getFonts() {
	if (egyptUpperMetrics == null) {
	    latinFont = new Font(Settings.inputTextFontName,
		    	Font.PLAIN, Settings.inputTextFontSize);
	    italicFont = new Font(Settings.inputTextFontName,
		    	Font.ITALIC, Settings.inputTextFontSize);
	    egyptLowerFont = TransHelper.translitLower(
		    	Settings.translitFontStyle, Settings.translitFontSize);
	    egyptUpperFont = TransHelper.translitUpper(
		    	Settings.translitFontStyle, Settings.translitFontSize);
	    latinMetrics = getFontMetrics(latinFont);
	    italicMetrics = getFontMetrics(italicFont);
	    egyptLowerMetrics = getFontMetrics(egyptLowerFont);
	    egyptUpperMetrics = getFontMetrics(egyptUpperFont);
	}
    }

    // Size is stable.
    public Dimension getMinimumSize() {
	return getPreferredSize();
    }
    public Dimension getMaximumSize() {
	return getPreferredSize();
    }

    ///////////////////////////////////////////////////////
    // Content setup.

    // Set orthography.
    private void setOrtho(Vector<OrthoElem> orthos) {
	this.orthos = new Vector<OrthoElem>(orthos);
	OrthoHelper.sort(this.orthos);
    }

    // Set transliteration. Do widening if needed.
    private void setAl(String al) {
	this.al = al;
	this.alParts = TransHelper.lowerUpperParts(al);
	this.alPartsWide = widen(alParts);
    }

    // Set hieroglyphic. Done after orthos and al, to do padding
    // if needed. Truncate too long fragments.
    private void setHi(String hi) {
	this.hi = hi;
	ResFragment hiParsed = ResFragment.parse(hi, parsingContext);
	hiParsed = (new NormalizerMnemonics()).normalize(hiParsed);
	int maxLen = 100;
	if (hiParsed.nGlyphs() > maxLen)
	    hiParsed = hiParsed.prefixGlyphs(maxLen);
	hiFormat = new FormatFragment(hiParsed, hieroContext);
	if (!center) 
	    hiFormatWide = hiFormat;
	else {
	    int width = Math.round(maxWidth());
	    int pad = width - Math.round(minWidthHi());
	    hiFormatWide = new FormatFragment(hiParsed,
		    hieroContext, pad);
	}
	hiNames = hiFormat.glyphNames();
    }

    ///////////////////////////////////////////////////////
    // Focussing.

    // How many can be focussed?
    public int nHiFocusables() {
	return hiRects.size();
    }
    
    // Remove focus.
    public void removeFocus() {
	hiFocussed.clear();
	funFocussed.clear();
	alFocussed.clear();
	repaint();
    }
    public void removeHiFocus() {
	hiFocussed.clear();
	repaint();
    }
    public void removeFunFocus() {
	funFocussed.clear();
	repaint();
    }
    public void removeAlFocus() {
	alFocussed.clear();
	repaint();
    }
    // Add focus.
    public void addHiFocus(int i) {
	hiFocussed.add(i);
	repaint();
    }
    public void addFunFocus(int i) {
	funFocussed.add(i);
	repaint();
    }
    public void addAlFocus(int i) {
	alFocussed.add(i);
	repaint();
    }
    // Set (only) focus.
    public void setHiFocus(int i) {
	hiFocussed.clear();
	addHiFocus(i);
	repaint();
    }
    public void setFunFocus(int i) {
	funFocussed.clear();
	addFunFocus(i);
	repaint();
    }
    public void setFunFocus(OrthoElem ortho) {
	int i = orthos.indexOf(ortho);
	if (i >= 0)
	    setFunFocus(i);
    }
    public void setAlFocus(int i) {
	alFocussed.clear();
	addAlFocus(i);
	repaint();
    }
    // Set focus to set.
    public void setHiFocus(int[] signs) {
	hiFocussed.clear();
	if (signs != null)
	    for (int i = 0; i < signs.length; i++) 
		hiFocussed.add(signs[i]);
	repaint();
    }
    public void setAlFocus(int[] letters) {
	alFocussed.clear();
	if (letters != null)
	    for (int i = 0; i < letters.length; i++) 
		alFocussed.add(letters[i]);
	repaint();
    }
    // Remove focus.
    public void removeHiFocus(int i) {
	hiFocussed.remove(i);
	repaint();
    }
    public void removeFunFocus(int i) {
	funFocussed.remove(i);
	repaint();
    }
    public void removeAlFocus(int i) {
	alFocussed.remove(i);
	repaint();
    }
    // Get focus.
    public TreeSet<Integer> getHiFocus() {
	return hiFocussed;
    }
    public TreeSet<Integer> getFunFocus() {
	return funFocussed;
    }
    public TreeSet<Integer> getAlFocus() {
	return alFocussed;
    }
    // Get focussed sign positions, plus i more after maximum if possible.
    // If not possible, return empty.
    public TreeSet<Integer> getHiFocusPlusNext(int i) {
	TreeSet<Integer> his = new TreeSet<Integer>(getHiFocus());
	if (his.isEmpty())
	    return his;
	int max = his.last();
	if (max+i < nHiFocusables()) {
	    for (int j = 1; j <= i; j++)
		his.add(max+j);
	    return his;
	} else
	    return new TreeSet<Integer>();
    }
    // Get name of first focussed hieroglyph.
    public String getFirstHiName() {
	if (!getHiFocus().isEmpty() && getHiFocus().first() < hiNames.size())
	    return hiNames.get(getHiFocus().first());
	else
	    return null;
    }
    // Get focussed glyphs as RES.
    public String getHiRes() {
	TreeSet<Integer> his = getHiFocus();
	Vector<String> glyphs = new Vector<String>();
	for (int i : his) 
	    glyphs.add(hiNames.get(i));
	ResComposer composer = new ResComposer();
	ResFragment composed = composer.composeNames(glyphs);
	return (new NormalizerMnemonics()).normalize(composed).toString();
    }
    // For focussed glyphs as RES, plus next j glyphs.
    // If not possible, then return empty.
    public String getHiResPlusNext(int j) {
	TreeSet<Integer> his = getHiFocusPlusNext(j);
	Vector<String> glyphs = new Vector<String>();
	for (int i : his) 
	    glyphs.add(hiNames.get(i));
	ResComposer composer = new ResComposer();
	ResFragment composed = composer.composeNames(glyphs);
	return (new NormalizerMnemonics()).normalize(composed).toString();
    }

    // Get function belonging to i-th rectangle.
    public OrthoElem getFun(int i) {
	if (i < orthos.size())
	    return orthos.get(i);
	else
	    return null;
    }
    // Get (first) focussed function. Or null.
    public OrthoElem getFun() {
	if (funFocussed.isEmpty())
	    return null;
	else
	    return getFun(funFocussed.first());
    }

    ///////////////////////////////////////////////////////
    // Drawing and formatting.

    // Dry run for painting.
    private void doFormatting() {
	hiRects = new Vector<Rectangle>();
	funRects = new Vector<Rectangle>();
	alRects = new Vector<Rectangle>();
	int width = fullWidth();
	int height = draw(null, hiRects, funRects, alRects);
	Dimension size = new Dimension(width, height);
	setPreferredSize(size);
    }

    // Paint.
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D) g;
	draw(g2, hiRects, funRects, alRects);
    }

    // Only draw if graphics is non-null.
    // Otherwise, this is 'dry-run' to determine height and rectangles.
    private int draw(Graphics2D g, Vector<Rectangle> hiRects, 
	    Vector<Rectangle> funRects, Vector<Rectangle> alRects) {
        int firstRowSep = 40;
        int secondRowSep = 40;
        // offsets
	int hiX = 0;
	int funX = 0;
	int alX = 0;
	if (maxWidth() < 1.5 * Settings.displayWidthInit) {
	    if (center && widthHi() < maxWidth())
		hiX = Math.round(maxWidth() - widthHi()) / 2;
	    if (center && widthOrtho() < maxWidth())
		funX = Math.round(maxWidth() - widthOrtho()) / 2;
	    if (center && widthAl() < maxWidth())
		alX = Math.round(maxWidth() - widthAl()) / 2;
	}
	int height = MARGIN;
	drawHi(hiFormatWide, MARGIN + hiX, height, g, hiRects);
	height += hiFormatWide.height() + firstRowSep;
	height += latinMetrics.getHeight() - latinMetrics.getDescent();;
	drawOrtho(MARGIN + funX, height, g, funRects);
	height += latinMetrics.getDescent() + secondRowSep;
	height += egyptUpperMetrics.getHeight() - egyptUpperMetrics.getDescent();;
	drawAl(center, MARGIN + alX, height, g, alRects);
	height += latinMetrics.getDescent() + MARGIN;
        if (g != null) {
	    for (int i = 0; i < orthos.size(); i++) {
		OrthoElem ortho = orthos.get(i);
		Rectangle funRect = funRects.get(i);
		int funMid = funRect.x + funRect.width / 2;
		int funTop = funRect.y;
		int funBottom = funRect.y + funRect.height;
		int[] signs = ortho.signs();
		int[] letters = ortho.letters();
		if (signs != null)
		    for (int j = 0; j < signs.length; j++) {
			int sign = signs[j];
			if (0 <= sign && sign < hiRects.size()) {
			    Rectangle hiRect = hiRects.get(sign);
			    int hiMid = hiRect.x + hiRect.width / 2;
			    int hiBottom = hiRect.y + hiRect.height;
			    g.drawLine(hiMid, hiBottom, funMid, funTop);
			}
		    }
		if (letters != null)
		    for (int j = 0; j < letters.length; j++) {
			int letter = letters[j];
			if (0 <= letter && letter < alRects.size()) {
			    Rectangle alRect = alRects.get(letter);
			    if (alRect != null) {
				int alMid = alRect.x + alRect.width / 2;
				int alTop = alRect.y;
				g.drawLine(funMid, funBottom, alMid, alTop);
			    }
			}
		    }
	    }
	    g.setColor(Color.BLUE);
	    for (int i : hiFocussed) 
		if (i < hiRects.size())
		    g.draw(hiRects.get(i));
	    for (int i : funFocussed) 
		if (i < funRects.size())
		    g.draw(funRects.get(i));
	    for (int i : alFocussed) 
		if (i < alRects.size() && alRects.get(i) != null)
		    g.draw(alRects.get(i));
	}
	if (isCurrent) 
	    height = drawValues(g, height);
        return height;
    }

    // Draw additional values of orthographic elements. Return height with
    // additional material.
    private int drawValues(Graphics2D g, int height) {
	for (int i = 0; i < orthos.size(); i++) {
	    OrthoElem elem = orthos.get(i);
	    String val = elem.argValue();
	    if (val != null && !val.equals("")) {
		String name = elem.argName();
		boolean isAl = name.equals("word") || name.equals("lit");
		Font font = isAl ? egyptLowerFont : latinFont;
		FontMetrics metrics = isAl ? egyptLowerMetrics : latinMetrics;
		height += metrics.getHeight() - metrics.getDescent();
		if (g != null) {
		    g.setFont(font);
		    g.setColor(Color.BLUE);
		    g.drawString(val, MARGIN, height);
		}
		height += metrics.getDescent();
	    } else {
		height += latinMetrics.getHeight() - latinMetrics.getDescent();
		if (g != null) {
		    g.setFont(latinFont);
		    g.setColor(Color.BLUE);
		    g.drawString("-", MARGIN, height);
		}
		height += latinMetrics.getDescent();
	    }
	}
	height += MARGIN;
	return height;
    }

    // Width of full entry. Take maximum of three rows.
    private int maxWidth() {
	return Math.max(minWidthHi(), Math.max(widthOrtho(), widthAl()));
    }

    // Width of full entry.
    private int fullWidth() {
        return maxWidth() + 2 * MARGIN;
    }

    private int drawHi(FormatFragment hi, int x, int y, 
	    Graphics2D g, Vector<Rectangle> rects) {
	if (hi.nGlyphs() == 0) 
	    return addDefault("hiero", x, y + hi.height(), g, rects);
	int width = 0;
	if (g != null) {
	    hi.write(g, x, y);
	} else if (rects != null) {
	    Vector<Rectangle> hiRects = hi.glyphRectangles();
	    for (Rectangle r : hiRects) {
		Rectangle moved = new Rectangle(x + r.x , y + r.y, r.width, r.height);
		rects.add(moved);
	    }
	}
	width += hi.width();
	return width;
    }
    private int widthHi() {
	return drawHi(hiFormatWide, 0, 0, null, null);
    }
    private int minWidthHi() {
	return drawHi(hiFormat, 0, 0, null, null);
    }

    // Draw orthography.
    // If graphics is null, then this is dry run.
    private int drawOrtho(int x, int y, Graphics2D g, Vector<Rectangle> rects) {
        int width = 0;
        for (int i = 0; i < orthos.size(); i++) {
            OrthoElem ortho = orthos.get(i);
            String name = ortho.name();
	    int nameWidth = latinMetrics.stringWidth(name);
	    if (g != null) {
		g.setFont(latinFont);
		g.setColor(isConsistent(ortho) ? Color.BLACK : Color.RED);
		g.drawString(name, x + width, y);
	    } else if (rects != null) {
		int ascent = latinMetrics.getAscent();
		int descent = latinMetrics.getDescent();
		Rectangle rect = new Rectangle(x + width, y - ascent, nameWidth, ascent+descent);
		rects.add(rect);
	    }
            width += nameWidth;
	    if (i < orthos.size() - 1)
		width += orthoSep();
        }
        return width;
    }
    private int widthOrtho() {
        return drawOrtho(0, 0, null, null);
    }

    // Space between orthographic elements.
    private int orthoSep() {
        return latinMetrics.stringWidth("  ");
    }

    // Draw transliteration.
    // If graphics is null, then this is dry run.
    private int drawAl(boolean wide, int x, int y, Graphics2D g,
		Vector<Rectangle> rects) {
	if (TransHelper.letters(al).size() == 0)
	    return addDefault("trans", x, y, g, rects);
        int width = 0;
	Vector<Object[]> parts = wide ? alPartsWide : alParts;
	for (int i = 0; i < parts.size(); i++) {
	    Object[] pair = parts.get(i);
	    String kind = (String) pair[0];
	    String info = (String) pair[1];
	    FontMetrics metrics = kind.equals("translower") ? egyptLowerMetrics : egyptUpperMetrics;
	    Font font = kind.equals("translower") ? egyptLowerFont : egyptUpperFont;
	    int infoWidth = metrics.stringWidth(info);
	    if (g != null) {
		g.setFont(font);
		g.setColor(Color.BLACK);
		g.drawString(info, x + width, y);
	    } else if (rects != null) {
		int ascent = metrics.getAscent();
		int descent = metrics.getDescent();
		for (int j = 0; j < info.length(); j++) {
		    if (!wide || j % 2 == 0) {
			if (info.charAt(j) == ' ') 
			    rects.add(null);
			else {
			    String pref = info.substring(0, j);
			    String preff = info.substring(0, j+1);
			    int prefWidth = metrics.stringWidth(pref);
			    int preffWidth = metrics.stringWidth(preff);
			    int letterWidth = preffWidth - prefWidth;
			    Rectangle rect = new Rectangle(x + width + prefWidth, y - ascent, 
					letterWidth, ascent+descent);
			    rects.add(rect);
			}
		    }
		}
	    }
	    width += infoWidth;
	}
        return width;
    }
    private int widthAl() {
	if (center)
	    return drawAl(center, 0, 0, null, null);
	else
	    return drawAl(false, 0, 0, null, null);
    }

    // For transliteration or hieroglyphic that is empty, add some default
    // string.
    private int addDefault(String defaultStr, int x, int y, Graphics2D g,
		Vector<Rectangle> rects) {
	int defaultWidth = italicMetrics.stringWidth(defaultStr);
	if (g != null) {
	    g.setFont(italicFont);
	    g.setColor(Color.BLACK);
	    g.drawString(defaultStr, x, y);
	} else if (rects != null) {
	    int ascent = italicMetrics.getAscent();
	    int descent = italicMetrics.getDescent();
	    Rectangle rect = new Rectangle(x, y - ascent, defaultWidth, ascent+descent);
	    rects.add(rect);
	}
	return defaultWidth;
    }
    // Insert space after each letter, in vector of parts.
    private static Vector widen(Vector parts) {
        Vector wideParts = new Vector();
        for (int j = 0; j < parts.size(); j++) {
            Object[] pair = (Object[]) parts.get(j);
            String kind = (String) pair[0];
            String info = (String) pair[1];
            StringBuffer b = new StringBuffer();
            for (int i = 0; i < info.length(); i++) {
                b.append("" + info.charAt(i));
		if (j < parts.size() - 1 || i < info.length() - 1)
		    b.append(" ");
	    }
            String wideInfo = b.toString();
            wideParts.add(new Object[] {kind, wideInfo});
        }
        return wideParts;
    }

    ///////////////////////////////////////////////////////
    // Consistency checks. Annotations that are not consistent with
    // the sign list are printed in red. Caching to avoid an external
    // check to happen more than once.

    private HashMap<OrthoElem,Boolean> consistency = new HashMap<OrthoElem,Boolean>(10);

    private boolean isConsistent(OrthoElem elem) {
	if (consistency.get(elem) == null) {
	    boolean consistent = isConsistent(elem, hiNames, al);
	    consistency.put(elem, consistent);
	} 
	return consistency.get(elem);
    }

    ///////////////////////////////////////////////////////
    // Mouse.

    // Listens to mouse clicks. 
    // When nothing found, widen search, upto bound.
    private class ClickListener extends MouseInputAdapter {
        public void mouseClicked(MouseEvent event) {
	    if (!isEnabled())
		return;
	    if (!isCurrent) {
		clicked(id);
		return;
	    }
	    int button = 
		event.getButton() == MouseEvent.BUTTON1 && !event.isControlDown() ?
		    MouseEvent.BUTTON1 :
		event.getButton() == MouseEvent.BUTTON3 || event.isControlDown() ?
		    MouseEvent.BUTTON3 :
		    event.getButton();
	    int clickX = event.getX();
	    int clickY = event.getY();
	    final int searchBound = 10;
	    for (int diff = 0; diff < searchBound; diff++) {
		for (int x = clickX-diff; x <= clickX+diff; x++) {
		    if (registerClick(x, clickY - diff, button))
			return;
		    if (registerClick(x, clickY + diff, button))
			return;
		}
		for (int y = clickY-diff; y <= clickY+diff; y++) {
		    if (registerClick(clickX - diff, y, button))
			return;
		    if (registerClick(clickX + diff, y, button))
			return;
		}
	    }
        }
    }

    // Try find a relevant area. If found return true.
    private boolean registerClick(int x, int y, int button) {
	for (int i = 0; i < hiRects.size(); i++) {
	    Rectangle rect = hiRects.get(i);
	    if (rect.contains(x, y)) {
		if (button == MouseEvent.BUTTON3)
		    hiRightClicked(i);
		else if (button == MouseEvent.BUTTON2)
		    hiMiddleClicked(i);
		else if (hiFormat.nGlyphs() > 0) 
		    hiClicked(i);
		return true;
	    }
	}
	for (int i = 0; i < funRects.size(); i++) {
	    Rectangle rect = funRects.get(i);
	    if (rect.contains(x, y)) {
		funClicked(i);
		return true;
	    }
	}
	for (int i = 0; i < alRects.size(); i++) {
	    Rectangle rect = alRects.get(i);
	    if (rect != null && rect.contains(x, y)) {
		if (button == MouseEvent.BUTTON3)
		    alRightClicked(i);
		else if (TransHelper.letters(al).size() > 0)
		    alClicked(i);
		return true;
	    }
	}
	return false;
    }

    // Communication of clicks to caller.
    public void hiClicked(int i) {
	// caller overrides
    }
    public void funClicked(int i) {
	// caller overrides
    }
    public void alClicked(int i) {
	// caller overrides
    }
    public void clicked(int id) {
	// caller overrides
    }
    public void hiRightClicked(int i) {
	// caller overrides
    }
    public void hiMiddleClicked(int i) {
	// caller overrides
    }
    public void alRightClicked(int i) {
	// caller overrides
    }
    public boolean isConsistent(OrthoElem ortho, Vector<String> hiNames, String al) {
	// caller overrides
	return true;
    }

}
