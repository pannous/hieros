/***************************************************************************/
/*                                                                         */
/*  FormattedPane.java                                                     */
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

// Pane showing formatted interlinear text.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.alignment.*;
import nederhof.corpus.*;
import nederhof.interlinear.*;
import nederhof.interlinear.labels.*;
import nederhof.util.*;

abstract class FormattedPane extends JPanel implements FormatListener {

    // Every time the text is (re)formatted according to a screen width,
    // this value is reassigned.
    // When the screen width changes from this width by more than the size of
    // the right margin, the system is reformatted.
    private int lastFormatWidth = 0;

    // Directory of corpus.
    private String corpusDirectory;

    // The text.
    private Text text;

    // Automatic alignment.
    private Autoaligner aligner;

    // Parameters for rendering.
    private RenderParameters params;

    // Keeps track of actions.
    private EditActionHelper editHelper;

    // Helps connection to external resource viewers.
    private ViewActionHelper viewHelper;

    // In edit mode?
    private boolean edit = false;

    // Is enabled?
    private boolean enabled = true;

    // The precedence resources.
    private Vector<ResourcePrecedence> precedences = new Vector<ResourcePrecedence>();

    // The auto aligns.
    private Vector<Object[]> autoaligns = new Vector();

    // Font for labels.
    private Font labelFont = 
	new Font(Settings.labelFontName, Font.PLAIN, Settings.labelFontSize);

    // The tiers.
    private Vector<Tier> tiers = new Vector<Tier>();
    // Numbers of tiers within resources.
    private Vector<Integer> tierNums = new Vector<Integer>();
    // Short names of tiers.
    private Vector<String> labels = new Vector<String>();
    // Versions of tiers.
    private Vector<String> versions = new Vector();
    // Vector of vectors of positions where phrases start.
    private Vector<Vector<Integer>> phraseStarts = 
	new Vector<Vector<Integer>>();
    // Resource for each tier.
    public Vector<TextResource> tierResources = new Vector<TextResource>();

    // Is there more than one tier that is shown?
    private boolean severalTiersShown = false;
    // Is there more than one resource that is shown?
    private boolean severalResourcesShown = false;

    // Where to place labels and versions. Negative if none.
    private int labelLocation = 0;
    private int versionLocation = 0;
    // Where to place text.
    private int textLocation = 0;

    // Popup showing autoaligned tiers of text.
    // Null if no such popup.
    private Popup autoalignPopup;

    // The sections that result from formatting.
    private Vector<Section> sections;

    public FormattedPane(String corpusDirectory,
	    Text text, Autoaligner aligner, RenderParameters params,
	    EditChainElement parent) {
	this.corpusDirectory = corpusDirectory;
	this.text = text;
	this.aligner = aligner;
	this.params = params;
	setBackground(Color.WHITE);
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	addComponentListener(new ResizeListener());
	editHelper = new EditActionHelper(corpusDirectory, parent) {
	    public void refresh() {
		repaint();
	    }
	    public void editPhrase(PhraseEditor editor) {
		FormattedPane.this.editPhrase(editor);
	    }
	    public void editResource(ResourceEditor editor) {
		FormattedPane.this.editResource(editor);
	    }
	};
	viewHelper = new ViewActionHelper() {
	    public void focusExternalViewer(TextResource resource, int pos) {
		FormattedPane.this.focusExternalViewer(resource, pos);
	    }
	};
    }

    // Format text and add to panel.
    // First, the resources are turned into tiers, which is then formatted.
    public void setResources(
	    Vector<TextResource> resources, 
	    Vector<ResourcePrecedence> precedences, 
	    Vector<Object[]> autoaligns,
	    boolean edit) {
	this.edit = edit;
	this.precedences = precedences;
	this.autoaligns = autoaligns;
	setCursor(new Cursor(Cursor.WAIT_CURSOR));
	TierGather gather = new TierGather(resources, precedences, autoaligns, 
		aligner, params, false, edit);
	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	tiers = gather.tiers;
	tierNums = gather.tierNums;
	labels = gather.labels;
	versions = gather.versions;
	phraseStarts = gather.phraseStarts;
	tierResources = gather.tierResources;
	severalTiersShown = gather.severalTiersShown;
	severalResourcesShown = gather.severalResourcesShown;
	editHelper.clear(tiers, tierNums, phraseStarts, tierResources, severalResourcesShown);
	reformat();
    }

    // Class of listening to changes to window size.
    // If change big, then reformat.
    private class ResizeListener implements ComponentListener {
	public void componentResized(ComponentEvent e) {
	    if (Math.abs(currentWidth() - lastFormatWidth) >= params.rightMargin) {
		storeMiddleTierPos();
		reformat();
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			retrieveMiddleTierPos();
		    }
		});
	    }
        }
        public void componentMoved(ComponentEvent e) {
        }
        public void componentShown(ComponentEvent e) {
        }
        public void componentHidden(ComponentEvent e) {
        }
    }

    private int currentWidth() {
	return getSize().width;
    }

    // Make new division into sections, to account for new window width,
    public void reformat() {
	lastFormatWidth = currentWidth();
	if (lastFormatWidth < 1 && tiers.size() > 0)
	    return;
	setCursor(new Cursor(Cursor.WAIT_CURSOR));
	makeLayout();
	sections = new Vector<Section>();
	params.initiateSituation();
	new InterlinearFormatting(tiers) {
	    protected float width() {
		return lastFormatWidth - textLocation - params.rightMargin;
	    }
	    protected boolean processSection(String[] modes,
		    Vector<TierSpan>[] sectionSpans,
		    TreeMap<Integer,Float>[] sectionSpanLocations) {
		Section sect = makeSection(modes,
			sectionSpans, sectionSpanLocations);
		if (sect != null)
		    sections.add(sect);
		return true;
	    }
	};
	Section footnoteSect = makeFootnoteSection();
	if (footnoteSect != null)
	    sections.add(footnoteSect);
	removeAll();
	for (int i = 0; i < sections.size(); i++) {
	    Section sect = sections.get(i);
	    add(sect);
	}
	revalidate();
	repaint();
	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    // Determine layout of pane.
    public void makeLayout() {
	boolean severalLabels = (new TreeSet(labels)).size() > 1;
	boolean severalVersions = (new TreeSet(versions)).size() > 1;
	int labelWidth = 0;
	for (int i = 0; i < labels.size(); i++) {
	    String label = labels.get(i);
	    labelWidth = Math.max(labelWidth, widthOf(label));
	}
	int versionWidth = 0;
	for (int i = 0; i < versions.size(); i++) {
	    String version = versions.get(i);
	    versionWidth = Math.max(versionWidth, widthOf(version));
	}
	int start = params.leftMargin;
	if (severalLabels) {
	    labelLocation = start;
	    start += labelWidth + params.colSep;
	} else
	    labelLocation = -1;
	if (severalVersions) {
	    versionLocation = start;
	    start += versionWidth + params.colSep;
	} else
	    versionLocation = -1;
	textLocation = start;
    }

    // Width of string in label.
    private int widthOf(String s) {
	return getFontMetrics(labelFont).stringWidth(s);
    }

    // Combine result from formatting with labels.
    // Make sure a section does not contain zero lines (then
    // return null).
    private Section makeSection(String[] modes,
	    Vector<TierSpan>[] sectionSpans, 
	    TreeMap<Integer,Float>[] sectionSpanLocations) {
	Vector<Line> lines = new Vector<Line>();
	Vector<TierPos> omittedTierPoss = new Vector<TierPos>();
	params.resetMarker();
	for (int i = 0; i < sectionSpans.length; i++) {
	    String mode = modes[i];
	    Vector<TierSpan> spans = sectionSpans[i];
	    Vector<Footnote> footnotes = FootnoteHelper.footnotes(spans);
	    if (!mode.equals(TextResource.OMITTED) || edit) {
		if (includesContent(spans)) {
		    TreeMap<Integer,Float> locs = sectionSpanLocations[i];
		    String label = labels.get(i);
		    String version = versions.get(i);
		    lines.add(new Line(label, version, spans, locs));
		}
		params.addFootnotes(footnotes);
	    } else if (spans.size() > 0) {
		TierSpan first = spans.get(0);
		omittedTierPoss.add(new TierPos(first.tier, first.fromPos));
	    }
	}
	if (!params.collectNotes) {
	    lines.addAll(makeFootnoteLines());
	}
	if (!lines.isEmpty())
	    return new Section(lines, omittedTierPoss);
	else
	    return null;
    }

    // Make section of footnotes.
    private Section makeFootnoteSection() {
	Vector lines = makeFootnoteLines();
	if (lines.isEmpty())
	    return null;
	else
	    return new Section(lines);
    }

    // Make lines of footnotes.
    private Vector<Line> makeFootnoteLines() {
	Vector<Line> lines = new Vector<Line>();
	Vector notes = params.getPendingNotes();
	params.initiateSituation();
	for (int i = 0; i < notes.size(); i++) {
	    Footnote note = (Footnote) notes.get(i);
	    Tier tier = note.getTier();
	    Vector<Tier> tiers = new Vector<Tier>();
	    tiers.add(tier);
	    LineMaker maker = new LineMaker(tiers);
	    lines.addAll(maker.lines);
	}
	return lines;
    }

    // At least one span is real content (not e.g. coordinate).
    private boolean includesContent(Vector<TierSpan> spans) {
	for (int i = 0; i < spans.size(); i++) {
	    TierSpan span = spans.get(i);
	    if (span.includesContent())
		return true;
	}
	return false;
    }

    // Format tiers and output lines. Used for footnotes.
    private class LineMaker {
	// Collected lines
	public Vector<Line> lines = new Vector();

	public LineMaker(Vector<Tier> tiers) {
	    new InterlinearFormatting(tiers) {
		protected float width() {
		    return lastFormatWidth - params.leftMargin - params.rightMargin;
		}
		protected boolean processSection(String[] modes,
			Vector<TierSpan>[] sectionSpans,
			TreeMap<Integer,Float>[] sectionSpanLocations) {
		    for (int i = 0; i < sectionSpans.length; i++) {
			Vector<TierSpan> spans = sectionSpans[i];
			if (spans.size() > 0) {
			    TreeMap locs = sectionSpanLocations[i];
			    lines.add(new Line(spans, locs));
			}
		    }
		    return true;
		}
	    };
	}
    }

    // Line contains label, version, and spans.
    private class Line {
	public boolean isFootnote = false;
	public String label;
	public String version;
	public Vector<TierSpan> spans;
	public TreeMap locs;

	// For normal line.
	public Line(String label, String version, Vector<TierSpan> spans, TreeMap locs) {
	    this.label = label;
	    this.version = version;
	    this.spans = spans;
	    this.locs = locs;
	}
	// For footnote.
	public Line(Vector<TierSpan> spans, TreeMap locs) {
	    this.spans = spans;
	    this.locs = locs;
	    isFootnote = true;
	}

	public float leadingAscent() {
	    float max = labelLocation >= 0 ? 
		getFontMetrics(labelFont).getLeading() +
		getFontMetrics(labelFont).getAscent() : 0; 
	    for (int i = 0; i < spans.size(); i++) {
		TierSpan span = spans.get(i);
		max = Math.max(max, span.leadingAscent());
	    }
	    return max;
	}
	public float descent() {
	    float max = labelLocation >= 0 ? 
		getFontMetrics(labelFont).getDescent() : 0;
	    for (int i = 0; i < spans.size(); i++) {
		TierSpan span = spans.get(i);
		max = Math.max(max, span.descent());
	    }
	    return max;
	}
	public void draw(Graphics2D g, int y) {
	    if (!isFootnote && labelLocation >= 0)
		drawString(g,
			label, labelLocation, y, Settings.labelColor);
	    if (!isFootnote && versionLocation >= 0)
		drawString(g,
			version, versionLocation, y, Settings.versionColor);
	    for (int i = 0; i < spans.size(); i++) {
		TierSpan span = spans.get(i);
		Float xInt = (Float) locs.get(new Integer(span.fromPos));
		if (xInt != null) {
		    int x = Math.round(xInt.floatValue());
		    if (!isFootnote)
			span.draw(g, textLocation + x, y);
		    else
			span.draw(g, params.leftMargin + x, y);
		}
	    }

	}

	private void drawString(Graphics2D g2,
		String s, int x, int y, Color color) {
	    g2.setFont(labelFont);
	    g2.setColor(color);
	    g2.drawString(s, x, y);
	}

	// Get tier that was clicked, with position.
	public TierPos getTierPos(int x, int y) {
	    for (int i = 0; i < spans.size(); i++) {
		TierSpan span = spans.get(i);
		Float xInt = (Float) locs.get(new Integer(span.fromPos));
		if (xInt != null) {
		    int xRound = Math.round(xInt.floatValue());
		    TierPos pos = span.getTierPos(x - (textLocation + xRound), y);
		    if (pos != null) 
			return pos;
		}
	    }
	    return null;
	}

	// Get rectangle for positions. Return null if cannot be found.
	public Rectangle getRectangle(int pos) {
	    for (int i = 0; i < spans.size(); i++) {
		TierSpan span = spans.get(i);
		if (span.fromPos <= pos && pos < span.toPos) {
		    Float xInt = (Float) locs.get(new Integer(span.fromPos));
		    int xRound = Math.round(xInt.floatValue());
		    Rectangle rect = span.getRectangle(pos);
		    if (rect != null)
			return new Rectangle(rect.x + xRound, rect.y, 
				rect.width, rect.height);
		}
	    }
	    return null;
	}

	// Get first tierpos on line. Return null if none.
	public TierPos getFirstTierPos() {
	    if (!isFootnote && spans.size() > 0) {
		TierSpan span = spans.get(0);
		return new TierPos(span.tier, span.fromPos);
	    } else
		return null;
	}
	// Get last tierpos on line. Return null if none.
	public TierPos getLastTierPos() {
	    if (!isFootnote && spans.size() > 0) {
		TierSpan span = spans.get(spans.size()-1);
		return new TierPos(span.tier, span.toPos);
	    } else
		return null;
	}

    }

    // A section contains a number of lines, the y's are
    // the vertical offset from top. The lines are input
    // as vector.
    private class Section extends JPanel {
        private int width;
        private int height;
        public Vector<Line> lines;
        public int nLines;
        private int[] ys;

	// 4-tuples of arrows to be drawn for precedence relations.
	private Vector arrows;
	// 4-tuples of rectangles, at ends of certain arrows.
	private Vector rectangles;

	// First tier positions in tiers that are omitted.
	public Vector<TierPos> omittedTierPoss = new Vector<TierPos>();

        // Prepare section for showing text and buttons.
        public Section(Vector<Line> lines) {
	    this(lines, new Vector<TierPos>());
	}
        public Section(Vector<Line> lines, Vector<TierPos> omittedTierPoss) {
            setLayout(null);
            setOpaque(true);
	    Insets insets = getInsets();
	    if (severalTiersShown)
		height = params.sectionSep / 2;
	    else
		height = params.lineSep / 2;
	    this.lines = lines;
	    this.omittedTierPoss = omittedTierPoss;
	    nLines = lines.size();
	    ys = new int[nLines];
	    for (int i = 0; i < nLines; i++) {
		Line line = lines.get(i);
                int leadingAscent = Math.round(line.leadingAscent());
                int descent = Math.round(line.descent());
		height += leadingAscent;
                ys[i] = height;
                height += descent;
                if (i < nLines - 1) {
                    if (line.isFootnote)
                        height += params.footnoteLineSep;
                    else
                        height += params.lineSep;
                }
            }
            if (severalTiersShown)
                height += params.sectionSep / 2;
            else
                height += params.lineSep / 2;
	    addMouseListener(new ClickListener());
	    setEnabled(enabled);
        }

	// Connect precedences in lines.
	// For all lines, look at all positions. See if there are
	// positions that are in precedence relation.
	private void connectArrows() {
	    if (arrows != null)
		return; // Do at most once, but after line at least once.
	    arrows = new Vector();
	    rectangles = new Vector();
	    for (int i = 0; i < lines.size(); i++) {
		Line line = lines.get(i);
		TierPos first = line.getFirstTierPos();
		TierPos last = line.getLastTierPos();
		if (first != null && last != null) {
		    Tier tier = first.tier;
		    for (int pos = first.pos; pos < last.pos; pos++) {
			TreeSet precedings = tier.manualPrecedings(pos);
			for (Iterator it = precedings.iterator();
				it.hasNext(); ) {
			    TierPos precedePos = (TierPos) it.next();
			    Rectangle rect1 = line.getRectangle(pos);
			    rect1.translate(textLocation, ys[i]);
			    connectArrow(i, rect1, pos, 
				    precedePos.tier, precedePos.pos, precedePos.type);
			}
		    }
		}
	    }
	}

	// Arrow is placed little to the right of left corner.
	private final int arrowOffset = 5;

	// Given position and position that is in precedence relation, 
	// look in which line (must be other line). Draw arrow.
	private void connectArrow(int i, Rectangle rect1, int pos1, 
		Tier tier2, int pos2, String type2) {
	    for (int j = 0; j < lines.size(); j++) 
		if (j != i) {
		    Line line = lines.get(j);
		    TierPos first = line.getFirstTierPos();
		    TierPos last = line.getLastTierPos();
		    if (first != null && last != null) {
			if (first.tier == tier2 && 
				first.pos <= pos2 && pos2 < last.pos) {
			    Rectangle rect2 = line.getRectangle(pos2);
			    rect2.translate(textLocation, ys[j]);
			    placeArrow(rect2, type2, rect1);
			    return;
			}
		    }
		}
	    Rectangle rectOrphan = new Rectangle(rect1.x - arrowOffset, 
		    rect1.y - arrowOffset, rect1.width, arrowOffset); 
	    placeArrow(rectOrphan, type2, rect1);

	}

	// Draw arrow, from bottom of one to top of the other, depending
	// on which is higher.
	// If the type is "after", print extra bar and shift and of arrow.
	private void placeArrow(Rectangle rect1, String type, Rectangle rect2) {
	    int barWidth = 2;
	    int barHeight = 6;
	    int afterShift = barWidth + 2;
	    int shift = type.equals("start") ? 0 : afterShift;
	    if (rect1.y < rect2.y)
		arrows.add(new int[] {
			rect1.x + arrowOffset + shift,
			rect1.y + rect1.height,
			rect2.x + arrowOffset,
			rect2.y});
	    else
		arrows.add(new int[] {
			rect1.x + arrowOffset + shift,
			rect1.y,
			rect2.x + arrowOffset,
			rect2.y + rect2.height});
	    if (type.equals("after")) {
		if (rect1.y < rect2.y)
		    rectangles.add(new int[] {
			    rect1.x + arrowOffset,
			    rect1.y + rect1.height - barHeight / 2,
			    barWidth,
			    barHeight});
		else
		    rectangles.add(new int[] {
			    rect1.x + arrowOffset,
			    rect1.y - barHeight / 2,
			    barWidth,
			    barHeight});
	    }
	}

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g2.setRenderingHint(RenderingHints.KEY_RENDERING,
		    RenderingHints.VALUE_RENDER_QUALITY);

            for (int i = 0; i < nLines; i++) {
		Line line = lines.get(i);
		line.draw(g2, ys[i]);
	    }
	    if (severalTiersShown) {
		g2.setColor(Color.BLACK);
		g2.drawLine(0, height-1, lastFormatWidth + params.rightMargin, height-1);
	    }
	    g2.setColor(Color.BLUE);

	    if (edit) {
		connectArrows();
		for (int i = 0; i < arrows.size(); i++) {
		    int[] arrow = (int[]) arrows.get(i);
		    GraphicsAux.drawArrow(g2, arrow[0], arrow[1], arrow[2], arrow[3]);
		}
		for (int i = 0; i < rectangles.size(); i++) {
		    int[] rect = (int[]) rectangles.get(i);
		    g2.fillRect(rect[0], rect[1], rect[2], rect[3]);
		}
	    }
	}

	private class ClickListener extends MouseInputAdapter {
	    public void mousePressed(MouseEvent event) {
		if (!enabled)
		    return;
		int clickX = event.getX();
		int clickY = event.getY();
		if (!edit) {
		    if (event.getButton() == MouseEvent.BUTTON1 &&
			    !event.isControlDown()) 
			for (int i = 0; i < nLines; i++) {
			    Line line = lines.get(i);
			    int descent = Math.round(line.descent());
			    int sep = 0;
			    if (i < nLines - 1) {
				if (line.isFootnote)
				    sep = params.footnoteLineSep;
				else
				    sep = params.lineSep;
			    }
			    if (clickY < ys[i] + descent + sep && !line.isFootnote) {
				TierPos pos = line.getTierPos(clickX, clickY - ys[i]);
				viewHelper.recordClick(pos, omittedTierPoss, 
					tierNums, tierResources);
				break;
			    }
			}
		    return;
		}
		if (event.getButton() == MouseEvent.BUTTON3 ||
			event.isControlDown()) {
		    if (autoalignPopup != null) {
			autoalignPopup.hide();
			autoalignPopup = null;
		    }
		    makeAutoalignPopup(event);
		    autoalignPopup.show();
		    editHelper.clear();
		} else if (event.getButton() == MouseEvent.BUTTON1 && 
			!event.isControlDown()) 
		    for (int i = 0; i < nLines; i++) {
			Line line = lines.get(i);
			int descent = Math.round(line.descent());
			int sep = 0;
			if (i < nLines - 1) {
			    if (line.isFootnote)
				sep = params.footnoteLineSep;
			    else
				sep = params.lineSep;
			}
			if (clickY < ys[i] + descent + sep && !line.isFootnote) {
			    TierPos pos = line.getTierPos(clickX, clickY - ys[i]);
			    if (pos != null)
				editHelper.recordClick(pos, tierNums, tierResources);
			    break;
			}
		    }
	    }
	    public void mouseReleased(MouseEvent event) {
		if (autoalignPopup != null) {
		    autoalignPopup.hide();
		    autoalignPopup = null;
		}
	    }
	}

	private void makeAutoalignPopup(MouseEvent event) {
	    Window window = SwingUtilities.windowForComponent(this);
	    event = SwingUtilities.convertMouseEvent(this, event, window);
	    int x = event.getX();
	    int y = event.getY();
	    autoalignPopup = 
		AutoalignPopup.createAutoalignPopup(Section.this, x, y, autoaligns);
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

	public void setEnabled(boolean b) {
	    super.setEnabled(b);
	    if (b)
		setBackground(Color.WHITE);
	    else
		setBackground(Color.LIGHT_GRAY);
	}
    }

    //////////////////////////////////// 
    // Communication back to caller.

    public abstract void editPhrase(PhraseEditor editor);

    public abstract void editResource(ResourceEditor editor);

    public abstract void focusExternalViewer(TextResource resource, int pos);

    //////////////////////////////////// 
    // Keyboard input.

    // Process action.
    public boolean actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("left")) {
	    editHelper.left();
	    reportError();
	    return false;
	} else if (e.getActionCommand().equals("right")) {
	    editHelper.right();
	    reportError();
	    return false;
	} else if (e.getActionCommand().equals("prepend")) {
	    editHelper.prependPhrase();
	    reportError();
	    return false;
	} else if (e.getActionCommand().equals("edit")) {
	    editHelper.editPhrase();
	    reportError();
	    return false;
	} else if (e.getActionCommand().equals("append")) {
	    editHelper.appendPhrase();
	    reportError();
	    return false;
	} else if (e.getActionCommand().equals("join")) {
	    editHelper.joinPhrases();
	    return !reportError();
	} else if (e.getActionCommand().equals("cut")) {
	    editHelper.cutPhrase();
	    return !reportError();
	}
	return false;
    }

    // If previous action was append, put clicks at end of resource.
    public void restoreAfterAppend() {
	editHelper.restoreAfterAppend();
    }

    // Add precedences, or remove.
    public Vector makePrecedence(int type) {
	Vector changed = editHelper.makePrecedence(type, precedences);
	reportError();
	return changed;
    }

    // Add auto align, or remove.
    public Vector autoalign(boolean b) {
	Vector changed = editHelper.autoalign(text, autoaligns, b);
	reportError();
	return changed;
    }

    // Report error. Return if there was error.
    private boolean reportError() {
	if (editHelper.errorMessage != null) 
	    JOptionPane.showMessageDialog(this, editHelper.errorMessage,
		    "Edit error", JOptionPane.ERROR_MESSAGE);
	return editHelper.errorMessage != null;
    }

    ////////////////////////////////////
    // Appearance.

    public void setEnabled(boolean b) {
	super.setEnabled(b);
	enabled = b;
	if (b)
	    setBackground(Color.WHITE);
	else
	    setBackground(Color.LIGHT_GRAY);
	for (int i = 0; i < sections.size(); i++) {
	    Section section = sections.get(i);
	    section.setEnabled(b);
	}
    }

    ////////////////////////////////////
    // Locations on screen.

    // Store middle position of screen.
    private void storeMiddleTierPos() {
	TierPos middleTierPos = middleTierPos();
	if (middleTierPos != null)
	    editHelper.storeMiddlePos(middleTierPos);
    }

    // Which position of which tier is at middle of screen?
    // Go to middle line, but avoid footnotes.
    private TierPos middleTierPos() {
	Section middleSection = middleSection();
	if (middleSection != null && middleSection.nLines > 0) {
	    int i = middleSection.nLines / 2;
	    Line middleLine = (Line) middleSection.lines.get(i);
	    while (middleLine.isFootnote && i > 0) 
		middleLine = (Line) middleSection.lines.get(--i);
	    return middleLine.getFirstTierPos();
	} else
	    return null;
    }

    // Which section is at middle of screen? If none, then null.
    private Section middleSection() {
	Rectangle rect = getVisibleRect();
	int middle = rect.y + rect.height/2;
	Section best = null;
	int bestDist = Integer.MAX_VALUE;
	for (int i = 0; i < sections.size(); i++) {
	    Section section = sections.get(i);
	    int y = section.getY() + section.getHeight()/2;
	    int dist = Math.abs(middle - y);
	    if (dist < bestDist) {
		best = section;
		bestDist = dist;
	    } 
	}
	return best;
    }

    // Scroll back to same tierpos as before.
    private void retrieveMiddleTierPos() {
	TierPos pos = editHelper.retrieveMiddlePos();
	if (pos != null)
	    for (int i = 0; i < sections.size(); i++) {
		Section section = sections.get(i);
		for (int j = 0; j < section.nLines; j++) {
		    Line line = (Line) section.lines.get(j);
		    TierPos linePos = line.getLastTierPos();
		    if (linePos != null && 
			    linePos.tier == pos.tier && 
			    linePos.pos > pos.pos) {
			scrollTo(section);
			return;
		    }
		}
	    }
    }

    // Scroll to section.
    private void scrollTo(Section section) {
	Rectangle rect = getVisibleRect();
	int extra = Math.max(0, rect.height - section.getHeight());
	Rectangle visibleRect = new Rectangle(
		section.getX(),
		section.getY() - extra/2,
		section.getWidth(),
		section.getHeight() + extra);
	scrollRectToVisible(visibleRect);
    }

    // Scroll to section holding position of (omitted) resource.
    public void scrollTo(TextResource resource, int pos) {
	for (Section section : sections) 
	    for (TierPos tierPos : section.omittedTierPoss) {
		Tier tier = tierPos.tier;
		int secPos = tierPos.pos;
		TextResource secResource = tierResources.get(tier.id());
		if (secResource == resource && secPos >= pos) {
		    scrollTo(section);
		    return;
		}
	    }
    }

}
