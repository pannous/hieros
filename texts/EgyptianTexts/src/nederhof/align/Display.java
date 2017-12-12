/***************************************************************************/
/*                                                                         */
/*  Display.java                                                           */
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

// Window allowing viewing of text.

package nederhof.align;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.fonts.*;
import nederhof.res.*;
import nederhof.util.*;

class Display extends JFrame implements 
	ActionListener, RenderContext, AWTFontMapper {

    // System to be formatted in window.
    private StreamSystem system;

    // Browser that called software. May be null.
    private AppletContext context;

    // Margins left and right of the actual text.
    // Space between version label and text.
    // Vertical space between paragraphs and between lines within paragraphs.
    // Vertical space between lines of footnotes.
    // All lines to be made of uniform ascent w.r.t. points.
    // Footnotes collected at end.
    private int leftMargin;
    private int rightMargin;
    private int colSep;
    private int parSep;
    private int lineSep;
    private int footnoteLineSep;
    private boolean uniformAscent;
    private boolean collectNotes;

    // Options for rendering lexical entries.
    private int lxSep;
    private int lxLeading;
    private int lxInnerMargin;
    private int lxLineThickness;
    private boolean lxAbbreviated;

    // Latin.
    private String latinFontName;
    private Integer latinFontStyle;
    private Integer latinFontSize;
    private Font latinFont;
    private GeneralFontMetrics latinFontMetrics;
    private Font footLatinFont;
    private GeneralFontMetrics footLatinFontMetrics;
    private Font normalFont;
    private GeneralFontMetrics normalFontMetrics;
    private Font italicFont;
    private GeneralFontMetrics italicFontMetrics;
    private Font footItalicFont;
    private GeneralFontMetrics footItalicFontMetrics;
    private Font header1Font;
    private GeneralFontMetrics header1FontMetrics;
    private Font header2Font;
    private GeneralFontMetrics header2FontMetrics;
    private Font header3Font;
    private GeneralFontMetrics header3FontMetrics;

    // Transliteration.
    private Font egyptFontPlain;
    private Font egyptFontBold;
    private Font egyptFontIt;
    private Font egyptFontBoldIt;
    private Integer egyptFontStyle;
    private Integer egyptFontSize;
    private Font egyptFont;
    private GeneralFontMetrics egyptFontMetrics;
    private Font footEgyptFont;
    private GeneralFontMetrics footEgyptFontMetrics;
    private TrMap egyptMap;

    // Hieroglyphic.
    private Integer hieroFontSize;
    private HieroRenderContext hieroContext;
    private HieroRenderContext footHieroContext;

    // Footnotes.
    private Integer footFontSizeReduction;

    // The scroll pane, and the pane within that containing actual text.
    private JScrollPane scroll;
    private TextView text;

    // Width of indication of creator if present.
    private float creatorWidth;
    // Width of indication of version if present.
    private float versionWidth;

    // The settings window, index window, find window, and help window.
    private JFrame settingsWindow = null;
    private JFrame indexWindow = null;
    private JFrame findWindow = null;
    private JFrame exportWindow = null;
    private JFrame helpWindow = null;
    private PreambleWindow[] resourceWindows;

    // Create window with aligned text.
    public Display(StreamSystem system, String title, AppletContext context) { 
	this.system = system;
	this.context = context;
	makeEmptyAuxWindows();
	getTranslitFonts();
	setTitle(title);
	setJMenuBar(getMenu());
	setSize(Settings.displayWidthInit, Settings.displayHeightInit);
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
	text = new TextView();
	scroll = new JScrollPane(text, 
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.getVerticalScrollBar().setUnitIncrement(10);
	content.add(scroll);
	addWindowListener(new Listener());
	setDefaultSettings();
	computeFonts();
	setVisible(true);
	ensureReformatted();
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

    // Make empty windows for info on resources.
    private void makeEmptyAuxWindows() {
	int nFiles = system.nFiles();
	resourceWindows = new PreambleWindow[nFiles];
	for (int file = 0; file < nFiles; file++) 
	    resourceWindows[file] = null;
    }

    // Get transliteration fonts.
    private void getTranslitFonts() {
	egyptFontPlain = null;
	egyptFontBold = null;
	egyptFontIt = null;
	egyptFontBoldIt = null;
	egyptMap = new TrMap(Settings.egyptMapFile);
    }

    // Listen if window to be closed.
    private class Listener extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	    dispose();
	}
    }

    //////////////////////////////////////////////////////////////////////////////

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
	// index
	box.add(new ClickButton(this,
		    "<u>I</u>ndex", "index", KeyEvent.VK_I));
	box.add(Box.createHorizontalStrut(STRUT_SIZE));
	// settings
	box.add(new ClickButton(this,
		    "<u>S</u>ettings", "settings", KeyEvent.VK_S));
	box.add(Box.createHorizontalStrut(STRUT_SIZE));
	// find
	box.add(new ClickButton(this,
		    "<u>F</u>ind", "find", KeyEvent.VK_F));
	box.add(Box.createHorizontalStrut(STRUT_SIZE));
	// export, only if not called from applet.
	if (context == null) {
	    box.add(new ClickButton(this,
			"<u>E</u>xport", "export", KeyEvent.VK_E));
	    box.add(Box.createHorizontalStrut(STRUT_SIZE));
	}
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
	} else if (e.getActionCommand().equals("index")) {
	    if (indexWindow == null) 
		indexWindow = new IndexWindow();
	    indexWindow.setVisible(true);
	} else if (e.getActionCommand().equals("settings")) {
	    if (settingsWindow == null) 
		settingsWindow = new SettingsWindow();
	    settingsWindow.setVisible(true);
	} else if (e.getActionCommand().equals("find")) {
	    if (findWindow == null) 
		findWindow = new FindWindow();
	    findWindow.setVisible(true);
	} else if (e.getActionCommand().equals("export")) {
	    if (exportWindow == null) 
		exportWindow = new ExportWindow(system, getTitle());
	    exportWindow.setVisible(true);
	} else if (e.getActionCommand().equals("help")) {
	    if (helpWindow == null) 
		helpWindow = new HTMLwindow(Align.programName, 
			"data/help/obsolete/alignhelp.html", context);
	    helpWindow.setVisible(true);
	} else if (e.getActionCommand().startsWith("resource")) {
	    String resourceStr = e.getActionCommand();
	    resourceStr = resourceStr.substring("resource".length());
	    try {
		int file = Integer.parseInt(resourceStr);
		if (resourceWindows[file] == null)
		    resourceWindows[file] = new PreambleWindow(
			    system.getName(file),
			    system.getCreated(file),
			    system.getHeader(file),
			    system.getBibl(file),
			    this, context, this);
		resourceWindows[file].setVisible(true);
	    } catch (NumberFormatException eConvert) {}
	}
    }

    // For stand-alone, exit upon window close.
    // (Resource leak in JFileChooser forces us to make this distinction.)
    private boolean standAlone = false;

    // Set whether this is stand-alone application.
    public void setStandAlone(boolean standAlone) {
	this.standAlone = standAlone;
    }

    // Kill all windows.
    public void dispose() {
	if (indexWindow != null)
	    indexWindow.dispose();
	if (settingsWindow != null)
	    settingsWindow.dispose();
	if (findWindow != null)
	    findWindow.dispose();
	if (exportWindow != null)
	    exportWindow.dispose();
	if (helpWindow != null)
	    helpWindow.dispose();
	int nFiles = system.nFiles();
	for (int file = 0; file < nFiles; file++) {
	    if (resourceWindows[file] != null)
		resourceWindows[file].dispose();
	}
	super.dispose();
	if (standAlone)
	    System.exit(0);
    }

    //////////////////////////////////////////////////////////////////////////////

    // Settings window.
    private class SettingsWindow extends JFrame implements ActionListener {
	private JTextField leftMarginField;
	private JTextField rightMarginField;
	private JTextField colSepField;
	private JTextField parSepField;
	private JTextField lineSepField;
	private JTextField footnoteLineSepField;
	private JTextField lxSepField;
	private JTextField lxLeadingField;
	private JTextField lxInnerMarginField;
	private JTextField lxLineThicknessField;
	private JCheckBox uniformAscentBox;
	private JCheckBox lxAbbreviatedBox;
	private JCheckBox collectNotesBox;
	private JComboBox latinFontNameCombo;
	private JComboBox latinFontStyleCombo;
	private JComboBox latinFontSizeCombo;
	private JComboBox egyptFontStyleCombo;
	private JComboBox egyptFontSizeCombo;
	private JComboBox hieroFontSizeCombo;
	private JComboBox reductionCombo;

	public SettingsWindow() {
	    final int STRUT_SIZE = 6;
	    setTitle("Settings");
	    setJMenuBar(new QuitMenu(this));
	    Container content = getContentPane();
	    content.setLayout(new BorderLayout());
	    JPanel leftPanel = new JPanel();
	    JPanel rightPanel = new JPanel();
	    JPanel buttonsPanel = new JPanel();
	    content.add(leftPanel, BorderLayout.WEST);
	    content.add(Box.createHorizontalStrut(STRUT_SIZE), BorderLayout.CENTER);
	    content.add(rightPanel, BorderLayout.EAST);
	    content.add(buttonsPanel, BorderLayout.SOUTH);
	    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
	    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
	    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

	    // Horizontal distances.
	    JPanel horPanel = new JPanel(new SpringLayout());
	    horPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Horizontal space"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    leftMarginField = new JTextField();
	    leftMarginField.setColumns(3);
	    leftMarginField.setMaximumSize(leftMarginField.getPreferredSize());
	    horPanel.add(new JLabel("Left margin:"));
	    horPanel.add(leftMarginField);
	    horPanel.add(new JLabel("pixels"));
	    horPanel.add(Box.createHorizontalGlue());
	    rightMarginField = new JTextField();
	    rightMarginField.setColumns(3);
	    rightMarginField.setMaximumSize(rightMarginField.getPreferredSize());
	    horPanel.add(new JLabel("Right margin:"));
	    horPanel.add(rightMarginField);
	    horPanel.add(new JLabel("pixels"));
	    horPanel.add(Box.createHorizontalGlue());
	    colSepField = new JTextField();
	    colSepField.setColumns(3);
	    colSepField.setMaximumSize(colSepField.getPreferredSize());
	    horPanel.add(new JLabel("Column sep:"));
	    horPanel.add(colSepField);
	    horPanel.add(new JLabel("pixels"));
	    horPanel.add(Box.createHorizontalGlue());
	    leftPanel.add(horPanel);
	    SpringUtilities.makeCompactGrid(horPanel, 3, 4, 5, 5, 5, 5);
	    leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // Vertical distances.
	    JPanel vertPanel = new JPanel(new SpringLayout());
	    vertPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Vertical space"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    parSepField = new JTextField();
	    parSepField.setColumns(3);
	    parSepField.setMaximumSize(parSepField.getPreferredSize());
	    vertPanel.add(new JLabel("Paragraph sep:"));
	    vertPanel.add(parSepField);
	    vertPanel.add(new JLabel("pixels"));
	    vertPanel.add(Box.createHorizontalGlue());
	    lineSepField = new JTextField();
	    lineSepField.setColumns(3);
	    lineSepField.setMaximumSize(lineSepField.getPreferredSize());
	    vertPanel.add(new JLabel("Line sep:"));
	    vertPanel.add(lineSepField);
	    vertPanel.add(new JLabel("pixels"));
	    vertPanel.add(Box.createHorizontalGlue());
	    footnoteLineSepField = new JTextField();
	    footnoteLineSepField.setColumns(3);
	    footnoteLineSepField.setMaximumSize(footnoteLineSepField.getPreferredSize());
	    vertPanel.add(new JLabel("Footnote line sep:"));
	    vertPanel.add(footnoteLineSepField);
	    vertPanel.add(new JLabel("pixels"));
	    vertPanel.add(Box.createHorizontalGlue());
	    leftPanel.add(vertPanel);
	    SpringUtilities.makeCompactGrid(vertPanel, 3, 4, 5, 5, 5, 5);
	    leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // Lexical entries.
	    JPanel lxPanel = new JPanel(new SpringLayout());
	    lxPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Lexical entries"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    lxSepField = new JTextField();
	    lxSepField.setColumns(3);
	    lxSepField.setMaximumSize(lxSepField.getPreferredSize());
	    lxPanel.add(new JLabel("Horizontal sep:"));
	    lxPanel.add(lxSepField);
	    lxPanel.add(new JLabel("pixels"));
	    lxPanel.add(Box.createHorizontalGlue());
	    lxLeadingField = new JTextField();
	    lxLeadingField.setColumns(3);
	    lxLeadingField.setMaximumSize(lxLeadingField.getPreferredSize());
	    lxPanel.add(new JLabel("Leading:"));
	    lxPanel.add(lxLeadingField);
	    lxPanel.add(new JLabel("pixels"));
	    lxPanel.add(Box.createHorizontalGlue());
	    lxInnerMarginField = new JTextField();
	    lxInnerMarginField.setColumns(3);
	    lxInnerMarginField.setMaximumSize(lxInnerMarginField.getPreferredSize());
	    lxPanel.add(new JLabel("Inner margin:"));
	    lxPanel.add(lxInnerMarginField);
	    lxPanel.add(new JLabel("pixels"));
	    lxPanel.add(Box.createHorizontalGlue());
	    lxLineThicknessField = new JTextField();
	    lxLineThicknessField.setColumns(3);
	    lxLineThicknessField.setMaximumSize(lxLineThicknessField.getPreferredSize());
	    lxPanel.add(new JLabel("Line thickness:"));
	    lxPanel.add(lxLineThicknessField);
	    lxPanel.add(new JLabel("pixels"));
	    lxPanel.add(Box.createHorizontalGlue());
	    leftPanel.add(lxPanel);
	    SpringUtilities.makeCompactGrid(lxPanel, 4, 4, 5, 5, 5, 5);
	    leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // Other formatting issues.
	    JPanel formatPanel = new JPanel(new SpringLayout());
	    formatPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Formatting"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    uniformAscentBox = new JCheckBox();
	    formatPanel.add(uniformAscentBox);
	    formatPanel.add(new JLabel("Uniform line ascents"));
	    formatPanel.add(Box.createHorizontalGlue());
	    lxAbbreviatedBox =  new JCheckBox();
	    formatPanel.add(lxAbbreviatedBox);
	    formatPanel.add(new JLabel("Lexical entries abbreviated to textal"));
	    formatPanel.add(Box.createHorizontalGlue());
	    collectNotesBox =  new JCheckBox();
	    formatPanel.add(collectNotesBox);
	    formatPanel.add(new JLabel("All footnotes together below text"));
	    formatPanel.add(Box.createHorizontalGlue());
	    leftPanel.add(formatPanel);
	    SpringUtilities.makeCompactGrid(formatPanel, 3, 3, 5, 5, 5, 5);
	    formatPanel.add(Box.createVerticalStrut(STRUT_SIZE));
	    leftPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // Latin font.
	    JPanel latinPanel = new JPanel(new SpringLayout());
	    latinPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Latin font"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    String envfonts[] = gEnv.getAvailableFontFamilyNames();
	    Vector fontVector = new Vector();
	    for (int i = 0; i < envfonts.length; i++) {
		if (!envfonts[i].equals(Settings.egyptFontName) &&
			!envfonts[i].matches("GlyphBasic.*") &&
			!envfonts[i].matches("HieroglyphicAux.*"))
		    fontVector.addElement(envfonts[i]);
	    }
	    latinFontNameCombo = new JComboBox(fontVector);
	    latinFontNameCombo.setMaximumRowCount(9);
	    latinFontNameCombo.setMaximumSize(latinFontNameCombo.getPreferredSize());
	    latinPanel.add(new JLabel("Font:"));
	    latinPanel.add(latinFontNameCombo);
	    latinPanel.add(Box.createHorizontalGlue());
	    latinPanel.add(Box.createHorizontalGlue());
	    latinFontStyleCombo = new JComboBox(FontTypeRenderer.fontTypes);
	    latinFontStyleCombo.setRenderer(new FontTypeRenderer());
	    latinFontStyleCombo.setMaximumRowCount(9);
	    latinFontStyleCombo.setMaximumSize(latinFontStyleCombo.getPreferredSize());
	    latinPanel.add(new JLabel("Style:"));
	    latinPanel.add(latinFontStyleCombo);
	    latinPanel.add(Box.createHorizontalGlue());
	    latinPanel.add(Box.createHorizontalGlue());
	    latinFontSizeCombo = new JComboBox(FontSizeRenderer.getFontSizes(10, 30, 2));
	    latinFontSizeCombo.setRenderer(new FontSizeRenderer());
	    latinFontSizeCombo.setMaximumRowCount(9);
	    latinFontSizeCombo.setMaximumSize(latinFontSizeCombo.getPreferredSize());
	    latinPanel.add(new JLabel("Size:"));
	    latinPanel.add(latinFontSizeCombo);
	    latinPanel.add(new JLabel("pt"));
	    latinPanel.add(Box.createHorizontalGlue());
	    rightPanel.add(latinPanel);
	    SpringUtilities.makeCompactGrid(latinPanel, 3, 4, 5, 5, 5, 5);
	    rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // Egyptological font.
	    JPanel egyptPanel = new JPanel(new SpringLayout());
	    egyptPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Egyptological font"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    egyptFontStyleCombo = new JComboBox(FontTypeRenderer.fontTypes);
	    egyptFontStyleCombo.setRenderer(new FontTypeRenderer());
	    egyptFontStyleCombo.setMaximumRowCount(9);
	    egyptFontStyleCombo.setMaximumSize(egyptFontStyleCombo.getPreferredSize());
	    egyptPanel.add(new JLabel("Style:"));
	    egyptPanel.add(egyptFontStyleCombo);
	    egyptPanel.add(Box.createHorizontalGlue());
	    egyptPanel.add(Box.createHorizontalGlue());
	    egyptFontSizeCombo = new JComboBox(FontSizeRenderer.getFontSizes(10, 30, 2));
	    egyptFontSizeCombo.setRenderer(new FontSizeRenderer());
	    egyptFontSizeCombo.setMaximumRowCount(9);
	    egyptFontSizeCombo.setMaximumSize(egyptFontSizeCombo.getPreferredSize());
	    egyptPanel.add(new JLabel("Size:"));
	    egyptPanel.add(egyptFontSizeCombo);
	    egyptPanel.add(new JLabel("pt"));
	    egyptPanel.add(Box.createHorizontalGlue());
	    rightPanel.add(egyptPanel);
	    SpringUtilities.makeCompactGrid(egyptPanel, 2, 4, 5, 5, 5, 5);
	    rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // Hieroglyphic font.
	    JPanel hieroPanel = new JPanel(new SpringLayout());
	    hieroPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Hieroglyphic font"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    hieroFontSizeCombo = new JComboBox(FontSizeRenderer.getFontSizes(18, 40, 2));
	    hieroFontSizeCombo.setRenderer(new FontSizeRenderer());
	    hieroFontSizeCombo.setMaximumRowCount(9);
	    hieroFontSizeCombo.setMaximumSize(hieroFontSizeCombo.getPreferredSize());
	    hieroPanel.add(new JLabel("Size:"));
	    hieroPanel.add(hieroFontSizeCombo);
	    hieroPanel.add(new JLabel("pt"));
	    hieroPanel.add(Box.createHorizontalGlue());
	    rightPanel.add(hieroPanel);
	    SpringUtilities.makeCompactGrid(hieroPanel, 1, 4, 5, 5, 5, 5);
	    rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // Reduction in font size for footnotes.
	    JPanel reductionPanel = new JPanel(new SpringLayout());
	    reductionPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Footnotes"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    reductionCombo = new JComboBox(FontSizeRenderer.getFontSizes(60, 100, 5));
	    reductionCombo.setRenderer(new FontSizeRenderer());
	    reductionCombo.setMaximumRowCount(9);
	    reductionCombo.setMaximumSize(reductionCombo.getPreferredSize());
	    reductionPanel.add(new JLabel("Size reduction:"));
	    reductionPanel.add(reductionCombo);
	    reductionPanel.add(new JLabel("%"));
	    reductionPanel.add(Box.createHorizontalGlue());
	    rightPanel.add(reductionPanel);
	    SpringUtilities.makeCompactGrid(reductionPanel, 1, 4, 5, 5, 5, 5);
	    rightPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // Confirmation buttons.
	    buttonsPanel.add(Box.createHorizontalGlue());
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>O</u>kay</html>", "okay", KeyEvent.VK_O));
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>A</u>pply</html>", "apply", KeyEvent.VK_A));
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>D</u>efaults</html>", "defaults", KeyEvent.VK_D));
	    buttonsPanel.add(Box.createHorizontalGlue());

	    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    addWindowListener(new ConservativeListener(this));
	    pack();
	    makeSettingsVisible();
	}

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals("okay")) {
		applySettings();
		setVisible(false);
	    } else if (e.getActionCommand().equals("apply")) {
		applySettings();
	    } else if (e.getActionCommand().equals("defaults")) {
		makeDefaultSettingsVisible();
	    } else if (e.getActionCommand().equals("quit")) {
		makeSettingsVisible();
		setVisible(false);
	    }
	}

	// Apply values from screen.
	private void applySettings() {
	    try { 
		int i = Integer.parseInt(leftMarginField.getText());
		leftMargin = i;
	    } catch (NumberFormatException e) {}
	    try { 
		int i = Integer.parseInt(rightMarginField.getText());
		rightMargin = i;
	    } catch (NumberFormatException e) {}
	    try { 
		int i = Integer.parseInt(colSepField.getText());
		colSep = i;
	    } catch (NumberFormatException e) {}
	    try { 
		int i = Integer.parseInt(parSepField.getText());
		parSep = i;
	    } catch (NumberFormatException e) {}
	    try { 
		int i = Integer.parseInt(lineSepField.getText());
		lineSep = i;
	    } catch (NumberFormatException e) {}
	    try { 
		int i = Integer.parseInt(footnoteLineSepField.getText());
		footnoteLineSep = i;
	    } catch (NumberFormatException e) {}
	    uniformAscent = uniformAscentBox.isSelected();
	    collectNotes = collectNotesBox.isSelected();
	    try { 
		int i = Integer.parseInt(lxSepField.getText());
		lxSep = i;
	    } catch (NumberFormatException e) {}
	    try { 
		int i = Integer.parseInt(lxLeadingField.getText());
		lxLeading = i;
	    } catch (NumberFormatException e) {}
	    try { 
		int i = Integer.parseInt(lxInnerMarginField.getText());
		lxInnerMargin = i;
	    } catch (NumberFormatException e) {}
	    try { 
		int i = Integer.parseInt(lxLineThicknessField.getText());
		lxLineThickness = i;
	    } catch (NumberFormatException e) {}
	    lxAbbreviated = lxAbbreviatedBox.isSelected();
	    footFontSizeReduction = (Integer) reductionCombo.getSelectedItem();
	    latinFontName = (String) latinFontNameCombo.getSelectedItem();
	    latinFontStyle = (Integer) latinFontStyleCombo.getSelectedItem();
	    latinFontSize = (Integer) latinFontSizeCombo.getSelectedItem();
	    egyptFontStyle = (Integer) egyptFontStyleCombo.getSelectedItem();
	    egyptFontSize = (Integer) egyptFontSizeCombo.getSelectedItem();
	    hieroFontSize = (Integer) hieroFontSizeCombo.getSelectedItem();
	    computeFonts();
	    makeSettingsVisible();
	    reformat();
	    propagateToResourceWindows();
	}

	// Set values in screen to current values.
	private void makeSettingsVisible() {
	    leftMarginField.setText(Integer.toString(leftMargin));
	    rightMarginField.setText(Integer.toString(rightMargin));
	    colSepField.setText(Integer.toString(colSep));
	    parSepField.setText(Integer.toString(parSep));
	    lineSepField.setText(Integer.toString(lineSep));
	    footnoteLineSepField.setText(Integer.toString(footnoteLineSep));
	    uniformAscentBox.setSelected(uniformAscent);
	    collectNotesBox.setSelected(collectNotes);
	    lxSepField.setText(Integer.toString(lxSep));
	    lxLeadingField.setText(Integer.toString(lxLeading));
	    lxInnerMarginField.setText(Integer.toString(lxInnerMargin));
	    lxLineThicknessField.setText(Integer.toString(lxLineThickness));
	    lxAbbreviatedBox.setSelected(lxAbbreviated);
	    latinFontNameCombo.setSelectedItem(latinFontName);
	    latinFontStyleCombo.setSelectedItem(latinFontStyle);
	    latinFontSizeCombo.setSelectedItem(latinFontSize);
	    egyptFontStyleCombo.setSelectedItem(egyptFontStyle);
	    egyptFontSizeCombo.setSelectedItem(egyptFontSize);
	    hieroFontSizeCombo.setSelectedItem(hieroFontSize);
	    reductionCombo.setSelectedItem(footFontSizeReduction);
	}

	// Set values in screen to default values.
	private void makeDefaultSettingsVisible() {
	    leftMarginField.setText(
		    Integer.toString(Settings.leftMarginDefault));
	    rightMarginField.setText(
		    Integer.toString(Settings.rightMarginDefault));
	    colSepField.setText(
		    Integer.toString(Settings.colSepDefault));
	    parSepField.setText(
		    Integer.toString(Settings.parSepDefault));
	    lineSepField.setText(
		    Integer.toString(Settings.lineSepDefault));
	    footnoteLineSepField.setText(
		    Integer.toString(Settings.footnoteLineSepDefault));
	    uniformAscentBox.setSelected(
		    Settings.uniformAscentDefault);
	    collectNotesBox.setSelected(
		    Settings.collectNotesDefault);
	    lxSepField.setText(
		    Integer.toString(Settings.lxSepDefault));
	    lxLeadingField.setText(
		    Integer.toString(Settings.lxLeadingDefault));
	    lxInnerMarginField.setText(
		    Integer.toString(Settings.lxInnerMarginDefault));
	    lxLineThicknessField.setText(
		    Integer.toString(Settings.lxLineThicknessDefault));
	    lxAbbreviatedBox.setSelected(
		    Settings.lxAbbreviatedDefault);
	    latinFontNameCombo.setSelectedItem(
		    Settings.latinFontNameDefault);
	    latinFontStyleCombo.setSelectedItem(
		    Settings.latinFontStyleDefault);
	    latinFontSizeCombo.setSelectedItem(
		    Settings.latinFontSizeDefault);
	    egyptFontStyleCombo.setSelectedItem(
		    Settings.egyptFontStyleDefault);
	    egyptFontSizeCombo.setSelectedItem(
		    Settings.egyptFontSizeDefault);
	    hieroFontSizeCombo.setSelectedItem(
		    Settings.hieroFontSizeDefault);
	    reductionCombo.setSelectedItem(
		    Settings.footFontSizeReductionDefault);
	}

	// Also propagate new settings (especially about fonts) 
	// to resource windows.
	private void propagateToResourceWindows() {
	    int nFiles = system.nFiles();
	    for (int file = 0; file < nFiles; file++) {
		if (resourceWindows[file] != null)
		    resourceWindows[file].reformat();
	    }
	}
    }

    // Set default settings.
    private void setDefaultSettings() {
	leftMargin = Settings.leftMarginDefault;
	rightMargin = Settings.rightMarginDefault;
	colSep = Settings.colSepDefault;
	parSep = Settings.parSepDefault;
	lineSep = Settings.lineSepDefault;
	footnoteLineSep = Settings.footnoteLineSepDefault;
	uniformAscent = Settings.uniformAscentDefault;
	collectNotes = Settings.collectNotesDefault;
	lxSep = Settings.lxSepDefault;
	lxLeading = Settings.lxLeadingDefault;
	lxInnerMargin = Settings.lxInnerMarginDefault;
	lxLineThickness = Settings.lxLineThicknessDefault;
	lxAbbreviated = Settings.lxAbbreviatedDefault;
	footFontSizeReduction = Settings.footFontSizeReductionDefault;
	latinFontName = Settings.latinFontNameDefault;
	latinFontStyle = Settings.latinFontStyleDefault;
	latinFontSize = Settings.latinFontSizeDefault;
	egyptFontStyle = Settings.egyptFontStyleDefault;
	egyptFontSize = Settings.egyptFontSizeDefault;
	hieroFontSize = Settings.hieroFontSizeDefault;
    }

    // Compute fonts based on settings.
    private void computeFonts() {
	computeLatinFont();
	computeEgyptFont();
	computeHieroFont();
	computePreludeWidth();
    }

    // Compute Latin font.
    private void computeLatinFont() {
	int footSize = Math.round(
		latinFontSize.intValue() * footFontSizeReduction.intValue() / 100.0f);
	latinFont = new Font(latinFontName, 
		latinFontStyle.intValue(),
		latinFontSize.intValue());
	latinFontMetrics = new AWTFontMetrics(getFontMetrics(latinFont));
	footLatinFont = new Font(latinFontName,
		latinFontStyle.intValue(),
		footSize);
	footLatinFontMetrics = new AWTFontMetrics(getFontMetrics(footLatinFont));
	normalFont = new Font(latinFontName,
		Font.PLAIN,
		latinFontSize.intValue());
	normalFontMetrics = new AWTFontMetrics(getFontMetrics(normalFont));
	italicFont = new Font(latinFontName,
		Font.ITALIC,
		latinFontSize.intValue());
	italicFontMetrics = new AWTFontMetrics(getFontMetrics(italicFont));
	footItalicFont = new Font(latinFontName,
		Font.ITALIC,
		footSize);
	footItalicFontMetrics = new AWTFontMetrics(getFontMetrics(footItalicFont));
	header1Font = new Font(latinFontName,
		Settings.headerFontStyle,
		Math.round(latinFontSize.intValue() * 
		    Settings.header1Increase));
	header1FontMetrics = 
	    new AWTFontMetrics(getFontMetrics(header1Font));
	header2Font = new Font(latinFontName,
		Settings.headerFontStyle,
		Math.round(latinFontSize.intValue() * 
		    Settings.header2Increase));
	header2FontMetrics = 
	    new AWTFontMetrics(getFontMetrics(header2Font));
	header3Font = new Font(latinFontName,
		Settings.headerFontStyle,
		Math.round(latinFontSize.intValue() * 
		    Settings.header3Increase));
	header3FontMetrics = 
	    new AWTFontMetrics(getFontMetrics(header3Font));
    }

    // Compute font for transliteration.
    private void computeEgyptFont() {
	Font f;
	if (egyptFontStyle.equals(FontTypeRenderer.plainInt)) {
	    if (egyptFontPlain == null)
		egyptFontPlain = FontUtil.font(Settings.egyptFontFilePlain);
	    f = egyptFontPlain;
	} else if (egyptFontStyle.equals(FontTypeRenderer.boldInt)) {
	    if (egyptFontBold == null)
		egyptFontBold = FontUtil.font(Settings.egyptFontFileBold);
	    f = egyptFontBold;
	} else if (egyptFontStyle.equals(FontTypeRenderer.italicInt)) {
	    if (egyptFontIt == null)
		egyptFontIt = FontUtil.font(Settings.egyptFontFileIt);
	    f = egyptFontIt;
	} else {
	    if (egyptFontBoldIt == null)
		egyptFontBoldIt = FontUtil.font(Settings.egyptFontFileBoldIt);
	    f = egyptFontBoldIt;
	}
	if (f == null) {
	    egyptFont = latinFont;
	    footEgyptFont = footLatinFont;
	} else {
	    int footSize = Math.round(
		    egyptFontSize.intValue() * footFontSizeReduction.intValue() / 100.0f);
	    egyptFont = f.deriveFont((float) egyptFontSize.intValue());
	    footEgyptFont = f.deriveFont((float) footSize);
	}
	egyptFontMetrics = new AWTFontMetrics(getFontMetrics(egyptFont));
	footEgyptFontMetrics = new AWTFontMetrics(getFontMetrics(footEgyptFont));
    }

    // Compute hieroglyphic fonts.
    private void computeHieroFont() {
	int footSize = Math.round(
		hieroFontSize.intValue() * footFontSizeReduction.intValue() / 100.0f);
	hieroContext = new HieroRenderContext(hieroFontSize.intValue(), true);
	footHieroContext = new HieroRenderContext(footSize, true);
    }

    // Compute size of material before actual text.
    // The main issue is the sizes of the labels at the beginning of lines.
    private void computePreludeWidth() {
	creatorWidth = 0.0f;
	if (mentionCreator()) {
	    int nFiles = system.nFiles();
	    for (int file = 0; file < nFiles; file++) {
		String name = system.getName(file);
		String shortName = Link.shortName(name);
		float width = Link.getButtonWidth(this,
			RenderContext.NORMAL_FONT, shortName);
		creatorWidth = Math.max(creatorWidth, width);
	    }
	}
	versionWidth = 0.0f;
	if (mentionVersion()) {
	    int nStreams = system.nStreams();
	    for (int str = 0; str < nStreams; str++) {
		StreamId id = system.getStreamId(str);
		String version = id.getVersion();
		String scheme = id.getScheme();
		VersionLabel lab = new VersionLabel(version, scheme);
		float width = lab.getWidth(this);
		versionWidth = Math.max(versionWidth, width);
	    }
	} 
    }

    // Get font metrics belonging to given font.
    public GeneralFontMetrics getFontMetrics(int f) {
	switch (f) {
	    case RenderContext.LATIN_FONT:
		return latinFontMetrics;
	    case RenderContext.FOOT_LATIN_FONT:
		return footLatinFontMetrics;
	    case RenderContext.NORMAL_FONT:
		return normalFontMetrics;
	    case RenderContext.ITALIC_FONT:
		return italicFontMetrics;
	    case RenderContext.FOOT_ITALIC_FONT:
		return footItalicFontMetrics;
	    case RenderContext.HEADER1_FONT:
		return header1FontMetrics;
	    case RenderContext.HEADER2_FONT:
		return header2FontMetrics;
	    case RenderContext.HEADER3_FONT:
		return header3FontMetrics;
	    case RenderContext.EGYPT_FONT:
		return egyptFontMetrics;
	    case RenderContext.FOOT_EGYPT_FONT:
		return footEgyptFontMetrics;
	    default:
		return latinFontMetrics;
	}
    }

    // Get font belonging to given font code.
    public Font getFont(int f) {
	switch (f) {
	    case RenderContext.LATIN_FONT:
		return latinFont;
	    case RenderContext.FOOT_LATIN_FONT:
		return footLatinFont;
	    case RenderContext.NORMAL_FONT:
		return normalFont;
	    case RenderContext.ITALIC_FONT:
		return italicFont;
	    case RenderContext.FOOT_ITALIC_FONT:
		return footItalicFont;
	    case RenderContext.HEADER1_FONT:
		return header1Font;
	    case RenderContext.HEADER2_FONT:
		return header2Font;
	    case RenderContext.HEADER3_FONT:
		return header3Font;
	    case RenderContext.EGYPT_FONT:
		return egyptFont;
	    case RenderContext.FOOT_EGYPT_FONT:
		return footEgyptFont;
	    default:
		return latinFont;
	}
    }

    // Get mapping to transliteration font characters.
    public TrMap getEgyptMap() {
	return egyptMap;
    }

    // The amount that footnote markers are to be raised above baseline,
    // as factor of ascent.
    public float getFootRaise() {
	return Settings.footRaiseDefault;
    }

    // Get environments for hieroglyphic.
    public HieroRenderContext getHieroContext() {
	return hieroContext;
    }

    // Same but in footnotes.
    public HieroRenderContext getFootHieroContext() {
	return footHieroContext;
    }

    // Is color allowed in hieroglyphic?
    public boolean hieroColor() {
	return true;
    }

    // Get Color to be used for points.
    public Color getPointColor() {
	return Settings.pointColorDefault;
    }

    // Get Color to be used for labels.
    public Color getLabelColor() {
	return Settings.labelColorDefault;
    }

    // Get Color to be used for footnote markers.
    public Color getNoteColor() {
	return Settings.noteColorDefault;
    }

    // The separation between lexical entries, in pixels.
    public float getLxSep() {
	return lxSep;
    }

    // The leading below lexical entries, in pixels.
    public float getLxLeading() {
	return lxLeading;
    }

    // The margin inside lexical entries.
    public float getLxInnerMargin() {
	return lxInnerMargin;
    }

    // The thickness of lines around lexical entries.
    public float getLxLineThickness() {
	return lxLineThickness;
    }

    // Are lexical entries to be abbreviated?
    public boolean lxAbbreviated() {
	return lxAbbreviated;
    }

    // Yield whether lines to have uniform ascent.
    public boolean uniformAscent() {
	return uniformAscent;
    }

    // Yield whether footnotes collected to end of text.
    public boolean collectNotes() {
	return collectNotes;
    }

    //////////////////////////////////////////////////////////////////////////////

    // Every time the system is (re)formatted according to a screen width, 
    // this value is reassigned.
    // When the screen width changes from this width by more than the size of
    // the right margin, the system is reformatted.
    private int lastFormatWidth = 0;

    // Textual part, main section of top window.
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

    // Reformatted at least once?
    private boolean reformattedOnce = false;

    // List of formatted paragraphs.
    private LinkedList formatted = new LinkedList();
    // List of equal length of JPanels where paragraphs are printed.
    private LinkedList formattedPanels = new LinkedList();

    // Make new division into paragraphs, to account for new window width,
    // or new settings.
    private void reformat() {
	reformattedOnce = true;
	lastFormatWidth = text.getSize().width;
	formatted = system.format(this);
	formattedPanels = new LinkedList();
	ListIterator iter = formatted.listIterator();
	text.removeAll();
	while (iter.hasNext()) {
	    Vector lines = (Vector) iter.next();
	    JPanel par = new Paragraph(lastFormatWidth, lines);
	    text.add(par);
	    formattedPanels.addLast(par);
	}
	validate();
	repaint();
    }

    // A paragraph contains a number of lines, the y's are
    // the vertical offset from top. The lines are input
    // as vector.
    private class Paragraph extends JPanel {
	private int width;
	private int height;
	private int nLines;
	private Vector lines;
	private int[] ys;

	// Prepare paragraph for showing text and buttons.
	public Paragraph(int width, Vector lines) {
	    setBackground(Color.WHITE);
	    setLayout(null);
	    setOpaque(true);
	    Insets insets = getInsets();
	    this.width = width;
	    if (system.nSelected() > 1) 
		height = parSep / 2;
	    else
		height = lineSep / 2;
	    nLines = lines.size();
	    this.lines = lines;
	    ys = new int[nLines];
	    for (int i = 0; i < nLines; i++) {
		Line line = (Line) lines.elementAt(i);
		int ascent = Math.round(line.getAscent(Display.this));
		int descent = Math.round(line.getDescent(Display.this));
		int leading = Math.round(line.getLeading(Display.this));
		line.placeButtons(Display.this, Display.this, Display.this,
			context, this, insets.left, insets.top + height + ascent);
		ys[i] = height + ascent;
		height += ascent + descent;
		if (i < nLines - 1) {
		    height += leading;
		    if (line.isFootnote())
			height += footnoteLineSep;
		    else
			height += lineSep;
		}
	    }
	    if (system.nSelected() > 1) 
		height += parSep / 2;
	    else
		height += lineSep / 2;
	}

	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    AWTDraw drawer = new AWTDraw(Display.this, g);
	    for (int i = 0; i < nLines; i++) {
		Line line = (Line) lines.elementAt(i);
		line.draw(Display.this, drawer, ys[i]);
	    }
	    if (system.nSelected() > 1) {
		g.setColor(Color.BLACK);
		g.drawLine(0, height-1, width-1, height-1);
	    }
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
    }

    // Left margin.
    public float leftBound() {
	return leftMargin;
    }

    // Size of indication of creator.
    public float creatorWidth() {
	return creatorWidth;
    }

    // Size of material before actual text.
    public float textOffset() {
	if (creatorWidth + versionWidth > 0)
	    return leftBound() + creatorWidth + versionWidth + colSep;
	else
	    return leftBound();
    }

    // Compute right boundary of text.
    public float rightBound() {
	return lastFormatWidth - rightMargin;
    }

    // Is there more than one file among selected streams?
    public boolean mentionCreator() {
	return system.nSelectedFiles() > 1;
    }

    // Are labels of versions to be printed? Is there only one stream?
    public boolean mentionVersion() {
	return system.nSelected() > 1;
    }

    // Get abbreviated name of resource, in button. Used for index.
    private JButton getNameButton(int file) {
	String name = system.getName(file);
	String shortName = Link.shortName(name);
	Font font = getFont(RenderContext.NORMAL_FONT);
	JButton but = Link.getButton(this, font, shortName);
	but.setActionCommand("resource" + file);
	but.addActionListener(this);
	return but;
    }

    //////////////////////////////////////////////////////////////////////////////

    // Index window.
    public class IndexWindow extends JFrame implements ActionListener {
	// Array of check buttons for each stream that indicate whether streams
	// are to be used. 
	private JCheckBox[] streamSelectionButtons;

	public IndexWindow() {
	    setTitle("Index");
	    setJMenuBar(new QuitMenu(this));
	    Container content = getContentPane();
	    content.setBackground(Color.LIGHT_GRAY);
	    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
	    streamSelectionButtons = new JCheckBox[system.nStreams()];
	    addFileOverviews();

	    // Confirmation buttons.
	    final int STRUT_SIZE = 6;
	    JPanel buttonsPanel = new JPanel();
	    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
	    buttonsPanel.add(Box.createHorizontalGlue());
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>O</u>kay</html>", "okay", KeyEvent.VK_O));
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>A</u>pply</html>", "apply", KeyEvent.VK_A));
	    buttonsPanel.add(Box.createHorizontalGlue());
	    content.add(buttonsPanel);

	    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    addWindowListener(new ConservativeListener(this));
	    pack();
	}

	private void addFileOverviews() {
	    final int STRUT_SIZE = 10;
	    for (int file = 0; file < system.nFiles(); file++) {
		JPanel filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout());
		JPanel fileDescr = new JPanel();
		fileDescr.setLayout(new BoxLayout(fileDescr, BoxLayout.X_AXIS));
		fileDescr.add(Box.createHorizontalStrut(STRUT_SIZE));
		fileDescr.add(getNameButton(file));
		fileDescr.add(Box.createHorizontalStrut(STRUT_SIZE));
		JLabel shortDescr = new JLabel(system.getCreatedString(file));
		fileDescr.add(shortDescr);
		filePanel.add(fileDescr, BorderLayout.NORTH);
		filePanel.add(Box.createHorizontalStrut(STRUT_SIZE), BorderLayout.WEST);
		JPanel checkPanel = new JPanel(new GridLayout(0, 1));

		LinkedList fileStreams = system.getFileStreams(file);
		ListIterator iter = fileStreams.listIterator();
		while (iter.hasNext()) {
		    Integer strInt = (Integer) iter.next();
		    int str = strInt.intValue();
		    JCheckBox strBox = streamBox(str);
		    checkPanel.add(strBox);
		    streamSelectionButtons[str] = strBox;
		}

		filePanel.add(checkPanel, BorderLayout.CENTER);
		this.getContentPane().add(filePanel);
	    }
	}

	// Make checkbox for stream.
	private JCheckBox streamBox(int str) {
	    StreamId id = system.getStreamID(str);
	    JCheckBox strBox = new JCheckBox();
	    String version = (id.getVersion().equals("") ? 
		    "" : 
		    id.getVersion() + " ");
	    String type = id.getTypeName();
	    String streamName = version + "(" + type + ")";
	    strBox.setText(streamName);
	    strBox.setSelected(true);
	    return strBox;
	}

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals("okay")) {
		applySettings();
		setVisible(false);
	    } else if (e.getActionCommand().equals("apply")) {
		applySettings();
		setVisible(true);
	    } else if (e.getActionCommand().equals("quit")) 
		setVisible(false);
	}

	// Obtain values from buttons and reformat.
	private void applySettings() {
	    for (int str = 0; str < system.nStreams(); str++)
		system.setSelected(str,
			streamSelectionButtons[str].isSelected());
	    computePreludeWidth();
	    reformat();
	}
    }

    //////////////////////////////////////////////////////////////////////////////

    // Find window.
    public class FindWindow extends JFrame implements ActionListener {
	Vector versionList;
	JComboBox versionAndScheme = null;
	JTextField tag;
	JLabel notFound;

	public FindWindow() {
	    setTitle("Find");
	    setJMenuBar(new QuitMenu(this));
	    Container content = getContentPane();
	    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

	    // Position.
	    JPanel posPanel = new JPanel(new SpringLayout());
	    versionList = getVersionList();
	    int rows = 1;
	    if (versionList.size() >= 2) {
		posPanel.add(new JLabel("Version:"));
		versionAndScheme = new JComboBox(versionList);
		versionAndScheme.setMaximumRowCount(9);
		posPanel.add(versionAndScheme);
		rows++;
	    }
	    posPanel.add(new JLabel("Position:"));
	    tag = new JTextField(10);
	    tag.setActionCommand("enter");
	    tag.addActionListener(this);
	    posPanel.add(tag);
	    SpringUtilities.makeCompactGrid(posPanel, rows, 2, 5, 5, 5, 5);
	    content.add(posPanel);

	    // Confirmation buttons.
	    final int STRUT_SIZE = 6;
	    JPanel buttonsPanel = new JPanel();
	    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
	    buttonsPanel.add(Box.createHorizontalGlue());
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>G</u>o</html>", "go", KeyEvent.VK_G));
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>C</u>lear</html>", "clear", KeyEvent.VK_C));
	    buttonsPanel.add(Box.createHorizontalGlue());
	    content.add(buttonsPanel);

	    // Not-found text.
	    JPanel notFoundPanel = new JPanel();
	    notFoundPanel.setLayout(new BoxLayout(notFoundPanel, BoxLayout.X_AXIS));
	    notFound = new JLabel(" ");
	    notFound.setForeground(Color.RED);
	    notFoundPanel.add(notFound);
	    content.add(notFoundPanel);

	    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    addWindowListener(new ConservativeListener(this));
	    pack();
	}

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals("go") ||
		    e.getActionCommand().equals("enter")) {
		gotoPosition();
	    } else if (e.getActionCommand().equals("clear")) {
		tag.setText("");
		notFound.setText(" ");
		setVisible(true);
	    } else if (e.getActionCommand().equals("quit")) {
		notFound.setText(" ");
		setVisible(false);
	    } 
	}

	// Get list of versions plus schemes.
	private Vector getVersionList() {
	    Vector list = new Vector();
	    int nStreams = system.nStreams();
	    for (int str = 0; str < nStreams; str++) {
		StreamId id = system.getStreamId(str);
		String version = id.getVersion();
		String scheme = id.getScheme();
		String combined = VersionLabel.getVersionLabel(version, scheme);
		if (!list.contains(combined))
		    list.addElement(combined);
	    }
	    return list;
	}

	// Find position and go there.
	private void gotoPosition() {
	    String label = "";
	    if (versionAndScheme != null)
		label = (String) versionAndScheme.getSelectedItem();
	    else if (versionList.size() == 1)
		label = (String) versionList.elementAt(0);
	    ListIterator parList = formatted.listIterator();
	    ListIterator panList = formattedPanels.listIterator();
	    while (parList.hasNext() && panList.hasNext()) {
		Vector par = (Vector) parList.next();
		JPanel pan = (JPanel) panList.next();
		if (posOccursIn(par, label)) {
		    int height = pan.getPreferredSize().height;
		    Rectangle rect = new Rectangle(new Dimension(0, height));
		    pan.scrollRectToVisible(rect);
		    notFound.setText(" ");
		    setVisible(false);
		    return;
		}
	    }
	    notFound.setText("Not found");
	    setVisible(true);
	}

	// Decide whether position occurs in one of lines.
	private boolean posOccursIn(Vector par, String label) {
	    for (int str = 0; str < par.size(); str++) {
		Line line = (Line) par.elementAt(str);
		if (line.isFootnote() || !line.getLabel().equals(label))
		    continue;
		LinkedList elems = line.getElems();
		ListIterator iter = elems.listIterator();
		while (iter.hasNext()) {
		    Elem elem = (Elem) iter.next();
		    if (elem instanceof Point) {
			Point point = (Point) elem;
			Pos pos = point.getPos();
			if (!pos.isPhrasal() && !point.isAlign()) {
			    String posTag = pos.getTag();
			    if (posTag.equals(tag.getText()))
				return true;
			}
		    }
		}
	    }
	    return false;
	}
    }
}
